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
	
	@Override
	public void decorateArea(int bx, int by) {
		int type=Rand.rint(60,bx,by,676969);
		switch (type) {
			case 1: case 2: case 3: {
				int num=1+Rand.rint(4,bx,by,546546)+(Rand.rint(3,bx,by,464)*Rand.rint(4,bx,by,6546));
				generateTrees(num,bx, by);
				break;
			}
				
			case 4: case 5: case 6:{
				generateTrees(1,bx, by);
				break;
			}
			
			case 7: case 8: {
				generateScattered(bx, by,0.02,Lib.BOULDER,Lib.PRED_BLOCKING);
				break;
			}
			
			case 9: {
				generateRuin(bx, by);
				break;
			}
			
			case 10: {
				generateBushes(bx, by);
				break;
			}
			
			case 11: {
				generateMushrooms(bx, by);
				break;
			}
			
			case 12: {
				generateScattered(bx, by,0.02,Lib.DEAD_BUSH,Lib.PRED_BLOCKING);
				break;
			}

			case 13: {
				generateScattered(bx, by,0.02,Lib.GREEN_BUSH,Lib.PRED_GROWABLE);
				generateTrees(1,bx, by);
				break;
			}


		}
	}

	private void generateTrees(int num, int bx, int by) {
		for (int i=0; i<num; i++) {
			int ox=1+Rand.rint(14,bx,by,678+i*56);
			int oy=1+Rand.rint(14,bx,by,5641564+i*456);
			int h=gen.heights[oy*16+ox];
			if (h>1) {
				generateTree(bx+ox,by+oy,h);
			}
		}
	}
	

	
	ACell[] MUSHROOMS=new ACell[] {Lib.PURPLE_MUSHROOM,Lib.RED_MUSHROOM,Lib.GREY_MUSHROOM,Lib.GREY_MUSHROOM, Lib.GREY_MUSHROOM};
	private void generateMushrooms(int bx, int by) {
		ACell type=MUSHROOMS[Rand.rint(MUSHROOMS.length, bx,by,7897)];
		generateScattered(bx,by,0.02,type,Lib.PRED_GROWABLE);
	}
	
	private void generateBushes(int bx, int by) {
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
	
	private static ACell[] ruinBlocks= new ACell[]{Lib.STONE_BLOCK,Lib.STONE, Lib.STONE_SLABS};
	private void generateRuin(int bx, int by) {
		if (gen.heights[8*16+8]<=0) return; // skip if no land
 		int size=2+Rand.rint(3,bx,by,456663);
		int xsize=size+Rand.rint(3,bx,by,67868); 
		int ysize=size+Rand.rint(3,bx,by,453);
		if (Rand.rint(3,bx,by,54475)==0) generateScattered(bx,by,0.02,Lib.BOULDER);
		for (int ox=0; ox<16; ox++) {
			for (int oy=0; oy<16; oy++) {
				if (!((Math.abs(ox-8)==xsize)||(Math.abs(oy-8)==ysize))) continue;
				if (!((Math.abs(ox-8)<=xsize)&&(Math.abs(oy-8)<=ysize))) continue;
				int h=gen.heights[oy*16+ox];
				int x=bx+ox;
				int y=by+oy;
				
				int ht=Math.max(0, (int)(WorldGen.plasma(x,y,8,5695)*6.0+3));
				
				for (int z=h; z<h+ht; z++) {
					ACell blk=ruinBlocks[Rand.rint(ruinBlocks.length,x,y,z)];
					engine.setBlockLocal(x, y, z, blk);
				}
			}
		}
	}

	private void generateTree(int x, int y, int h) {
		int ht=3+Rand.rint(5,x,y,h);
		engine.fillBlocks(x-1,y-1,h+ht-1,x+1,y+1,h+ht+1,Lib.LEAVES);
		engine.fillBlocks(x,y,h,x,y,h+ht,Lib.LOG);
		engine.setBlockLocal(x,y,h+ht+2,Lib.LEAVES);
		// System.out.println("Built tree with height: "+ht);
	}


}
