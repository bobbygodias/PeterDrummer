# Continuidade

- Projeto: Peter Drummer.
- Repositório: `bobbygodias/PeterDrummer`.
- Reinício: 20/08/2026.
- Stack ativa: Kotlin nativo + Android Canvas.
- Package: `com.bobbydias.peterdrummer`.
- Distribuição pretendida: APK pessoal/offline para Peter; repositório público
  sob CC0 conforme estado anterior.
- Branch pública ativa: `feature/even-flow-engine-v0.2-public`.

## Existe agora

- estrutura Gradle;
- oito pistas fixas;
- ordem canônica: chimbal, prato de ataque, prato de condução, snare, tom 1,
  tom 2, surdo e bumbo;
- tela de pista responsiva e multitouch;
- estado de performance do Peter preparado para golpes simultâneos;
- partitura temporal de calibração;
- pontuação e mensagens;
- menus, configurações, SAF e ranking;
- intro e 144 samples preparados no commit local privado `7bfed99`;
- catálogo público preparado para chimbal aberto e pedal;
- seleção por intensidade e round-robin;
- esqueleto temporal de **Even Flow** documentado e testado.

## Ainda não foi validado

- build Android no GitHub Actions: testes e `assembleDebug` passaram;
- execução local nesta sessão: indisponível por ausência de SDK/Gradle;
- execução em emulador ou aparelho;
- geração do APK pessoal com os assets privados;
- reprodução e latência dos samples em aparelho;
- backing track externo sincronizado;
- animações finais do Peter;
- eventos completos da partitura de **Even Flow**;
- partituras das demais músicas.

## Próxima ação técnica

Executar testes e `assembleDebug` do motor público no GitHub. Para montar o
APK pessoal audível, injetar os assets privados fora do repositório e então
medir carregamento, multitouch, resposta sonora e latência.
