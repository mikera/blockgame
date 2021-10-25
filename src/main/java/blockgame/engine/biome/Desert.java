package blockgame.engine.biome;

import blockgame.engine.Lib;
import blockgame.engine.Rand;
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
	
	@Override
	public void decorateArea(int bx, int by) {
		int type=Rand.rint(30,bx,by,676969);
		switch (type) {
			case 0: case 1:{
				generateScattered(bx, by,0.02,Lib.DEAD_BUSH,Lib.PRED_BLOCKING);
				break;
			}
						
			case 2: { 
				generateScattered(bx, by,0.02,Lib.BOULDER,Lib.PRED_BLOCKING);
				break;
			}
		}
	}
}
