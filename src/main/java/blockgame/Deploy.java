package blockgame;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import convex.api.Convex;
import convex.core.Result;
import convex.core.data.Address;
import convex.core.transactions.Invoke;
import convex.core.util.Utils;

public class Deploy {

	public static Address world;
	
	/**
	 * Deploys the game work to a connected Convex network
	 * @param convex Convex client instance
	 * @return Address of world actor
	 */
	public static Address doDeploy(Convex convex) {
		try {
			world=deployCode(convex,"convex/world.cvx");
		} catch (Throwable t) {
			Utils.sneakyThrow(t);
		}
		return world;
	}

	private static Address deployCode(Convex convex, String path) throws IOException, TimeoutException {
		String code=Utils.readResourceAsString(path);
		
		Result r=convex.transactSync(Invoke.create(convex.getAddress(), 0, "(deploy `(do "+ code +"\n))"));
		if (r.isError()) {
			throw new Error(r.toString());
		}
		
		Address a=r.getValue();
		
		return a;
	}
}
