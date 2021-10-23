package blockgame.engine;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import convex.core.data.ACell;

public class WorldGen {
	
	private static final double BASE_TERRAIN_SIZE = 20.0;
	private static final double BASE_TERRAIN_SCALE = 500.0;
	private static final double BASE_TERRAIN_OFFSET = 2.0;

	
	private static final double HILLOCKS_SIZE = 2.0;
	private static final double HILLOCKS_SCALE = 40.0;
	
	private static final double PLATE_SIZE = 10.0;
	private static final double PLATE_SCALE = 300.0;
	private static final double PLATE_THICKNESS = 1.25; // average plate thickness

	
	private static final double PLATEAU_SIZE = 7.0;
	private static final double PLATEAU_SCALE = 80.0;
	private static final double PLATEAU_HEIGHT_SCALE = 60.0;


	private final int BOTTOM = -32;
	
	private final Engine engine;
	private final long worldSeed;
	
	private WorldGen(Engine e) {
		this.engine=e;
		worldSeed=new SecureRandom().nextLong();
	}
	
	public long seed() {
		return worldSeed;
	}
	
	/**
	 * 1D seeds
	 * @param a Random seed variation
	 * @return Random seed long
	 */
	public long seed(long a) {
		return Rand.xorshift64(worldSeed*a);
	}
	
	/**
	 * 2D seeds	 
	 * @param a Random seed variation
	 * @return Random seed long
	 */
	public long seed(long a, long b) {
		return Rand.xorshift64(b*Rand.xorshift64(worldSeed*a));
	}

	public static WorldGen create(Engine engine) {
		return new WorldGen(engine);
	}

	public void generate() throws TimeoutException, IOException {
		//Convex convex=engine.getConvex();
		//Result r=convex.transactSync(Invoke.create(convex.getAddress(), 0, Reader.read("(call "+Config.world+" (build-house))")));
		//if (r.isError()) throw new Error(r.toString());
		int GENRANGE=8;
		
		for (int i=-GENRANGE; i<=GENRANGE; i++) {
			for (int j=-GENRANGE; j<=GENRANGE; j++) {
				generateArea(i,j);
			}
		}
	}
	
	public int[] heights=new int[256];
	
	private void generateArea(int i, int j) {
		int bx=i*16;
		int by=j*16;
		System.out.println("Generating Chunk Area: "+bx+","+by);
		
		int maxHeight=0;
		for (int x=0; x<16; x++) {
			for (int y=0; y<16; y++) {
				int h=generateTile(bx+x,by+y);
				heights[y*16+x]=h;
				maxHeight=Math.max(maxHeight, h);
			}
		}
		
		decorateArea(bx,by);
		
		for (int k=BOTTOM; k<=maxHeight; k+=16) {
			engine.uploadChunk(bx, by, k);
		}
	}

