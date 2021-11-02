package blockgame.assets;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import convex.core.util.Utils;

public class Assets {

	public static BufferedImage font;

	public static BufferedImage textureImage;
	
	public static BufferedImage skybox;

	public static BufferedImage player;

	
	static {
		//ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		ClassLoader classLoader =Assets.class.getClassLoader();

		try {
			Assets.textureImage = ImageIO.read(classLoader.getResource("images/textures.png"));
			Assets.font = ImageIO.read(classLoader.getResource("images/font.png"));
			Assets.skybox = ImageIO.read(classLoader.getResource("images/skybox.png"));
			Assets.player = ImageIO.read(classLoader.getResource("models/player.png"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw Utils.sneakyThrow(e1);
		}
		
	}
}
