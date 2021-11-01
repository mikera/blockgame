package blockgame;

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

}
