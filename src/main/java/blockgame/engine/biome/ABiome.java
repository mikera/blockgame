package blockgame.engine.biome;

import blockgame.engine.Engine;
import blockgame.engine.WorldGen;
import convex.core.data.ACell;

public abstract class ABiome {

	protected WorldGen gen;
	protected Engine engine;
	
	public static double BIOME_SCALE=100.0;

	public void setWorldGen(WorldGen worldGen) {
		this.gen=worldGen;
		engine=worldGen.getEngine();
	}

	public abstract ACell topTile(int x, int y);

	public abstract double calcScore(int x, int y);

	public void addTileDecoration(int x, int y, int ht) {
		
	}

	protected Engine engine() {
		return engine;
	}
}
