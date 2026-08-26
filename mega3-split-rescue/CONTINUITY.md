# Continuidade — MEGA 3 Split Rescue

- Estado: v0.1 funcional em código; aguarda build e teste no MEGA 3.
- Pacote: `com.andrewvox.mega3splitrescue`
- Android alvo: 15 / API 35; mínimo API 26.
- Ponte privilegiada: Shizuku API 13.1.5, iniciada por depuração sem fio.
- Dados salvos: nenhum.
- Rede própria: nenhuma; o botão de instalação abre a página oficial do Shizuku
  em um navegador externo somente se ele não estiver instalado.
- Acessibilidade: sem leitura de conteúdo; só `GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN`.
- Primeiro teste: parear Shizuku pela notificação, conceder permissão ao app,
  executar Diagnóstico e depois Reparar.
