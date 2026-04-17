# PeterDrummer - Pacotão de Código

Este arquivo agrega os principais arquivos de código e documentação do projeto em um único lugar.

## Índice
- `Docs/Architecture.md`
- `Docs/Tablet-APK-Guide.md`
- `README.md`
- `.github/workflows/android-apk-build.yml`
- `Unity/Assets/Scripts/Audio/AudioAnalyzer.cs`
- `Unity/Assets/Scripts/Audio/AudioImportService.cs`
- `Unity/Assets/Scripts/Audio/RuntimeAudioLoader.cs`
- `Unity/Assets/Scripts/Core/DifficultyController.cs`
- `Unity/Assets/Scripts/Core/GameFlowController.cs`
- `Unity/Assets/Scripts/Core/ScoreSystem.cs`
- `Unity/Assets/Scripts/Core/SongConductor.cs`
- `Unity/Assets/Scripts/Data/GameDifficulty.cs`
- `Unity/Assets/Scripts/Data/HitJudgement.cs`
- `Unity/Assets/Scripts/Data/RhythmTypes.cs`
- `Unity/Assets/Scripts/Input/HitZone.cs`
- `Unity/Assets/Scripts/Input/PlayerInputController.cs`
- `Unity/Assets/Scripts/Rhythm/NoteObject.cs`
- `Unity/Assets/Scripts/Rhythm/NoteSpawner.cs`
- `Unity/Assets/Scripts/Visual/CharacterVisualController.cs`
- `Unity/Assets/Scripts/Visual/HudFeedbackController.cs`
- `Unity/Assets/Scripts/Visual/IntroVideoController.cs`
- `Unity/Assets/Scripts/Visual/ResultsPanelController.cs`

---

## Docs/Architecture.md

```markdown
# Peter Drummer - Arquitetura Modular (Unity / Android)

## 1) Estrutura de classes recomendada

- **Core**
  - `GameFlowController`: fluxo principal do jogo.
  - `SongConductor`: relógio DSP e controle de playback + evento de fim da música.
  - `ScoreSystem`: pontuação, combo, accuracy e contadores de julgamento.
  - `DifficultyController`: presets Easy/Normal/Hard (janelas, velocidade e score).
- **Audio**
  - `AudioImportService`: integração com seletor nativo Android.
  - `RuntimeAudioLoader`: carrega MP3/WAV em `AudioClip`.
  - `AudioAnalyzer`: onset/beat detection por bandas.
- **Rhythm**
  - `NoteSpawner`: spawn com antecedência exata por timestamp.
  - `NoteObject`: movimentação usando `AudioSettings.dspTime`.
- **InputSystem**
  - `PlayerInputController`: toque/botão -> valida hit.
  - `HitZone`: zona de acerto por lane (Kick/Snare/HiHat).
- **Visual**
  - `CharacterVisualController`: anima personagem em eventos de input.
  - `HudFeedbackController`: atualiza score/combo/judgement na UI.
  - `ResultsPanelController`: mostra resultado no fim da música.
  - `IntroVideoController`: toca vídeo de introdução e permite skip por toque.
- **Data**
  - `DrumLane`, `BeatEvent`, `HitJudgement` e `GameDifficulty`: tipos compartilhados.

## 2) Matemática de sincronização (módulo crítico)

### Variáveis
- `T_event`: timestamp da batida (segundos no áudio analisado).
- `x_spawn`: posição X do spawn.
- `x_hit`: posição X da zona de acerto.
- `v`: velocidade da esteira (units/s).

### Tempo de viagem
`travelTime = abs(x_spawn - x_hit) / v`

### Condição de spawn
No frame atual, com tempo da música `songNow`:

- Spawn quando `T_event - songNow <= travelTime`

Assim, a nota nasce com antecedência exatamente igual ao tempo de deslocamento.

### Posição da nota baseada no tempo absoluto da música
Para reduzir drift por FPS e jitter de frame:

- Em cada `Update`:
  - `songNow = conductor.CurrentSongTimeSec`
  - `remaining = T_event - songNow`
  - `x = x_hit + remaining * v * sign(x_spawn - x_hit)`

> Resultado: quando `songNow == T_event`, a nota cruza exatamente a zona de acerto.

## 3) Mapeamento de lanes sugerido
- **Kick**: graves (40–180 Hz)
- **Snare**: médios/agudos (180–2500 Hz)
- **HiHat**: agudos (4k–12k Hz)

## 4) Dependências Android
- Unity 2022 LTS+ (ou superior).
- Plugin **NativeFilePicker** para abrir seletor de arquivo nativo no Android.
- Build target: Android API 28+ (Android 9 ou superior).

## 5) Organização de cena sugerida
- **TopHalf**: personagem, luzes e animações.
- **BottomHalf**: trilha, hit zones, spawn point, prefabs das notas.
- Canvas com 3 botões grandes (Kick/Snare/HiHat) para toque.

```

## Docs/Tablet-APK-Guide.md

