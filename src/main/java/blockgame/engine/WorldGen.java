package blockgame.engine;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

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
		int GENRANGE=5;
		
		for (int i=-GENRANGE; i<=GENRANGE; i++) {
			for (int j=-GENRANGE; j<=GENRANGE; j++) {
				generateBlock(i,j);
			}
		}
	}

	
	private void generateBlock(int i, int j) {
		int bx=i*16;
		int by=j*16;
		System.out.println("Generating Chunk: "+bx+","+by);
		
		int maxHeight=0;
		for (int x=0; x<16; x++) {
			for (int y=0; y<16; y++) {
				int h=generateTile(bx+x,by+y);
				maxHeight=Math.max(maxHeight, h);
			}

		}
		
		for (int k=-16; k<maxHeight; k+=32) {
			engine.uploadChunk(bx, by, k);
		}
	}

	public int generateTile(int x, int y) {
		int height=(int)((Simplex.noise(x*0.02f, y*0.02f)-0.5)*10);
		engine.fillBlocks(x,y,-16,x,y,height-1,CVMLong.create(2));
		if (height>0) {
			engine.setBlockLocal(x, y, height-1, CVMLong.create(1));	
		} else if (height<0) {
			engine.fillBlocks(x, y, height, x, y, -1, CVMLong.create(11));
		} else {
			engine.setBlockLocal(x, y, height-1, CVMLong.create(10));	
		}
		return Math.max(0, height);
	}
}
