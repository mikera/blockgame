package blockgame.render;

import java.nio.FloatBuffer;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Lightweight stateful vertex buffer builder.
 * Up to caller to determine vertex layout
 */
public class Buildable {

	private final int FLOATS_PER_VERTEX;
	private FloatBuffer fb;
	
	private Buildable(int fpv) {
		this.FLOATS_PER_VERTEX=fpv;
		fb=FloatBuffer.allocate(100*FLOATS_PER_VERTEX);
	}

	public static Buildable create(int floatsPerVertex) {
		return new Buildable(floatsPerVertex);
	}
	
	public Buildable put(float a) {
		ensureSpace(1);
		fb.put(a);
		return this;
	}
	
	public Buildable put(float a, float b) {
		ensureSpace(2);
		fb.put(a).put(b);
		return this;
	}
	
	public Buildable put(float a, float b, float c) {
		ensureSpace(3);
		fb.put(a).put(b).put(c);
		return this;
	}
	
	public Buildable put(float[] fs) {
		ensureSpace(fs.length);
		fb.put(fs);
		return this;
	}
	
	public Buildable put(Vector3f v) {
		ensureSpace(3);
		fb.put(v.x).put(v.y).put(v.z);
		return this;
	}
	
	public Buildable put(Vector2f v) {
		ensureSpace(2);
		fb.put(v.x).put(v.y);
		return this;
	}
	
	public void clear() {
		fb.clear();
	}

	private void ensureSpace(int required) {
		int current=fb.remaining();
		if (current<required) {
			int pos=fb.position();
			int newCapacity=Math.max(pos+required, 2*pos);
			if (newCapacity>fb.capacity()) {
				fb=Utils.resizeBuffer(fb, newCapacity);
			} else {
				System.out.println("Surprised no resize?");
			}
		}
	}
	
	public int floatsPerTriangle() {
		return 3*FLOATS_PER_VERTEX;
	}

	/**
	 * Gets flipped buffer after building. Should clear afterwards.
	 * @return Flipped vertex buffer
	 */
	public FloatBuffer getFlippedBuffer() {
		return fb.flip();
	}

	public int strideInBytes() {
		return 4*FLOATS_PER_VERTEX;
	}

	/**
	 * Get vertex count based on position in buffer
	 * @return number of vertices
	 */
	public int vertexCount() {
		return fb.position()/FLOATS_PER_VERTEX;
	}


}
