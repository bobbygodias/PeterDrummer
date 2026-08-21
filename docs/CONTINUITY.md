# Continuidade

- Projeto: Peter Drummer.
- Repositório: `bobbygodias/PeterDrummer`.
- Reinício: 20/08/2026.
- Stack ativa: Kotlin nativo + Android Canvas.
- Package: `com.bobbydias.peterdrummer`.
- Distribuição pretendida: APK pessoal/offline para Peter; repositório público
  sob CC0 conforme estado anterior.
- Branch antiga preservada: `archive/unity-prototype-2026-08-20`.
- Branch nova: `rebuild/native-android-v0.1`.

## Existe agora

- estrutura Gradle;
- oito pistas fixas;
- tela de pista responsiva e multitouch;
- partitura temporal de calibração;
- pontuação e mensagens;
- menus, configurações, SAF e ranking;
- ganchos para intro e oito WAVs.

## Ainda não foi validado

- build Android: o ambiente desta sessão não possui SDK/Gradle instalados;
- execução em emulador ou aparelho;
- intro real empacotada;
- samples reais;
- backing track externo sincronizado;
- animações finais do Peter;
- catálogo corrigido e partituras das músicas.

## Próxima ação técnica

Gerar o wrapper binário em um ambiente com Gradle 8.13, adicionar a intro e os
oito WAVs, executar `./gradlew test assembleDebug` e instalar o APK no aparelho
alvo para medir toque e latência.
