package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import blockgame.engine.Engine;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

import org.joml.Vector3i;

import blockgame.assets.Assets;
import blockgame.engine.Face;
import convex.core.data.ABlob;
import convex.core.data.ACell;
import convex.core.data.AHashMap;
import convex.core.data.AVector;
import convex.core.data.Keyword;
import convex.core.data.prim.CVMLong;

public class Chunk {
	int vbo=0;
	int triangleCount=0;
	
	// int[] vals=new int[4096];
	private AVector<ACell> chunkData;
	private final Engine engine;
	private final Vector3i position;
	
	private Chunk(Vector3i cpos, Engine engine) {
		this.position=cpos;
		this.engine=engine;
	}
	
	public static Chunk create(Vector3i cpos, Engine engine) {
		Chunk c= new Chunk(cpos,engine);
		AVector<ACell> current=engine.getChunk(cpos);
		c.setData(current);
		c.createVBO();
		return c;
	}
	
	private void setData(AVector<ACell> chunkData) {
		this.chunkData=chunkData;
	}
	
	public void refresh() {
		AVector<ACell> latest=engine.getChunk(position);
		if (chunkData!=latest) {
			setData(latest);
			glDeleteBuffers(vbo);
			vbo=createVBO();
		};
	}

	public static final int FLOATS_PER_VERTEX=3+3+2; // position + texture + normal
	public static final int VERTICES_PER_FACE=6; // 2 triangles, 3 vertices each
	
	private int createVBO() {
		// Geometry in current context
		FloatBuffer built = buildAll();
		
		int n=built.remaining();
		triangleCount=n/(3*FLOATS_PER_VERTEX);
		if (triangleCount>0) {
			
			FloatBuffer vertexBuffer = memAllocFloat(n);
			vertexBuffer.put(built);
			vertexBuffer.flip();
	
			vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
			
			memFree(vertexBuffer);
		} else {
			vbo=0;
		}
		
		// System.out.println("VBO built! "+vbo);
		return vbo;
	}
	
	FloatBuffer vb = FloatBuffer.allocate(30000);
	private FloatBuffer buildAll() {
		vb.clear();
		if (!chunkData.equals(Engine.EMPTY_CHUNK)) {
			while (true) {
				try {
					for (int k=0; k<16; k++) {
						for (int j=0; j<16; j++) {
							for (int i=0; i<16; i++) {
								long ix=i+j*16+k*256;
								ACell block=chunkData.get(ix);
								if (block!=null) {
									vb=addBlock(vb,i,j,k,block);
								}
							}
						}
					}
				} catch (BufferOverflowException be) {
					vb=FloatBuffer.allocate(vb.capacity()*2);
					continue;
				}
				break;
			}
		}
		
		vb.flip();
		return vb;
	}
	
	@SuppressWarnings("unchecked")
	private FloatBuffer addBlock(FloatBuffer vb, int x, int y, int z, ACell type) {
		// Destructure compound block
		if (type instanceof AVector) {
			AVector<?> v=(AVector<?>)type;
			if (v.count()>0) {
				type=v.get(0);
			}
		}
		
		AHashMap<Keyword,ACell> meta=(AHashMap<Keyword, ACell>) Assets.blockData.get(type);
		if (meta==null) meta=(AHashMap<Keyword, ACell>) Assets.blockData.get(CVMLong.ONE);
		AVector<ABlob> tex=(AVector<ABlob>) meta.get(Assets.TEX_KEY);
		
		for (int face=0; face<6; face++) {
			Vector3i fd=Face.DIR[face];
			
			// Why does x need - ?? Error in lookup tables?
			ACell facing=engine.getBlock(x-fd.x+position.x, y+fd.y+position.y, z+fd.z+position.z);
			if (facing!=null) {
				continue;
			}
			vb=addFace(vb, x, y, z,face,tex.get(face).toLong());
		}
		
		return vb;
	}
	
	// North = +y East = +x
	// x,y,z offsets
	float[][] VERTS= {{0,0,0},{0,0,1},{0,1,0},{0,1,1},{1,0,0},{1,0,1},{1,1,0},{1,1,1}};
	// U, N, E, S, W, D
	int[][] FACES= {{7,3,1,5},{3,7,6,2},{1,3,2,0},{5,1,0,4},{7,5,4,6},{2,6,4,0}};
	
	// texture tile size
	float TD=1.0f/128;
	
	public FloatBuffer addFace(FloatBuffer fb, float bx, float by, float bz,int face, long texRef) {
		int[] FACE=FACES[face];
		// VErtices of face specified clockwise
		float[] v0=VERTS[FACE[0]]; // top left
		float[] v1=VERTS[FACE[1]]; // top right
		float[] v2=VERTS[FACE[2]]; // bottom right
		float[] v3=VERTS[FACE[3]]; // bottom left
		
		float tx=(texRef&0xFF)*TD;
		float ty=((texRef&0xFF00)>>8)*TD;
		
		float[] normal=Face.NORMAL[face];
		
		// Vertices in square numbered clockwise 0,1,2,3
		// 0,1,3
		fb.put(v0[0]+bx).put(v0[1]+by).put(v0[2]+bz).put(normal).put(tx).put(ty);
		fb.put(v1[0]+bx).put(v1[1]+by).put(v1[2]+bz).put(normal).put(tx+TD).put(ty);
		fb.put(v3[0]+bx).put(v3[1]+by).put(v3[2]+bz).put(normal).put(tx).put(ty+TD);
		// 3,1,2
		fb.put(v3[0]+bx).put(v3[1]+by).put(v3[2]+bz).put(normal).put(tx).put(ty+TD);
		fb.put(v1[0]+bx).put(v1[1]+by).put(v1[2]+bz).put(normal).put(tx+TD).put(ty);
		fb.put(v2[0]+bx).put(v2[1]+by).put(v2[2]+bz).put(normal).put(tx+TD).put(ty+TD);
		
		return fb;
	}
	
	public int getVBO() {
		return vbo;
	}
	
	public int getTriangleCount() {
		return triangleCount;
	}

	public void draw() {
		refresh();
		if (vbo!=0) {
			// Bind buffer
			glBindBuffer(GL_ARRAY_BUFFER, getVBO());
			
			int stride=Chunk.FLOATS_PER_VERTEX*4;
			
			// define vertex format, should be after glBindBuffer
			glVertexAttribPointer(Renderer.c_vs_inputPosition,3,GL_FLOAT,false,stride,0L); // Note: stride in bytes
	        glEnableVertexAttribArray(Renderer.c_vs_inputPosition);
	        
			glVertexAttribPointer(Renderer.c_vs_normalPosition,3,GL_FLOAT,false,stride,12L); // Note: stride in bytes
	        glEnableVertexAttribArray(Renderer.c_vs_normalPosition);

	        glVertexAttribPointer(Renderer.c_vs_texturePosition,2,GL_FLOAT,false,stride,24L); // Note: stride in bytes
	        glEnableVertexAttribArray(Renderer.c_vs_texturePosition);

			
			glDrawArrays(GL_TRIANGLES, 0, getTriangleCount()*3);
		}
	}


}