```markdown
# Guia rápido: gerar APK pelo tablet (GitHub)

Este guia permite gerar o APK do **Peter Drummer** sem precisar abrir o Unity localmente.

## Pré-requisitos (1x)

No repositório GitHub, configure em **Settings → Secrets and variables → Actions**:

- `UNITY_EMAIL`
- `UNITY_PASSWORD`
- `UNITY_LICENSE`

> Esses segredos são exigidos pelo `game-ci/unity-builder` para build no GitHub Actions.

## Rodar build no tablet

1. Abra o repositório no GitHub.
2. Vá para a aba **Actions**.
3. Selecione o workflow **Android APK Build & Release**.
4. Toque em **Run workflow**.
5. Defina:
   - `publish_release = true` (publica em Releases automaticamente)
   - `release_tag = v0.1.0` (ou a versão desejada)
6. Toque em **Run workflow** novamente.

## Onde pegar o APK

Após sucesso do workflow:

- Em **Actions → run concluído**, baixe o artefato **PeterDrummer-APK**
- Se `publish_release=true`, também estará em **Releases** como `PeterDrummer.apk`

## Problemas comuns

- **Falha de licença Unity**: segredo `UNITY_LICENSE` inválido/ausente.
- **Nenhum APK encontrado**: estrutura do projeto Unity incompleta ou build falhou antes da etapa final.
- **Link do README não baixa**: confirme se existe Release com arquivo `PeterDrummer.apk`.
```

## README.md

```markdown
# Peter Drummer 🥁
Toque bateria no ritmo da sua própria música no Android (celular, tablet ou TV com Android 9+).

## 📦 Baixar APK (Android 9+)
> **Link direto (primeira página do repositório):**

➡️ **[Download PeterDrummer.apk](https://github.com/bobbygodias/PeterDrummer/releases/latest/download/PeterDrummer.apk)**

> O arquivo APK deve ser publicado em **Releases** com exatamente o nome `PeterDrummer.apk`.

---



## 🤖 Build APK pelo GitHub (tablet-friendly)
Sem computador, você consegue gerar APK direto pelo GitHub Actions:

1. Vá em **Actions → Android APK Build & Release**
2. Clique em **Run workflow**
3. Marque `publish_release = true` para já publicar em Releases
4. Aguarde o job terminar e baixe o artefato/APK

Guia detalhado em `Docs/Tablet-APK-Guide.md`.

---

## 🎬 Vídeo de introdução (10s)
- Link enviado: **https://drive.google.com/file/d/128ZM10Tj9-PjYlbRDdSzx1X6bHDDiV5T/view?usp=sharing**
- Para usar localmente no app: exporte para `intro.mp4` e coloque em `Assets/StreamingAssets/intro.mp4`.
- Script de controle: `Unity/Assets/Scripts/Visual/IntroVideoController.cs` (autoplay + skip por toque).

## 🎮 Visão de layout (paisagem)
- **Metade superior:** arte do personagem + animações de batida.
- **Metade inferior:** esteira de notas rolando da direita para a esquerda.
- **Zona de acerto:** lado esquerdo da esteira.
- **Spawn:** lado direito da esteira.

A imagem base enviada é ideal para usar como plano de fundo da área superior.

---

## 🧱 Arquitetura modular implementada (Unity/C#)
Scripts criados em `Unity/Assets/Scripts`:

- `Core/GameFlowController.cs`
- `Core/SongConductor.cs`
- `Core/ScoreSystem.cs`
- `Core/DifficultyController.cs`
- `Audio/AudioImportService.cs`
- `Audio/RuntimeAudioLoader.cs`
- `Audio/AudioAnalyzer.cs`
- `Rhythm/NoteSpawner.cs`
- `Rhythm/NoteObject.cs`
- `Input/PlayerInputController.cs`
- `Input/HitZone.cs`
- `Visual/CharacterVisualController.cs`
- `Visual/HudFeedbackController.cs`
- `Visual/ResultsPanelController.cs`
- `Visual/IntroVideoController.cs`
- `Data/RhythmTypes.cs`

Detalhes de arquitetura e matemática de sincronização:

➡️ `Docs/Architecture.md`

---

## 🔁 Pipeline do jogo
1. Usuário escolhe MP3/WAV no seletor Android.
2. Áudio é carregado dinamicamente.
3. Analisador detecta batidas por bandas de frequência.
4. `NoteSpawner` cria notas com antecedência calculada por distância/velocidade.
5. `SongConductor` usa `AudioSettings.dspTime` para relógio global.
6. Input detecta acerto/erro na zona de hit (incluindo miss automático quando a nota passa).
7. Evento de input dispara animação do personagem na parte superior.
8. `ScoreSystem` acumula pontos, combo, accuracy e julgamentos em tempo real no HUD.
9. `SongConductor` dispara evento de fim e o `ResultsPanelController` mostra os resultados finais.

---

## ⚙️ Requisitos
- Unity 2022 LTS+
- Android API level 28+ (Android 9+)
- Plugin NativeFilePicker (seletor de arquivos nativo)

---

## 🚀 Próximos passos recomendados
- Ajuste fino de thresholds do analisador por música.
- Ajustar presets de dificuldade (Easy/Normal/Hard) por playtest.
- Quantização opcional por BPM estimado.
- Tela de resultados e ranking local.
```

