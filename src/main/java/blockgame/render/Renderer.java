package blockgame.render;
 
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glViewport;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import blockgame.Config;
import blockgame.Util;
import blockgame.engine.Engine;
import blockgame.engine.Face;
import convex.core.data.ACell;

public class Renderer {
	
	public Engine engine;
	private HUD hud;
	Chunks chunks=Chunks.create(this);

	protected int width;
	protected int height;


	public void init() {
		try {
			engine=Config.getEngine();
			engine.onChunkRefresh=ca->{
				
			};
			hud=new HUD(this);
			
			Chunk.init();
			chunks.init();
			HUD.init(engine);
			Skybox.init();
			Text.init();
		} catch (Throwable e) {
			throw new Error(e);
		}

		// Set the clear color
		glClearColor(0.2f, 0.7f, 0.85f, 0.0f);
	}

	public void close() {
		chunks.close();
	}

	Vector3f tempDir=new Vector3f(0,0,0);
	Vector3f playerPos=new Vector3f(8,-4,6);
	Vector3f playerVelocity=new Vector3f(0,0,0);
	
	Engine.HitResult hitResult=new Engine.HitResult();


	Vector3f up=new Vector3f(0,0,1);
	

	
	public void render(float t) {
		// clear the framebuffer
		glDepthMask(true);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
		glViewport(0,0,width,height);
		GL20.glFrontFace(GL20.GL_CW);
		
		Skybox.draw(width,height,heading,pitch);

		chunks.drawChunks();
		chunks.drawEntities();
		drawHUD();
		
		updateStats();
	}
	
	static long frames=0;
	static double fps=0;
	static long lastTime=System.currentTimeMillis();
	private void updateStats() {
		long time=System.currentTimeMillis();
		long elapsed=time-lastTime;
		frames++;
		if (elapsed>0) {
			fps=fps*0.9+0.1*(1000.0/elapsed); 
		} else {
			fps+=1.0;
		}
		lastTime=time;
	}

	public void setupPerspective(Matrix4f projectionMatrix) {
		projectionMatrix.setPerspective((float) (Math.PI/4), ((float)width)/height, 0.1f, 1000f);
	}
	
	private void drawHUD() {	
		hud.draw(width,height);
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
		
		if (pitch>Util.QUARTER_TURN) pitch =Util.QUARTER_TURN;
		if (pitch<-Util.QUARTER_TURN) pitch =-Util.QUARTER_TURN;
		
		// System.out.println("Heading = "+heading+"    pitch = "+pitch + "   pos = "+playerPos);
	}

	public void applyMove(float backForward, float leftRight, float upDown, float dt) {
		if (dt<0) throw new Error("Time going backwards! "+dt);
		if (dt>0.1f) dt=0.1f; // max 100ms time step
		
		float SPEED=50f;
		
		tempDir.set(0,1,0);
		tempDir.rotateZ(-heading);
		tempDir.mul(backForward*dt*SPEED);
		playerVelocity.add(tempDir);
		
		tempDir.set(1,0,0);
		tempDir.rotateZ(-heading);
		tempDir.mul(leftRight*dt*SPEED);
		playerVelocity.add(tempDir);
		
		tempDir.set(0,0,1);
		tempDir.mul(upDown*dt*SPEED);
		playerVelocity.add(tempDir);
		
		playerPos.fma(dt, playerVelocity);
		playerVelocity.mul((float)Math.exp(-dt*5));

	}

	public void applyRightClick() {
		tempDir.set(0,0,-1);
		tempDir.rotateX((Util.QUARTER_TURN+pitch));
		tempDir.rotateZ(-heading);
		Engine.intersect(playerPos, tempDir, (x,y,z)->engine.getBlock(x,y,z), hitResult);
		
		if (hitResult.hit==null) {
			System.out.println("Mouse RIGHT clicked in empty space pos=" +playerPos+ " dir="+tempDir);
		} else {
			int x=hitResult.x;
			int y=hitResult.y;
			int z=hitResult.z;
			Vector3i target=new Vector3i(x,y,z);
			target.add(Face.DIR[hitResult.face]); // block on face
			
			ACell block=engine.getPlaceableBlock();
			if (block==null) {
				return;
			}
			engine.setBlock(target,block);
		}
	}

	public void applyLeftClick() {
		tempDir.set(0,0,-1);
		tempDir.rotateX((Util.QUARTER_TURN+pitch));
		tempDir.rotateZ(-heading);
		Engine.intersect(playerPos, tempDir, (x,y,z)->engine.getBlock(x,y,z), hitResult);
		
		if (hitResult.hit==null) {
			System.out.println("Mouse LEFT clicked in empty space pos=" +playerPos+ " dir="+tempDir);
		} else {
			int x=hitResult.x;
			int y=hitResult.y;
			int z=hitResult.z;
			Vector3i target=new Vector3i(x,y,z);
			engine.setBlock(target,null);
			maybeRebuildChunk(x,y,z);
			System.out.println("Block deleted at "+target+ " in chunk "+engine.chunkAddress(x,y,z));
		}
	}

	private void maybeRebuildChunk(int x, int y, int z) {
		int bx=x&0xf;
		if (bx==0) chunks.rebuildChunk(x-1,y,z);
		if (bx==15) chunks.rebuildChunk(x+1,y,z);
		int by=y&0xf;
		if (by==0) chunks.rebuildChunk(x,y-1,z);
		if (by==15) chunks.rebuildChunk(x,y+1,z);
		int bz=z&0xf;
		if (bz==0) chunks.rebuildChunk(x,y,z-1);
		if (bz==15) chunks.rebuildChunk(x,y,z+1);
	}



	public void applyTool(int i) {
		engine.setToolIndex(i);
	}

	public void applyKeyPress(int key) {
		switch (key) {
			case GLFW.GLFW_KEY_SLASH: {
				Chunk.toggleSlice((int)playerPos.z-2);
				break;
			}
		 	case GLFW.GLFW_KEY_PERIOD: {
		 		Chunk.adjustSlice(-1);
		 		break;
			}
		 	case GLFW.GLFW_KEY_COMMA: {
		 		Chunk.adjustSlice(1);
		 		break;
			}
		}
	}

}
