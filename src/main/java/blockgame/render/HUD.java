package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glGetAttribLocation;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import blockgame.engine.Engine;
import blockgame.engine.Lib;
import convex.core.data.ACell;

public class HUD {

	protected final Engine engine;
	private static int cursorVBO;
	private static int toolsVBO;
	
	static int hudProgram;
	
	static int h_vs_inputPosition;
	static int h_vs_texturePosition;
	static int h_vs_MVPPosition;
	static int h_vs_ColourPosition;


	public HUD(Engine engine) {
		this.engine=engine;
	}
	
	public static final int FLOATS_PER_VERTEX=3+2; // position + texture
	static int triangleCount=0;

	static int createHUDProgram() throws IOException {
		int program = glCreateProgram();
		int vshader = Utils.createShader("shaders/hud-shader.vert", GL_VERTEX_SHADER);
		int fshader = Utils.createShader("shaders/hud-shader.frag", GL_FRAGMENT_SHADER);
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
		h_vs_inputPosition = glGetAttribLocation(program, "position");
		h_vs_texturePosition = glGetAttribLocation(program, "texture");
		
		h_vs_MVPPosition = glGetUniformLocation(program, "MVP");
		h_vs_ColourPosition = glGetUniformLocation(program, "colour");

		return program;
	}
	
	Matrix4f transformation=new Matrix4f();
	private final FloatBuffer matbufferMV = BufferUtils.createFloatBuffer(16);

	public void draw(int width, int height) {
		glUseProgram(hudProgram);
		
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		
		transformation.setOrtho2D(-width/2, width/2, height/2, -height/2); // note vertical flip
		transformation.get(0, matbufferMV);		
		
		glUniformMatrix4fv(h_vs_MVPPosition, false,  matbufferMV);
		GL20.glUniform4f(h_vs_ColourPosition, 1, 1, 1,1);

		// Bind general tile texture
		Chunk.texture.bind();
		
		drawCursor();
		drawTools(width, height);

		drawHUDText(width, height);
	}

	
	private Buildable tools=Buildable.create(FLOATS_PER_VERTEX);
	private void drawTools(int width, int height) {
		tools.clear();
		buildTools(width,height);
		
		FloatBuffer fb=tools.getFlippedBuffer();
		int n=fb.remaining();
		FloatBuffer vertexBuffer = memAllocFloat(n);
		vertexBuffer.put(fb);
		vertexBuffer.flip();
		
		// Copy native vertexbuffer to GL buffer
		glBindBuffer(GL_ARRAY_BUFFER, toolsVBO);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STREAM_DRAW);
		memFree(vertexBuffer);
		
		int stride=FLOATS_PER_VERTEX*4;
		// define vertex format, must be after glBindBuffer
		// position is location 0
		glVertexAttribPointer(h_vs_inputPosition,3,GL_FLOAT,false,stride,0L); // Note: stride in bytes
        glEnableVertexAttribArray(h_vs_inputPosition);
        
        // texture is location 1
        glVertexAttribPointer(h_vs_texturePosition,2,GL_FLOAT,false,stride,12L); // Note: stride in bytes
        glEnableVertexAttribArray(h_vs_texturePosition);
 
		glDrawArrays(GL_TRIANGLES, 0, n/FLOATS_PER_VERTEX);
	}

	/**
	 * Display for tools at the bottom of screen
	 * @param w Screen width
	 * @param h Screen height
	 */
	private void buildTools(int w, int h) {
		
		int s=128;
		int b=16;
		int selected=engine.getToolIndex();
			
		for (int i=1; i<=9; i++) {
			ACell tool=engine.getTool(i);
			int tx=Lib.getToolTexture(tool);
			// tx=0x0202;
			
			float x=(i-5)*(s+b*2);
			float y=(h/2)-(s+b*2);
			
			addQuad(tools,x-b,y-b,s+2*b,(i==selected)?0x1401:0x1400);
			
			int quant=engine.getToolQuantity(tool);
			if (quant>0) {
				addQuad(tools,x,y,s,tx);
				if (quant>1) {
				Text.addText(x, y+s-Text.SIZE, Integer.toString(quant));
				}
			}
		}
		
		// addQuad(tools,0,0,64,0);
	}

	private void drawHUDText(int width, int height) {
		StringBuilder ht=new StringBuilder();
		ht.append("CONVEX Craft\n");
		ht.append("Chunks Loaded: "+engine.chunks.size()+"\n");
		ht.append("FPS:           "+FPSformat.format(Renderer.fps)+"\n");
		
		Text.addText(-width/2, -height/2, ht.toString());
		Text.draw();
	}

	private void drawCursor() {
		glBindBuffer(GL_ARRAY_BUFFER, cursorVBO);

		int stride=FLOATS_PER_VERTEX*4;
		
		// define vertex format, must be after glBindBuffer
		// position is location 0
		glVertexAttribPointer(h_vs_inputPosition,3,GL_FLOAT,false,stride,0L); // Note: stride in bytes
        glEnableVertexAttribArray(h_vs_inputPosition);
        
        // texture is location 1
        glVertexAttribPointer(h_vs_texturePosition,2,GL_FLOAT,false,stride,12L); // Note: stride in bytes
        glEnableVertexAttribArray(h_vs_texturePosition);
        
        //glEnable(GL_BLEND);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glDrawArrays(GL_TRIANGLES, 0, getTriangleCount()*3);
	}
	
	DecimalFormat FPSformat = new DecimalFormat("0.0");

	private int getTriangleCount() {
		return triangleCount;
	}

	public static void init(Engine engine) throws IOException {
		hudProgram=createHUDProgram();
		cursorVBO=createVBO();	
		toolsVBO=glGenBuffers();
	}

	private static int createVBO() {
		// Geometry in current context
		FloatBuffer built = buildAll();
		
		int n=built.remaining();
		triangleCount=n/(3*FLOATS_PER_VERTEX);
		
		FloatBuffer vertexBuffer = memAllocFloat(n);
		vertexBuffer.put(built);
		vertexBuffer.flip();

		cursorVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, cursorVBO);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		
		memFree(vertexBuffer);
		
		// System.out.println("VBO built! "+vbo);
		return cursorVBO;
	}
	
	private static FloatBuffer buildAll() {
		FloatBuffer vb=buildCursor();
		return vb;
	}
	
	private static FloatBuffer buildCursor() {
		float s=32f;
		Buildable vb = Buildable.create(FLOATS_PER_VERTEX);
		addQuad(vb,-s,-s,s*2,0x0007);
		return vb.getFlippedBuffer();
	}
	
	private static void addQuad(Buildable builder,float x1, float y1, float size, int texture) {
		addQuad(builder,x1,y1,size,size,texture);

	}
	
	private static void addQuad(Buildable builder,float x1, float y1, float w, float h, int texture) {
		float tx=Texture.tx(texture);
		float ty=Texture.ty(texture);
		float TD=Texture.TD;
		
		builder.put(x1,y1,0).put(tx,ty);
		builder.put(x1+w,y1,0).put(tx+TD,ty);
		builder.put(x1,y1+h,0).put(tx,ty+TD);
		
		builder.put(x1,y1+h,0).put(tx,ty+TD);
		builder.put(x1+w,y1,0).put(tx+TD,ty);
		builder.put(x1+w,y1+h,0).put(tx+TD,ty+TD);

	}

}