## .github/workflows/android-apk-build.yml

```yaml
name: Android APK Build & Release

on:
  workflow_dispatch:
    inputs:
      publish_release:
        description: "Publicar APK em GitHub Releases"
        required: true
        default: "true"
        type: choice
        options:
          - "true"
          - "false"
      release_tag:
        description: "Tag da release (quando publish_release=true)"
        required: true
        default: "v0.1.0"
        type: string

permissions:
  contents: write

jobs:
  build-android:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Cache Unity Library
        uses: actions/cache@v4
        with:
          path: Unity/Library
          key: Library-Android-${{ hashFiles('Unity/Assets/**', 'Unity/Packages/**', 'Unity/ProjectSettings/**') }}
          restore-keys: |
            Library-Android-

      - name: Build APK (Unity)
        uses: game-ci/unity-builder@v4
        env:
          UNITY_EMAIL: ${{ secrets.UNITY_EMAIL }}
          UNITY_PASSWORD: ${{ secrets.UNITY_PASSWORD }}
          UNITY_LICENSE: ${{ secrets.UNITY_LICENSE }}
        with:
          projectPath: Unity
          targetPlatform: Android
          buildName: PeterDrummer

      - name: Locate APK
        id: apk
        shell: bash
        run: |
          APK_PATH="$(find build -type f -name '*.apk' | head -n 1)"
          if [ -z "$APK_PATH" ]; then
            echo "Nenhum APK encontrado em /build"
            exit 1
          fi
          cp "$APK_PATH" PeterDrummer.apk
          echo "apk_path=PeterDrummer.apk" >> "$GITHUB_OUTPUT"

      - name: Upload workflow artifact
        uses: actions/upload-artifact@v4
        with:
          name: PeterDrummer-APK
          path: PeterDrummer.apk

      - name: Publish GitHub Release
        if: ${{ inputs.publish_release == 'true' }}
        uses: ncipollo/release-action@v1
        with:
          tag: ${{ inputs.release_tag }}
          allowUpdates: true
          artifactErrorsFailBuild: true
          artifacts: PeterDrummer.apk
          body: |
            APK gerado automaticamente pelo workflow Android APK Build & Release.
          name: Peter Drummer ${{ inputs.release_tag }}
```

## Unity/Assets/Scripts/Audio/AudioAnalyzer.cs

```csharp
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
```

## Unity/Assets/Scripts/Audio/AudioImportService.cs

```csharp
using System;
using System.Collections;
using UnityEngine;

namespace PeterDrummer.Audio
{
    /// <summary>
    /// Abre seletor de arquivo no Android e devolve caminho absoluto.
    /// Requer plugin NativeFilePicker para Android.
    /// </summary>
    public class AudioImportService : MonoBehaviour
    {
        public void PickAudioFile(Action<string> onPathPicked, Action<string> onError)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            NativeFilePicker.Permission permission = NativeFilePicker.CheckPermission();
            if (permission != NativeFilePicker.Permission.Granted)
            {
                permission = NativeFilePicker.RequestPermission();
            }

            if (permission != NativeFilePicker.Permission.Granted)
            {
                onError?.Invoke("Permissão de armazenamento negada.");
                return;
            }

            NativeFilePicker.PickFile((path) =>
            {
                if (string.IsNullOrWhiteSpace(path))
                {
                    onError?.Invoke("Nenhum arquivo foi selecionado.");
                    return;
                }

                if (!path.EndsWith(".mp3", StringComparison.OrdinalIgnoreCase) &&
                    !path.EndsWith(".wav", StringComparison.OrdinalIgnoreCase))
                {
                    onError?.Invoke("Formato inválido. Use MP3 ou WAV.");
                    return;
                }

                onPathPicked?.Invoke(path);
            }, new[] { "audio/*" });
#else
            onError?.Invoke("File picker nativo disponível somente no Android build.");
#endif
        }
    }
}
```

## Unity/Assets/Scripts/Audio/RuntimeAudioLoader.cs

```csharp
using System;
using System.Collections;
using UnityEngine;
using UnityEngine.Networking;

namespace PeterDrummer.Audio
{
    public class RuntimeAudioLoader : MonoBehaviour
    {
        public void LoadFromAbsolutePath(string absolutePath, Action<AudioClip> onLoaded, Action<string> onError)
        {
            StartCoroutine(LoadRoutine(absolutePath, onLoaded, onError));
        }

        private IEnumerator LoadRoutine(string absolutePath, Action<AudioClip> onLoaded, Action<string> onError)
        {
            if (string.IsNullOrWhiteSpace(absolutePath))
            {
                onError?.Invoke("Caminho de áudio inválido.");
                yield break;
            }

            string url = $"file://{absolutePath}";
            AudioType audioType = absolutePath.EndsWith(".wav", StringComparison.OrdinalIgnoreCase)
                ? AudioType.WAV
                : AudioType.MPEG;

            using UnityWebRequest req = UnityWebRequestMultimedia.GetAudioClip(url, audioType);
            yield return req.SendWebRequest();

            if (req.result != UnityWebRequest.Result.Success)
            {
                onError?.Invoke($"Falha ao carregar áudio: {req.error}");
                yield break;
            }

            AudioClip clip = DownloadHandlerAudioClip.GetContent(req);
            onLoaded?.Invoke(clip);
        }
    }
}
```

