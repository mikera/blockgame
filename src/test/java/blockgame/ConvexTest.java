package blockgame;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

import convex.api.Convex;
import convex.core.Result;
import convex.core.State;
import convex.core.crypto.AKeyPair;
import convex.core.data.AccountKey;
import convex.core.data.Address;
import convex.core.data.Keyword;
import convex.core.data.Keywords;
import convex.core.data.Lists;
import convex.core.init.Init;
import convex.core.lang.Reader;
import convex.core.transactions.Invoke;
import convex.peer.API;
import convex.peer.Server;

public class ConvexTest {
	public static final AKeyPair[] KEYPAIRS = new AKeyPair[] {
			AKeyPair.createSeeded(1337),
	};
	
	private static Server SERVER;
	private static Convex CONVEX;
	private static Address ADDRESS;
	
	static AKeyPair USER_KP=AKeyPair.createSeeded(1234567);
	static AccountKey USER_KEY=USER_KP.getAccountKey();
	static Address USER_ADDRESS;
	
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
			Result r=CONVEX.transactSync(Invoke.create(ADDRESS, 0, Reader.read("(let [addr (create-account "+USER_KEY+")] (transfer addr 100000000000) addr)")));
			USER_ADDRESS=r.getValue();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUserBalance() throws IOException {
		Long bal=CONVEX.getBalance(ADDRESS);
		assertTrue(bal>0);
		
		bal=CONVEX.getBalance(USER_ADDRESS);
		assertTrue(bal>0);
	}
}
