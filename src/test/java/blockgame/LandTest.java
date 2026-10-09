package blockgame;

import static blockgame.TestUtil.itemBalance;
import static blockgame.TestUtil.mineGold;
import static blockgame.TestUtil.newPlayer;
import static blockgame.TestUtil.ok;
import static blockgame.TestUtil.query;
import static blockgame.TestUtil.transact;
import static blockgame.TestUtil.world;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import convex.api.Convex;
import convex.core.ErrorCodes;
import convex.core.Result;
import convex.core.data.ACell;
import convex.core.data.AVector;

/**
 * Land claims and world access control.
 *
 * All test classes share one deployed world, so every test here uses its own
 * far-away chunk, well clear of the origin and of AssetTest's (100,100,100).
 */
public class LandTest {

	static final long DIRT = 2, GOLD = 13;

	static {
		TestSetup.ensureInit();
	}

	private static String place(int x, int y, int z) {
		return world("place-block " + x + " " + y + " " + z + " " + DIRT);
	}

	private static void assertDenied(Result r) {
		assertTrue(r.isError(), "Expected TRUST error, got " + r);
		assertEquals(ErrorCodes.TRUST, r.getErrorCode(), r.toString());
	}

	@Test
	public void testMineGold() throws Exception {
		Convex alice = Config.getConvex(); // deployer, so the world controller
		long gold = itemBalance(alice, GOLD);
		mineGold(alice, 4600, 4600, 3);
		assertEquals(gold + 3, itemBalance(alice, GOLD));

		// fill is controller-only, so ordinary players cannot conjure blocks
		Convex bob = newPlayer(9101);
		assertDenied(transact(bob, world("fill 4610 4610 2 4610 4610 2 13")));
	}

	@Test
	public void testClaimAndBuild() throws Exception {
		Convex alice = Config.getConvex();
		Convex bob = newPlayer(9102);
		ok(bob, world("claim-starter-kit"));
		mineGold(alice, 4700, 4700, 4);

		// Chunk A: alice's land
		int ax = 4000, ay = 4000, az = 8;
		long gold = itemBalance(alice, GOLD);
		ok(alice, world("claim-land " + ax + " " + ay + " " + az));
		assertEquals(gold - 4, itemBalance(alice, GOLD), "Claiming burns gold");
		assertEquals(alice.getAddress(), query(alice, world("land-owner " + ax + " " + ay + " " + az)).getValue());

		// Second claim of the same chunk is refused, even by the owner
		Result dup = transact(alice, world("claim-land " + (ax + 3) + " " + (ay + 3) + " " + az));
		assertEquals(ErrorCodes.STATE, dup.getErrorCode(), dup.toString());

		// Bob cannot build on alice's land, alice can
		assertDenied(transact(bob, place(ax + 1, ay + 1, az)));
		ok(alice, place(ax + 1, ay + 1, az));

		// Alice allows bob, who can then build and break there
		ok(alice, world("allow-builder " + ax + " " + ay + " " + az + " " + bob.getAddress()));
		ok(bob, place(ax + 2, ay + 2, az));
		ok(bob, world("break-block " + (ax + 2) + " " + (ay + 2) + " " + az));
		ok(alice, world("disallow-builder " + ax + " " + ay + " " + az + " " + bob.getAddress()));
		assertDenied(transact(bob, place(ax + 2, ay + 2, az)));

		// Only the owner can release, transfer or manage builders
		assertDenied(transact(bob, world("release-land " + ax + " " + ay + " " + az)));
		assertDenied(transact(bob, world("transfer-land " + ax + " " + ay + " " + az + " " + bob.getAddress())));
		assertDenied(transact(bob, world("allow-builder " + ax + " " + ay + " " + az + " " + bob.getAddress())));

		// Chunk B: bob claims with his starter-kit gold, then hands it to alice
		int bx = 4000, by = 4100, bz = 8;
		ok(bob, world("claim-land " + bx + " " + by + " " + bz));
		assertDenied(transact(alice, place(bx + 1, by + 1, bz)));
		ok(bob, world("transfer-land " + bx + " " + by + " " + bz + " " + alice.getAddress()));
		assertEquals(alice.getAddress(), query(bob, world("land-owner " + bx + " " + by + " " + bz)).getValue());
		ok(alice, place(bx + 1, by + 1, bz));
		assertDenied(transact(bob, place(bx + 2, by + 2, bz)));

		// Released land is open to everyone again
		ok(alice, world("release-land " + bx + " " + by + " " + bz));
		assertNull(query(bob, world("land-info " + bx + " " + by + " " + bz)).getValue());
		ok(bob, place(bx + 2, by + 2, bz));
	}

	@Test
	public void testClaimNeedsGold() throws Exception {
		Convex carol = newPlayer(9103); // no starter kit, so no gold
		assertEquals(0, itemBalance(carol, GOLD));
		Result r = transact(carol, world("claim-land 4200 4000 8"));
		assertTrue(r.isError());
		assertEquals(ErrorCodes.FUNDS, r.getErrorCode(), r.toString());
		assertNull(query(carol, world("land-owner 4200 4000 8")).getValue());
	}

	@Test
	public void testSetChunkOnlyForNewChunks() throws Exception {
		Convex alice = Config.getConvex();
		long cpos = Config.getEngine().chunkAddress(4400, 4400, 0);
		assertNull(query(alice, world("get-chunk " + cpos)).getValue());

		ok(alice, world("set-chunk " + cpos + " [1 2 3]"));
		AVector<ACell> stored = query(alice, world("get-chunk " + cpos)).getValue();
		assertEquals(3, stored.count());

		// Existing chunks cannot be overwritten wholesale, by anyone
		Result again = transact(alice, world("set-chunk " + cpos + " [9 9 9]"));
		assertEquals(ErrorCodes.STATE, again.getErrorCode(), again.toString());
		Convex bob = newPlayer(9104);
		Result bobTry = transact(bob, world("set-chunk " + cpos + " [9 9 9]"));
		assertEquals(ErrorCodes.STATE, bobTry.getErrorCode(), bobTry.toString());
		assertFalse(query(alice, world("get-chunk " + cpos)).toString().contains("9 9 9"));
	}
}
