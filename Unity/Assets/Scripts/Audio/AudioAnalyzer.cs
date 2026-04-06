using System;
using System.Collections.Generic;
using PeterDrummer.Data;
using UnityEngine;

namespace PeterDrummer.Audio
{
    /// <summary>
    /// Análise simples de onsets por bandas (graves/agudos) usando FFT por janela.
    /// </summary>
    public class AudioAnalyzer : MonoBehaviour
    {
        [Header("FFT")]
        [SerializeField] private int fftSize = 1024; // potência de 2
        [SerializeField] private int hopSize = 512;

        [Header("Bandas (Hz)")]
        [SerializeField] private Vector2 kickBandHz = new(40f, 180f);
        [SerializeField] private Vector2 snareBandHz = new(180f, 2500f);
        [SerializeField] private Vector2 hihatBandHz = new(4000f, 12000f);

        [Header("Detecção")]
        [SerializeField] private float kickThreshold = 1.35f;
        [SerializeField] private float snareThreshold = 1.30f;
        [SerializeField] private float hihatThreshold = 1.25f;
        [SerializeField] private float minGapSec = 0.065f;

        public List<BeatEvent> Analyze(AudioClip clip)
        {
            if (clip == null) throw new ArgumentNullException(nameof(clip));

            int totalSamples = clip.samples * clip.channels;
            float[] interleaved = new float[totalSamples];
            clip.GetData(interleaved, 0);

            float[] mono = MixToMono(interleaved, clip.channels);
            float[] window = BuildHann(fftSize);

            int totalFrames = Math.Max(0, (mono.Length - fftSize) / hopSize);
            List<BeatEvent> result = new();

            float prevKick = 0f;
            float prevSnare = 0f;
            float prevHihat = 0f;
            double lastKick = -999d;
            double lastSnare = -999d;
            double lastHihat = -999d;

            for (int frame = 0; frame < totalFrames; frame++)
            {
                int offset = frame * hopSize;
                float[] re = new float[fftSize];
                float[] im = new float[fftSize];

                for (int i = 0; i < fftSize; i++)
                {
                    re[i] = mono[offset + i] * window[i];
                    im[i] = 0f;
                }

                FFT(re, im);

                float kickEnergy = BandEnergy(re, im, clip.frequency, kickBandHz);
                float snareEnergy = BandEnergy(re, im, clip.frequency, snareBandHz);
                float hihatEnergy = BandEnergy(re, im, clip.frequency, hihatBandHz);

                float kickFlux = Mathf.Max(0f, kickEnergy - prevKick);
                float snareFlux = Mathf.Max(0f, snareEnergy - prevSnare);
                float hihatFlux = Mathf.Max(0f, hihatEnergy - prevHihat);

                prevKick = kickEnergy;
                prevSnare = snareEnergy;
                prevHihat = hihatEnergy;

                double t = (double)offset / clip.frequency;

                if (kickEnergy > 0.0001f && kickFlux / (kickEnergy + 1e-6f) > kickThreshold && t - lastKick > minGapSec)
                {
                    result.Add(new BeatEvent(DrumLane.Kick, t, kickFlux));
                    lastKick = t;
                }

                if (snareEnergy > 0.0001f && snareFlux / (snareEnergy + 1e-6f) > snareThreshold && t - lastSnare > minGapSec)
                {
                    result.Add(new BeatEvent(DrumLane.Snare, t, snareFlux));
                    lastSnare = t;
                }

                if (hihatEnergy > 0.0001f && hihatFlux / (hihatEnergy + 1e-6f) > hihatThreshold && t - lastHihat > minGapSec)
                {
                    result.Add(new BeatEvent(DrumLane.HiHat, t, hihatFlux));
                    lastHihat = t;
                }
            }

            result.Sort((a, b) => a.TimeSec.CompareTo(b.TimeSec));
            return result;
        }

        private static float[] MixToMono(float[] interleaved, int channels)
        {
            int monoLen = interleaved.Length / channels;
            float[] mono = new float[monoLen];

            for (int i = 0; i < monoLen; i++)
            {
                float acc = 0f;
                int baseIdx = i * channels;
                for (int c = 0; c < channels; c++) acc += interleaved[baseIdx + c];
                mono[i] = acc / channels;
            }

            return mono;
        }

        private static float[] BuildHann(int n)
        {
            float[] w = new float[n];
            for (int i = 0; i < n; i++) w[i] = 0.5f * (1f - Mathf.Cos((2f * Mathf.PI * i) / (n - 1)));
            return w;
        }

        private static float BandEnergy(float[] re, float[] im, int sampleRate, Vector2 band)
        {
            int n = re.Length;
            float hzPerBin = (float)sampleRate / n;
            int minBin = Mathf.Clamp(Mathf.FloorToInt(band.x / hzPerBin), 0, n / 2 - 1);
            int maxBin = Mathf.Clamp(Mathf.CeilToInt(band.y / hzPerBin), minBin + 1, n / 2);

            float e = 0f;
            for (int k = minBin; k < maxBin; k++)
            {
                float mag2 = re[k] * re[k] + im[k] * im[k];
                e += mag2;
            }
            return e;
        }

        // FFT iterativa radix-2 (in-place)
        private static void FFT(float[] re, float[] im)
        {
            int n = re.Length;
            int j = 0;
            for (int i = 1; i < n; i++)
            {
                int bit = n >> 1;
                while ((j & bit) != 0)
                {
                    j ^= bit;
                    bit >>= 1;
                }
                j ^= bit;

                if (i < j)
                {
                    (re[i], re[j]) = (re[j], re[i]);
                    (im[i], im[j]) = (im[j], im[i]);
                }
            }

            for (int len = 2; len <= n; len <<= 1)
            {
                float ang = -2f * Mathf.PI / len;
                float wLenRe = Mathf.Cos(ang);
                float wLenIm = Mathf.Sin(ang);

                for (int i = 0; i < n; i += len)
                {
                    float wRe = 1f;
                    float wIm = 0f;

                    for (int k = 0; k < len / 2; k++)
                    {
                        int u = i + k;
                        int v = i + k + len / 2;

                        float vr = re[v] * wRe - im[v] * wIm;
                        float vi = re[v] * wIm + im[v] * wRe;

                        re[v] = re[u] - vr;
                        im[v] = im[u] - vi;
                        re[u] += vr;
                        im[u] += vi;

                        float nextRe = wRe * wLenRe - wIm * wLenIm;
                        wIm = wRe * wLenIm + wIm * wLenRe;
                        wRe = nextRe;
                    }
                }
            }
        }
    }
}
