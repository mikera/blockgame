package blockgame;

import static org.junit.jupiter.api.Assertions.assertFalse;

import convex.api.Convex;
import convex.core.Result;
import convex.core.crypto.AKeyPair;
import convex.core.cvm.Address;
import convex.core.cvm.transactions.Invoke;
import convex.core.data.prim.CVMLong;
import convex.core.lang.Reader;

/**
 * Shared helpers for tests that drive the deployed world and inventory actors.
 */
public class TestUtil {

	private static Invoke invoke(Address origin, String code) {
		return Invoke.create(origin, 0, Reader.read(code));
	}

	/** Creates a funded player account and returns a client that transacts as it. */
	public static Convex newPlayer(long seed) throws Exception {
		AKeyPair kp = AKeyPair.createSeeded(seed);
		Result r = Config.PEER_CONVEX.transactSync(invoke(Config.PEER_ADDRESS,
				"(let [a (create-account " + kp.getAccountKey() + ")] (transfer a 1000000000) a)"));
		assertFalse(r.isError(), r.toString());
		Address a = r.getValue();
		return Convex.connect(Config.SERVER, a, kp);
	}

	/** Transacts a form as the client's account and returns the result. */
	public static Result transact(Convex c, String code) throws Exception {
		return c.transactSync(invoke(c.getAddress(), code));
	}

	/** Transacts and asserts success. */
	public static Result ok(Convex c, String code) throws Exception {
		Result r = transact(c, code);
		assertFalse(r.isError(), code + " => " + r);
		return r;
	}

	public static Result query(Convex c, String code) throws Exception {
		return c.querySync(Reader.read(code));
	}

	/** Builds a call form against the deployed world actor. */
	public static String world(String call) {
		return "(call " + Deploy.world + " (" + call + "))";
	}

	public static long itemBalance(Convex c, Address owner, long item) throws Exception {
		Result r = query(c, "(call [" + Deploy.inventory + " " + item + "] (balance " + owner + "))");
		assertFalse(r.isError(), r.toString());
		return ((CVMLong) r.getValue()).longValue();
	}

	public static long itemBalance(Convex c, long item) throws Exception {
		return itemBalance(c, c.getAddress(), item);
	}

	public static long totalSupply(Convex c, long item) throws Exception {
		Result r = query(c, "(call [" + Deploy.inventory + " " + item + "] (total-supply))");
		assertFalse(r.isError(), r.toString());
		return ((CVMLong) r.getValue()).longValue();
	}

	/**
	 * Mines n gold for the controller account by placing a gold column with the
	 * controller-only fill and breaking it, exercising the real gameplay loop.
	 */
	public static void mineGold(Convex controller, int x, int y, int n) throws Exception {
		ok(controller, world("fill " + x + " " + y + " 2 " + x + " " + y + " " + (1 + n) + " 13"));
		for (int z = 2; z <= 1 + n; z++) {
			ok(controller, world("break-block " + x + " " + y + " " + z));
		}
	}
}
