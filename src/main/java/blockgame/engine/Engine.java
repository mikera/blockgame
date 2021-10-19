package blockgame.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.joml.Vector3f;
import org.joml.Vector3i;

import blockgame.Config;
import blockgame.Deploy;
import convex.api.Convex;
import convex.core.Result;
import convex.core.data.ACell;
import convex.core.data.AMap;
import convex.core.data.AVector;
import convex.core.data.Address;
import convex.core.data.Vectors;
import convex.core.data.prim.CVMLong;
import convex.core.lang.Reader;
import convex.core.lang.Symbols;
import convex.core.transactions.Invoke;
import convex.core.util.Utils;
import mikera.util.Maths;

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
	
	public void createPlayer() throws TimeoutException, IOException {
		Convex convex=getConvex();
		Address inv=Deploy.inventory;
		Address player=convex.getAddress();
		StringBuilder cmds=new StringBuilder();
		cmds.append("(do ");
		for (int i=1; i<=9; i++) {
			ACell tool=getTool(i);
			if (tool!=null) {
				cmds.append("(call "+inv+" (set-stack "+player+" "+tool+" 9)) ");
			}
		}
		cmds.append(" (call "+inv+" (balance *address*)))");
		Invoke trans=Invoke.create(player, 0, Reader.read(cmds.toString()));
		Result r=convex.transactSync(trans);
		if (r.isError()) throw new Error(r.toString());
		invMap=r.getValue();
	}
	
	private Future<Result> refreshInventory() {
		Convex convex=getConvex();
		Address inv=Deploy.inventory;
		String cmd=(" (call "+inv+" (balance *address*))");
		ACell queryCmd=Reader.read(cmd);
		CompletableFuture<Result> result=null;
		try {
			result = (CompletableFuture<Result>) convex.query(queryCmd);
			result.thenAcceptAsync(r->{
				if (r.isError()) {
					System.err.println(r);
				} else {
					AMap<ACell,ACell> newInv=r.getValue();
					boolean changed=!newInv.equals(invMap);
					System.out.println("Inventory update: "+changed);
					if (changed) {
						invMap=r.getValue();
					}
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;		
	}
	
	private AMap<ACell,ACell> invMap;
	
	public long chunkAddress(long bx, long by, long bz) {
		bx&=~0xfl; by&=~0xfl; bz&=~0xfl; // ensure rounded chunk address
		return bx+ (by * 1048576l) + bz*1099511627776l;
	}
	
	public void uploadChunk(int x, int y, int z) {
		// Chunk base location
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		try {
			// String chunkString="["+bx+" "+by+" "+bz+"]"; // Old format
			long chunkPos= chunkAddress(bx,by,bz);
			String chunkString=Long.toString(chunkPos);
			AVector<ACell> chunkData=getChunk(bx,by,bz);
			ACell form=Reader.read("(call "+Config.world+" (set-chunk "+chunkString+" "+chunkData.toString()+"))");
			Convex convex=Config.getConvex();
			CompletableFuture<Result> cf=(CompletableFuture<Result>) convex.transact(Invoke.create(convex.getAddress(), 0, form));
			cf.thenAcceptAsync(r-> {
				if (r.isError()) throw new Error("Bad result: "+r);
				System.out.println("Uploaded chunk at "+locString(bx,by,bz) + " : "+chunkString);
				chunks.put(chunkPos, chunkData);
			}).exceptionallyAsync(e->{		
				System.err.println(form); 
				System.err.println(e); 
				return null;
			});
		} catch (IOException e) {
			throw Utils.sneakyThrow(e);
		}
	}
	
	public void loadChunk(int x, int y, int z) {
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		try {
			// String chunkString="["+bx+" "+by+" "+bz+"]"; // Old format
			long chunkPos= chunkAddress(bx,by,bz);
			String chunkString=Long.toString(chunkPos);
			ACell queryForm=Reader.read("(call "+Config.world+" (get-chunk "+chunkString+"))");
			Convex convex=Config.getConvex();
			CompletableFuture<Result> cf=(CompletableFuture<Result>) convex.query(queryForm);
			cf.thenAcceptAsync(r-> {
				if (r.isError()) throw new Error("Bad result: "+r);
				AVector<ACell> chunk=r.getValue();
				if (chunk==null) chunk=EMPTY_CHUNK;
				Long cpos=chunkAddress(bx,by,bz);
				chunks.put(cpos, chunk);
				// System.out.println("Loaded chunk at "+locString(bx,by,bz));
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
	
	/**
	 * Get the chunk that includes the specified block location
	 */
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
	
	/**
	 * Get the chunk that includes the specified block location from local store
	 * @return Chunk data or null if not set locally
	 */
	public AVector<ACell> getChunkLocal(int x, int y, int z) {
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		
		Long cpos=chunkAddress(bx,by,bz);
		AVector<ACell> chunk=chunks.get(cpos);
		
		return chunk;
	}
	
	public AVector<ACell> getChunk(Vector3i target) {
		return getChunk(target.x,target.y,target.z);
	}
	
	/**
	 * Get a Vector3i pointing to the chunk base location
	 */
	public Vector3i chunkPos(int x, int y, int z) {
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		Vector3i cpos=new Vector3i(bx,by,bz);
		return cpos;
	}
 	
	/**
	 * Get the block index within a chunk (0-4095)
	 * @param x X Position
	 * @param y Y Position
	 * @param z Z Position
	 * @return Block index
	 */
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
		ACell trans;
		if (block==null) {
			trans=Reader.read("(call "+Config.world+" (break-block "+locString(x,y,z)+"))");
		} else {
			trans=Reader.read("(call "+Config.world+" (place-block "+locString(x,y,z)+" "+block+"))");
		}
		try {
			Convex convex=Config.getConvex();
			convex.transact(Invoke.create(Config.addr, 0, trans)).whenComplete((r,ex)->{
				if (r==null) {
					System.err.println(ex);
					return;
				}
				if (r.isError()) {
					System.err.println("Error setting block in chunk: "+chunkAddress(x,y,z)+" : "+r);
				} else {
					System.out.println("Block "+block+" placed at "+Engine.locString(x,y,z));
					setBlockLocal(x,y,z,block);
				};
				refreshInventory();
			});
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public void setBlockLocal(int x, int y, int z, ACell block) {
		AVector<ACell> chunk=getChunkLocal(x,y,z);
		if (chunk==null) {
			if (block==null) return;
			chunk=EMPTY_CHUNK;
		}
		chunk=chunk.assoc(chunkIndex(x,y,z), block);
		int bx=x&~0xf;
		int by=y&~0xf;
		int bz=z&~0xf;
		chunks.put(chunkAddress(bx,by,bz), chunk);
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

	public void setToolIndex(int i) {
		tool=i;
	}

	public int getToolIndex() {
		return tool;
	}
	
	public ACell getTool(int i) {
		return toolBar[i];
	}
	
	private ACell[] toolBar= new ACell[]{null,Lib.GRASS,Lib.DIRT,Lib.STONE,Lib.STONE_BRICKS, Lib.BOULDER,Lib.LOG, Lib.LEAVES,Lib.RED_MUSHROOM,Lib.MEDIUM_GRASS};

	/**
	 * Gets the currently selected placeable block value, or null if cannot place
	 * @return
	 */
	public ACell getPlaceableBlock() {
		return toolBar[tool];
	}

	/**
	 * Get currently configured Convex client instance
	 * @return
	 */
	public Convex getConvex() {
		return Config.getConvex();
	}

	/**
	 * Fill blocks in the local environment
	 * @param x1 Start x
	 * @param y1 Start y
	 * @param z1 Start z
	 * @param x2 End x
	 * @param y2 End y
	 * @param z2 End z
	 * @param block Block to fill with
	 */
	public void fillBlocks(int x1, int y1, int z1, int x2, int y2, int z2, ACell block) {
		int t;
		if (x1>x2) {t=x1; x1=x2; x2=t;}
		if (y1>y2) {t=y1; y1=y2; y2=t;}
		if (z1>z2) {t=z1; z1=z2; z2=t;}
		
		for (int x=x1; x<=x2; x++) {
			for (int y=y1; y<=y2; y++) {
				for (int z=z1; z<=z2; z++) {
					setBlockLocal(x,y,z,block);
				}
			}
			
		}
	}

	public void doScroll(int yoffset) {
		setToolIndex(Maths.middle(tool-yoffset,1,9));
	}

	public int getToolQuantity(ACell tool) {
		ACell q= invMap.get(tool);
		if (q==null) return 0;
		return (int) ((CVMLong)q).longValue();
	}





}