## Unity/Assets/Scripts/Core/DifficultyController.cs

```csharp
using PeterDrummer.Data;
using PeterDrummer.InputSystem;
using PeterDrummer.Rhythm;
using UnityEngine;

namespace PeterDrummer.Core
{
    /// <summary>
    /// Aplica presets de dificuldade em janela de hit, velocidade e multiplicador de score.
    /// </summary>
    public class DifficultyController : MonoBehaviour
    {
        [SerializeField] private PlayerInputController inputController;
        [SerializeField] private NoteSpawner noteSpawner;
        [SerializeField] private ScoreSystem scoreSystem;
        [SerializeField] private GameDifficulty currentDifficulty = GameDifficulty.Normal;

        private void Awake()
        {
            ApplyDifficulty(currentDifficulty);
        }

        public void SetEasy() => ApplyDifficulty(GameDifficulty.Easy);
        public void SetNormal() => ApplyDifficulty(GameDifficulty.Normal);
        public void SetHard() => ApplyDifficulty(GameDifficulty.Hard);

        public void ApplyDifficulty(GameDifficulty difficulty)
        {
            currentDifficulty = difficulty;

            switch (difficulty)
            {
                case GameDifficulty.Easy:
                    inputController.ConfigureHitWindows(0.065, 0.120);
                    noteSpawner.SetScrollSpeed(6.0f);
                    scoreSystem.ConfigureScoreMultiplier(0.90f);
                    break;

                case GameDifficulty.Normal:
                    inputController.ConfigureHitWindows(0.045, 0.090);
                    noteSpawner.SetScrollSpeed(8.0f);
                    scoreSystem.ConfigureScoreMultiplier(1.00f);
                    break;

                case GameDifficulty.Hard:
                    inputController.ConfigureHitWindows(0.030, 0.065);
                    noteSpawner.SetScrollSpeed(10.5f);
                    scoreSystem.ConfigureScoreMultiplier(1.20f);
                    break;
            }
        }
    }
}
```

## Unity/Assets/Scripts/Core/GameFlowController.cs

```csharp
using PeterDrummer.Audio;
using PeterDrummer.Rhythm;
using UnityEngine;

namespace PeterDrummer.Core
{
    /// <summary>
    /// Orquestra pipeline: importar áudio -> carregar -> analisar -> tocar + spawn.
    /// </summary>
    public class GameFlowController : MonoBehaviour
    {
        [SerializeField] private AudioImportService importService;
        [SerializeField] private RuntimeAudioLoader audioLoader;
        [SerializeField] private AudioAnalyzer analyzer;
        [SerializeField] private SongConductor conductor;
        [SerializeField] private NoteSpawner spawner;
        [SerializeField] private ScoreSystem scoreSystem;

        public void SelectAndStartSong()
        {
            importService.PickAudioFile(OnPathPicked, OnError);
        }

        private void OnPathPicked(string path)
        {
            audioLoader.LoadFromAbsolutePath(path, OnAudioLoaded, OnError);
        }

        private void OnAudioLoaded(AudioClip clip)
        {
            var beatEvents = analyzer.Analyze(clip);
            spawner.SetBeatMap(beatEvents);
            scoreSystem?.ResetRun();
            conductor.PrepareAndPlay(clip);
            Debug.Log($"Beatmap gerado com {beatEvents.Count} eventos.");
        }

        private void OnError(string error)
        {
            Debug.LogError(error);
        }
    }
}
```

## Unity/Assets/Scripts/Core/ScoreSystem.cs

