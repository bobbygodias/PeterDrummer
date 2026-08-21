# Contrato dos assets de bateria

Colocar em `app/src/main/res/raw/`:

| Recurso | Peça |
|---|---|
| `drum_hihat.wav` | Chimbal |
| `drum_crash.wav` | Prato de ataque |
| `drum_ride.wav` | Prato de condução |
| `drum_snare.wav` | Snare / caixa |
| `drum_tom_high.wav` | Tom 1 |
| `drum_tom_mid.wav` | Tom 2 |
| `drum_floor_tom.wav` | Surdo |
| `drum_kick.wav` | Bumbo |

## Alvo sonoro

- hard rock dos anos 80;
- bumbo e tons com caráter de pele hidráulica;
- caixa encorpada, sem som plástico;
- pratos B20 musicais, brilhantes e com decay natural;
- 48 kHz, PCM WAV, mesma quantidade de canais em todo o kit.

O primeiro arquivo de cada peça valida o motor. A evolução deverá oferecer de
quatro a oito camadas de velocidade e variações round-robin para evitar efeito
de metralhadora.

Não rotular samples como Zildjian oficiais sem procedência/licença que permita
essa afirmação. A referência é sonora, não de marca.
