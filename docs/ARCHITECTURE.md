# Arquitetura

## Escolha de stack

O reinício usa Android nativo em Kotlin. A pista é uma `View` desenhada em
`Canvas`, o que permite controlar geometria, multitouch e animação sem carregar
um motor Unity inteiro.

As telas comuns usam componentes da plataforma Android. Isso mantém o primeiro
projeto pequeno e reduz dependências antes de o motor de áudio definitivo estar
validado.

## Fonte de verdade

A fonte de verdade rítmica é `RhythmChart.events`, nunca uma análise espectral
da música. Cada evento contém:

- instante em milissegundos;
- pista/peça;
- intensidade MIDI de 1 a 127.

Eventos com o mesmo tempo e pistas diferentes representam batidas simultâneas.

## Relógio

O protótipo usa `SystemClock.uptimeMillis()` ancorado ao início da execução. A
trilha externa será sincronizada a esse relógio com um `audioOffsetMs` próprio
por arquivo e uma compensação global do aparelho.

O relógio visual nunca deverá depender de contagem de quadros. Quedas de frame
alteram a posição renderizada, não o tempo musical.

## Áudio

O `DrumSamplePlayer` atual usa `SoundPool`, suficiente para validar interação e
nomes de assets. O motor definitivo deve usar Oboe/AAudio para reduzir latência,
com samples a 48 kHz e callbacks sem alocação, bloqueio ou I/O.

O backing track e os samples terão controles de volume independentes. Bluetooth
precisará de calibração explícita devido à latência variável.

## Armazenamento

- Pasta das músicas: Storage Access Framework com permissão persistente de
  leitura; nenhuma permissão ampla de armazenamento.
- Configurações: `SharedPreferences` no protótipo.
- Ranking: JSON local em `SharedPreferences`, ordenado e limitado a cinco.
- Futuro catálogo: arquivo empacotado + associações locais entre `songId` e URI.

## Arte e animação

A imagem conceitual não será usada como interface interativa pronta. Pistas,
notas, linhas, textos e botões são desenhados pelo motor para garantir contagem
e posição exatas.

Peter será um asset separado com estados para as oito peças. A partitura conduz
as animações no modo automático; o toque real do jogador as conduz nos modos
jogáveis.