```csharp
using PeterDrummer.Data;
using PeterDrummer.InputSystem;
using UnityEngine;

namespace PeterDrummer.Core
{
    /// <summary>
    /// Sistema simples de score/combos para primeira versão testável.
    /// </summary>
    public class ScoreSystem : MonoBehaviour
    {
        [SerializeField] private PlayerInputController inputController;
        [SerializeField] private float scoreMultiplier = 1f;

        public int Score { get; private set; }
        public int Combo { get; private set; }
        public int BestCombo { get; private set; }
        public int PerfectCount { get; private set; }
        public int GoodCount { get; private set; }
        public int MissCount { get; private set; }

        public int TotalJudged => PerfectCount + GoodCount + MissCount;
        public float AccuracyPercent => TotalJudged == 0
            ? 0f
            : ((PerfectCount * 1f + GoodCount * 0.5f) / TotalJudged) * 100f;

        public HitJudgement LastJudgement { get; private set; }
        public double LastErrorMs { get; private set; }

        private void OnEnable()
        {
            inputController.OnJudgement += OnJudgement;
        }

        private void OnDisable()
        {
            inputController.OnJudgement -= OnJudgement;
        }

        public void ConfigureScoreMultiplier(float multiplier)
        {
            scoreMultiplier = Mathf.Max(0.1f, multiplier);
        }

        public void ResetRun()
        {
            Score = 0;
            Combo = 0;
            BestCombo = 0;
            PerfectCount = 0;
            GoodCount = 0;
            MissCount = 0;
            LastErrorMs = 0d;
            LastJudgement = HitJudgement.Miss;
        }

        private void OnJudgement(DrumLane lane, HitJudgement judgement, double errorMs)
        {
            LastJudgement = judgement;
            LastErrorMs = errorMs;

            switch (judgement)
            {
                case HitJudgement.Perfect:
                    PerfectCount++;
                    Combo++;
                    Score += Mathf.RoundToInt((300 + Combo * 2) * scoreMultiplier);
                    break;

                case HitJudgement.Good:
                    GoodCount++;
                    Combo++;
                    Score += Mathf.RoundToInt((150 + Combo) * scoreMultiplier);
                    break;

                case HitJudgement.Miss:
                    MissCount++;
                    Combo = 0;
                    break;
            }

            if (Combo > BestCombo)
            {
                BestCombo = Combo;
            }
        }
    }
}
```

## Unity/Assets/Scripts/Core/SongConductor.cs

```csharp
using System;
using UnityEngine;

namespace PeterDrummer.Core
{
    /// <summary>
    /// Relógio central do jogo. Toda lógica de sincronização usa DSP time.
    /// </summary>
    public class SongConductor : MonoBehaviour
    {
        [SerializeField] private AudioSource musicSource;
        [SerializeField] private float startDelaySec = 0.15f;

        public double SongStartDspTime { get; private set; }
        public bool IsPlaying { get; private set; }
        public event Action OnSongEnded;

        private bool _songEndedRaised;

        public double CurrentSongTimeSec
        {
            get
            {
                if (!IsPlaying) return 0d;
                return AudioSettings.dspTime - SongStartDspTime;
            }
        }

        private void Update()
        {
            if (!IsPlaying || _songEndedRaised || musicSource.clip == null) return;

            if (CurrentSongTimeSec >= musicSource.clip.length)
            {
                _songEndedRaised = true;
                IsPlaying = false;
                OnSongEnded?.Invoke();
            }
        }

        public void PrepareAndPlay(AudioClip clip)
        {
            musicSource.clip = clip;
            SongStartDspTime = AudioSettings.dspTime + startDelaySec;
            _songEndedRaised = false;
            musicSource.PlayScheduled(SongStartDspTime);
            IsPlaying = true;
        }

        public void StopSong()
        {
            musicSource.Stop();
            IsPlaying = false;
        }
    }
}
```

## Unity/Assets/Scripts/Data/GameDifficulty.cs

```csharp
namespace PeterDrummer.Data
{
    public enum GameDifficulty
    {
        Easy = 0,
        Normal = 1,
        Hard = 2
    }
}
```

## Unity/Assets/Scripts/Data/HitJudgement.cs

```csharp
namespace PeterDrummer.Data
{
    public enum HitJudgement
    {
        Perfect = 0,
        Good = 1,
        Miss = 2
    }
}
```

## Unity/Assets/Scripts/Data/RhythmTypes.cs

```csharp
using System;

namespace PeterDrummer.Data
{
    public enum DrumLane
    {
        Kick = 0,
        Snare = 1,
        HiHat = 2
    }

    [Serializable]
    public struct BeatEvent
    {
        public DrumLane Lane;
        public double TimeSec;
        public float Strength;

        public BeatEvent(DrumLane lane, double timeSec, float strength)
        {
            Lane = lane;
            TimeSec = timeSec;
            Strength = strength;
        }
    }
}
```

## Unity/Assets/Scripts/Input/HitZone.cs

```csharp
using System;
using System.Collections.Generic;
using PeterDrummer.Core;
using PeterDrummer.Data;
using PeterDrummer.Rhythm;
using UnityEngine;

namespace PeterDrummer.InputSystem
{
    public class HitZone : MonoBehaviour
    {
        [SerializeField] private SongConductor conductor;
        [SerializeField] private DrumLane lane;

        private readonly List<NoteObject> _inside = new();

        public event Action<DrumLane> OnAutoMiss;
        public DrumLane Lane => lane;

        public NoteObject GetClosest(double songTimeSec)
        {
            NoteObject best = null;
            double bestAbs = double.MaxValue;

            foreach (NoteObject note in _inside)
            {
                if (note == null) continue;
                double err = Math.Abs(note.TargetSongTimeSec - songTimeSec);
                if (err < bestAbs)
                {
                    bestAbs = err;
                    best = note;
                }
            }

            return best;
        }

        private void OnTriggerEnter2D(Collider2D other)
        {
            if (other.TryGetComponent(out NoteObject note) && note.Lane == lane)
            {
                _inside.Add(note);
            }
        }

        private void OnTriggerExit2D(Collider2D other)
        {
            if (!other.TryGetComponent(out NoteObject note)) return;
            if (!_inside.Remove(note)) return;

            if (conductor == null || !conductor.IsPlaying) return;
            if (note.Lane != lane) return;

            // Nota passou da zona sem hit => MISS automático.
            if (conductor.CurrentSongTimeSec > note.TargetSongTimeSec)
            {
                OnAutoMiss?.Invoke(lane);
            }
        }

        public void Remove(NoteObject note) => _inside.Remove(note);
    }
}
```

