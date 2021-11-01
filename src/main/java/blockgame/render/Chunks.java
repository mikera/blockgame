package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL20.glUniform3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import blockgame.Util;

public class Chunks {
	
	private Renderer r;
	private Billboard billboard=new Billboard();

	private Chunks(Renderer r) {
		this.r=r;
		
	}

	
	public static Chunks create(Renderer renderer) {
		return new Chunks(renderer);
	}
	
	public void init() throws IOException {
		billboard.init();
	}
	
	/**
	 * Chunks indexed by base chunk co-ordinate
	 */
	private HashMap<Vector3i,Chunk> chunks=new HashMap<>(100);

	Matrix4f mv=new Matrix4f();
	Matrix4f view=new Matrix4f();
	Matrix4f model=new Matrix4f();
	Matrix4f projection=new Matrix4f();
	Vector4f vLightDir=new Vector4f(); // light dir in MV space

	
	private final FloatBuffer matbufferP = BufferUtils.createFloatBuffer(16);
	private final FloatBuffer matbufferMV = BufferUtils.createFloatBuffer(16);
	private final FloatBuffer matbufferVLightDir = BufferUtils.createFloatBuffer(4);

	
	public void drawChunks() {			
		Chunk.prepareState();
		Vector3f playerPos=r.playerPos;

		// General set up for projection and view matrices
		r.setupPerspective(projection);
		
		// Projection Matrix
		projection.get(0, matbufferP);		
		glUniformMatrix4fv(Chunk.c_vs_PPosition, false,  matbufferP);
		
		view.identity();
		view.translate(playerPos);
		view.rotateZ(-r.heading);
		view.rotateX(Util.QUARTER_TURN+r.pitch);
		view.invert();
		
		// Light direction
		vLightDir.set(-2,-1,4,0);
		vLightDir.mul(view);
		vLightDir.normalize();
		vLightDir.get(0, matbufferVLightDir);		
		glUniform3fv(Chunk.c_fs_LightDirPosition, matbufferVLightDir);

		
		
		// Player chunk position
		int plx=Util.chunkBase(playerPos.x);
		int ply=Util.chunkBase(playerPos.y);
		int plz=Util.chunkBase(playerPos.z);
		
		int DIST=160;
		for (int cx=plx-DIST; cx<=plx+DIST; cx+=16) {
			for (int cy=ply-DIST; cy<=ply+DIST; cy+=16) {
				for (int cz=plz-DIST; cz<=plz+DIST; cz+=16) {
					double dist=Util.dist(cx+8,cy+8,cz+8,playerPos.x,playerPos.y,playerPos.z);
					if (dist>=(DIST-16)) {
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
	
	public void drawEntities() {
		glUseProgram(Billboard.getProgram());
		
		glDisable(GL_CULL_FACE); // Billboards don't want this
		glEnable(GL_DEPTH_TEST); // Still do depth test
		glEnable(GL_BLEND); // We want alpha blending

		
		r.setupPerspective(projection);
		
		// Projection Matrix
		projection.get(0, matbufferP);		
		glUniformMatrix4fv(Chunk.c_vs_PPosition, false,  matbufferP);

		view.identity();
		view.translate(r.playerPos);
		view.rotateZ(-r.heading);
		view.rotateX(Util.QUARTER_TURN+r.pitch);
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
	
	private Chunk getChunk(int cx, int cy, int cz) {
		Vector3i cpos=new Vector3i(cx,cy,cz);
		Chunk chunk=chunks.get(cpos);
		if (chunk==null) {
			chunk=Chunk.create(cpos, r.engine);
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
	
	public void rebuildChunk(int x, int y, int z) {
		// round to chunk base co-ordinate
		x&=~0xf;
		y&=~0xf;
		z&=~0xf;
		Vector3i cpos=new Vector3i(x,y,z);
		Chunk chunk=chunks.get(cpos);
		if (chunk!=null) chunk.rebuild();
	}
	
	public void close() {
		// TODO Auto-generated method stub
		for (Chunk c:chunks.values()) {
			c.dispose();
		}
	}


	

}
