package blockgame;

import convex.etch.EtchStore;

/**
 * Test bootstrap: initialises Config against a temporary Etch store so tests
 * never read or mutate the live blockgame-db.etch world database (and cannot
 * conflict with a running game holding its file lock).
 */
public class TestSetup {

	public static void ensureInit() {
		try {
			Config.init(true, EtchStore.createTemp("blockgame-test"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}
}
