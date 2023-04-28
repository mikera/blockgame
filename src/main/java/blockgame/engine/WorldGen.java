package blockgame.engine;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.TimeoutException;

import blockgame.engine.biome.ABiome;
import blockgame.engine.biome.Desert;
import blockgame.engine.biome.FlameLands;
import blockgame.engine.biome.Forest;
import blockgame.engine.biome.GrassLands;
import blockgame.engine.biome.BlackLands;
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

	private static final double CAVE_WIRE_SCALE = 80.0;
	private static final double CAVE_WIRE_FLATNESS= 1.5;
	private static final double CAVE_WIRE_AMOUNT= 0.1;
	
	private static final double CAVE_BLOB_SCALE = 50.0;
	private static final double CAVE_BLOB_FLATNESS= 2.0;
	
	private static final double CAVE_BLOB_THRESHOLD= 0.4;
	private static final double CAVE_BLOB_VARIATION_SCALE = 300.0;
	private static final double CAVE_BLOB_THRESHOLD_VARIATION = 0.35;


	private final int BOTTOM = -32;
	
	private final Engine engine;
	private final long worldSeed;
	
	private WorldGen(Engine e) {
		this.engine=e;
		worldSeed=new SecureRandom().nextLong();
		for (ABiome b: biomes) {
			b.setWorldGen(this);
		}
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
				generateArea(i*16,j*16);
			}
		}
	}
	
	public int[] heights=new int[256];
	
	public synchronized void generateArea(int bx, int by) {
		// System.out.println("Generating Chunk Area: "+bx+","+by);
		
		int maxHeight=0;
		for (int x=0; x<16; x++) {
			for (int y=0; y<16; y++) {
				int h=generateTile(bx+x,by+y);
				heights[y*16+x]=h;
				maxHeight=Math.max(maxHeight, h);
			}
		}
		
		// Decorate area with central biome
		biome=selectBiome(bx+8,by+8);
		biome.decorateArea(bx,by);
		
		for (int k=BOTTOM; k<=maxHeight; k+=16) {
			engine.uploadChunk(bx, by, k);
		}
	}

	ACell top=null;
	
	/**
	 * Top surface (fill up to this level minus one)
	 */
	int ht;
	
	private ABiome[] biomes = new ABiome[]{new GrassLands(),new Desert(),new BlackLands(), new Forest(),new FlameLands()};
	private ABiome biome=biomes[0];
	
	public int generateTile(int x, int y) {
		biome=selectBiome(x,y);
		top=biome.topTile(x,y);
		
		double height=calcHeight(x,y); 
		ht=(int)height;
		
		fillRock(x, y, ht-1);
		if (ht>0) {
			if (top!=null) {
				engine.setBlockLocal(x, y, ht-1, top);	
				biome.addTileDecoration(x,y,ht);
		
				
			}
		} else if (ht<0) {
			engine.fillBlocks(x, y, ht, x, y, -1, Lib.WATER); // water
		} else {
			ACell top=(height<0)?Lib.WATER:Lib.SAND;
			engine.setBlockLocal(x, y, ht-1, top);	
		}
		return Math.max(0, ht);
	}

	/**
	 * Calculate the biome for any x,y position in the world
	 * @param x
	 * @param y
	 * @return
	 */
	private ABiome selectBiome(int x, int y) {
		double maxScore=Double.NEGATIVE_INFINITY;
		for (int i = 0; i<biomes.length; i++) {
			ABiome b=biomes[i];
			double score=b.calcScore(x,y);
			if (score>maxScore) {
				biome=b;
				maxScore=score;
			}
		}
		return biome;
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
		// Wire caves
		double n1=snoise(x,y,z*CAVE_WIRE_FLATNESS,CAVE_WIRE_SCALE,seed(19));
		double n2=snoise(x,y,z*CAVE_WIRE_FLATNESS,CAVE_WIRE_SCALE,seed(22));
		if (((n1*n1)+(n2*n2))<(CAVE_WIRE_AMOUNT*CAVE_WIRE_AMOUNT)) return true;
		
		// Blob caves
		double blobScaleNoise=snoise(x,y,z,CAVE_BLOB_VARIATION_SCALE,seed(1780));
		
		double n3=plasma(x,y,z*CAVE_BLOB_FLATNESS,CAVE_BLOB_SCALE,seed(78));
		
		double threshold=CAVE_BLOB_THRESHOLD+blobScaleNoise*CAVE_BLOB_THRESHOLD_VARIATION;
		if (n3>threshold) return true;
		
		return false;
		
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
		return plasma(x,y,0.0,scale,seed);
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
	 * Calculate surface height variables for the current tile. May be negative for sea
	 * @param x x location of tile
	 * @param y y location of tile
	 * @return surface height
	 */
	public double calcHeight(double x, double y) {
		baseHeight=plasma(x,y,BASE_TERRAIN_SCALE,seed(5))*BASE_TERRAIN_SIZE+BASE_TERRAIN_OFFSET;

		double hillocks=plasma(x,y,HILLOCKS_SCALE,seed(2))*HILLOCKS_SIZE;
		
		
		// Plateau if 1.0, no plateau if 0.0, transition in between
		double plateaus=Math.max(0,Math.min(1, plasma(x,y,PLATEAU_SCALE,seed(107))*150));
		
		// Clear top block if in a transition
		if (Math.floor(plateaus)!=plateaus) {
			top=null;
		}
		
		// Height if plateaus present
		double plateauHeight=Math.max(0,plasma(x,y,PLATEAU_HEIGHT_SCALE,seed(200))*PLATEAU_SIZE+1);

		// Plateau delta only applies if on land
		double plateauDelta=0;
		if (baseHeight>0) {
			plateauDelta+=plateaus*plateauHeight;
		}
		
		surfaceHeight=plateauDelta+hillocks+baseHeight;
		return surfaceHeight;
	}

	public Engine getEngine() {
		return engine;
	}
}
