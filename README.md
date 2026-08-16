# Piston Extension

A Fabric mod that lets a regular or sticky piston extend up to 3 blocks instead of vanilla's fixed 1, with the extension length adjustable per-piston by right-clicking it.

## Features

- **Right-click a powered, extended piston** to cycle its extension length between 1, 2, and 3 blocks (an overlay message confirms the new length)
- Works on both regular and sticky pistons: sticky pistons pull the block back when retracting, and re-adjusting length while extended repositions any stuck block accordingly
- Adds an intermediate **Piston Shaft** block that fills the gap between the piston base and its head when extended more than 1 block; it behaves as an inert, unmovable connector and is removed automatically when the piston retracts
- Extension length is persisted per-piston (survives world reloads) via a `SavedData`-backed store
- Respects vanilla pushable/block-count rules when extending: a push that would fail on a vanilla piston (unmovable blocks, too many blocks in the chain) still fails here

## Requirements

- Targets the Minecraft, Fabric Loader, and Fabric API versions declared in this mod's `gradle.properties`. Check there for the exact currently-supported version
- Java version as declared in `fabric.mod.json`'s `depends` block
- Pandorical (see below)

## Pandorical

Piston Extension runs server-side, and Pandorical is a hard dependency (`fabric.mod.json`): the server will not load this mod without it. The Piston Shaft is registered as a Pandorical content block and its assets are synced from this jar.

Clients are the optional half. A player on a Pandorical client sees the shaft; because it is a real registered block, a vanilla client cannot render it and cannot receive chunks containing one. Extension length, sticky behaviour and the push rules are all server-side and unaffected.

## Installation

Install server-side alongside its declared dependencies (see `fabric.mod.json`). Connecting clients need only Pandorical.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
