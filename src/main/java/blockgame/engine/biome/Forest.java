package blockgame.engine.biome;

import blockgame.engine.Lib;
import blockgame.engine.Rand;
import blockgame.engine.WorldGen;
import convex.core.data.ACell;

public class Forest extends ANaturalBiome {

	@Override
	public ACell topTile(int x, int y) {
		return Lib.DIRT;
	}

	@Override
	public double calcScore(int x, int y) {
		double score=WorldGen.plasma(x, y, BIOME_SCALE, gen.seed(6576));
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
	
	@Override
	public void decorateArea(int bx, int by) {
		int type=Rand.rint(40,bx,by,676969);
		switch (type) {
			case 0: {
				generateMushrooms(bx, by);
				break;
			}
			
			case 1: {
				generateScattered(bx, by,0.02,Lib.DEAD_BUSH,Lib.PRED_BLOCKING);
				break;
			}

			case 2: {
				generateScattered(bx, by,0.02,Lib.GREEN_BUSH,Lib.PRED_GROWABLE);
				generateTrees(1,bx, by);
				break;
			}

			default: {
				int num=1+Rand.rint(4,bx,by,546546)+(Rand.rint(3,bx,by,464)*Rand.rint(4,bx,by,6546));
				generateTrees(num,bx, by);
				break;
			}
		}
	}


	




}
