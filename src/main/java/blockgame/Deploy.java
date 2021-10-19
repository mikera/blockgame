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
	public static Address inventory;
	
	/**
	 * Deploys the game work to a connected Convex network
	 * @param convex Convex client instance, configured for world controller Account
	 * @return Address of world actor
	 */
	public static Address doDeploy(Convex convex) {
		try {
			inventory=deployCode(convex,"convex/inventory.cvx");
			world=deployCode(convex,"convex/world.cvx");
			doSync(convex,"(eval-as "+world+" '(def inventory "+inventory+"))");
		} catch (Throwable t) {
			Utils.sneakyThrow(t);
		}
		return world;
	}

	private static Address deployCode(Convex convex, String path) throws IOException, TimeoutException {
		String code=Utils.readResourceAsString(path);
		Address god=convex.getAddress();
		System.out.println("Deploying code with controller address: " +god);
		
		Result r=convex.transactSync(Invoke.create(god, 0, "(deploy `(do (def *controller* "+god+") (set-controller *controller*) "+ code +"\n))"));
		if (r.isError()) {
			throw new Error(r.toString());
		}
		
		Address addr=r.getValue();
		
		return addr;
	}
	
	private static Result doSync(Convex convex,String code) throws TimeoutException, IOException {
		Invoke trans=Invoke.create(convex.getAddress(), 0, code);
		return convex.transactSync(trans);
	}
}
