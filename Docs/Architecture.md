# Peter Drummer - Arquitetura Modular (Unity / Android)

## 1) Estrutura de classes recomendada

- **Core**
  - `GameFlowController`: fluxo principal do jogo.
  - `SongConductor`: relógio DSP e controle de playback.
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
- **Data**
  - `DrumLane` e `BeatEvent`: tipos compartilhados.

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

### Posição da nota baseada em DSP
Para reduzir drift por FPS:

- No spawn, salvar `dspSpawn = AudioSettings.dspTime`
- Em cada `Update`:
  - `elapsed = AudioSettings.dspTime - dspSpawn`
  - `alpha = clamp01(elapsed / travelTime)`
  - `x = lerp(x_spawn, x_hit, alpha)`

> Resultado: a posição da nota segue o relógio de áudio, não o delta de frame.

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

