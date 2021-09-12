package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.FloatBuffer;

public class Chunk {
	int vbo;
	int triangleCount=0;
	
	int[] vals=new int[4096];
	
	private Chunk() {
		vals[0]=1;
	}
	
	public static Chunk create() {
		Chunk c= new Chunk();
		c.createVBO();
		return c;
	}
	
	public void createVBO() {
		// Geometry in current context
		FloatBuffer vertexBuffer = buildAll();

		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

		triangleCount=4;
		
		memFree(vertexBuffer);
	}
	
	public FloatBuffer buildAll() {
		FloatBuffer vertexBuffer = memAllocFloat(2* (6 * (3+2)));
		vertexBuffer=addFace(vertexBuffer,0.0f, 0.0f, 0.0f,0,0);
		vertexBuffer=addFace(vertexBuffer,0.0f, 0.0f, 0.0f,3,0);
		vertexBuffer.flip();
		
		return vertexBuffer;
	}
	
	// North = +y East = +x
	// x,y,z offsets
	float[][] VERTS= {{0,0,0},{0,0,1},{0,1,0},{0,1,1},{1,0,0},{1,0,1},{1,1,0},{1,1,1}};
	// top, N, E, S, W, bottom
	int[][] FACES= {{7,3,1,5},{3,7,6,2},{1,3,2,0},{5,1,0,4},{7,5,4,6},{2,6,4,0}};
	
	// texture tile size
	float TD=1.0f/128;
	
	public FloatBuffer addFace(FloatBuffer fb, float bx, float by, float bz,int face, int texRef) {
		int[] FACE=FACES[face];
		// VErtices of face specified clockwise
		float[] v0=VERTS[FACE[0]]; // top left
		float[] v1=VERTS[FACE[1]]; // top right
		float[] v2=VERTS[FACE[2]]; // bottom right
		float[] v3=VERTS[FACE[3]]; // bottom left
		
		float tx=(texRef&0xFF)*TD;
		float ty=((texRef&0xFF00)>>8)*TD;
		
		// 0,1,3
		fb.put(v0[0]+bx).put(v0[1]+by).put(v0[2]+bz).put(tx).put(ty);
		fb.put(v1[0]+bx).put(v1[1]+by).put(v1[2]+bz).put(tx+TD).put(ty);
		fb.put(v3[0]+bx).put(v3[1]+by).put(v3[2]+bz).put(tx).put(ty+TD);
		// 3,1,2
		fb.put(v3[0]+bx).put(v3[1]+by).put(v3[2]+bz).put(tx).put(ty+TD);
		fb.put(v1[0]+bx).put(v1[1]+by).put(v1[2]+bz).put(tx+TD).put(ty);
		fb.put(v2[0]+bx).put(v2[1]+by).put(v2[2]+bz).put(tx+TD).put(ty+TD);
		
		return fb;
	}
	
	public int getVBO() {
		return vbo;
	}
	
	public int getTriangleCount() {
		return triangleCount;
	}

	public void draw() {
		glBindBuffer(GL_ARRAY_BUFFER, getVBO());
		glDrawArrays(GL_TRIANGLES, 0, getTriangleCount()*3);
	}
}
