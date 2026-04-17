# Peter Drummer 🥁
Toque bateria no ritmo da sua própria música no Android (celular, tablet ou TV com Android 9+).

## 📦 Baixar APK (Android 9+)
> **Link direto (primeira página do repositório):**

➡️ **[Download PeterDrummer.apk](https://github.com/bobbygodias/PeterDrummer/releases/latest/download/PeterDrummer.apk)**

> O arquivo APK deve ser publicado em **Releases** com exatamente o nome `PeterDrummer.apk`.

---



## 📚 Pacotão de código (arquivo único)
Se você quiser tudo consolidado em um arquivo só, use:

- `Docs/Pacotao-Codigo.md`

Esse arquivo contém os scripts principais + docs + workflow de build APK em formato único para leitura/backup.

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
