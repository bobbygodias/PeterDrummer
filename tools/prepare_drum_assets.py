#!/usr/bin/env python3
"""Prepare Bobby Dias's certified FreestyleDrums pack for the Android pilot."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import tempfile
import zipfile
from pathlib import Path

import numpy as np
from scipy.io import wavfile


SOURCE_PATTERN = re.compile(
    r"FreestyleDrums/(?P<family>[^/]+)/[^/]+ (?P<layer>\d+) (?P<variant>\d+)\.opus$"
)
FAMILY_FOLDER = {
    "Crash": "crash",
    "Ride": "ride",
    "Snare": "snare",
    "Tom1": "tom1",
    "Tom2": "tom2",
    "Tom3": "tom3",
    "Kick": "kick",
}
SAMPLE_RATE = 48_000


def run(*args: str) -> None:
    subprocess.run(args, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def duration(path: Path) -> float:
    result = subprocess.run(
        [
            "ffprobe",
            "-v",
            "error",
            "-show_entries",
            "format=duration",
            "-of",
            "csv=p=0",
            str(path),
        ],
        check=True,
        capture_output=True,
        text=True,
    )
    return float(result.stdout.strip())


def require_valid_audio(path: Path) -> None:
    if not path.is_file() or path.stat().st_size == 0:
        raise RuntimeError(f"Audio output is empty: {path}")
    if duration(path) <= 0.0:
        raise RuntimeError(f"Audio output has no duration: {path}")


def decode_float_stereo(path: Path) -> np.ndarray:
    result = subprocess.run(
        [
            "ffmpeg",
            "-v",
            "error",
            "-i",
            str(path),
            "-ar",
            str(SAMPLE_RATE),
            "-ac",
            "2",
            "-f",
            "f32le",
            "pipe:1",
        ],
        check=True,
        capture_output=True,
    )
    return np.frombuffer(result.stdout, dtype="<f4").reshape(-1, 2).copy()


def encode_opus(wav_path: Path, output_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(suffix=".opus") as temporary_output:
        run(
            "ffmpeg",
            "-y",
            "-v",
            "error",
            "-i",
            str(wav_path),
            "-ar",
            str(SAMPLE_RATE),
            "-ac",
            "2",
            "-c:a",
            "libopus",
            "-b:a",
            "96k",
            "-vbr",
            "on",
            "-application",
            "audio",
            temporary_output.name,
        )
        require_valid_audio(Path(temporary_output.name))
        shutil.copyfile(temporary_output.name, output_path)
    require_valid_audio(output_path)


def transcode_source(source: Path, output: Path) -> None:
    source_duration = duration(source)
    filters = ["volume=0.501187"]
    if source_duration > 5.0:
        filters.extend(["atrim=0:5", "afade=t=out:st=4.72:d=0.28"])
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(suffix=".opus") as temporary_output:
        run(
            "ffmpeg",
            "-y",
            "-v",
            "error",
            "-i",
            str(source),
            "-af",
            ",".join(filters),
            "-ar",
            str(SAMPLE_RATE),
            "-ac",
            "2",
            "-c:a",
            "libopus",
            "-b:a",
            "96k",
            "-vbr",
            "on",
            "-application",
            "audio",
            temporary_output.name,
        )
        require_valid_audio(Path(temporary_output.name))
        shutil.copyfile(temporary_output.name, output)
    require_valid_audio(output)


def spectral_tail(source: np.ndarray, seconds: float, seed: int) -> np.ndarray:
    frame_size = 2048
    hop = 512
    window = np.hanning(frame_size).astype(np.float32)
    frames = []
    start = int(0.045 * SAMPLE_RATE)
    analysis = source[start:]
    for offset in range(0, max(1, len(analysis) - frame_size), hop):
        frame = analysis[offset : offset + frame_size]
        if len(frame) == frame_size:
            frames.append(np.abs(np.fft.rfft(frame * window[:, None], axis=0)))
    if not frames:
        padded = np.pad(source, ((0, max(0, frame_size - len(source))), (0, 0)))
        frames.append(np.abs(np.fft.rfft(padded[:frame_size] * window[:, None], axis=0)))
    spectral_envelope = np.sqrt(np.mean(np.square(frames), axis=0))

    output_samples = int(seconds * SAMPLE_RATE)
    output = np.zeros((output_samples + frame_size, 2), dtype=np.float64)
    weight = np.zeros(output_samples + frame_size, dtype=np.float64)
    rng = np.random.default_rng(seed)
    tau = seconds / 2.4
    frame_index = 0
    for offset in range(0, output_samples, hop):
        time_seconds = offset / SAMPLE_RATE
        decay = np.exp(-time_seconds / tau)
        jitter = rng.uniform(0.86, 1.14, size=(1, 2))
        phase = rng.uniform(-np.pi, np.pi, size=spectral_envelope.shape)
        spectrum = spectral_envelope * jitter * decay * np.exp(1j * phase)
        frame = np.fft.irfft(spectrum, n=frame_size, axis=0).real
        output[offset : offset + frame_size] += frame * window[:, None]
        weight[offset : offset + frame_size] += window * window
        frame_index += 1
    output /= np.maximum(weight[:, None], 1e-6)
    return output[:output_samples].astype(np.float32)


def build_open_hi_hat(source_path: Path, output_path: Path, layer: int, variant: int) -> None:
    source = decode_float_stereo(source_path)
    seconds = 1.38 + layer * 0.22
    tail = spectral_tail(source, seconds, seed=1701 + layer * 10 + variant)
    combined = tail * 0.78

    source_length = min(len(source), len(combined))
    original_envelope = np.linspace(1.0, 0.0, source_length, dtype=np.float32)[:, None]
    combined[:source_length] += source[:source_length] * original_envelope

    fade_in_samples = int(0.055 * SAMPLE_RATE)
    combined[:fade_in_samples] *= np.linspace(0.35, 1.0, fade_in_samples)[:, None]
    fade_out_samples = int(0.28 * SAMPLE_RATE)
    combined[-fade_out_samples:] *= np.linspace(1.0, 0.0, fade_out_samples)[:, None]

    source_peak = float(np.max(np.abs(source)))
    combined_peak = max(float(np.max(np.abs(combined))), 1e-9)
    combined *= min(1.0, source_peak * 0.501187 / combined_peak)

    with tempfile.NamedTemporaryFile(suffix=".wav") as wav:
        wavfile.write(wav.name, SAMPLE_RATE, np.clip(combined * 32767, -32768, 32767).astype("<i2"))
        encode_opus(Path(wav.name), output_path)


def build_pedal_hi_hat(source_path: Path, output_path: Path) -> None:
    source = decode_float_stereo(source_path)
    output_samples = int(0.24 * SAMPLE_RATE)
    pedal = np.zeros((output_samples, 2), dtype=np.float32)
    copied = min(len(source), output_samples)
    pedal[:copied] = source[:copied]
    pedal *= np.exp(-np.linspace(0.0, 7.2, output_samples, dtype=np.float32))[:, None]
    pedal *= 0.44
    with tempfile.NamedTemporaryFile(suffix=".wav") as wav:
        wavfile.write(wav.name, SAMPLE_RATE, np.clip(pedal * 32767, -32768, 32767).astype("<i2"))
        encode_opus(Path(wav.name), output_path)


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("source_zip", type=Path)
    parser.add_argument("output_dir", type=Path)
    args = parser.parse_args()

    if args.output_dir.exists():
        shutil.rmtree(args.output_dir)
    args.output_dir.mkdir(parents=True)

    manifest: list[dict[str, object]] = []
    with tempfile.TemporaryDirectory() as temporary:
        temporary_path = Path(temporary)
        with zipfile.ZipFile(args.source_zip) as archive:
            archive.extractall(temporary_path)
            for certificate_name in (
                "CERTIFICATION.txt",
                "LICENSE.txt",
                "BOBBY_DIAS_PUBLIC_KEY.asc",
            ):
                source = temporary_path / certificate_name
                if source.exists():
                    shutil.copy2(source, args.output_dir / f"SOURCE_{certificate_name}")

        hi_hat_sources: dict[tuple[int, int], Path] = {}
        for source in sorted((temporary_path / "FreestyleDrums").glob("*/*.opus")):
            relative = source.relative_to(temporary_path).as_posix()
            match = SOURCE_PATTERN.match(relative)
            if not match:
                raise ValueError(f"Unexpected sample name: {relative}")
            family = match.group("family")
            layer = int(match.group("layer"))
            variant = int(match.group("variant"))

            if family == "Hihat":
                output = args.output_dir / "hihat" / "closed" / f"l{layer}_v{variant}.opus"
                hi_hat_sources[layer, variant] = source
            else:
                output = args.output_dir / FAMILY_FOLDER[family] / f"l{layer}_v{variant}.opus"
            transcode_source(source, output)
            manifest.append(
                {
                    "path": output.relative_to(args.output_dir).as_posix(),
                    "source": relative,
                    "derived": False,
                    "sha256": digest(output),
                }
            )

        for (layer, variant), source in sorted(hi_hat_sources.items()):
            output = args.output_dir / "hihat" / "open" / f"l{layer}_v{variant}.opus"
            build_open_hi_hat(source, output, layer, variant)
            manifest.append(
                {
                    "path": output.relative_to(args.output_dir).as_posix(),
                    "source": source.relative_to(temporary_path).as_posix(),
                    "derived": True,
                    "derivation": "spectral open-hi-hat tail",
                    "sha256": digest(output),
                }
            )

        for variant in range(1, 4):
            source = hi_hat_sources[2, variant]
            output = args.output_dir / "hihat" / "pedal" / f"v{variant}.opus"
            build_pedal_hi_hat(source, output)
            manifest.append(
                {
                    "path": output.relative_to(args.output_dir).as_posix(),
                    "source": source.relative_to(temporary_path).as_posix(),
                    "derived": True,
                    "derivation": "short pedal-close envelope",
                    "sha256": digest(output),
                }
            )

    (args.output_dir / "manifest.json").write_text(
        json.dumps({"sampleCount": len(manifest), "samples": manifest}, indent=2) + "\n",
        encoding="utf-8",
    )
    opus_files = list(args.output_dir.rglob("*.opus"))
    if len(opus_files) != len(manifest):
        raise RuntimeError(
            f"Expected {len(manifest)} Opus files, found {len(opus_files)}"
        )
    for opus_file in opus_files:
        require_valid_audio(opus_file)
    print(f"Prepared {len(manifest)} samples in {args.output_dir}")


if __name__ == "__main__":
    main()
