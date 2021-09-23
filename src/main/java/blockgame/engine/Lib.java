package blockgame.engine;

import convex.core.data.prim.CVMLong;

public class Lib {
	public static final CVMLong AIR=null;
	
	public static final CVMLong GRASS=CVMLong.create(1);
	public static final CVMLong DIRT=CVMLong.create(2);
	
	public static final CVMLong CHALK=CVMLong.create(53);
	public static final CVMLong STONE=CVMLong.create(63);
	public static final CVMLong GRANITE=CVMLong.create(73);
	public static final CVMLong BASALT=CVMLong.create(83);
	
	public static final CVMLong LOG=CVMLong.create(20);
	public static final CVMLong LEAVES=CVMLong.create(4);

	public static final CVMLong CHALK_BLOCK=CVMLong.create(50);
	public static final CVMLong CHALK_SLABS=CVMLong.create(51);
	public static final CVMLong CHALK_BRICKS=CVMLong.create(52);
	
	public static final CVMLong STONE_BLOCK=CVMLong.create(60);
	public static final CVMLong STONE_SLABS=CVMLong.create(61);
	public static final CVMLong STONE_BRICKS=CVMLong.create(62);

	
	public static final CVMLong BOULDER=STONE_BLOCK;


}
