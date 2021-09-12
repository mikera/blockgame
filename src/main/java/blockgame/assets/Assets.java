package blockgame.assets;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import convex.core.data.ACell;
import convex.core.data.AHashMap;
import convex.core.data.Keyword;
import convex.core.data.prim.CVMLong;
import convex.core.lang.Reader;
import convex.core.util.Utils;

public class Assets {

	public static final Keyword TEX_KEY = Keyword.create("tex");

	public static BufferedImage textureImage;
	
	public static AHashMap<CVMLong,ACell> blockData;

	static {
		//ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		ClassLoader classLoader =Assets.class.getClassLoader();

		try {
			Assets.textureImage = ImageIO.read(classLoader.getResource("images/textures.png"));
			blockData=Reader.read(Utils.readResourceAsString("lib/block-data.cvx"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw Utils.sneakyThrow(e1);
		}
		
	}
}
