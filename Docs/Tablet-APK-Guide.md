# Guia rápido: gerar APK pelo tablet (GitHub)

Este guia permite gerar o APK do **Peter Drummer** sem precisar abrir o Unity localmente.

## Pré-requisitos (1x)

No repositório GitHub, configure em **Settings → Secrets and variables → Actions**:

- `UNITY_EMAIL`
- `UNITY_PASSWORD`
- `UNITY_LICENSE`

> Esses segredos são exigidos pelo `game-ci/unity-builder` para build no GitHub Actions.

## Rodar build no tablet

1. Abra o repositório no GitHub.
2. Vá para a aba **Actions**.
3. Selecione o workflow **Android APK Build & Release**.
4. Toque em **Run workflow**.
5. Defina:
   - `publish_release = true` (publica em Releases automaticamente)
   - `release_tag = v0.1.0` (ou a versão desejada)
6. Toque em **Run workflow** novamente.

## Onde pegar o APK

Após sucesso do workflow:

- Em **Actions → run concluído**, baixe o artefato **PeterDrummer-APK**
- Se `publish_release=true`, também estará em **Releases** como `PeterDrummer.apk`

## Problemas comuns

- **Falha de licença Unity**: segredo `UNITY_LICENSE` inválido/ausente.
- **Nenhum APK encontrado**: estrutura do projeto Unity incompleta ou build falhou antes da etapa final.
- **Link do README não baixa**: confirme se existe Release com arquivo `PeterDrummer.apk`.
