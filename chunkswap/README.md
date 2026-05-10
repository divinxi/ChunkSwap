# ChunkSwap Mod — by Divinxxii

A Fabric mod for Minecraft 1.21 that creates the **Random Chunk Swap Challenge**.

Every N seconds, the chunk you're standing in gets swapped with a completely random chunk from somewhere else in the world. You stay in place — but everything around you changes instantly.

---

## Requirements

- **Minecraft**: 1.21
- **Mod loader**: Fabric Loader 0.15.11+
- **Fabric API**: 0.100.4+1.21 (required)
- **Java**: 21

---

## Building from Source

### 1. Prerequisites

- Install [Java 21 JDK](https://adoptium.net/)
- Install [Gradle](https://gradle.org/) (or use the included wrapper)

### 2. Clone / download the project

Place the project folder wherever you like.

### 3. Run the build

```bash
cd chunkswap
./gradlew build
```

The compiled `.jar` will be in:
```
build/libs/chunkswap-1.0.0.jar
```

### 4. Install

Copy `chunkswap-1.0.0.jar` into your Minecraft `mods/` folder.
Make sure **Fabric API** is also in `mods/`.

---

## Commands

| Command | Description |
|---|---|
| `/start <seconds>` | Start the challenge. Swaps every N seconds. |
| `/pause` | Pause the challenge. |

Both commands require **operator permissions** (level 2).

### Examples

```
/start 30   → swap every 30 seconds
/start 10   → swap every 10 seconds (very chaotic!)
/pause      → pause without losing your timer setting
/start 30   → resume (resets timer to 30s)
```

---

## HUD

A countdown timer appears **above your hotbar** while the challenge is running:

- `Chunk Swap: 28s` — normal countdown (green)
- `⚡ 3s` — warning flash (red, final 3 seconds)
- `⏸ Paused` — while paused

---

## How the Swap Works

1. Timer hits 0
2. The 16×16 chunk you're standing in is copied block-by-block
3. A random chunk (minimum 512 blocks away, max ~100,000 blocks) is loaded
4. Both chunks exchange their full block data, fluids, and tile entities (chests, spawners, etc.)
5. You stay at your exact coordinates — the world around you changes
6. Warning sounds play at 3, 2, and 1 second before each swap

---

## File Structure

```
src/
├── main/java/com/divinxxii/chunkswap/
│   ├── ChunkSwapMod.java          ← mod entrypoint
│   ├── command/
│   │   └── ChunkSwapCommand.java  ← /start and /pause commands
│   ├── timer/
│   │   └── SwapScheduler.java     ← countdown timer + swap trigger
│   └── util/
│       └── ChunkSwapper.java      ← block copy/swap logic
└── client/java/com/divinxxii/chunkswap/client/
    └── ChunkSwapClient.java       ← HUD overlay
```

---

## Known Limitations / Notes

- The swap copies blocks one at a time (no native chunk copy API in Fabric). For very fast intervals (<10s), this may cause a brief lag spike.
- Cross-dimension swaps are intentionally not supported.
- Entities in the swapped chunk are **not** transferred (vanilla Minecraft doesn't store entities in chunk data easily without mixins).
- If a random chunk hasn't been generated yet, it will be force-generated on swap — this is intentional and part of the randomness.

---

## Content Tips for YouTube / TikTok

- Use `/start 30` for a good paced video (tension builds naturally)
- Use `/start 10` for chaotic TikTok-length clips
- Build a base between swaps for max drama when it disappears
- Record in Spectator briefly after each swap to show the full chunk view

---

## License

MIT — free to use, modify, and share. Credit appreciated!
