package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import blockgame.assets.Assets;
import blockgame.engine.Face;

public class Skybox {

	private static int skyboxVBO;
	
	static Texture texture;

	public static final int FLOATS_PER_VERTEX=3+2; // position + texture
	private static int triangleCount;
	
	public static void init() {
		texture=Texture.createTexture(Assets.skybox);
		skyboxVBO=createVBO();
	}
	
	private static final Matrix4f trans=new Matrix4f();
	private static final Matrix4f view=new Matrix4f();
	private static final FloatBuffer matbufferMV = BufferUtils.createFloatBuffer(16);


	public static void draw(int width, int height, float heading, float pitch) {
		glUseProgram(HUD.hudProgram);
		texture.bind();
		
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		
		view.identity();
		view.rotateZ(-heading);
		view.rotateX(Renderer.QUARTER_TURN+pitch);
		view.invert();
		
		trans.setPerspective((float) (Math.PI/3), width/height, 0.1f, 100f);
		
		trans.mul(view); // multiple with view
		
		trans.get(0, matbufferMV);		

		
		glUniformMatrix4fv(HUD.h_vs_MVPPosition, false,  matbufferMV);
		GL20.glUniform4f(HUD.h_vs_ColourPosition, 1, 1, 1,1);

		glBindBuffer(GL_ARRAY_BUFFER, skyboxVBO);

		// define vertex format, must be after glBindBuffer
		int stride=FLOATS_PER_VERTEX*4;
		// position is location 0
		glVertexAttribPointer(HUD.h_vs_inputPosition,3,GL_FLOAT,false,stride,0L); // Note: stride in bytes
        glEnableVertexAttribArray(HUD.h_vs_inputPosition);
        
        // texture is location 1
        glVertexAttribPointer(HUD.h_vs_texturePosition,2,GL_FLOAT,false,stride,12L); // Note: stride in bytes
        glEnableVertexAttribArray(HUD.h_vs_texturePosition);
		
		glDrawArrays(GL_TRIANGLES, 0, triangleCount*3);
	}

	private static int createVBO() {
		// Geometry in current context
		FloatBuffer built = buildAll();
		
		int n=built.remaining();
		triangleCount=n/(3*FLOATS_PER_VERTEX);
		
		FloatBuffer vertexBuffer = memAllocFloat(n);
		vertexBuffer.put(built);
		vertexBuffer.flip();

		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		
		memFree(vertexBuffer);
		
		// System.out.println("VBO built! "+vbo);
		return vbo;
	}
	
	private static FloatBuffer buildAll() {
		FloatBuffer vb = FloatBuffer.allocate(6*6*FLOATS_PER_VERTEX);
		addFace(vb,Face.U,0.25f,0.0f);
		addFace(vb,Face.N,0.75f,0.25f);
		addFace(vb,Face.E,0.50f,0.25f);
		addFace(vb,Face.S,0.25f,0.25f);
		addFace(vb,Face.W,0.00f,0.25f);
		addFace(vb,Face.D,0.25f,0.5f);
		
		vb.flip();
		return vb;
	}
	
	// North = +y East = +x
	// x,y,z offsets
	static float[][] VERTS= {{-1,-1,-1},{-1,-1,1},{-1,1,-1},{-1,1,1},{1,-1,-1},{1,-1,1},{1,1,-1},{1,1,1}};
	// U, N, E, S, W, D
	static int[][] FACES= {{7,3,1,5},{3,7,6,2},{1,3,2,0},{5,1,0,4},{7,5,4,6},{2,6,4,0}};

	
	private static void addFace(FloatBuffer fb, int face, float tx, float ty) {
		float TD=0.25f;
		int[] FACE=FACES[face];
		// VErtices of face specified clockwise
		float[] v0=VERTS[FACE[0]]; // top left
		float[] v1=VERTS[FACE[1]]; // top right
		float[] v2=VERTS[FACE[2]]; // bottom right
		float[] v3=VERTS[FACE[3]]; // bottom left
		
		// Vertices in square numbered clockwise 0,1,2,3
		// 0,1,3
		fb.put(v0[0]).put(v0[1]).put(v0[2]).put(tx).put(ty);
		fb.put(v1[0]).put(v1[1]).put(v1[2]).put(tx+TD).put(ty);
		fb.put(v3[0]).put(v3[1]).put(v3[2]).put(tx).put(ty+TD);
		// 3,1,2
		fb.put(v3[0]).put(v3[1]).put(v3[2]).put(tx).put(ty+TD);
		fb.put(v1[0]).put(v1[1]).put(v1[2]).put(tx+TD).put(ty);
		fb.put(v2[0]).put(v2[1]).put(v2[2]).put(tx+TD).put(ty+TD);


	}
}
