package blockgame.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.joml.Vector3f;
import org.joml.Vector3i;

import blockgame.Config;
import convex.api.Convex;
import convex.core.Result;
import convex.core.data.ACell;
import convex.core.data.AVector;
import convex.core.data.Vectors;
import convex.core.data.prim.CVMLong;
import convex.core.lang.Reader;
import convex.core.lang.Symbols;
import convex.core.transactions.Invoke;
import convex.core.util.Utils;

/**
 * Main off-chain game engine
 */
public class Engine {
	
	static {
		System.out.println(Config.kp.getAccountKey());
	}

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

	public static final AVector<ACell> EMPTY_CHUNK=Vectors.repeat(null, 4096);
	
	private Engine() {
	}
	
	public static Engine create() {
		return new Engine();
	}
	
	long chunkAddress(int bx, int by, int bz) {
		return bx+ (by * 1048576l) + bz*1099511627776l;
	}
	
	public void loadChunk(int x, int y, int z) {
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		try {
			// String chunkString="["+bx+" "+by+" "+bz+"]"; // Old format
			long chunkPos= chunkAddress(bx,by,bz);
			String chunkString=Long.toString(chunkPos);
			ACell queryForm=Reader.read("(call "+Config.worldAddress+" (get-chunk "+chunkString+"))");
			Convex convex=Config.getConvex();
			CompletableFuture<Result> cf=(CompletableFuture<Result>) convex.query(queryForm);
			cf.thenAcceptAsync(r-> {
				if (r.isError()) throw new Error("Bad result: "+r);
				AVector<ACell> chunk=r.getValue();
				if (chunk==null) chunk=EMPTY_CHUNK;
				Long cpos=chunkAddress(bx,by,bz);
				chunks.put(cpos, chunk);
				System.out.println("Loaded chunk at "+locString(bx,by,bz));
			}).exceptionallyAsync(e->{		
				System.err.println(queryForm); 
				System.err.println(e); 
				return null;
			});
		} catch (IOException e) {
			throw Utils.sneakyThrow(e);
		}
	}
	
	public static String locString(int bx, int by, int bz) {
		return bx+","+by+","+bz;
	}
	
	public static String locString(Vector3i pos) {
		return locString(pos.x,pos.y,pos.z);
	}

	public HashMap<Long,AVector<ACell>> chunks=new HashMap<>(91);
	
	public AVector<ACell> getChunk(int x, int y, int z) {
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		
		Long cpos=chunkAddress(bx,by,bz);
		AVector<ACell> chunk=chunks.get(cpos);
		if (chunk==null) {
			loadChunk(bx,by,bz); // schedule chunk load
			chunk=EMPTY_CHUNK; // placeholder empty chunk
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
		return (z*256)+(y*16)+x;
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
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		chunks.put(chunkAddress(bx,by,bz), chunk);
		if (block==null) block=Symbols.NIL;
		ACell trans=Reader.read("(call "+Config.worldAddress+" (place-block "+locString(x,y,z)+" "+block+"))");
		try {
			Convex convex=Config.getConvex();
			convex.transact(Invoke.create(Config.addr, 0, trans));
		} catch (IOException e) {
			System.out.println(e);
		}
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
		e.loadChunk(0,0,0);
	}
	
	private int tool=1;

	public void setTool(int i) {
		tool=i;
	}

	public int getTool() {
		return tool;
	}
	
	private int[] toolBar= {0,1,2,3,10,11,12,13,14,20};

	/**
	 * Gets the currently selected placeable block value
	 * @return
	 */
	public ACell getPlaceableBlock() {
		return CVMLong.create(toolBar[tool]);
	}





}
