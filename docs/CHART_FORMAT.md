# Formato de partitura Peter Drummer

Partituras extras usam UTF-8, extensão `.pdrum.json` e `schemaVersion` 1. As
partituras oficiais usam o mesmo conteúdo dentro de `app/src/main/assets/charts`.

```json
{
  "schemaVersion": 1,
  "id": "pearl_jam_even_flow",
  "title": "Even Flow",
  "artist": "Pearl Jam",
  "durationMs": 293000,
  "audioOffsetMs": 0,
  "audioFileNames": [
    "Pearl Jam - Even Flow (Drumless).mp3"
  ],
  "events": [
    { "timeMs": 1842, "lane": "kick", "velocity": 116 },
    { "timeMs": 1842, "lane": "crash", "velocity": 108 },
    {
      "timeMs": 2318,
      "lane": "hi_hat",
      "velocity": 92,
      "articulation": "open"
    }
  ]
}
```

## IDs estáveis das oito pistas

| Ordem visual | `lane` | Peça |
|---:|---|---|
| 1 | `snare` | Caixa |
| 2 | `tom1` | Tom 1 |
| 3 | `tom2` | Tom 2 |
| 4 | `floor_tom` | Surdo |
| 5 | `kick` | Bumbo |
| 6 | `hi_hat` | Chimbal |
| 7 | `crash` | Prato de ataque |
| 8 | `ride` | Prato de condução |

Articulações aceitas: `standard`, `closed`, `open` e `pedal`. As três últimas
só são válidas na pista `hi_hat`. Eventos simultâneos repetem o mesmo `timeMs`
em pistas diferentes. `velocity` vai de 1 a 127.

O arquivo é rejeitado se tiver ID vazio, duração inválida, evento fora da
música, pista desconhecida, eventos fora de ordem após a leitura ou duas notas
da mesma pista no mesmo instante. Partituras extras não podem substituir uma
partitura interna com o mesmo `id`.
