package blockgame.engine.biome;

import blockgame.engine.Lib;
import blockgame.engine.Rand;
import blockgame.engine.WorldGen;
import convex.core.data.ACell;

public class FlameLands extends ABiome {

	@Override
	public ACell topTile(int x, int y) {
		return Lib.FIRESTONE;
	}
	
	@Override
	public double calcScore(int x, int y) {
		double score=WorldGen.plasma(x, y, BIOME_SCALE*1.5, gen.seed(755748));
		return score;
	}
	
	@Override
	public void addTileDecoration(int x, int y, int ht) {
		
	}
	
	@Override
	public void decorateArea(int bx, int by) {
		int type=Rand.rint(40,bx,by,676969);
		switch (type) {
			case 0: case 1: case 2: {
				generateScattered(bx, by,0.02,Lib.DEAD_BUSH,Lib.PRED_BLOCKING);
				break;
			}

			case 3: {
				generateScattered(bx, by,0.02,Lib.FIRESTONE_BLOCK,Lib.PRED_BLOCKING);
				break;
			}

			default: {
				
				break;
			}
		}
	}
}
