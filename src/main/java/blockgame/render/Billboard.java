package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glGetAttribLocation;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.io.IOException;
import java.nio.FloatBuffer;

public class Billboard {
	
	int vbo=0;
	int triangleCount=0;

	public static final int FLOATS_PER_VERTEX=3+2; // position + texture + normal

	FloatBuffer fb=FloatBuffer.allocate(6*FLOATS_PER_VERTEX);
	
	static int b_fs_LightDirPosition;
	static int b_vs_MVPosition;
	static int b_vs_PPosition;
	static int b_vs_normalPosition;
	static int b_vs_texturePosition;
	static int b_vs_inputPosition;
	
	private static int program;

	private static int createProgram() throws IOException {
		int program = glCreateProgram();
		int vshader = Utils.createShader("shaders/billboard-shader.vert", GL_VERTEX_SHADER);
		int fshader = Utils.createShader("shaders/billboard-shader.frag", GL_FRAGMENT_SHADER);
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
		
		// set up positions for input attributes
		b_vs_inputPosition = glGetAttribLocation(program, "position");
		b_vs_texturePosition = glGetAttribLocation(program, "texture");
		
		b_vs_PPosition = glGetUniformLocation(program, "P");
		b_vs_MVPosition = glGetUniformLocation(program, "MV");

		return program;
		// TODO: do we need to dispose the program somehow?
	}

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
			
			int stride=FLOATS_PER_VERTEX*4;
			
			// define vertex format, should be after glBindBuffer
			glVertexAttribPointer(b_vs_inputPosition,3,GL_FLOAT,false,stride,0L); // Note: stride in bytes
	        glEnableVertexAttribArray(b_vs_inputPosition);

	        glVertexAttribPointer(b_vs_texturePosition,2,GL_FLOAT,false,stride,12L); // Note: stride in bytes
	        glEnableVertexAttribArray(b_vs_texturePosition);

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
		fb.put(-0.5f).put(0).put(0.5f).put(tx).put(ty);
		fb.put(0.5f).put(0).put(0.5f).put(tx+TD).put(ty);
		fb.put(-0.5f).put(0).put(-0.5f).put(tx).put(ty+TD);
		// 3,1,2
		fb.put(-0.5f).put(0).put(-0.5f).put(tx).put(ty+TD);
		fb.put(0.5f).put(0).put(0.5f).put(tx+TD).put(ty);
		fb.put(0.5f).put(0).put(-0.5f).put(tx+TD).put(ty+TD);

		return fb;
	}

	public void init() throws IOException {
		program=createProgram();
		vbo=createVBO();
	}

	public static int getProgram() {
		return program;
	}
}
