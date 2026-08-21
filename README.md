# Peter Drummer

Jogo rítmico Android feito para Peter tocar bateria sobre faixas **drumless**
guardadas no próprio aparelho.

> Estado atual: reconstrução nativa iniciada em 20/08/2026. Ainda não existe
> um APK testado em aparelho, mas testes unitários e `assembleDebug` passam no
> GitHub Actions.

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
| 0 | Chimbal |
| 1 | Prato de ataque / crash |
| 2 | Prato de condução / ride |
| 3 | Snare / caixa |
| 4 | Tom 1 |
| 5 | Tom 2 |
| 6 | Surdo |
| 7 | Bumbo |

Os índices fazem parte do formato das partituras e não devem ser reordenados
depois que os mapas forem publicados.

## Primeira fatia implementada

- projeto Android Kotlin nativo, sem Unity;
- interface vertical e offline;
- intro opcional antes do menu;
- menu com os três modos definidos para o produto;
- seleção persistente da pasta externa;
- volumes independentes de música e bateria;
- pista em perspectiva com exatamente oito linhas e oito botões;
- linha de preparação separada da zona real de acerto;
- suporte a multitouch e notas simultâneas;
- estado de performance do Peter com poses compostas para braços e pedal;
- julgamento temporal determinístico;
- três mensagens finais por faixa de acerto;
- ranking local com cinco posições;
- demonstração automática usando a mesma partitura de calibração.

Enquanto as partituras e WAVs definitivos não chegam, os três modos abrem uma
partitura curta de calibração. Isso prova o motor sem fingir que o catálogo já
está pronto.

## Assets ainda necessários

Coloque o vídeo aprovado em:

`app/src/main/res/raw/peter_drummer_intro.mp4`

Os WAVs seguirão os nomes documentados em
[`docs/AUDIO_ASSETS.md`](docs/AUDIO_ASSETS.md). Arquivos ausentes ficam em
silêncio para não impedir a evolução do código.

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
- [`docs/CONTINUITY.md`](docs/CONTINUITY.md): estado verificável para retomada.

## Conteúdo externo

Este repositório não distribui as faixas musicais. O usuário fornece os próprios
arquivos drumless e concede acesso somente à pasta escolhida.

Código e documentação: CC0 1.0 Universal, conforme o arquivo `LICENSE`.
