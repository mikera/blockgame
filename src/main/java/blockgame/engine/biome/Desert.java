package blockgame.engine.biome;

import blockgame.engine.Lib;
import blockgame.engine.WorldGen;
import convex.core.data.ACell;

public class Desert extends ABiome {

	@Override
	public ACell topTile(int x, int y) {
		return Lib.SAND;
	}
	
	@Override
	public double calcScore(int x, int y) {
		double score=WorldGen.plasma(x, y, BIOME_SCALE, gen.seed(168757));
		return score;
	}
}
