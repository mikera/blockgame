package blockgame.render;

import java.nio.FloatBuffer;

/**
 * Stateful vertex buffer builder.
 */
public class Buildable {

	public final int FLOATS_PER_VERTEX;
	private FloatBuffer fb;
	
	private Buildable(int fpv) {
		this.FLOATS_PER_VERTEX=fpv;
		fb=FloatBuffer.allocate(100*FLOATS_PER_VERTEX);
	}
	
	public static Buildable create(int floatsPerVertex) {
		return new Buildable(floatsPerVertex);
	}
	
	public Buildable put(float a) {
		ensureCapacity(fb.position()+1);
		fb.put(a);
		return this;
	}
	
	public Buildable put(float a, float b) {
		ensureCapacity(fb.position()+2);
		fb.put(a).put(b);
		return this;
	}
	
	public Buildable put(float a, float b, float c) {
		ensureCapacity(fb.position()+3);
		fb.put(a).put(b).put(c);
		return this;
	}
	
	public Buildable put(float[] fs) {
		ensureCapacity(fb.position()+fs.length);
		fb.put(fs);
		return this;
	}
	
	public void clear() {
		fb.clear();
	}

	private void ensureCapacity(int required) {
		int current=fb.capacity();
		if (current<required) {
			int newCapacity=Math.max(required, 2*current);
			fb=Utils.resizeBuffer(fb, newCapacity);
		}
	}
	
	public int getTriangleCount() {
		return fb.remaining()/(3*FLOATS_PER_VERTEX);
	}

	public FloatBuffer getFlippedBuffer() {
		fb.flip();
		return fb;
	}
}
