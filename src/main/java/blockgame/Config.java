package blockgame;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import convex.api.Convex;
import convex.core.crypto.AKeyPair;
import convex.core.data.Address;
import convex.core.util.Utils;

public class Config {

	/**
	 * Keypair for user account
	 */
	public static AKeyPair kp=AKeyPair.createSeeded(156778);
	
	/**
	 * Address for user account
	 */
	public static Address addr=Address.create(4564);
	
	/**
	 * Address for on-chain world root
	 */
	public static Address worldAddress=Address.create(4562);

	private static Convex convex=null;
	
	public static Convex getConvex() {
		try {
			if (convex==null) {
				convex =Convex.connect(Utils.toInetSocketAddress("convex.world:18888"));
				convex.setAddress(addr);
				convex.setKeyPair(kp);
			}
		} catch (IOException|TimeoutException e ) {
			throw Utils.sneakyThrow(e);
		}

		return convex;
	}

}
