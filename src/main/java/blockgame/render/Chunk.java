package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
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

import org.bouncycastle.util.Arrays;
import org.joml.Vector3f;
import org.joml.Vector3i;

import blockgame.assets.Assets;
import blockgame.engine.Engine;
import blockgame.engine.Face;
import blockgame.engine.Lib;
import blockgame.engine.Rand;
import convex.core.data.ABlob;
import convex.core.data.ACell;
import convex.core.data.AHashMap;
import convex.core.data.AVector;
import convex.core.data.Keyword;
import convex.core.data.prim.CVMLong;

public class Chunk {
	int vbo = 0;
	int triangleCount = 0;
	
	// Flags for control
	private boolean rebuilding=false;


	// int[] vals=new int[4096];
	private AVector<ACell> chunkData;
	
	private final Engine engine;
	private final Vector3i position;

	static Texture texture;

	private Chunk(Vector3i cpos, Engine engine) {
		this.position = cpos;
		this.engine = engine;
	}

	public static Chunk create(Vector3i cpos, Engine engine) {
		Chunk c = new Chunk(cpos, engine);
		AVector<ACell> current = engine.getChunk(cpos);
		c.setData(current);
		c.createVBO();
		return c;
	}

	static int chunkProgram;
	static int c_fs_LightDirPosition;
	static int c_vs_MVPosition;
	static int c_vs_PPosition;
	
	static int c_vs_inputPosition;
	static int c_vs_normalPosition;
	static int c_vs_texturePosition;
	static int c_vs_colourPosition;

	private static int createProgram() throws IOException {
		int program = glCreateProgram();
		int vshader = Utils.createShader("shaders/chunk-shader.vert", GL_VERTEX_SHADER);
		int fshader = Utils.createShader("shaders/chunk-shader.frag", GL_FRAGMENT_SHADER);
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
		c_vs_inputPosition = glGetAttribLocation(program, "position");
		c_vs_texturePosition = glGetAttribLocation(program, "texture");
		c_vs_normalPosition = glGetAttribLocation(program, "normal");
		c_vs_colourPosition = glGetAttribLocation(program, "vertex_colour");

		c_vs_PPosition = glGetUniformLocation(program, "P");
		c_vs_MVPosition = glGetUniformLocation(program, "MV");

		c_fs_LightDirPosition = glGetUniformLocation(program, "vLightDir");

		return program;
		// TODO: do we need to dispose the program somehow?
	}

	public static void init() throws IOException {
		chunkProgram = createProgram();
		texture = Texture.createTexture(Assets.textureImage);
	}

	private void setData(AVector<ACell> chunkData) {
		this.chunkData = chunkData;
	}

	public void refresh() {
		AVector<ACell> latest = engine.getChunk(position);
		if (rebuilding||(chunkData != latest)) {
			rebuilding=false;
			setData(latest);
			glDeleteBuffers(vbo);
			vbo = createVBO();
		}
	}
	
	public void rebuild() {
		rebuilding=true;
	}

	public static final int FLOATS_PER_VERTEX = 3 + 3 + 2 + 3; // position + normal + texture + colour
	public static final int VERTICES_PER_FACE = 6; // 2 triangles, 3 vertices each

	private int createVBO() {
		// Geometry in current context
		FloatBuffer built = buildAll();

		int n = built.remaining();
		triangleCount = n / (3 * FLOATS_PER_VERTEX);
		if (triangleCount > 0) {

			FloatBuffer vertexBuffer = memAllocFloat(n);
			vertexBuffer.put(built);
			vertexBuffer.flip();

			vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

			memFree(vertexBuffer);
		} else {
			vbo = 0;
		}

		// System.out.println("VBO built! "+vbo);
		return vbo;
	}

	Buildable geom = Buildable.create(FLOATS_PER_VERTEX);

	private FloatBuffer buildAll() {
		geom.clear();
		if (!chunkData.equals(Engine.EMPTY_CHUNK)) {
			for (int k = 0; k < 16; k++) {
				for (int j = 0; j < 16; j++) {
					for (int i = 0; i < 16; i++) {
						long ix = i + j * 16 + k * 256;
						ACell block = chunkData.get(ix);
						if (block != null) {
							addBlock(geom, i, j, k, block);
						}
					}
				}
			}
		}
		return geom.getFlippedBuffer();
	}

