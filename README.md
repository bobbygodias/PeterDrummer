# Peter Drummer

Jogo rítmico Android feito para Peter tocar bateria sobre faixas **drumless**
guardadas no próprio aparelho.

> Estado atual: catálogo e contrato de pistas da v0.3 em validação. A v0.2.3
> já foi executada em tablet e revelou os ajustes de ordem, áudio e persistência
> que esta versão corrige.

## O que torna este jogo diferente

- As músicas não são empacotadas no APK.
- O jogador escolhe uma pasta externa usando o seletor seguro do Android.
- Cada música possui uma partitura temporal exata incluída no aplicativo.
- O jogo não tenta adivinhar a bateria analisando frequências do MP3.
- Os toques disparam samples internos e a animação correspondente do Peter.
- O modo automático executa a mesma partitura usada no modo jogável.

## Oito pistas fixas

| Índice | Peça |
|---:|---|
| 0 | Snare / caixa |
| 1 | Tom 1 |
| 2 | Tom 2 |
| 3 | Surdo |
| 4 | Bumbo |
| 5 | Chimbal |
| 6 | Prato de ataque / crash |
| 7 | Prato de condução / ride |

Os índices fazem parte do formato das partituras e não devem ser reordenados
depois que os mapas forem publicados.

## Primeira fatia implementada

- projeto Android Kotlin nativo, sem Unity;
- interface vertical e offline;
- intro opcional antes do menu;
- menu com os três modos definidos para o produto;
- seleção persistente e separada das pastas de músicas e partituras extras;
- leitura de partituras internas empacotadas e extras `.pdrum.json`;
- dificuldade e velocidade visual calculadas a partir de cada partitura;
- volumes independentes de música e bateria;
- pista em perspectiva com exatamente oito linhas e oito botões;
- linha de preparação separada da zona real de acerto;
- suporte a multitouch e notas simultâneas;
- estado de performance do Peter com poses compostas para braços e pedal;
- julgamento temporal determinístico;
- três mensagens finais por faixa de acerto;
- ranking local com cinco posições;
- demonstração automática usando a mesma partitura de calibração.
- suporte à intro real no build pessoal;
- catálogo para 126 samples em camadas de intensidade e round-robin;
- catálogo para 15 chimbais abertos derivados e 3 fechamentos por pedal;
- notas distintas para chimbal fechado, aberto e pedal na mesma pista;
- esqueleto temporal verificado da faixa-laboratório **Even Flow**.

Enquanto a transcrição de **Even Flow** não está concluída, os três modos
abrem uma partitura curta de calibração que já exercita as oito pistas e as
três articulações do chimbal.

Os binários da intro e do kit não são publicados no repositório. O build
pessoal os recebe localmente depois da validação do pacote certificado.

## Compilar

Requisitos previstos:

- JDK 17;
- Android SDK 36;
- Android Gradle Plugin 8.13;
- Gradle 8.13;
- Kotlin 2.4.10.

```bash
gradle test
gradle assembleDebug
```

O CI usa Gradle 8.13 diretamente. O wrapper binário será gerado depois do
primeiro build validado, evitando publicar um JAR que esta sessão não conseguiu
produzir ou verificar.

## Documentação

- [`docs/PRODUCT.md`](docs/PRODUCT.md): fluxo completo aprovado.
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md): decisões técnicas e relógio.
- [`docs/AUDIO_ASSETS.md`](docs/AUDIO_ASSETS.md): contrato dos samples.
- [`docs/EVEN_FLOW_PILOT.md`](docs/EVEN_FLOW_PILOT.md): faixa-laboratório.
- [`docs/CHART_FORMAT.md`](docs/CHART_FORMAT.md): formato das partituras extras.
- [`docs/CONTINUITY.md`](docs/CONTINUITY.md): estado verificável para retomada.

## Conteúdo externo

Este repositório não distribui as faixas musicais. O usuário fornece os próprios
arquivos drumless e concede acesso somente à pasta escolhida.

Código e documentação: CC0 1.0 Universal. Áudio e intro seguem as exceções
descritas em [`ASSET_LICENSE.md`](ASSET_LICENSE.md).