## Unity/Assets/Scripts/Input/PlayerInputController.cs

```csharp
using System;
using PeterDrummer.Core;
using PeterDrummer.Data;
using PeterDrummer.Rhythm;
using UnityEngine;

namespace PeterDrummer.InputSystem
{
    /// <summary>
    /// Mapeia botões/toques para lanes e valida hit por janela temporal.
    /// </summary>
    public class PlayerInputController : MonoBehaviour
    {
        [SerializeField] private SongConductor conductor;
        [SerializeField] private HitZone kickZone;
        [SerializeField] private HitZone snareZone;
        [SerializeField] private HitZone hihatZone;
        [SerializeField] private double perfectWindowSec = 0.045;
        [SerializeField] private double goodWindowSec = 0.090;

        public event Action<DrumLane> OnLanePlayed;
        public event Action<DrumLane, HitJudgement, double> OnJudgement;

        private void OnEnable()
        {
            kickZone.OnAutoMiss += RegisterAutoMiss;
            snareZone.OnAutoMiss += RegisterAutoMiss;
            hihatZone.OnAutoMiss += RegisterAutoMiss;
        }

        private void OnDisable()
        {
            kickZone.OnAutoMiss -= RegisterAutoMiss;
            snareZone.OnAutoMiss -= RegisterAutoMiss;
            hihatZone.OnAutoMiss -= RegisterAutoMiss;
        }

        public void PressKick() => TryHit(kickZone, DrumLane.Kick);
        public void PressSnare() => TryHit(snareZone, DrumLane.Snare);
        public void PressHiHat() => TryHit(hihatZone, DrumLane.HiHat);

        public void ConfigureHitWindows(double perfectSec, double goodSec)
        {
            perfectWindowSec = perfectSec;
            goodWindowSec = goodSec;
        }

        private void Update()
        {
            // Atalhos úteis para testes no editor.
            if (UnityEngine.Input.GetKeyDown(KeyCode.A)) PressKick();
            if (UnityEngine.Input.GetKeyDown(KeyCode.S)) PressSnare();
            if (UnityEngine.Input.GetKeyDown(KeyCode.D)) PressHiHat();
        }

        private void TryHit(HitZone zone, DrumLane lane)
        {
            if (!conductor.IsPlaying) return;

            OnLanePlayed?.Invoke(lane);

            double now = conductor.CurrentSongTimeSec;
            NoteObject note = zone.GetClosest(now);
            if (note == null)
            {
                EmitJudgement(lane, HitJudgement.Miss, -1d);
                return;
            }

            double error = Math.Abs(note.TargetSongTimeSec - now);
            if (error <= perfectWindowSec)
            {
                zone.Remove(note);
                Destroy(note.gameObject);
                EmitJudgement(lane, HitJudgement.Perfect, error * 1000d);
                return;
            }

            if (error <= goodWindowSec)
            {
                zone.Remove(note);
                Destroy(note.gameObject);
                EmitJudgement(lane, HitJudgement.Good, error * 1000d);
                return;
            }

            EmitJudgement(lane, HitJudgement.Miss, error * 1000d);
        }

        private void RegisterAutoMiss(DrumLane lane)
        {
            EmitJudgement(lane, HitJudgement.Miss, -1d);
        }

        private void EmitJudgement(DrumLane lane, HitJudgement judgement, double errorMs)
        {
            OnJudgement?.Invoke(lane, judgement, errorMs);
        }
    }
}
```

## Unity/Assets/Scripts/Rhythm/NoteObject.cs

```csharp
using PeterDrummer.Core;
using PeterDrummer.Data;
using UnityEngine;

namespace PeterDrummer.Rhythm
{
    public class NoteObject : MonoBehaviour
    {
        [SerializeField] private double autoDespawnAfterHitSec = 0.2d;

        public DrumLane Lane { get; private set; }
        public double TargetSongTimeSec { get; private set; }

        private SongConductor _conductor;
        private float _hitX;
        private float _directionSign;
        private float _scrollSpeed;

        public void Initialize(
            DrumLane lane,
            double targetSongTimeSec,
            SongConductor conductor,
            float spawnX,
            float hitX,
            float scrollSpeed)
        {
            Lane = lane;
            TargetSongTimeSec = targetSongTimeSec;
            _conductor = conductor;
            _hitX = hitX;
            _scrollSpeed = scrollSpeed;
            _directionSign = Mathf.Sign(spawnX - hitX);
        }

        private void Update()
        {
            if (_conductor == null || !_conductor.IsPlaying) return;

            // Posição absoluta pela diferença entre o tempo atual da música e o tempo alvo da nota.
            // Quando now == target, a nota está exatamente no hitX.
            double now = _conductor.CurrentSongTimeSec;
            double remaining = TargetSongTimeSec - now;
            float x = _hitX + (float)(remaining * _scrollSpeed * _directionSign);

            Vector3 pos = transform.position;
            pos.x = x;
            transform.position = pos;

            if (now > TargetSongTimeSec + autoDespawnAfterHitSec)
            {
                Destroy(gameObject);
            }
        }
    }
}
```

