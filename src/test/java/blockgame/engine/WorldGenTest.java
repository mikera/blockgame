package blockgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pure world generation checks. No peer, no GL.
 */
public class WorldGenTest {

	/** Fraction of positions in a 64x64 column slab over [z1,z2] that are gold ore. */
	static double oreDensity(long seed, int z1, int z2) {
		long ore = 0, total = 0;
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 64; y++) {
				for (int z = z1; z <= z2; z++) {
					if (WorldGen.isGoldOre(x, y, z, seed)) ore++;
					total++;
				}
			}
		}
		return ore / (double) total;
	}

	@Test
	public void testGoldOreDensity() {
		for (long seed : new long[] { 1, 2, 3 }) {
			double deep = oreDensity(seed, -32, -20);
			double shallow = oreDensity(seed, -8, -2);
			double surface = oreDensity(seed, -1, 30);
			System.out.println("gold ore seed " + seed + ": deep=" + deep + " shallow=" + shallow + " surface=" + surface);

			assertEquals(0.0, surface, "No gold above sea level");
			assertTrue(deep > shallow, "Gold gets richer with depth");
			assertTrue(deep >= 0.002 && deep <= 0.05, "Deep density out of range: " + deep);
			assertTrue(shallow >= 0.0001 && shallow <= 0.02, "Shallow density out of range: " + shallow);
		}
	}

	@Test
	public void testGoldOreIsDeterministic() {
		assertEquals(WorldGen.isGoldOre(5, 7, -25, 42), WorldGen.isGoldOre(5, 7, -25, 42));
		// Different seeds give different worlds somewhere in a sample volume
		boolean differs = false;
		for (int x = 0; x < 32 && !differs; x++) {
			for (int y = 0; y < 32 && !differs; y++) {
				for (int z = -32; z < -20 && !differs; z++) {
					differs = WorldGen.isGoldOre(x, y, z, 1) != WorldGen.isGoldOre(x, y, z, 2);
				}
			}
		}
		assertTrue(differs);
	}
}
