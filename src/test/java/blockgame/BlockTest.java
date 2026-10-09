package blockgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import blockgame.assets.Assets;
import blockgame.engine.Engine;
import convex.api.Convex;
import convex.core.Result;
import convex.core.data.ACell;
import convex.core.data.AVector;
import convex.core.data.prim.CVMLong;
import convex.core.lang.Reader;

/**
 * Headless tests for client-side block and chunk logic.
 *
 * Must never touch GLFW or OpenGL: the suite runs with java.awt.headless=true
 * and BlockGame.init() refuses to start under it.
 */
public class BlockTest {

	static {
		TestSetup.ensureInit();
	}

	@Test
	public void testEngineHeadless() {
		assertTrue(Boolean.getBoolean("java.awt.headless"),"test JVM must set java.awt.headless=true (see surefire argLine in pom.xml)");
		Engine engine=Config.getEngine();
		assertNotNull(engine);
		assertEquals(4096,Engine.EMPTY_CHUNK.count());
	}

	/**
	 * The client's chunk addressing must agree with pos-to-chunk in world.cvx,
	 * including for negative coordinates.
	 */
	@Test
	public void testChunkAddressingMatchesChain() throws Exception {
		Engine engine=Config.getEngine();
		Convex convex=Config.getConvex();
		int[][] positions= {{0,0,0},{1,2,3},{15,15,15},{16,0,0},{-1,-1,-1},{-17,33,-100},{1000,-2000,3000}};
		for (int[] p:positions) {
			int x=p[0], y=p[1], z=p[2];
			Result r=convex.querySync(Reader.read("("+Deploy.world+"/pos-to-chunk "+x+" "+y+" "+z+")"));
			assertFalse(r.isError(), r.toString());
			AVector<ACell> v=r.getValue();
			long addr=((CVMLong)v.get(0)).longValue();
			long index=((CVMLong)v.get(1)).longValue();
			String at=x+","+y+","+z;
			assertEquals(engine.chunkAddress(Util.chunkBase(x),Util.chunkBase(y),Util.chunkBase(z)),addr,"chunk address at "+at);
			assertEquals(Engine.chunkIndex(x,y,z),index,"chunk index at "+at);
		}
	}

	@Test
	public void testAssets() {
		assertEquals(2048,Assets.textureImage.getWidth());
		assertEquals(2048,Assets.textureImage.getHeight());
	}
}
