package blockgame;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

import convex.api.Convex;
import convex.core.State;
import convex.core.crypto.AKeyPair;
import convex.core.data.Address;
import convex.core.data.Keyword;
import convex.core.data.Keywords;
import convex.core.data.Lists;
import convex.core.init.Init;
import convex.peer.API;
import convex.peer.Server;

public class ConvexTest {
	public static final AKeyPair[] KEYPAIRS = new AKeyPair[] {
			AKeyPair.createSeeded(1337),
	};
	
	private static Server SERVER;
	private static Convex CONVEX;
	private static Address ADDRESS;

	static {
		State genesisState=Init.createState(Lists.of(KEYPAIRS[0].getAccountKey()));
		
		HashMap<Keyword, Object> config=new HashMap<>();
		config.put(Keywords.STATE, genesisState);
		config.put(Keywords.KEYPAIR, KEYPAIRS[0]);
		SERVER=API.launchPeer(config);
		try {
			CONVEX = Convex.connect(SERVER);
			ADDRESS=Init.getGenesisAddress();
			CONVEX.setAddress(ADDRESS, KEYPAIRS[0]);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUserBalance() throws IOException {
		Long bal=CONVEX.getBalance(ADDRESS);
		assertTrue(bal>0);
	}
}
