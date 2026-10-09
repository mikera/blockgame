package blockgame;

import static blockgame.TestUtil.itemBalance;
import static blockgame.TestUtil.ok;
import static blockgame.TestUtil.query;
import static blockgame.TestUtil.totalSupply;
import static blockgame.TestUtil.transact;
import static blockgame.TestUtil.world;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import convex.api.Convex;
import convex.core.ErrorCodes;
import convex.core.Result;
import convex.core.data.AMap;
import convex.core.data.Keyword;

/**
 * On-chain crafting: recipes burn inputs and mint outputs via the inventory.
 */
public class CraftTest {

	static final long DIRT = 2, BRICK = 12, PLANKS = 14, LOG = 20, STONE_BRICKS = 62, STONE = 63, FIRESTONE = 93;

	static {
		TestSetup.ensureInit();
	}

	@Test
	public void testCraftPlanks() throws Exception {
		Convex alice = Config.getConvex();
		long logs = itemBalance(alice, LOG);
		long planks = itemBalance(alice, PLANKS);
		long supply = totalSupply(alice, PLANKS);
		assertTrue(logs >= 1, "Starter kit should include logs");

		ok(alice, world("craft :planks"));

		assertEquals(logs - 1, itemBalance(alice, LOG));
		assertEquals(planks + 4, itemBalance(alice, PLANKS));
		assertEquals(supply + 4, totalSupply(alice, PLANKS), "Minted output increases supply");
	}

	@Test
	public void testCraftItemFindsRecipe() throws Exception {
		Convex alice = Config.getConvex();
		long stone = itemBalance(alice, STONE);
		long bricks = itemBalance(alice, STONE_BRICKS);
		assertTrue(stone >= 4);

		ok(alice, world("craft-item " + STONE_BRICKS));

		assertEquals(stone - 4, itemBalance(alice, STONE));
		assertEquals(bricks + 4, itemBalance(alice, STONE_BRICKS));
	}

	@Test
	public void testCraftInsufficientInputs() throws Exception {
		Convex alice = Config.getConvex();
		long dirt = itemBalance(alice, DIRT);
		long brick = itemBalance(alice, BRICK);
		assertEquals(0, itemBalance(alice, FIRESTONE), "Starter kit has no firestone");

		Result r = transact(alice, world("craft :bricks"));
		assertTrue(r.isError());
		assertEquals(ErrorCodes.FUNDS, r.getErrorCode());

		// Nothing was burned or minted
		assertEquals(dirt, itemBalance(alice, DIRT));
		assertEquals(brick, itemBalance(alice, BRICK));
	}

	@Test
	public void testUnknownRecipe() throws Exception {
		Convex alice = Config.getConvex();
		Result r = transact(alice, world("craft :unobtainium"));
		assertTrue(r.isError());
		assertEquals(ErrorCodes.ARGUMENT, r.getErrorCode());

		Result r2 = transact(alice, world("craft-item 999"));
		assertTrue(r2.isError());
		assertEquals(ErrorCodes.ARGUMENT, r2.getErrorCode());
	}

	@Test
	public void testRecipesQuery() throws Exception {
		Convex alice = Config.getConvex();
		Result r = query(alice, world("get-recipes"));
		assertFalse(r.isError(), r.toString());
		AMap<?, ?> recipes = r.getValue();
		assertTrue(recipes.containsKey(Keyword.create("planks")));
		assertTrue(recipes.count() >= 10);
	}
}
