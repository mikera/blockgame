package blockgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import blockgame.assets.Assets;

public class BlockTest {

	@Test public void test1() {
		assertTrue(true);
	}
	
	@Test
	
	public void testAssets() {
		assertEquals(2048,Assets.spriteImage.getWidth());
		assertEquals(2048,Assets.spriteImage.getHeight());
	}
}