## Unity/Assets/Scripts/Rhythm/NoteSpawner.cs

```csharp
using System.Collections.Generic;
using PeterDrummer.Core;
using PeterDrummer.Data;
using UnityEngine;

namespace PeterDrummer.Rhythm
{
    /// <summary>
    /// Spawna notas com antecedência baseada em distância/velocidade,
    /// garantindo cruzamento no timestamp exato da música.
    /// </summary>
    public class NoteSpawner : MonoBehaviour
    {
        [SerializeField] private SongConductor conductor;
        [SerializeField] private Transform spawnPoint;
        [SerializeField] private Transform hitPoint;
        [SerializeField] private float scrollSpeedUnitsPerSec = 8f;
        [SerializeField] private NoteObject kickPrefab;
        [SerializeField] private NoteObject snarePrefab;
        [SerializeField] private NoteObject hihatPrefab;

        private readonly List<BeatEvent> _events = new();
        private int _nextIndex;

        private float TravelDistance => Mathf.Abs(spawnPoint.position.x - hitPoint.position.x);
        public float TravelTimeSec => TravelDistance / scrollSpeedUnitsPerSec;


        public void SetScrollSpeed(float speedUnitsPerSec)
        {
            scrollSpeedUnitsPerSec = Mathf.Max(0.01f, speedUnitsPerSec);
        }

        public void SetBeatMap(List<BeatEvent> beatEvents)
        {
            _events.Clear();
            _events.AddRange(beatEvents);
            _events.Sort((a, b) => a.TimeSec.CompareTo(b.TimeSec));
            _nextIndex = 0;
        }

        private void Update()
        {
            if (!conductor.IsPlaying || _events.Count == 0) return;

            double songNow = conductor.CurrentSongTimeSec;

            // event.TimeSec - songNow <= TravelTime => nota deve nascer agora.
            while (_nextIndex < _events.Count)
            {
                BeatEvent evt = _events[_nextIndex];
                if (evt.TimeSec - songNow > TravelTimeSec) break;

                Spawn(evt);
                _nextIndex++;
            }
        }

        private void Spawn(BeatEvent evt)
        {
            NoteObject prefab = evt.Lane switch
            {
                DrumLane.Kick => kickPrefab,
                DrumLane.Snare => snarePrefab,
                _ => hihatPrefab
            };

            NoteObject note = Instantiate(prefab, spawnPoint.position, Quaternion.identity, transform);
            note.Initialize(
                evt.Lane,
                evt.TimeSec,
                conductor,
                spawnPoint.position.x,
                hitPoint.position.x,
                scrollSpeedUnitsPerSec);
        }
    }
}
```

## Unity/Assets/Scripts/Visual/CharacterVisualController.cs

```csharp
using PeterDrummer.Data;
using PeterDrummer.InputSystem;
using UnityEngine;

namespace PeterDrummer.Visual
{
    /// <summary>
    /// Recebe eventos de input e dispara animações/efeitos no personagem.
    /// </summary>
    public class CharacterVisualController : MonoBehaviour
    {
        [SerializeField] private PlayerInputController inputController;
        [SerializeField] private Animator animator;

        private static readonly int KickHash = Animator.StringToHash("Kick");
        private static readonly int SnareHash = Animator.StringToHash("Snare");
        private static readonly int HiHatHash = Animator.StringToHash("HiHat");

        private void OnEnable()
        {
            inputController.OnLanePlayed += OnLanePlayed;
        }

        private void OnDisable()
        {
            inputController.OnLanePlayed -= OnLanePlayed;
        }

        private void OnLanePlayed(DrumLane lane)
        {
            int trigger = lane switch
            {
                DrumLane.Kick => KickHash,
                DrumLane.Snare => SnareHash,
                _ => HiHatHash
            };

            animator.SetTrigger(trigger);
        }
    }
}
```

## Unity/Assets/Scripts/Visual/HudFeedbackController.cs

```csharp
using PeterDrummer.Core;
using PeterDrummer.Data;
using UnityEngine;
using UnityEngine.UI;

namespace PeterDrummer.Visual
{
    /// <summary>
    /// Atualiza textos de score/combo/judgement em HUD simples.
    /// </summary>
    public class HudFeedbackController : MonoBehaviour
    {
        [SerializeField] private ScoreSystem scoreSystem;
        [SerializeField] private Text scoreText;
        [SerializeField] private Text comboText;
        [SerializeField] private Text judgementText;

        private void Update()
        {
            if (scoreSystem == null) return;

            if (scoreText != null) scoreText.text = $"Score: {scoreSystem.Score}";
            if (comboText != null) comboText.text = $"Combo: {scoreSystem.Combo}";

            if (judgementText != null)
            {
                string err = scoreSystem.LastErrorMs < 0d ? "--" : $"{scoreSystem.LastErrorMs:0.0}ms";
                judgementText.text = $"{scoreSystem.LastJudgement} ({err})";
                judgementText.color = scoreSystem.LastJudgement switch
                {
                    HitJudgement.Perfect => Color.cyan,
                    HitJudgement.Good => Color.green,
                    _ => Color.red
                };
            }
        }
    }
}
```

