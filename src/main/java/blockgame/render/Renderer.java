package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glGetAttribLocation;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import blockgame.assets.Assets;
import blockgame.engine.Engine;
import convex.core.data.ACell;
import convex.core.data.AVector;

public class Renderer {
	
	int program;
	int texture;
	int vs_inputPosition;
	int vs_texturePosition;

	public FloatBuffer matbuffer = BufferUtils.createFloatBuffer(16);
	
	private Engine engine=Engine.create();

	
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
		
		// set up positions for input attributes
		vs_inputPosition = glGetAttribLocation(program, "position");
		vs_texturePosition = glGetAttribLocation(program, "texture");
		
		return program;
		// TODO: do we need to dispose the program somehow?
	}
	
    int createTexture() throws IOException {

        BufferedImage bi=Assets.textureImage;
        IntBuffer data=BufferUtils.createIntBuffer(2048*2048);
         
        int[] argb=new int [2048*2048];
        bi.getRGB(0, 0, 2048, 2048, argb, 0, 2048);
        for (int i=0; i<argb.length; i++) {
        	int col=argb[i];
        	col=Integer.rotateLeft(col, 8); // rotate to RGBA
        	// col|=0xFF; // Max alpha
        	col=Integer.reverseBytes(col);
        	argb[i]=col;
        }
        
        data.put(argb);
        data.flip();
    
        int id = glGenTextures();
        System.out.println("Loaded texture: "+id);
                
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 2048,2048, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        return id;
    }

    int vbo;
	private Chunk chunk;

	public void init() {
		try {
			program=createProgram();
			texture=createTexture();
		} catch (Throwable e) {
			throw new Error(e);
		}
		
		createModel();
 
        
		// Set the clear color
		glClearColor(0.2f, 0.7f, 0.85f, 0.0f);
		
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);

	}
	


	private void createModel() {
		AVector<ACell> chunkData;
		chunkData = engine.loadChunk(0,0,0);
		chunk = Chunk.create(chunkData);
		
		int stride=Chunk.FLOATS_PER_VERTEX*4;
		
		// define vertex format
		glVertexAttribPointer(vs_inputPosition,3,GL_FLOAT,false,stride,0L); // Note: stride in bytes
        glEnableVertexAttribArray(vs_inputPosition);
        
		glVertexAttribPointer(vs_texturePosition,2,GL_FLOAT,false,stride,12L); // Note: stride in bytes
        glEnableVertexAttribArray(vs_texturePosition);
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	Matrix4f mvp=new Matrix4f();
	Matrix4f view=new Matrix4f();
	Matrix4f model=new Matrix4f();
	Matrix4f projection=new Matrix4f();
	Vector3f playerDir=new Vector3f(0,0,0);
	Vector3f playerPos=new Vector3f(0,-3,2);
	Vector3f playerVelocity=new Vector3f(0,0,0);

	Vector3f tpos=new Vector3f(0,0,0);
	Vector3f up=new Vector3f(0,0,1);
	
	float QUARTER_TURN=(float) (Math.PI/2);

	protected int width;
	protected int height;
	
	public void render(float t) {
		// clear the framebuffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 

		//view.lookAt(0, -1, 0, 0, 0, 0, 0, 0, 1);
		//view.rotateX(pitch).rotateZ(heading);
		//view.setLookAt(0,0,0,0,1,0,0,0,1);
		//view.rotateX(pitch).rotateY(heading);
		
		//view.identity();
		//view.setLookAt(0,1,0,0,0,0,0,0,1);
		//view.rotationXYZ(-pitch, 0, -heading);
		//view.translate(0,-4,-1);
		//view.translate(playerPos);
		// view.invert(); // View = inv(T.R)
		view.identity();
		view.translate(playerPos);
		view.rotateZ(-heading);
		view.rotateX(QUARTER_TURN+pitch);
		view.invert();
		
		model.identity();
		model.rotateZ(t*0.003f);
		model.translate(tpos);
		
		projection.setPerspective((float) (Math.PI/3), width/height, 0.1f, 100f);
		
		mvp.identity();	
		mvp.mul(projection);
		mvp.mul(view);
		mvp.mul(model);
		
		mvp.get(0, matbuffer);
		
		glUniformMatrix4fv(0, false,  matbuffer);
	
		chunk.draw();
	}


	public void setSize(int width, int height) {
		this.width=width;
		this.height=height;
	}
	
	float heading=0;
	float pitch=0;

	public void onMouseMove(double dx, double dy) {
		// Called whenever the mouse moves
		heading=heading+(float)(dx*0.001);
		pitch=pitch-(float)(dy*0.002);
		
		if (pitch>QUARTER_TURN) pitch =QUARTER_TURN;
		if (pitch<-QUARTER_TURN) pitch =-QUARTER_TURN;
		
		System.out.println("Heading = "+heading+"    pitch = "+pitch + "   pos = "+playerPos);
	}

	public void applyMove(float backForward, float leftRight, float dt) {
		if (dt<0) throw new Error("Time going backwards! "+dt);
		
		playerDir.set(0,1,0);
		playerDir.rotateZ(-heading);
		playerDir.mul(backForward*dt*20f);
		playerVelocity.add(playerDir);
		
		playerDir.set(1,0,0);
		playerDir.rotateZ(-heading);
		playerDir.mul(leftRight*dt*20f);
		playerVelocity.add(playerDir);
		
		playerPos.fma(dt, playerVelocity);
		playerVelocity.mul((float)Math.exp(-dt*5));

	}

}
