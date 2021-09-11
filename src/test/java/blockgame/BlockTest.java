package blockgame;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import blockgame.assets.Assets;

public class BlockTest {

	@Test public void test1() {
		new Main().init();
	}
	
	@Test
	
	public void testAssets() {
		assertEquals(2048,Assets.textureImage.getWidth());
		assertEquals(2048,Assets.textureImage.getHeight());
	}
}
