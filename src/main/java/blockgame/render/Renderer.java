package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
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
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20C.glGetAttribLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.BufferUtils;

import blockgame.assets.Assets;
import blockgame.engine.Engine;
import blockgame.engine.Face;
import convex.core.data.ACell;
import convex.core.data.AVector;
import convex.core.data.prim.CVMLong;

public class Renderer {
	
	int program;
	int texture;
	static int vs_inputPosition;
	static int vs_texturePosition;
	static int vs_normalPosition;

	static int vs_PPosition;
	static int vs_MVPosition;

	
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
		vs_normalPosition = glGetAttribLocation(program, "normal");
		
		vs_PPosition = glGetUniformLocation(program, "P");
		vs_MVPosition = glGetUniformLocation(program, "MV");
		
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
		

	}
	


	private void createModel() {
		AVector<ACell> chunkData;
		chunkData = engine.getChunk(0,0,0);
		chunk = Chunk.create(chunkData);
		
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	Matrix4f mv=new Matrix4f();
	Matrix4f view=new Matrix4f();
	Matrix4f model=new Matrix4f();
	Matrix4f projection=new Matrix4f();
	Vector3f tempDir=new Vector3f(0,0,0);
	Vector3f playerPos=new Vector3f(0,-3,2);
	Vector3f playerVelocity=new Vector3f(0,0,0);
	
	Engine.HitResult hitResult=new Engine.HitResult();

	Vector3f tpos=new Vector3f(0,0,0);
	Vector3f up=new Vector3f(0,0,1);
	
	float QUARTER_TURN=(float) (Math.PI/2);

	protected int width;
	protected int height;
	
	public void render(float t) {
		// clear the framebuffer
		glDepthMask(true);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 

		drawChunks();
		drawHUD();
	}
	
	private final FloatBuffer matbufferP = BufferUtils.createFloatBuffer(16);
	private final FloatBuffer matbufferMV = BufferUtils.createFloatBuffer(16);
	
	private void drawChunks() {		
		projection.setPerspective((float) (Math.PI/3), width/height, 0.1f, 100f);
		
		// Projection Matrix
		projection.get(0, matbufferP);		
		glUniformMatrix4fv(Renderer.vs_PPosition, false,  matbufferP);
		
		view.identity();
		view.translate(playerPos);
		view.rotateZ(-heading);
		view.rotateX(QUARTER_TURN+pitch);
		view.invert();
		
		model.identity();
		model.translate(tpos);
	
		// ModelView Matrix
		mv.identity();	
		mv.mul(view);
		mv.mul(model);

		mv.get(0, matbufferMV);		
		glUniformMatrix4fv(Renderer.vs_MVPosition, false,  matbufferMV);
		
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		
		chunk.draw();
		
		// Clear Buffer
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	private void drawHUD() {
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		
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
		
		// System.out.println("Heading = "+heading+"    pitch = "+pitch + "   pos = "+playerPos);
	}

	public void applyMove(float backForward, float leftRight, float upDown, float dt) {
		if (dt<0) throw new Error("Time going backwards! "+dt);
		
		tempDir.set(0,1,0);
		tempDir.rotateZ(-heading);
		tempDir.mul(backForward*dt*20f);
		playerVelocity.add(tempDir);
		
		tempDir.set(1,0,0);
		tempDir.rotateZ(-heading);
		tempDir.mul(leftRight*dt*20f);
		playerVelocity.add(tempDir);
		
		tempDir.set(0,0,1);
		tempDir.mul(upDown*dt*20f);
		playerVelocity.add(tempDir);
		
		playerPos.fma(dt, playerVelocity);
		playerVelocity.mul((float)Math.exp(-dt*5));

	}

	public void applyRightClick() {
		tempDir.set(0,0,-1);
		tempDir.rotateX((QUARTER_TURN+pitch));
		tempDir.rotateZ(-heading);
		Engine.intersect(playerPos, tempDir, (x,y,z)->engine.getBlock(x,y,z), hitResult);
		
		if (hitResult.hit==null) {
			System.out.println("Mouse RIGHT clicked in empty space pos=" +playerPos+ " dir="+tempDir);
		} else {
			Vector3i target=new Vector3i(hitResult.x,hitResult.y, hitResult.z);
			target.add(Face.DIR[hitResult.face]); // block on face
			
			ACell block=CVMLong.create(1);
			
			engine.setBlock(target,block);
			AVector<ACell> chunkData=engine.getChunk(target);
			System.out.println("Block placed at "+target);
			chunk.refresh(chunkData);
		}
	}

	public void applyLeftClick() {
		tempDir.set(0,0,-1);
		tempDir.rotateX((QUARTER_TURN+pitch));
		tempDir.rotateZ(-heading);
		Engine.intersect(playerPos, tempDir, (x,y,z)->engine.getBlock(x,y,z), hitResult);
		
		if (hitResult.hit==null) {
			System.out.println("Mouse LEFT clicked in empty space pos=" +playerPos+ " dir="+tempDir);
		} else {
			Vector3i target=new Vector3i(hitResult.x,hitResult.y, hitResult.z);
			
			engine.setBlock(target,null);
			AVector<ACell> chunkData=engine.getChunk(target);
			System.out.println("Block deleted at "+target);
			chunk.refresh(chunkData);
		}
	}

}
