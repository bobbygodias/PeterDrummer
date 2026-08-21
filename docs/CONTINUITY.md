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
- ordem canônica: caixa, tom 1, tom 2, surdo, bumbo, chimbal, crash e ride;
- tela de pista responsiva e multitouch;
- estado de performance do Peter preparado para golpes simultâneos;
- partitura temporal de calibração;
- pontuação e mensagens;
- menus, configurações, SAF e ranking;
- intro e 144 samples preparados no commit local privado `7bfed99`;
- catálogo público preparado para chimbal aberto e pedal;
- seleção por intensidade e round-robin;
- esqueleto temporal de **Even Flow** documentado e testado.
- duas pastas SAF independentes: músicas e partituras extras;
- catálogo unificado para partituras internas/externas e os três modos;
- dificuldade e aproximação visual derivadas da partitura.

## Ainda não foi validado

- build Android no GitHub Actions: testes e `assembleDebug` passaram;
- execução local nesta sessão: indisponível por ausência de SDK/Gradle;
- execução da v0.2.3 em tablet revelou e reproduziu a antiga ordem incorreta;
- persistência das duas pastas e a nova ordem da v0.3 ainda aguardam novo APK;
- geração do APK pessoal com os assets privados;
- reprodução e latência dos samples em aparelho;
- backing track externo sincronizado;
- animações finais do Peter;
- eventos completos da partitura de **Even Flow**;
- partituras das demais músicas.

## Próxima ação técnica

Executar testes e `assembleDebug` da v0.3 no GitHub. Depois, montar o APK
pessoal com os assets privados e validar no mesmo tablet: oito sons na ordem,
permanência das duas pastas, pareamento música/partitura e os três modos.
