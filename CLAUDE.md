# Claude Code Guidelines for Blockgame

## Project Overview

Blockgame is a 3D voxel game whose world state and game logic live on Convex.
The Java client (LWJGL/OpenGL) renders and interacts with a local Convex peer
(standalone mode) or a remote peer (networked mode). The on-chain game engine
is written in Convex Lisp.

## Build System

Single-module Maven project, Java 21+.

```bash
mvn compile              # compile
mvn test                 # run tests
mvn package              # build blockgame.jar with dependencies
```

Run via the main class `blockgame.BlockGame`.

## Convex Dependency

Depends on `world.convex:convex-core` and `world.convex:convex-peer`
(currently `0.8.4` released). If building against a local Convex checkout,
install it first:

```bash
cd ../convex && mvn clean install -DskipTests
```

## Module Layout

| Package | Purpose |
|---------|---------|
| `blockgame` | Entry point (`BlockGame`), `Config` (peer/store lifecycle), `Deploy` (deploys Convex Lisp code) |
| `blockgame.engine` | Game engine — world generation, biomes, block/face logic |
| `blockgame.render` | OpenGL rendering — chunks, billboards, shaders |
| `blockgame.model` | OBJ model loading |
| `blockgame.assets` | Asset / texture loading |
| `src/main/resources/convex/` | On-chain Convex Lisp code (`world.cvx`, `inventory.cvx`) |

## Runtime Modes

- **Local** (`Config.local = true`): launches an in-process Convex peer
  backed by an Etch store file (`blockgame-db.etch` in the working dir).
  Deploys Convex Lisp actors on startup.
- **Remote**: connects to `convex.world:18888` with hard-coded addresses.

## Shutdown

The Convex peer uses non-daemon Netty threads and holds an exclusive lock
on the Etch file. On exit, `Config.close()` must run to release the lock
and let the JVM terminate. `BlockGame.run()` wraps the game loop in
try/finally, registers a JVM shutdown hook, and `main` calls `System.exit(0)`
as a safety net. If shutdown regresses, the symptom is
`java.io.IOException: File lock failed` on the next launch.

## Natives / LWJGL

`pom.xml` pulls in LWJGL natives for Windows, macOS and Linux. The
`lwjgl.natives` property is set to `natives-windows` by default — override
if building on another platform (or rely on the classifier-only deps for
dev-time runs, which include all three).

## Language and Style

British English, concise. See root `D:/git/CLAUDE.md` for workspace-wide
Convex terminology (CVM coin, juice, copper, peer, actor, lattice, CPoS,
etch, CAD, CNS, Protonet).

## License

Mixed: Convex-derived components under the Convex Public License,
everything else Apache-2.0.
