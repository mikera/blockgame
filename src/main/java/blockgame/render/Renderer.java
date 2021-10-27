package blockgame.render;
 
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glUniform3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.FloatBuffer;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
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
	private Billboard billboard=new Billboard();

	protected int width;
	protected int height;


	public void init() {
		try {
			engine=Config.getEngine();
			engine.onChunkRefresh=ca->{
				
			};
			hud=new HUD(engine);
			
			Chunk.init();
			HUD.init(engine);
			Skybox.init();
			billboard.init();
			Text.init();
		} catch (Throwable e) {
			throw new Error(e);
		}

		// Set the clear color
		glClearColor(0.2f, 0.7f, 0.85f, 0.0f);
	}
	
	/**
	 * Chunks indexed by base chunk co-ordinate
	 */
	private HashMap<Vector3i,Chunk> chunks=new HashMap<>(100);


	private Chunk getChunk(int cx, int cy, int cz) {
		Vector3i cpos=new Vector3i(cx,cy,cz);
		Chunk chunk=chunks.get(cpos);
		if (chunk==null) {
			chunk=Chunk.create(cpos, engine);
			if (chunk!=null) {
				chunks.put(cpos, chunk);
			}
		}
		return chunk;
	}
	
	private void setChunk(int cx, int cy, int cz, Chunk chunk) {
		Vector3i cpos=new Vector3i(cx,cy,cz);
		if (chunk==null) {
			Chunk prev=chunks.get(cpos);
			if (prev!=null) {
				prev.dispose();
			}
			chunks.remove(cpos);
		} else {
			chunks.put(cpos, chunk);
		}
	}

	public void close() {
		// TODO Auto-generated method stub
		for (Chunk c:chunks.values()) {
			c.dispose();
		}
	}

	Vector3f tempDir=new Vector3f(0,0,0);
	Vector3f playerPos=new Vector3f(8,-4,6);
	Vector3f playerVelocity=new Vector3f(0,0,0);
	
	Engine.HitResult hitResult=new Engine.HitResult();


	Vector3f up=new Vector3f(0,0,1);
	
	static float QUARTER_TURN=(float) (Math.PI/2);

	
	public void render(float t) {
		// clear the framebuffer
		glDepthMask(true);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
		glViewport(0,0,width,height);
		GL20.glFrontFace(GL20.GL_CW);
		
		Skybox.draw(width,height,heading,pitch);

		drawChunks();
		drawEntities();
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

	private void drawEntities() {
		glUseProgram(Billboard.getProgram());
		
		glDisable(GL_CULL_FACE); // Billboards don't want this
		glEnable(GL_DEPTH_TEST); // Still do depth test
		glEnable(GL_BLEND); // We want alpha blending

		
		setupPerspective(projection);
		
		// Projection Matrix
		projection.get(0, matbufferP);		
		glUniformMatrix4fv(Chunk.c_vs_PPosition, false,  matbufferP);

		view.identity();
		view.translate(playerPos);
		view.rotateZ(-heading);
		view.rotateX(QUARTER_TURN+pitch);
		view.invert();
		
		model.identity();
		model.translate(0,0,5);

		// ModelView Matrix
		mv.set(view);
		mv.mul(model);

		mv.get(0, matbufferMV);		
		glUniformMatrix4fv(Chunk.c_vs_MVPosition, false,  matbufferMV);
		
		billboard.draw();

	}

	Matrix4f mv=new Matrix4f();
	Matrix4f view=new Matrix4f();
	Matrix4f model=new Matrix4f();
	Matrix4f projection=new Matrix4f();
	Vector4f vLightDir=new Vector4f(); // light dir in MV space

	
	private final FloatBuffer matbufferP = BufferUtils.createFloatBuffer(16);
	private final FloatBuffer matbufferMV = BufferUtils.createFloatBuffer(16);
	private final FloatBuffer matbufferVLightDir = BufferUtils.createFloatBuffer(4);
	
	private void drawChunks() {			
		Chunk.prepareState();

		// General set up for projection and view matrices
		setupPerspective(projection);
		
		// Projection Matrix
		projection.get(0, matbufferP);		
		glUniformMatrix4fv(Chunk.c_vs_PPosition, false,  matbufferP);
		
		view.identity();
		view.translate(playerPos);
		view.rotateZ(-heading);
		view.rotateX(QUARTER_TURN+pitch);
		view.invert();
		
		// Light direction
		vLightDir.set(-2,-1,4,0);
		vLightDir.mul(view);
		vLightDir.normalize();
		vLightDir.get(0, matbufferVLightDir);		
		glUniform3fv(Chunk.c_fs_LightDirPosition, matbufferVLightDir);

		// Player chunk position
		int plx=((int)Math.floor(playerPos.x))&~0xf;
		int ply=((int)Math.floor(playerPos.y))&~0xf;
		int plz=((int)Math.floor(playerPos.z))&~0xf;
		
		int DIST=160;
		for (int cx=plx-DIST; cx<=plx+DIST; cx+=16) {
			for (int cy=ply-DIST; cy<=ply+DIST; cy+=16) {
				for (int cz=plz-DIST; cz<=plz+DIST; cz+=16) {
					double dist=Util.dist(cx+8,cy+8,cz+8,playerPos.x,playerPos.y,playerPos.z);
					if (dist>(DIST-16)) {
						setChunk(cx,cy,cz,null);
						continue;
					}
					Chunk chunk=getChunk(cx,cy,cz);
					if (chunk!=null) {
			
						model.identity();
						model.translate(cx,cy,cz);
					
						// ModelView Matrix
						mv.set(view);
						mv.mul(model);
				
						mv.get(0, matbufferMV);		
						glUniformMatrix4fv(Chunk.c_vs_MVPosition, false,  matbufferMV);
						
						chunk.draw();
					}
				}
			}
		}
	}


	private void setupPerspective(Matrix4f projectionMatrix) {
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
		
		if (pitch>QUARTER_TURN) pitch =QUARTER_TURN;
		if (pitch<-QUARTER_TURN) pitch =-QUARTER_TURN;
		
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
		tempDir.rotateX((QUARTER_TURN+pitch));
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
		tempDir.rotateX((QUARTER_TURN+pitch));
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
		if (bx==0) rebuildChunk(x-1,y,z);
		if (bx==15) rebuildChunk(x+1,y,z);
		int by=y&0xf;
		if (by==0) rebuildChunk(x,y-1,z);
		if (by==15) rebuildChunk(x,y+1,z);
		int bz=z&0xf;
		if (bz==0) rebuildChunk(x,y,z-1);
		if (bz==15) rebuildChunk(x,y,z+1);

	}

	private void rebuildChunk(int x, int y, int z) {
		// round to chunk base co-ordinate
		x&=~0xf;
		y&=~0xf;
		z&=~0xf;
		Chunk chunk=getChunk(x,y,z);
		if (chunk!=null) chunk.rebuild();
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
