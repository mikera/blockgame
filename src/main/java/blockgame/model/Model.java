package blockgame.model;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;

import blockgame.model.obj.Obj;
import blockgame.render.Buildable;

public class Model {

	public static final int FLOATS_PER_VERTEX= 3 + 3 + 2 + 3; // position + normal + texture + colour
	public static final Vector3f COLOUR=new Vector3f(0.5f,0.5f,0.5f);
	
	private final Buildable build=Buildable.create(FLOATS_PER_VERTEX);
	private final int objectCount; 
	
	private final String[] names;
	private final int[] starts;
	private final int[] ends;
	
	private int vbo=0;
	private int triangleCount=0;
	
	private Model(int objCount) {
		objectCount=objCount;
		starts=new int[objCount];
		ends=new int[objCount];
		names=new String[objCount];
	}
	
	public static Model fromObj(Obj obj ) {
		int objCount=obj.getFaces().size();
		Model model=new Model(objCount);
		
		int oi=0;
		for (Map.Entry<String,List<Obj.Face>> e:obj.getFaces().entrySet()) {
			model.starts[oi]=model.build.vertexCount();
			String name=e.getKey();
			model.names[oi]=name;
			List<Obj.Face> faces=e.getValue();
			
			for (Obj.Face face:faces) {
				int[] vs=face.getVertices();
				int[] ns=face.getNormals();
				int[] ts=face.getTextureCoords();
				int n=vs.length;
				
				// index = last vertex
				for (int i=2; i<n; i++) {
					model.addTri(obj,vs,ts,ns,0,i-1,i);
				}
			}
			model.ends[oi]=model.build.vertexCount();
			oi++;
		}
		
		model.createVBO();
		
		return model;
	}
	
	private synchronized void createVBO() {
		dispose();
		
		// Geometry in current context
		FloatBuffer built = build.getFlippedBuffer();

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
	}
	
	public void dispose() {
		if (vbo!=0) {
			synchronized(this) {
				if (vbo!=0) {
					int tempVBO=vbo;
					vbo=0;
					glDeleteBuffers(tempVBO);
				}
			}
			
		}
	}

	private void addTri(Obj obj, int[] vs, int[] ts, int[] ns, int i1, int i2, int i3) {
		addVert(obj,vs[i1],ns[i1],ts[i1]);
		addVert(obj,vs[i2],ns[i2],ts[i2]);
		addVert(obj,vs[i3],ns[i3],ts[i3]);
	}

	private void addVert(Obj obj, int vi, int ni, int ti) {
		Vector3f v=obj.getVertex(vi);
		Vector3f n=obj.getNormal(ni);
		Vector2f t=obj.getTextureCoordinate(ti);
		build.put(v).put(n).put(t).put(COLOUR);
	}

	public int getObjectCount() {
		return objectCount;
	}

	public int getVertexCount() {
		return build.vertexCount();
	}
}
