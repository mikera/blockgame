package blockgame.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import org.joml.Vector3f;
import org.joml.Vector3i;

import convex.api.Convex;
import convex.core.Result;
import convex.core.data.ACell;
import convex.core.data.AVector;
import convex.core.data.Address;
import convex.core.data.Vectors;
import convex.core.data.prim.CVMLong;
import convex.core.lang.Reader;
import convex.core.util.Utils;

/**
 * Main off-chain game engine
 */
public class Engine {
	
	

	public static class HitResult {
		public int x;
		public int y;
		public int z;
		public int face;
		public float distance;
		public Object hit;
	}
	
	public interface HitFunction {
		public Object test(int x, int y, int z);
	}

	private static final AVector<ACell> EMPTY_CHUNK;
	
	static {
		EMPTY_CHUNK=Vectors.repeat(null, 4096);
	}

	private Convex convex=null;
	
	private Engine() {
		try {
			convex=Convex.connect(Utils.toInetSocketAddress("convex.world:18888"));
			convex.setAddress(Address.create(4411));
		} catch (IOException|TimeoutException e ) {
			throw Utils.sneakyThrow(e);
		}
	}
	
	public static Engine create() {
		return new Engine();
	}
	
	public AVector<ACell> loadChunk(int x, int y, int z) {
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		Result r;
		try {
			r = convex.querySync(Reader.read("(call #4411 (get-chunk ["+bx+" "+by+" "+bz+"]))"));
		} catch (TimeoutException | IOException e) {
			throw Utils.sneakyThrow(e);
		}
		if (r.isError()) throw new Error("Bad result: "+r);
		AVector<ACell> chunk=r.getValue();

		return chunk;
	}
	
	public HashMap<Vector3i,AVector<ACell>> chunks=new HashMap<>();
	
	public AVector<ACell> getChunk(int x, int y, int z) {
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		
		Vector3i cpos=new Vector3i(bx,by,bz);
		AVector<ACell> chunk=chunks.get(cpos);
		if (chunk==null) {
			chunk=loadChunk(bx,by,bz);
			if (chunk==null) chunk=EMPTY_CHUNK;
			chunks.put(cpos,chunk);
		}
		
		return chunk;
	}
	
	public AVector<ACell> getChunk(Vector3i target) {
		return getChunk(target.x,target.y,target.z);
	}
	
	public Vector3i chunkPos(int x, int y, int z) {
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		Vector3i cpos=new Vector3i(bx,by,bz);
		return cpos;
	}
 	
	public static int chunkIndex(int x, int y, int z) {
		x&=0xf;
		y&=0xf;
		z&=0xf;
		return z*256+y*16+x;
	}

	public ACell getBlock(int x, int y, int z) {
		AVector<ACell> chunk=getChunk(x,y,z);
		if (chunk==null) return null;
		return chunk.get(chunkIndex(x,y,z));
	}
	
	public void setBlock(int x, int y, int z, ACell block) {
		AVector<ACell> chunk=getChunk(x,y,z);
		if (chunk==null) {
			if (block==null) return;
			chunk=EMPTY_CHUNK;
		}
		chunk=chunk.assoc(chunkIndex(x,y,z), block);
		Vector3i cpos=chunkPos(x,y,z);
		chunks.put(cpos, chunk);
	}
	
	public void setBlock(Vector3i target, ACell block) {
		int x=target.x;
		int y=target.y;
		int z=target.z;
		setBlock(x,y,z,block);
	}

	
	public static void intersect(Vector3f pos, Vector3f dir, HitFunction test, HitResult hr) {
		dir.normalize();
		// initial block
		int x=(int) Math.floor(pos.x);
		int y=(int) Math.floor(pos.y);
		int z=(int) Math.floor(pos.z);
		
		// partial position within block
		float px=pos.x-x;
		float py=pos.y-y;
		float pz=pos.z-z;
		
		// distance between intersections (always positive, may be infinite)
		float stepX=1.0f/Math.abs(dir.x);
		float stepY=1.0f/Math.abs(dir.y);
		float stepZ=1.0f/Math.abs(dir.z);
		
		// distances to next intersections
		float distX=stepX*((dir.x>0)?(1-px):(px));
		float distY=stepY*((dir.y>0)?(1-py):(py));
		float distZ=stepZ*((dir.z>0)?(1-pz):(pz));
		
		float dist=0.0f;
		float MAX_DIST=20f;
		hr.hit=null;
		int face=0;
		
		while (dist<MAX_DIST) {
			int axis=0;
			
			// get axis for next shift
			if (distX<distY) {
				if (distX<distZ) {
					axis=0;
				} else {
					axis=2;
				}
			} else {
				if (distY<distZ) {
					axis=1;
				} else {
					axis=2;
				}
			}
			
			switch (axis) {
				case 0: {
					int move=(dir.x>0)?1:-1;
					face=(move==1)?Face.W:Face.E; // West, East
					x+=move;
					dist=distX;
					distX+=stepX;
					break;
				}
				case 1: {
					int move=(dir.y>0)?1:-1;
					face=(move==1)?Face.S:Face.N; // South, North
					y+=move;
					dist=distY;
					distY+=stepY;
					break;
				}
				case 2: {
					int move=(dir.z>0)?1:-1;
					face=(move==1)?Face.D:Face.U; // Down, Up
					z+=move;
					dist=distZ;
					distZ+=stepZ;
					break;
				}
			}
			
			Object hit=test.test(x, y, z);
			if (hit!=null) {
				hr.hit=hit;
				break;
			}
		}
		
		hr.distance=dist;
		hr.x=x;
		hr.y=y;
		hr.z=z;
		hr.face=face;
		return;
	}
	
	
	public static void main(String[] args) throws TimeoutException, IOException {
		Engine e=new Engine();
		System.out.println(e.loadChunk(0,0,0));
	}
	
	private int tool=1;

	public void setTool(int i) {
		tool=i;
	}

	public int getTool() {
		return tool;
	}
	
	private int[] toolBar= {0,1,2,3,10,11,12,1,2,3};

	/**
	 * Gets the currently selected placeable block value
	 * @return
	 */
	public ACell getPlaceableBlock() {
		return CVMLong.create(toolBar[tool]);
	}



}