## Unity/Assets/Scripts/Visual/IntroVideoController.cs

```csharp
using System.Text.RegularExpressions;
using UnityEngine;
using UnityEngine.Video;

namespace PeterDrummer.Visual
{
    /// <summary>
    /// Controla vídeo de introdução (local ou URL) com opção de pular por toque.
    /// </summary>
    public class IntroVideoController : MonoBehaviour
    {
        [SerializeField] private VideoPlayer videoPlayer;
        [SerializeField] private GameObject introRoot;
        [SerializeField] private bool autoPlayOnStart = true;
        [SerializeField] private bool allowSkip = true;
        [SerializeField] private string localStreamingAssetFile = "intro.mp4";

        private bool _isPlaying;

        private void Awake()
        {
            if (videoPlayer != null)
            {
                videoPlayer.loopPointReached += OnVideoFinished;
            }
        }

        private void OnDestroy()
        {
            if (videoPlayer != null)
            {
                videoPlayer.loopPointReached -= OnVideoFinished;
            }
        }

        private void Start()
        {
            if (autoPlayOnStart)
            {
                PlayFromStreamingAssets();
            }
        }

        private void Update()
        {
            if (!allowSkip || !_isPlaying) return;

            if (Input.touchCount > 0 || Input.GetKeyDown(KeyCode.Space) || Input.GetMouseButtonDown(0))
            {
                Skip();
            }
        }

        public void PlayFromStreamingAssets()
        {
            if (videoPlayer == null) return;

            string url = $"{Application.streamingAssetsPath}/{localStreamingAssetFile}";
            videoPlayer.source = VideoSource.Url;
            videoPlayer.url = url;
            Play();
        }

        public void PlayFromUrl(string url)
        {
            if (videoPlayer == null || string.IsNullOrWhiteSpace(url)) return;

            videoPlayer.source = VideoSource.Url;
            videoPlayer.url = ConvertGoogleDriveShareToDirect(url);
            Play();
        }

        private void Play()
        {
            if (introRoot != null) introRoot.SetActive(true);
            _isPlaying = true;
            videoPlayer.Play();
        }

        public void Skip()
        {
            if (videoPlayer != null) videoPlayer.Stop();
            OnVideoFinished(null);
        }

        private void OnVideoFinished(VideoPlayer _)
        {
            _isPlaying = false;
            if (introRoot != null) introRoot.SetActive(false);
        }

        // Ex.: https://drive.google.com/file/d/<ID>/view?... -> https://drive.google.com/uc?export=download&id=<ID>
        private static string ConvertGoogleDriveShareToDirect(string input)
        {
            Match m = Regex.Match(input, @"/file/d/([a-zA-Z0-9_-]+)");
            if (!m.Success) return input;
            string id = m.Groups[1].Value;
            return $"https://drive.google.com/uc?export=download&id={id}";
        }
    }
}
```

## Unity/Assets/Scripts/Visual/ResultsPanelController.cs

```csharp
using PeterDrummer.Core;
using UnityEngine;
using UnityEngine.UI;

namespace PeterDrummer.Visual
{
    /// <summary>
    /// Exibe resultados finais quando a música termina.
    /// </summary>
    public class ResultsPanelController : MonoBehaviour
    {
        [SerializeField] private SongConductor conductor;
        [SerializeField] private ScoreSystem scoreSystem;
        [SerializeField] private GameObject panelRoot;
        [SerializeField] private Text scoreText;
        [SerializeField] private Text comboText;
        [SerializeField] private Text accuracyText;
        [SerializeField] private Text countsText;

        private void OnEnable()
        {
            conductor.OnSongEnded += ShowResults;
        }

        private void OnDisable()
        {
            conductor.OnSongEnded -= ShowResults;
        }

        private void Start()
        {
            if (panelRoot != null)
            {
                panelRoot.SetActive(false);
            }
        }

        private void ShowResults()
        {
            if (panelRoot != null)
            {
                panelRoot.SetActive(true);
            }

            if (scoreText != null) scoreText.text = $"Score: {scoreSystem.Score}";
            if (comboText != null) comboText.text = $"Best Combo: {scoreSystem.BestCombo}";
            if (accuracyText != null) accuracyText.text = $"Accuracy: {scoreSystem.AccuracyPercent:0.00}%";

            if (countsText != null)
            {
                countsText.text =
                    $"Perfect: {scoreSystem.PerfectCount}\n" +
                    $"Good: {scoreSystem.GoodCount}\n" +
                    $"Miss: {scoreSystem.MissCount}";
            }
        }
    }
}
```
