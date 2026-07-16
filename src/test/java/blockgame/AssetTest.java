package blockgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import convex.api.Convex;
import convex.core.Result;
import convex.core.crypto.AKeyPair;
import convex.core.cvm.Address;
import convex.core.cvm.transactions.Invoke;
import convex.core.data.prim.CVMLong;
import convex.core.lang.Reader;

/**
 * Verifies the inventory actor behaves as a CAD29 fungible asset: items are
 * owned by player accounts, transferable via the standard convex.asset
 * library, with engine-only minting and consistent supply.
 */
public class AssetTest {

	static final long GRASS = 1;

	static {
		TestSetup.ensureInit();
	}

	long itemBalance(Convex convex, Address owner, long item) throws Exception {
		Result r = convex.querySync(Reader.read("(call [" + Deploy.inventory + " " + item + "] (balance " + owner + "))"));
		assertFalse(r.isError(), r.toString());
		return ((CVMLong) r.getValue()).longValue();
	}

	@Test
	public void testAssetTransfer() throws Exception {
		Convex alice = Config.getConvex();
		Address inv = Deploy.inventory;

		// Create a second player account
		AKeyPair kpBob = AKeyPair.createSeeded(8888);
		Result rb = Config.PEER_CONVEX.transactSync(Invoke.create(Config.PEER_ADDRESS, 0,
				Reader.read("(let [a (create-account " + kpBob.getAccountKey() + ")] (transfer a 1000000000) a)")));
		assertFalse(rb.isError(), rb.toString());
		Address bob = rb.getValue();

		long aliceStart = itemBalance(alice, alice.getAddress(), GRASS);
		assertTrue(aliceStart >= 5, "Starter kit should include grass");

		// Standard CAD29 transfer via the convex.asset library
		Result rt = alice.transactSync(Invoke.create(alice.getAddress(), 0,
				Reader.read("(do (import convex.asset :as asset) (asset/transfer " + bob + " [[" + inv + " " + GRASS + "] 5]))")));
		assertFalse(rt.isError(), rt.toString());

		assertEquals(aliceStart - 5, itemBalance(alice, alice.getAddress(), GRASS));
		assertEquals(5, itemBalance(alice, bob, GRASS));

		// asset/owns? sees the holding
		Result ro = alice.querySync(Reader.read(
				"(do (import convex.asset :as asset) (asset/owns? " + bob + " [[" + inv + " " + GRASS + "] 5]))"));
		assertFalse(ro.isError(), ro.toString());
		assertTrue(ro.getValue().equals(convex.core.data.prim.CVMBool.TRUE));

		// Supply is conserved by transfers: total = sum of all balances
		Result rs = alice.querySync(Reader.read("(call [" + inv + " " + GRASS + "] (total-supply))"));
		long supply = ((CVMLong) rs.getValue()).longValue();
		assertEquals(supply, itemBalance(alice, alice.getAddress(), GRASS) + itemBalance(alice, bob, GRASS));
	}

	@Test
	public void testMintDenied() throws Exception {
		Convex alice = Config.getConvex();
		// Only the world actor may mint: direct add-stack from a user account must fail
		Result r = alice.transactSync(Invoke.create(alice.getAddress(), 0,
				Reader.read("(call " + Deploy.inventory + " (add-stack *address* " + GRASS + " 1000))")));
		assertTrue(r.isError(), "Minting from a user account should be denied");
	}

	@Test
	public void testDoubleClaimDenied() throws Exception {
		Convex alice = Config.getConvex();
		// Starter kit was already claimed during init
		Result r = alice.transactSync(Invoke.create(alice.getAddress(), 0,
				Reader.read("(call " + Deploy.world + " (claim-starter-kit))")));
		assertTrue(r.isError(), "Second starter kit claim should be denied");
	}

	@Test
	public void testPlaceAndBreakConservation() throws Exception {
		Convex alice = Config.getConvex();
		long DIRT = 2;
		long start = itemBalance(alice, alice.getAddress(), DIRT);
		assertTrue(start >= 1, "Starter kit should include dirt");

		// Placing a block burns one item from inventory
		Result rp = alice.transactSync(Invoke.create(alice.getAddress(), 0,
				Reader.read("(call " + Deploy.world + " (place-block 100 100 100 " + DIRT + "))")));
		assertFalse(rp.isError(), rp.toString());
		assertEquals(start - 1, itemBalance(alice, alice.getAddress(), DIRT));

		// Breaking it mints the item back
		Result rk = alice.transactSync(Invoke.create(alice.getAddress(), 0,
				Reader.read("(call " + Deploy.world + " (break-block 100 100 100))")));
		assertFalse(rk.isError(), rk.toString());
		assertEquals(start, itemBalance(alice, alice.getAddress(), DIRT));
	}

	@Test
	public void testDecimals() throws Exception {
		Convex alice = Config.getConvex();
		Result r = alice.querySync(Reader.read("(call [" + Deploy.inventory + " " + GRASS + "] (decimals))"));
		assertEquals(0L, ((CVMLong) r.getValue()).longValue());
	}
}
