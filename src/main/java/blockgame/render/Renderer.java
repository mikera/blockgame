package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.FloatBuffer;

public class Renderer {

	public void init() {
		// Geometry in current context
		FloatBuffer buffer = memAllocFloat(3 * 2);
		buffer.put(-0.5f).put(-0.5f);
		buffer.put(+0.5f).put(-0.5f);
		buffer.put(+0.0f).put(+0.5f);
		buffer.flip();

		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

		memFree(buffer);

		// define vertex format
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(2, GL_FLOAT, 0, 0L);

		
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

}
