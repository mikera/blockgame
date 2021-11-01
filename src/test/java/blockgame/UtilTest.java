package blockgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

public class UtilTest {

	@Test public void testChunkBase() {
		assertEquals(-16,Util.chunkBase(-0.01f));
		assertEquals(0,Util.chunkBase(-0.00f));
		assertEquals(160,Util.chunkBase(168.0f));
	}
	
	@Test public void testVector3i() {
		Vector3i a = new Vector3i(1,2,3);
		Vector3i b = new Vector3i(1,2,3);
		assertNotSame(a,b);
		assertEquals(a,b);
		assertEquals(a.hashCode(),b.hashCode());
	}
}
