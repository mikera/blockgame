package blockgame.engine;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import blockgame.Config;
import convex.api.Convex;
import convex.core.Result;
import convex.core.lang.Reader;
import convex.core.transactions.Invoke;

public class WorldGen {

	public static void generate(Convex convex) throws TimeoutException, IOException {
		Result r=convex.transactSync(Invoke.create(convex.getAddress(), 0, Reader.read("(call "+Config.world+" (build-house))")));
		if (r.isError()) throw new Error(r.toString());
	}

}
