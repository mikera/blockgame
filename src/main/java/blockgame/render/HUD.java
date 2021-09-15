package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.FloatBuffer;

import blockgame.engine.Engine;

public class HUD {

	protected Engine engine;
	private int vbo;

	public HUD(Engine engine) {
		this.engine=engine;
	}
	
	public static final int FLOATS_PER_VERTEX=3+2; // position + texture
	int triangleCount=0;


	public void draw() {
		glBindBuffer(GL_ARRAY_BUFFER, vbo);

		int stride=FLOATS_PER_VERTEX*4;
		
		// define vertex format, should be after glBindBuffer
		// position is location 0
		glVertexAttribPointer(Renderer.h_vs_inputPosition,3,GL_FLOAT,false,stride,0L); // Note: stride in bytes
        glEnableVertexAttribArray(Renderer.h_vs_inputPosition);
        
        // texture is location 1
        glVertexAttribPointer(Renderer.h_vs_texturePosition,2,GL_FLOAT,false,stride,12L); // Note: stride in bytes
        glEnableVertexAttribArray(Renderer.h_vs_texturePosition);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glDrawArrays(GL_TRIANGLES, 0, getTriangleCount()*3);

	}

	private int getTriangleCount() {
		return triangleCount;
	}

	public void init() {
		vbo=createVBO();
	}
	


	private int createVBO() {
		// Geometry in current context
		FloatBuffer built = buildAll();
		
		int n=built.remaining();
		triangleCount=n/(3*FLOATS_PER_VERTEX);
		
		FloatBuffer vertexBuffer = memAllocFloat(n);
		vertexBuffer.put(built);
		vertexBuffer.flip();

		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		
		memFree(vertexBuffer);
		
		// System.out.println("VBO built! "+vbo);
		return vbo;
	}
	
	private FloatBuffer buildAll() {
		float s=32f;
		FloatBuffer vb = FloatBuffer.allocate(30);
		vb.put(new float[] {-1*s,-1*s,0,7/128f,0/128f});
		vb.put(new float[] {1*s,-1*s,0,8/128f,0/128f});
		vb.put(new float[] {-1*s,1*s,0,7/128f,1/128f});
		
		vb.put(new float[] {1*s,-1*s,0,8/128f,0/128f});
		vb.put(new float[] {-1*s,1*s,0,7/128f,1/128f});
		vb.put(new float[] {1*s,1*s,0,8/128f,1/128f});
		
		vb.flip();
		return vb;
	}

}
