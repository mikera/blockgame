package blockgame.engine.biome;

import blockgame.engine.Lib;
import blockgame.engine.Rand;
import blockgame.engine.WorldGen;
import convex.core.data.ACell;

public class Rocks extends ABiome {

	@Override
	public ACell topTile(int x, int y) {
		return Lib.GRANITE;
	}
	
	@Override
	public double calcScore(int x, int y) {
		double score=WorldGen.plasma(x, y, BIOME_SCALE*1.5, gen.seed(434));
		return score;
	}
	
	@Override
	public void addTileDecoration(int x, int y, int ht) {
		
		double gzone=Math.max(0.0,WorldGen.snoise(x, y, 130, 587587));
		if (gzone>0) {
			int rockiness=(int)(300.0/(gzone*50));
			int gtop=Rand.rint(rockiness, x, y, 4564);
			switch (gtop) {
			case 0: engine().setBlockLocal(x, y, ht, Lib.GRANITE); break;
			case 1: engine().setBlockLocal(x, y, ht, Lib.BOULDER); break;
			default:
				// nothing
			}
		}
	}
}
