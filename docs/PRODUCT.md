# Produto: Peter Drummer

## Pessoa e momento

- Usuário principal: Peter.
- Uso: celular Android, vertical, offline e sem conta.
- Propósito: tocar bateria por cima de uma coleção pessoal de faixas drumless.

## Fluxo

1. Abrir o ícone.
2. Reproduzir a intro.
3. Mostrar Peter desenhado girando uma baqueta enquanto aguarda.
4. Exibir **“Clique Aqui e Vamos Arrebentar!”**.
5. O clique toca caixa, seguido por uma virada e crash.
6. Mostrar:
   - **Modo Aleatório**;
   - **Escolha Aê, Fera**;
   - **Só Quer Curtir?**.
7. Todos os botões produzem uma baquetada de caixa.
8. Jogar ou assistir à demonstração usando uma partitura temporal exata.
9. Mostrar a mensagem correspondente à proporção de eventos acertados:
   - até 1/3: **“Precisa Praticar Mais, Cara!”**;
   - acima de 1/3 até 60%: **“É, Tá Quase Lá, Mano!”**;
   - acima de 60%: **“Cara, Você Toca Pra Caramba!”**.
10. Reexibir os três modos e **“Por Hoje Já Deu!”**.
11. Ao encerrar, exibir o ranking de cinco posições, salvar nome/pontuação e
    fechar o aplicativo.

## Configuração permitida

- Pasta externa das músicas.
- Pasta externa opcional de partituras extras.
- Volume da música.
- Volume da bateria.
- Calibração de latência será adicionada quando o áudio real for integrado.

A configuração aparece somente na primeira tela. Depois de escolher as pastas,
os três modos usam o catálogo persistido sem pedir configuração novamente.

## Regras de conteúdo

- Nenhuma das músicas drumless entra no APK.
- As partituras temporais entram no APK.
- Partituras extras no formato `.pdrum.json` podem ser adicionadas numa pasta
  separada, sem substituir as internas.
- Os samples do kit entram no APK.
- O catálogo associa a partitura ao arquivo externo, com alternativa de
  associação manual quando o nome ou a duração não coincidirem.

## Tela de jogo

- Exatamente oito pistas e oito botões.
- Peter e sua bateria ficam no ponto de fuga.
- A pista nasce perto de Peter e cresce em direção ao jogador.
- Uma linha de preparação fica aproximadamente em 60% da altura útil.
- A zona real de acerto fica perto dos alvos, aproximadamente em 82%.
- Essa separação reserva cerca de 1,3 s de antecipação no protótipo atual.
- Ordem canônica da esquerda para a direita: snare/caixa, tom 1, tom 2, surdo,
  bumbo, chimbal, prato de ataque/crash e prato de condução/ride.
- Batidas simultâneas são eventos independentes alinhados no mesmo instante.
- Nos modos jogáveis, Peter anima a peça realmente tocada pelo usuário, mesmo
  quando o toque estiver errado para a partitura; sem toque, não há golpe.
- No modo automático, a própria partitura conduz os golpes de Peter.
- Toques simultâneos combinam braços e pedal numa única pose composta.
- Chimbal fechado, aberto e pedal usam a mesma pista com símbolos diferentes.
- Densidade, acordes e andamento registrados na partitura determinam a
  dificuldade e a velocidade visual; não existe dificuldade genérica de menu.

## Condição de pronto da v0.1

Uma música drumless real deve tocar do começo ao fim com uma partitura revisada,
samples reais, animações correspondentes, pontuação coerente, resultado e
ranking persistente em um aparelho Android do Peter.