	private void decorateArea(int bx, int by) {
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
				generateScattered(bx, by,0.02,Lib.BOULDER);
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
				generateScattered(bx, by,0.02,Lib.DEAD_BUSH,Lib.PRED_GROWABLE);
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
			int h=heights[oy*16+ox];
			if (h>1) {
				generateTree(bx+ox,by+oy,h);
			}
		}
	}
	
	/**
	 * Generate scattered blocks in chunk at surface level
	 * @param bx
	 * @param by
	 * @param freq
	 * @param type
	 */
	private void generateScattered(int bx, int by, double freq, ACell type) {
		generateScattered(bx,by,freq,type,null);
	}
	
	private void generateScattered(int bx, int by, double freq, ACell type, Predicate<ACell> topTest) {
		int chances=(int)(1.0/freq);
		for (int ox=0; ox<16; ox++) {
			for (int oy=0; oy<16; oy++) {
				int h=heights[oy*16+ox];
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
	
	ACell[] MUSHROOMS=new ACell[] {Lib.PURPLE_MUSHROOM,Lib.RED_MUSHROOM,Lib.GREY_MUSHROOM,Lib.GREY_MUSHROOM, Lib.GREY_MUSHROOM};
	private void generateMushrooms(int bx, int by) {
		ACell type=MUSHROOMS[Rand.rint(MUSHROOMS.length, bx,by,7897)];
		generateScattered(bx,by,0.02,type,Lib.PRED_GROWABLE);
	}
	
	private void generateBushes(int bx, int by) {
		int sparseness=10+Rand.rint(100,bx,by,54676);
		for (int ox=0; ox<16; ox++) {
			for (int oy=0; oy<16; oy++) {
				int h=heights[oy*16+ox];
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
		if (heights[8*16+8]<=0) return; // skip if no land
 		int size=2+Rand.rint(3,bx,by,456663);
		int xsize=size+Rand.rint(3,bx,by,67868); 
		int ysize=size+Rand.rint(3,bx,by,453);
		if (Rand.rint(3,bx,by,54475)==0) generateScattered(bx,by,0.02,Lib.BOULDER);
		for (int ox=0; ox<16; ox++) {
			for (int oy=0; oy<16; oy++) {
				if (!((Math.abs(ox-8)==xsize)||(Math.abs(oy-8)==ysize))) continue;
				if (!((Math.abs(ox-8)<=xsize)&&(Math.abs(oy-8)<=ysize))) continue;
				int h=heights[oy*16+ox];
				int x=bx+ox;
				int y=by+oy;
				
				int ht=Math.max(0, (int)(plasma(x,y,8,5695)*6.0+3));
				
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


	ACell top=null;
	
	/**
	 * Top surface (fill up to this level minus one)
	 */
	int ht;
	
	public int generateTile(int x, int y) {
		top=Lib.GRASS;
		
		double height=calcHeight(x,y); 
		ht=(int)height;
		
		fillRock(x, y, ht-1);
		if (ht>0) {
			if (top!=null) {
				engine.setBlockLocal(x, y, ht-1, top);	
				if (top==Lib.GRASS) {
					double gzone=Math.max(0.0,snoise(x, y, 130, 6876987)-0.1);
					if (gzone>0) {
						int grassiness=(int)(1000.0/(gzone*50));
						int gtop=Rand.rint(grassiness, x, y, 568565);
						switch (gtop) {
						case 0: engine.setBlockLocal(x, y, ht, Lib.MEDIUM_GRASS); break;
						case 1: engine.setBlockLocal(x, y, ht, Lib.SHORT_GRASS); break;
						default:
							// nothing
						}
					}
				}
				
			}
		} else if (ht<0) {
			engine.fillBlocks(x, y, ht, x, y, -1, Lib.WATER); // water
		} else {
			ACell top=(height<0)?Lib.WATER:Lib.SAND;
			engine.setBlockLocal(x, y, ht-1, top);	
		}
		return Math.max(0, ht);
	}

	private static ACell[] rockLayers=new ACell[] {Lib.STONE,Lib.STONE,Lib.STONE, Lib.STONE, Lib.STONE, Lib.STONE, Lib.CHALK, Lib.CHALK, Lib.GRANITE};
	private void fillRock(int x, int y, int h) {
		double plate=noise(x,y,PLATE_SCALE,seed(16))*PLATE_SIZE;
		for (int z=BOTTOM; z<=h; z++) {
			
			double pz=plate+(z/PLATE_THICKNESS)+noise(z,0,1.0,seed(26))*0.2; // layer height
			int type=Rand.rint(rockLayers.length,(int)pz);
			
			boolean cave=isCave(x,y,z);
			if (cave) {
				// leave blank
			} else {
				engine.setBlockLocal(x,y,z,rockLayers[type]);
			}
		}
	}
	
	private boolean isCave(int x, int y, int z) {
		double n1=snoise(x,y,z,30,seed(19));
		double n2=snoise(x,y,z,30,seed(22));
		return ((n1*n1)+(n2*n2))<0.03;
		
	}
	
	public static double noise(double x, double y, double scale,long seed) {
		return noise(x,y,0.0,scale,seed);
	}

	
	public static double noise(double x, double y, double z, double scale,long seed) {
		seed &=0xffffffl; // avoid overflows
		x+=62.1*seed;
		y-=74.3*seed;
		z-=12.4*seed;
		return Simplex.noise(x/scale, y/scale, z/scale);
	}
	
	public static  double snoise(double x, double y, double scale,long seed) {
		return snoise(x,y,0.0,scale,seed);
	}
	
	public static  double snoise(double x, double y, double z, double scale,long seed) {
		seed &=0xffffffl; // avoid overflows
		x-=52.1*seed;
		y-=174.3*seed;
		z-=12.4*seed;
		return Simplex.snoise(x/scale, y/scale, z/scale);
	}
	
	public static double plasma(double x, double y, double scale,long seed) {
		return plasma(x,y,0,scale,seed);
	}

	public static double plasma(double x, double y, double z,double scale,long seed) {
		seed &=0xffffffl; // avoid overflows
		double FALLOFF=3;
		double a=0;
		double amp=1.0;
		for (int i=0; i<20; i++) {
			x+=50.1*seed;
			y+=107.3*seed;
			z+=-107.3*seed;
			a+=amp*Simplex.snoise(x/scale, y/scale,z/scale);
			amp/=FALLOFF;
			scale/=2;
			if (scale<2) break;
		}
		return a*(1.0-1/FALLOFF);
	}
	
	/**
	 * Base height of land mass. Negative is water.
	 */
	double baseHeight;
	
	/**
	 * Surface height. 0 is water.
	 */
	double surfaceHeight;
	
	/**
	 * Calculate surface height variables for the current tile
	 * @param x x location of tile
	 * @param y y location of tile
	 * @return surface height
	 */
	public double calcHeight(double x, double y) {
		baseHeight=plasma(x,y,BASE_TERRAIN_SCALE,seed(5))*BASE_TERRAIN_SIZE+BASE_TERRAIN_OFFSET;

		double hillocks=plasma(x,y,HILLOCKS_SCALE,seed(2))*HILLOCKS_SIZE;
		
		double plateauDelta=0;
		double plateaus=Math.max(0,Math.min(1, plasma(x,y,PLATEAU_SCALE,seed(107))*150));
		double plateauHeight=Math.max(0,plasma(x,y,PLATEAU_HEIGHT_SCALE,seed(200))*PLATEAU_SIZE+1);
		if (Math.floor(plateaus)!=plateaus) {
			top=null;
		}

		if (baseHeight>0) {
			plateauDelta+=plateaus*plateauHeight;
		}
		
		surfaceHeight=plateauDelta+hillocks+baseHeight;
		return surfaceHeight;
	}
}
