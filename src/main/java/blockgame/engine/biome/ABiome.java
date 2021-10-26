package blockgame.engine.biome;

import java.util.function.Predicate;

import blockgame.engine.Engine;
import blockgame.engine.Rand;
import blockgame.engine.WorldGen;
import convex.core.data.ACell;

public abstract class ABiome {

	protected WorldGen gen;
	protected Engine engine;
	
	public static double BIOME_SCALE=150.0;

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

	public void decorateArea(int bx, int by) {
		
	}
	
	/**
	 * Generate scattered blocks in chunk at surface level
	 * @param bx
	 * @param by
	 * @param freq
	 * @param type
	 */
	protected void generateScattered(int bx, int by, double freq, ACell type) {
		generateScattered(bx,by,freq,type,null);
	}
	
	protected void generateScattered(int bx, int by, double freq, ACell type, Predicate<ACell> topTest) {
		int chances=(int)(1.0/freq);
		for (int ox=0; ox<16; ox++) {
			for (int oy=0; oy<16; oy++) {
				int h=gen.heights[oy*16+ox];
				int x=bx+ox;
				int y=by+oy;
				
				int c=Rand.rint(chances,x,y,595+chances);
				if (c==0) {
					if ((topTest==null)||(topTest.test(engine.getBlock(x, y, h-1)))) {
						engine.setBlockLocal(x, y, h, type);
					}
				}
			}
		}
	}
}
