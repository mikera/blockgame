package blockgame.engine.biome;

import blockgame.engine.Lib;
import blockgame.engine.Rand;
import convex.core.data.ACell;

public abstract class ANaturalBiome extends ABiome {
	protected void generateTree(int x, int y, int h) {
		int ht=3+Rand.rint(5,x,y,h);
		engine.fillBlocks(x-1,y-1,h+ht-1,x+1,y+1,h+ht+1,Lib.LEAVES);
		engine.fillBlocks(x,y,h,x,y,h+ht,Lib.LOG);
		engine.setBlockLocal(x,y,h+ht+2,Lib.LEAVES);
		// System.out.println("Built tree with height: "+ht);
	}
	
	ACell[] MUSHROOMS=new ACell[] {Lib.PURPLE_MUSHROOM,Lib.RED_MUSHROOM,Lib.GREY_MUSHROOM,Lib.GREY_MUSHROOM, Lib.GREY_MUSHROOM};
	protected void generateMushrooms(int bx, int by) {
		ACell type=MUSHROOMS[Rand.rint(MUSHROOMS.length, bx,by,7897)];
		generateScattered(bx,by,0.02,type,Lib.PRED_BLOCKING);
	}

	protected void generateTrees(int num, int bx, int by) {
		for (int i=0; i<num; i++) {
			int ox=1+Rand.rint(14,bx,by,678+i*56);
			int oy=1+Rand.rint(14,bx,by,5641564+i*456);
			int h=gen.heights[oy*16+ox];
			if (h>1) {
				generateTree(bx+ox,by+oy,h);
			}
		}
	}
	
	
	protected void generateBushes(int bx, int by) {
		int sparseness=10+Rand.rint(100,bx,by,54676);
		for (int ox=0; ox<16; ox++) {
			for (int oy=0; oy<16; oy++) {
				int h=gen.heights[oy*16+ox];
				if (h<=0) continue;
				int x=bx+ox;
				int y=by+oy;
				
				
				int c=Rand.rint(sparseness,x,y,595); 
				if (c==0) {
					engine.setBlockLocal(x, y, h, Lib.LEAVES);
				}
			}
		}
	}
}
