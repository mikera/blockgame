package blockgame.assets;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import convex.core.data.ACell;
import convex.core.data.AHashMap;
import convex.core.data.AString;
import convex.core.data.Keyword;
import convex.core.data.Keywords;
import convex.core.data.Maps;
import convex.core.data.prim.CVMLong;
import convex.core.lang.Reader;
import convex.core.util.Utils;

public class Assets {

	public static final Keyword TEX_KEY = Keyword.create("tex");

	public static BufferedImage font;

	public static BufferedImage textureImage;
	
	public static BufferedImage skybox;

	
	public static AHashMap<CVMLong,ACell> blockData;
	public static AHashMap<AString,CVMLong> namelookup;

	static {
		//ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		ClassLoader classLoader =Assets.class.getClassLoader();

		try {
			Assets.textureImage = ImageIO.read(classLoader.getResource("images/textures.png"));
			Assets.font = ImageIO.read(classLoader.getResource("images/font.png"));
			Assets.skybox = ImageIO.read(classLoader.getResource("images/skybox.png"));
			
			blockData=Reader.read(Utils.readResourceAsString("lib/block-data.cvx"));
			namelookup=blockData.reduceEntries((m,me)->{
				@SuppressWarnings("unchecked")
				AHashMap<ACell,ACell> data=(AHashMap<ACell, ACell>) me.getValue();
				m=m.assoc(data.get(Keywords.NAME),me.getKey());
				return m;
			}, Maps.empty());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw Utils.sneakyThrow(e1);
		}
		
	}
}
