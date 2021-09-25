package blockgame;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import convex.api.Convex;
import convex.core.data.Address;

public class ConvexTest {

	static {
		Config.init(true);
	}

	@Test
	public void testUserBalance() throws IOException {
		Convex convex = Config.getConvex();
		Long bal = convex.getBalance(Config.PEER_ADDRESS);
		assertTrue(bal > 0);

		bal = convex.getBalance(convex.getAddress());
		assertTrue(bal > 0);
	}
}
