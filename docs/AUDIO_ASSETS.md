# Contrato dos assets de bateria

O catálogo do build pessoal contém 144 arquivos Opus:

| Família | Quantidade |
|---|---:|
| Bumbo | 6 intensidades × 3 variações |
| Snare | 6 × 3 |
| Chimbal fechado | 5 × 3 |
| Chimbal aberto derivado | 5 × 3 |
| Chimbal de pedal derivado | 3 variações |
| Tom 1 | 5 × 3 |
| Tom 2 | 5 × 3 |
| Surdo | 5 × 3 |
| Prato de ataque | 5 × 3 |
| Prato de condução | 5 × 3 |

O pacote privado possui `manifest.json` com origem, derivação e SHA-256 de
cada asset preparado. Os binários não são distribuídos no repositório público.

## Alvo sonoro

- hard rock dos anos 80;
- bumbo e tons com caráter de pele hidráulica;
- caixa encorpada, sem som plástico;
- pratos B20 musicais, brilhantes e com decay natural;
- 48 kHz, estéreo, Opus sem compressão adicional pelo empacotador Android.

O piloto usa `SoundPool` com −6 dB de headroom. Pratos acima de cinco segundos
são encerrados com fade para respeitar o limite de memória decodificada por
sample. O motor nativo futuro poderá voltar às caudas integrais sem mudar o
formato das partituras.

Fechado ou pedal interrompe a voz aberta do chimbal antes do novo ataque.

Não rotular samples como Zildjian oficiais sem procedência/licença que permita
essa afirmação. A referência é sonora, não de marca.
