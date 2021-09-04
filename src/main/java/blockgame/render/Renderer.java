package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glLoadMatrixf;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Renderer {
	
	int program;
	
	public FloatBuffer matbuffer = BufferUtils.createFloatBuffer(16);
	
	int createProgram() throws IOException {
		int program = glCreateProgram();
		int vshader = Utils.createShader("shaders/vertex-shader.vert", GL_VERTEX_SHADER);
		int fshader = Utils.createShader("shaders/fragment-shader.frag", GL_FRAGMENT_SHADER);
		glAttachShader(program, vshader);
		glAttachShader(program, fshader);
		glLinkProgram(program);
		int linked = glGetProgrami(program, GL_LINK_STATUS);
		String programLog = glGetProgramInfoLog(program);
		if (programLog.trim().length() > 0) {
			System.err.println(programLog);
		}
		if (linked == 0) {
			throw new AssertionError("Could not link program");
		}
		glUseProgram(program);
		// transformUniform = glGetUniformLocation(program, "transform");
		// glUseProgram(0);
		return program;
	}


	public void init() {
		try {
			program=createProgram();
		} catch (IOException e) {
			throw new Error(e);
		}
		
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

		// Set the clear color
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	Matrix4f m=new Matrix4f();

	protected int width;
	protected int height;
	
	public void render(float t) {
		// clear the framebuffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 

		glMatrixMode(GL_MODELVIEW);
		Matrix4f mv = new Matrix4f().rotateY(t);
		mv.get(matbuffer);
		
		glUniformMatrix4fv(0, false,  matbuffer);
	
		glDrawArrays(GL_TRIANGLES, 0, 3);
	}


	public void setSize(int width, int height) {
		this.width=width;
		this.height=height;
	}

}
