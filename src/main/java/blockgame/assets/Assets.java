package blockgame.assets;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Assets {

	public static BufferedImage textureImage;

	static {
		//ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		ClassLoader classLoader =Assets.class.getClassLoader();

		try {
			Assets.textureImage = ImageIO.read(classLoader.getResource("images/textures.png"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
