package blockgame.engine;

import java.io.IOException;
import java.util.function.Predicate;

import convex.core.data.ABlob;
import convex.core.data.ACell;
import convex.core.data.AHashMap;
import convex.core.data.AString;
import convex.core.data.AVector;
import convex.core.data.Keyword;
import convex.core.cvm.Keywords;
import convex.core.data.Maps;
import convex.core.data.Strings;
import convex.core.data.prim.CVMLong;
import convex.core.lang.RT;
import convex.core.lang.Reader;
import convex.core.util.Utils;

public class Lib {
	public static final AHashMap<CVMLong,AHashMap<Keyword,ACell>> blockData;

	public static final AHashMap<AString,CVMLong> namelookup;

	static {
		try {
			blockData=Reader.read(Utils.readResourceAsString("/lib/block-data.cvx"));
			namelookup=blockData.reduceEntries((m,me)->{
				AHashMap<Keyword,ACell> data=me.getValue();
				m=m.assoc(data.get(Keywords.NAME),me.getKey());
				return m;
			}, Maps.empty());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw Utils.sneakyThrow(e1);
		}
		
	}
	
	public static final CVMLong AIR=null;
	
	public static final CVMLong GRASS=block("grass");
	public static final CVMLong DIRT=block("dirt");
	public static final CVMLong SAND=block("sand");
	public static final CVMLong WATER=block("water");

	
	public static final CVMLong CHALK=block("chalk");
	public static final CVMLong STONE=block("stone");
	public static final CVMLong GRANITE=block("granite");
	public static final CVMLong BASALT=block("basalt");
	
	public static final CVMLong FIRESTONE=block("firestone");
	public static final CVMLong FIRESTONE_BLOCK=block("firestone block");
	
	public static final CVMLong PURPLE_MUSHROOM=block("purple mushroom");
	public static final CVMLong RED_MUSHROOM=block("red mushroom");
	public static final CVMLong GREY_MUSHROOM=block("grey mushroom");

	public static final CVMLong MEDIUM_GRASS=block("medium grass");
	public static final CVMLong SHORT_GRASS=block("short grass");
	public static final CVMLong DEAD_BUSH=block("dead bush");
	public static final CVMLong GREEN_BUSH=block("green bush");

	
	public static final CVMLong LOG=CVMLong.create(20);
	public static final CVMLong LEAVES=CVMLong.create(4);

	public static final CVMLong CHALK_BLOCK=CVMLong.create(50);
	public static final CVMLong CHALK_SLABS=CVMLong.create(51);
	public static final CVMLong CHALK_BRICKS=CVMLong.create(52);
	
	public static final CVMLong STONE_BLOCK=CVMLong.create(60);
	public static final CVMLong STONE_SLABS=CVMLong.create(61);
	public static final CVMLong STONE_BRICKS=CVMLong.create(62);

	
	public static final Keyword KEY_TRANS=Keyword.create("trans");
	public static final Keyword KEY_TEX = Keyword.create("tex");
	public static final Keyword KEY_MODEL = Keyword.create("model");
	public static final Keyword KEY_PASSABLE = Keyword.create("passable");

	
	public static final CVMLong BOULDER=STONE_BLOCK;

	public static final Predicate<ACell> PRED_GROWABLE = new Predicate<ACell>() {
		@Override
		public boolean test(ACell t) {
			if (t==null) return false;
			return t.equals(GRASS);
		}	
	};
	
	public static final Predicate<ACell> PRED_BLOCKING = new Predicate<ACell>() {
		@Override
		public boolean test(ACell t) {
			if (t==null) return false;
			return !RT.bool(RT.get(blockData.get(t),KEY_PASSABLE));
		}	
	};
	
	private static CVMLong block(String string) {
		return Lib.namelookup.get(Strings.create(string));
	}

	@SuppressWarnings("unchecked")
	public static int getToolTexture(ACell type) {
		AHashMap<Keyword, ACell> meta = (AHashMap<Keyword, ACell>) blockData.get(type);
		if (meta==null) return 0;
		AVector<ABlob> tex = (AVector<ABlob>) meta.get(Lib.KEY_TEX);
		if (tex==null) return 0;
		long c=tex.count();
		return (int) tex.get((c>1)?1:0).longValue();
	}

	public static boolean isTransparent(ACell block) {
		if (block==null) return true;
		return blockData.get(block).get(KEY_TRANS)!=null;
	}




}
