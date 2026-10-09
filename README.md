# blockgame

A Block Game, inspired by Boulder Dash, Minecraft, Roguelikes and similiar. 

<img src="https://github.com/mikera/blockgame/blob/master/src/main/resources/images/screenshot.png?raw=true" width="500" height="300">

The first 3D game world run entirely on a public decentralised network. The game engine is implemented in Convex Lisp, a Clojure-like language for on-chain smart contracts.

<a href="https://www.patreon.com/bePatron?u=4731307">Become a Patron!</a>

## Design

The game features a huge persistent virtual 3D space where players can build bases and cities , explore new areas, build an army of minions, take on fearsome monsters and trade with other players.

All in-game items are managed and secure on [Convex](https://convex.world). These can be both fungible items or unique NFTs. A system of ownership enables players to control how other players interact with their items, land and minions.

 

## Features

This is an experimental game exploring the boundaries of blockchain, metaverse, NFTs and gaming.

Features and ideas under development:

- NFTs used for game objects and land ownership rights
- In-game trading of items and NFTs
- Ability to "reify" digital assets as in-game objects
- Real money marketplaces via the Torus decentralised currency exchange
- Off-chain AI controllers for monsters and rogue nations
- Time-based growth and harvesting of resources.

## Running the game

To run the game, just run the main class `blockgame.BlockGame` with your preferred development environment.

## Collaboration

We're open to open source collaborators and contributors! Join the discussion at the [Convex Discord](https://discord.com/invite/xfYGq4CT7v). 

## Playing

- Move with W, A, S, D and Space. The mouse looks around; left click breaks a block, right click places the selected one.
- Keys 1 to 9, or the scroll wheel, pick a hotbar slot. Slash toggles a horizontal slice view, with comma and period moving the slice.
- Blocks you break go into your on-chain inventory and blocks you place are spent from it.
- C crafts the selected hotbar item from raw materials: stone bricks from stone, planks from oak logs, and so on. Recipes live in world.cvx.
- Gold ore is seeded below sea level and gets richer the deeper you dig, so explore the caves.
- K claims the chunk you are standing in for 4 gold. Only you, and builders you allow, can then break or place blocks there. The HUD shows who owns the land you are on.
- Owners can release or transfer land and allow or disallow builders by calling the world actor directly (release-land, transfer-land, allow-builder, disallow-builder).

## License

Convex parts and library components are under the Convex Public License

Everything else Apache License, Version 2.0
