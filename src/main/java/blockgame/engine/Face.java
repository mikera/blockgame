package blockgame.engine;

import org.joml.Vector3i;

public class Face {
	public static final int U=0;
	public static final int N=1;
	public static final int E=2;
	public static final int S=3;
	public static final int W=4;
	public static final int D=5;
	
	public static final Vector3i[] DIR= {
			new Vector3i(0,0,1),
			new Vector3i(0,1,0),
			new Vector3i(1,0,0),
			new Vector3i(0,-1,0),
			new Vector3i(-1,0,0),
			new Vector3i(0,0,-1)
	};
	
	public static final float[][] NORMAL= {
			{0,0,1},
			{0,1,0},
			{1,0,0},
			{0,-1,0},
			{-1,0,0},
			{0,0,-1}
	};
}
