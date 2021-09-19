package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.FloatBuffer;

public class Billboard {
	
	int vbo=0;
	int triangleCount=0;

	public static final int FLOATS_PER_VERTEX=3+3+2; // position + texture + normal

	FloatBuffer fb=FloatBuffer.allocate(6*FLOATS_PER_VERTEX);

	private int createVBO() {
		// Geometry in current context
		fb.clear();
		FloatBuffer built = addEntity(fb,0x1000);
		fb.flip();
		
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
	
	public void draw() {
		if (vbo!=0) {
			// Bind buffer
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			
			int stride=Chunk.FLOATS_PER_VERTEX*4;
			
			// define vertex format, should be after glBindBuffer
			glVertexAttribPointer(Chunk.c_vs_inputPosition,3,GL_FLOAT,false,stride,0L); // Note: stride in bytes
	        glEnableVertexAttribArray(Chunk.c_vs_inputPosition);
	        
			glVertexAttribPointer(Chunk.c_vs_normalPosition,3,GL_FLOAT,false,stride,12L); // Note: stride in bytes
	        glEnableVertexAttribArray(Chunk.c_vs_normalPosition);

	        glVertexAttribPointer(Chunk.c_vs_texturePosition,2,GL_FLOAT,false,stride,24L); // Note: stride in bytes
	        glEnableVertexAttribArray(Chunk.c_vs_texturePosition);

			glDrawArrays(GL_TRIANGLES, 0, triangleCount*3);
		}
	}
	
	static float[] normal = new float[] {0,-1,0};
	
	// texture tile size
	float TD=1.0f/128;

	
	private FloatBuffer addEntity(FloatBuffer fb, int texRef) {
		
		float tx=(texRef&0xFF)*TD;
		float ty=((texRef&0xFF00)>>8)*TD;

		// 0,1,3
		fb.put(-0.5f).put(0).put(0.5f).put(normal).put(tx).put(ty);
		fb.put(0.5f).put(0).put(0.5f).put(normal).put(tx+TD).put(ty);
		fb.put(-0.5f).put(0).put(-0.5f).put(normal).put(tx).put(ty+TD);
		// 3,1,2
		fb.put(-0.5f).put(0).put(-0.5f).put(normal).put(tx).put(ty+TD);
		fb.put(0.5f).put(0).put(0.5f).put(normal).put(tx+TD).put(ty);
		fb.put(0.5f).put(0).put(-0.5f).put(normal).put(tx+TD).put(ty+TD);

		return fb;
	}

	public void init() {
		vbo=createVBO();
	}
}
