package blockgame.engine.biome;

import blockgame.engine.Lib;
import blockgame.engine.Rand;
import blockgame.engine.WorldGen;
import convex.core.data.ACell;

public class GrassLands extends ABiome {

	@Override
	public ACell topTile(int x, int y) {
		return Lib.GRASS;
	}

	@Override
	public double calcScore(int x, int y) {
		double score=WorldGen.plasma(x, y, BIOME_SCALE, gen.seed(123));
		return score;
	}
	
	@Override
	public void addTileDecoration(int x, int y, int ht) {
		
		double gzone=Math.max(0.0,WorldGen.snoise(x, y, 130, 6876987)-0.1);
		if (gzone>0) {
			int grassiness=(int)(600.0/(gzone*50));
			int gtop=Rand.rint(grassiness, x, y, 568565);
			switch (gtop) {
			case 0: engine().setBlockLocal(x, y, ht, Lib.MEDIUM_GRASS); break;
			case 1: engine().setBlockLocal(x, y, ht, Lib.SHORT_GRASS); break;
			default:
				// nothing
			}
		}
	}


}
