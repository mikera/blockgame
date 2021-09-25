package blockgame.engine;

import blockgame.assets.Assets;
import convex.core.data.ABlob;
import convex.core.data.ACell;
import convex.core.data.AHashMap;
import convex.core.data.AVector;
import convex.core.data.Keyword;
import convex.core.data.Strings;
import convex.core.data.prim.CVMLong;

public class Lib {
	public static final CVMLong AIR=null;
	
	public static final CVMLong GRASS=block("grass");
	public static final CVMLong DIRT=block("dirt");
	
	public static final CVMLong CHALK=block("chalk");
	public static final CVMLong STONE=block("stone");
	public static final CVMLong GRANITE=block("granite");
	public static final CVMLong BASALT=block("basalt");
	
	public static final CVMLong LOG=CVMLong.create(20);
	public static final CVMLong LEAVES=CVMLong.create(4);

	public static final CVMLong CHALK_BLOCK=CVMLong.create(50);
	public static final CVMLong CHALK_SLABS=CVMLong.create(51);
	public static final CVMLong CHALK_BRICKS=CVMLong.create(52);
	
	public static final CVMLong STONE_BLOCK=CVMLong.create(60);
	public static final CVMLong STONE_SLABS=CVMLong.create(61);
	public static final CVMLong STONE_BRICKS=CVMLong.create(62);
	
	public static final CVMLong WATER=CVMLong.create(4);


	
	public static final CVMLong BOULDER=STONE_BLOCK;



	private static CVMLong block(String string) {
		return Assets.namelookup.get(Strings.create(string));
	}



	@SuppressWarnings("unchecked")
	public static int getToolTexture(ACell type) {
		AHashMap<Keyword, ACell> meta = (AHashMap<Keyword, ACell>) Assets.blockData.get(type);
		if (meta==null) return 0;
		AVector<ABlob> tex = (AVector<ABlob>) meta.get(Assets.TEX_KEY);
		if (tex==null) return 0;
		
		return (int) tex.get(1).toLong();
	}


}
