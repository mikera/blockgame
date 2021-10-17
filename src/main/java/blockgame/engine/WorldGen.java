package blockgame.engine;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import convex.core.data.ACell;
import convex.core.data.prim.CVMLong;

public class WorldGen {
	
	private Engine engine;
	private WorldGen(Engine e) {
		this.engine=e;
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
		
		for (int k=-16; k<maxHeight; k+=32) {
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
				generateRocks(bx, by);
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
	
	private void generateRocks(int bx, int by) {
		for (int ox=0; ox<16; ox++) {
			for (int oy=0; oy<16; oy++) {
				int h=heights[oy*16+ox];
				int x=bx+ox;
				int y=by+oy;
				
				int c=Rand.rint(50,x,y,595); // average ~5 rocks
				if (c==0) {
					engine.setBlockLocal(x, y, h, Lib.BOULDER);
				}
			}
		}
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
		if (Rand.rint(3,bx,by,54475)==0) generateRocks(bx,by);
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
	int ht;
	
	public int generateTile(int x, int y) {
		top=Lib.GRASS;
		
		double height=calcHeight(x,y); 
		int ht=(int)height;
		
		fillRock(x, y, ht-1);
		if (ht>0) {
			if (top!=null) engine.setBlockLocal(x, y, ht-1, top);	
		} else if (ht<0) {
			engine.fillBlocks(x, y, ht, x, y, -1, CVMLong.create(11)); // water
		} else {
			engine.setBlockLocal(x, y, ht-1, (height<0)?CVMLong.create(11):CVMLong.create(15));	
		}
		return Math.max(0, ht);
	}

	private static ACell[] rockLayers=new ACell[] {Lib.STONE,Lib.STONE,Lib.STONE, Lib.STONE, Lib.STONE, Lib.STONE, Lib.CHALK, Lib.CHALK, Lib.GRANITE};
	private void fillRock(int x, int y, int h) {
		for (int z=-16; z<=h; z++) {
			int type=Rand.rint(rockLayers.length,z);
			
			engine.setBlockLocal(x,y,z,rockLayers[type]);
		}
	}
	
	public double noise(double x, double y, double scale,long seed) {
		x+=62.1*seed;
		y-=74.3*seed;
		return Simplex.noise(x/scale, y/scale);
	}
	
	public double snoise(double x, double y, double scale,long seed) {
		x-=62.1*seed;
		y-=174.3*seed;
		return Simplex.snoise(x/scale, y/scale);
	}
	
	public double plasma(double x, double y, double scale,long seed) {
		double FALLOFF=3;
		double a=0;
		double amp=1.0;
		for (int i=0; i<20; i++) {
			x+=50.1*seed;
			y+=107.3*seed;
			a+=amp*Simplex.snoise(x/scale, y/scale);
			amp/=FALLOFF;
			scale/=2;
			if (scale<2) break;
		}
		return a*(1.0-1/FALLOFF);
	}
	
	public double calcHeight(double x, double y) {
		double delta=0;
		double noise=plasma(x,y,40,2)*2;
		
		double zones=plasma(x,y,200,50)*10+1;
		
		double plateaus=Math.max(0,Math.min(1, plasma(x,y,60,107)*150));
		double plateauHeight=Math.max(0,plasma(x,y,80,20466)*7+1);
		if (Math.floor(plateaus)!=plateaus) {
			top=null;
		}

		if (zones>0) {
			delta+=plateaus*plateauHeight;
		}
		
		return delta+noise+zones;
	}
}
