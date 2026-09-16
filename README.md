# Piston Extension

A Fabric mod that lets a regular or sticky piston extend up to 3 blocks instead of vanilla's fixed 1, with the extension length adjustable per-piston by right-clicking it.

## Features

- **Right-click a powered, extended piston with an empty hand** to cycle its extension length between 1, 2, and 3 blocks (an overlay message confirms the new length)
- Works on both regular and sticky pistons: sticky pistons pull the block back when retracting, and re-adjusting length while extended repositions any stuck block accordingly
- Adds an intermediate **Piston Shaft** block that fills the gap between the piston base and its head when extended more than 1 block; it behaves as an inert, unmovable connector and is removed automatically when the piston retracts
- Extension length is persisted per-piston (survives world reloads) via a `SavedData`-backed store
- Respects vanilla pushable/block-count rules when extending: a push that would fail on a vanilla piston (unmovable blocks, too many blocks in the chain) still fails here

## Learning It

Right-clicking a piston cycles how far it pushes, and there is nothing to find: no recipe, no item, no particle. The block looks identical at every setting, so the only way anyone learns this is being told.

With [village-quests](https://github.com/fatlard1993/village-quests) installed, a mason or toolsmith wants a gate that actually opens and asks you to set a piston to a reach of three. One is what a piston already does and two could be an accident; three is unmistakably somebody who knows.

With [block-tip](https://github.com/fatlard1993/block-tip) installed, looking at a piston set to 2 or 3 shows "Pushes 2" or "Pushes 3". A piston at 1 shows nothing, since that is what every piston does.

Both are optional and guarded: without them the mod behaves exactly as before.

## Pandorical

Piston Extension runs server-side, and Pandorical is required: the server will not load this mod without it. The Piston Shaft is registered as a Pandorical content block and its assets are synced from this jar.

Clients are the optional half. A player on a Pandorical client sees the shaft; because it is a real registered block, a vanilla client cannot render it and cannot receive chunks containing one. Extension length, sticky behaviour and the push rules are all server-side and unaffected.

## Development

Installing is in [DEVELOPMENT.md](DEVELOPMENT.md).

## License

MIT, see [LICENSE](LICENSE).