	private void addBlock(Buildable geom, int x, int y, int z, ACell type) {
		// Destructure compound block
		if (type instanceof AVector) {
			AVector<?> v = (AVector<?>) type;
			if (v.count() > 0) {
				type = v.get(0);
			}
		}
		AHashMap<Keyword, ACell> meta = (AHashMap<Keyword, ACell>) Lib.blockData.get(type);
		if (meta == null)
			meta = (AHashMap<Keyword, ACell>) Lib.blockData.get(Lib.GRASS);
		ACell model=meta.get(Lib.KEY_MODEL);
		if (model==null) {
			addStandardBlock(geom,x,y,z,meta);
		} else {
			int mod=(int)((CVMLong)model).longValue();
			switch (mod) {
			case 1:
				addCrossBillboard(geom,x,y,z,meta);
				break;
			default: 
				addStandardBlock(geom,x,y,z,meta);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addCrossBillboard(Buildable geom, int x, int y, int z, AHashMap<Keyword, ACell> meta) {
		AVector<ABlob> tex = (AVector<ABlob>) meta.get(Lib.KEY_TEX);

		Utils.fill(a0,1.0f);
		addBillboardPlane(1,7,6,0,x,y,z,tex.get(0));
		addBillboardPlane(3,5,4,2,x,y,z,tex.get(0));
	}
	
	float[] bnormal=new float[3];
	Vector3f bnm=new Vector3f();
	Vector3f bnm2=new Vector3f();
	private void addBillboardPlane(int i0, int i1, int i2, int i3, int x, int y, int z, ABlob tex) {
		long texRef=tex.toLong();
		float tdelta=Texture.TD*0.02f;
		float tx=Texture.tx(texRef)+tdelta;
		float ty=Texture.ty(texRef)+tdelta;
		float TD=Texture.TD-(tdelta*2);

		float[] v0=VERTS[i0];
		float[] v1=VERTS[i1];
		float[] v2=VERTS[i2];
		float[] v3=VERTS[i3];
		computeNormal(v0,v1,v2);
		geom.put(v0[0] + x,v0[1] + y,v0[2] + z).put(bnormal).put(tx,ty).put(a0);
		geom.put(v1[0] + x,v1[1] + y,v1[2] + z).put(bnormal).put(tx+TD,ty).put(a0);
		geom.put(v2[0] + x,v2[1] + y,v2[2] + z).put(bnormal).put(tx + TD,ty + TD).put(a0);
		geom.put(v2[0] + x,v2[1] + y,v2[2] + z).put(bnormal).put(tx + TD,ty + TD).put(a0);
		geom.put(v3[0] + x,v3[1] + y,v3[2] + z).put(bnormal).put(tx,ty+TD).put(a0);
		geom.put(v0[0] + x,v0[1] + y,v0[2] + z).put(bnormal).put(tx,ty).put(a0);
		Utils.negate(bnormal);
		geom.put(v0[0] + x,v0[1] + y,v0[2] + z).put(bnormal).put(tx,ty).put(a0);
		geom.put(v2[0] + x,v2[1] + y,v2[2] + z).put(bnormal).put(tx + TD,ty + TD).put(a0);
		geom.put(v1[0] + x,v1[1] + y,v1[2] + z).put(bnormal).put(tx+TD,ty).put(a0);
		geom.put(v2[0] + x,v2[1] + y,v2[2] + z).put(bnormal).put(tx + TD,ty + TD).put(a0);
		geom.put(v0[0] + x,v0[1] + y,v0[2] + z).put(bnormal).put(tx,ty).put(a0);
		geom.put(v3[0] + x,v3[1] + y,v3[2] + z).put(bnormal).put(tx,ty+TD).put(a0);

	}

	private void computeNormal(float[] v0, float[] v1, float[] v2) {
		bnm.set(v1);
		bnm.sub(v0[0],v0[1],v0[2]);
		bnm2.set(v2);
		bnm2.sub(v0[0],v0[1],v0[2]);
		bnm.cross(bnm2);
		bnm.normalize();
		bnormal[0]=bnm.x;
		bnormal[1]=bnm.y;
		bnormal[2]=bnm.z;
		
	}

	@SuppressWarnings("unchecked")
	private void addStandardBlock(Buildable geom, int x, int y, int z, AHashMap<Keyword, ACell> meta) {
		AVector<ABlob> tex = (AVector<ABlob>) meta.get(Lib.KEY_TEX);

		for (int face = 0; face < 6; face++) {
			Vector3i fd = Face.DIR[face];

			// Why does x need - ?? Error in lookup tables?
			int ft = transparency(x + fd.x + position.x, y + fd.y + position.y, z + fd.z + position.z);
			if (ft==0) {
				continue;
			}
			addFace(geom, x, y, z, face, tex.get(face).toLong());
		}
	}

	// North = +y East = +x
	// x,y,z offsets
	private static final float[][] VERTS = { { 0, 0, 0 }, { 0, 0, 1 }, { 0, 1, 0 }, { 0, 1, 1 }, { 1, 0, 0 }, { 1, 0, 1 }, { 1, 1, 0 },
			{ 1, 1, 1 } };
	// U, N, E, S, W, D
	private static final int[][] FACES = { { 3,7,5,1 }, { 7,3,2,6 }, { 5,7,6,4 }, { 1,5,4,0 }, { 3,1,0,2 }, { 6,2,0,4 } };

	// ambient lighting for the 4 vertices of a visible block face
	private final float [] a0=new float[3];
	private final float [] a1=new float[3];
	private final float [] a2=new float[3];
	private final float [] a3=new float[3];
	
	private void computeAmbients(int bx, int by, int bz, int face) {
		int[] FACE = FACES[face];
		// Vertices of face specified clockwise
		float[] v0 = VERTS[FACE[0]]; // top left
		float[] v1 = VERTS[FACE[1]]; // top right
		float[] v3 = VERTS[FACE[3]]; // bottom left
		Vector3i DIR = Face.DIR[face];
		
		// Set bx,by,bz to world position immediately facing location
		bx+=DIR.x+position.x;
		by+=DIR.y+position.y;
		bz+=DIR.z+position.z;
		
		// world direction for checks to left of face (v0 - v1)
		int dx=Math.round(v0[0]-v1[0]);
		int dy=Math.round(v0[1]-v1[1]);
		int dz=Math.round(v0[2]-v1[2]);
				
		// world direction for checks to top of face (v0 - v3)
		int ex=Math.round(v0[0]-v3[0]);
		int ey=Math.round(v0[1]-v3[1]);
		int ez=Math.round(v0[2]-v3[2]);
			
		int left=transparency(bx+dx,by+dy,bz+dz);
		int right=transparency(bx-dx,by-dy,bz-dz);
		int top=transparency(bx+ex,by+ey,bz+ez);
		int bot=transparency(bx-ex,by-ey,bz-ez);
		
		computeAmbient(a0,left+top+((left+top)==0?0:transparency(bx+dx+ex,by+dy+ey,bz+dz+ez)));	
		computeAmbient(a1,right+top+((right+top)==0?0:transparency(bx-dx+ex,by-dy+ey,bz-dz+ez)));	
		computeAmbient(a2,right+bot+((right+bot)==0?0:transparency(bx-dx-ex,by-dy-ey,bz-dz-ez)));	
		computeAmbient(a3,left+bot+((left+bot)==0?0:transparency(bx+dx-ex,by+dy-ey,bz+dz-ez)));	

		float diff=0.15f;
		float dr=(0.5f+Rand.rint(100, bx, by, bz)/100.f)*diff;
		float dg=(0.5f+Rand.rint(100, bx, by, bz)/100.f)*diff;
		float db=(0.5f+Rand.rint(100, bx, by, bz)/100.f)*diff;
		a0[0]+=dr; a0[1]+=dg; a0[2]+=db;
		a1[0]+=dr; a1[1]+=dg; a1[2]+=db;
		a2[0]+=dr; a2[1]+=dg; a2[2]+=db;
		a3[0]+=dr; a3[1]+=dg; a3[2]+=db;
	}

	private int transparency(int x, int y, int z) {
		ACell b=engine.getBlock(x, y, z);
		return Lib.isTransparent(b)?1:0;
	}

	private void computeAmbient(float[] as, int x) {
		float light=(x+1)*0.25f;
		as[0]=light;
		as[1]=light;
		as[2]=light;
	}

	public void addFace(Buildable geom, int bx, int by, int bz, int face, long texRef) {
		int[] FACE = FACES[face];
		// Vertices of face specified clockwise
		float[] v0 = VERTS[FACE[0]]; // top left
		float[] v1 = VERTS[FACE[1]]; // top right
		float[] v2 = VERTS[FACE[2]]; // bottom right
		float[] v3 = VERTS[FACE[3]]; // bottom left

		float tdelta=Texture.TD*0.02f;
		float tx=Texture.tx(texRef)+tdelta;
		float ty=Texture.ty(texRef)+tdelta;
		float TD=Texture.TD-(tdelta*2);

		float[] normal = Face.NORMAL[face];
		
		computeAmbients(bx,by,bz,face);

		// Vertices in square numbered clockwise 0,1,2,3
		// Build geometry as triangles chosen to round off shadows
		if (a0[1]+a2[1]>=a1[1]+a3[1]) {
			// 0,1,2
			geom.put(v0[0] + bx,v0[1] + by,v0[2] + bz).put(normal).put(tx,ty).put(a0);
			geom.put(v1[0] + bx,v1[1] + by,v1[2] + bz).put(normal).put(tx + TD,ty).put(a1);
			geom.put(v2[0] + bx,v2[1] + by,v2[2] + bz).put(normal).put(tx + TD,ty + TD).put(a2);
			// 0,2,3
			geom.put(v0[0] + bx,v0[1] + by,v0[2] + bz).put(normal).put(tx,ty).put(a0);
			geom.put(v2[0] + bx,v2[1] + by,v2[2] + bz).put(normal).put(tx + TD,ty + TD).put(a2);
			geom.put(v3[0] + bx,v3[1] + by,v3[2] + bz).put(normal).put(tx,ty + TD).put(a3);
			
		} else {
			// 0,1,3
			geom.put(v0[0] + bx,v0[1] + by,v0[2] + bz).put(normal).put(tx,ty).put(a0);
			geom.put(v1[0] + bx,v1[1] + by,v1[2] + bz).put(normal).put(tx + TD,ty).put(a1);
			geom.put(v3[0] + bx,v3[1] + by,v3[2] + bz).put(normal).put(tx,ty + TD).put(a3);
			// 3,1,2
			geom.put(v3[0] + bx,v3[1] + by,v3[2] + bz).put(normal).put(tx,ty + TD).put(a3);
			geom.put(v1[0] + bx,v1[1] + by,v1[2] + bz).put(normal).put(tx + TD,ty).put(a1);
			geom.put(v2[0] + bx,v2[1] + by,v2[2] + bz).put(normal).put(tx + TD,ty + TD).put(a2);
		}
	}

	public int getVBO() {
		if (rebuilding) {
			vbo=createVBO();
			rebuilding=false;
		}
		return vbo;
	}

	public int getTriangleCount() {
		return triangleCount;
	}

	/**
	 * Set up OpenGL state for drawing chunks
	 */
	public static void prepareState() {
		texture.bind();

		glUseProgram(Chunk.chunkProgram);

		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);

		glDisable(GL_BLEND);
		glDepthMask(true);
	}

	public void draw() {
		refresh();
		if (vbo != 0) {
			// Bind buffer
			glBindBuffer(GL_ARRAY_BUFFER, getVBO());

			int stride = geom.strideInBytes();

			// define vertex format, should be after glBindBuffer
			glVertexAttribPointer(c_vs_inputPosition, 3, GL_FLOAT, false, stride, 0L); // Note: stride in bytes
			glEnableVertexAttribArray(c_vs_inputPosition);

			glVertexAttribPointer(c_vs_normalPosition, 3, GL_FLOAT, false, stride, 12L); // Note: stride in bytes
			glEnableVertexAttribArray(c_vs_normalPosition);

			glVertexAttribPointer(c_vs_texturePosition, 2, GL_FLOAT, false, stride, 24L); // Note: stride in bytes
			glEnableVertexAttribArray(c_vs_texturePosition);

			glVertexAttribPointer(c_vs_colourPosition, 3, GL_FLOAT, false, stride, 32L); // Note: stride in bytes
			glEnableVertexAttribArray(c_vs_colourPosition);

			glDrawArrays(GL_TRIANGLES, 0, getTriangleCount() * 3);
		}
	}

}
