# Resgate de Tela — Blackview MEGA 3

Aplicativo pessoal e rootless para diagnosticar e reparar os flags de
multi-janela do DokeOS P 4.2 / Android 15.

## Limites honestos

- O primeiro pareamento do Shizuku precisa ser feito manualmente pela
  notificação do próprio Shizuku.
- Sem root, o Shizuku precisa ser iniciado novamente após cada reinicialização.
- O aplicativo não lê arquivos, tela, diário, senhas ou conteúdo de outros apps.
- O serviço de acessibilidade não observa eventos nem recupera conteúdo; ele
  apenas solicita a ação global de alternar divisão de tela.

## Build

```bash
gradle :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Comandos aplicados pelo botão de reparo

```text
settings put global force_resizable_activities 1
settings put global enable_non_resizable_multi_window 1
wm set-multi-window-config --supportsNonResizable 1 --respectsActivityMinWidthHeight -1
```

Não há entrada livre de comandos: todos são fixos no código.
