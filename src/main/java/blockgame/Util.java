package blockgame;

import org.joml.Vector3i;

public class Util {
	
	
	public static final float QUARTER_TURN=(float) (Math.PI/2);


	public static double dist(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Math.sqrt(dist2(x1-x2,y1-y2,z1-z2));
	}

	private static double dist2(double dx, double dy, double dz) {
		return (dx*dx)+(dy*dy)+(dz*dz);
	}

	public static int chunkBase(float a) {
		return ((int)Math.floor(a))&~0xf;
	}
	
	public static int chunkBase(int a) {
		return a&~0xf;
	}

	public static String chunkString(int x, int y, int z) {
		x=chunkBase(x);
		y=chunkBase(y);
		z=chunkBase(z);
		return "["+locString(x,y,z)+"]";
	}

	/**
	 * Location as 3 longs separated with whitespace (commas)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static String locString(int x, int y, int z) {
		return x+","+y+","+z;
	}

	public static String locString(Vector3i pos) {
		return locString(pos.x,pos.y,pos.z);
	}

}
