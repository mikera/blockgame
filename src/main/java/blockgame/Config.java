package blockgame;

import java.io.File;
import java.util.HashMap;
import blockgame.engine.Engine;
import convex.api.Convex;
import convex.core.Result;
import convex.core.State;
import convex.core.crypto.AKeyPair;
import convex.core.data.Address;
import convex.core.data.Keyword;
import convex.core.data.Keywords;
import convex.core.data.Lists;
import convex.core.init.Init;
import convex.core.lang.Reader;
import convex.core.store.Stores;
import convex.core.transactions.Invoke;
import convex.core.util.Utils;
import convex.peer.API;
import convex.peer.Server;
import etch.EtchStore;

public class Config {
	/**
	 * Keypair for user account
	 */
	public static AKeyPair kp=AKeyPair.createSeeded(156778);
	
	/**
	 * Address for user account
	 */
	public static Address addr;
	
	/**
	 * Address for on-chain world root
	 */
	public static Address world;

	public static final AKeyPair[] LOCAL_KEYPAIRS = new AKeyPair[] {
			AKeyPair.createSeeded(1337),
	};

	/**
	 * Game engine instance
	 */
	private static Engine engine;

	public static Server SERVER;
	static Convex PEER_CONVEX;
	static Address PEER_ADDRESS;
	static EtchStore STORE;

	private static Convex convex=null;
	
	public static boolean local=true;
	
	public static synchronized void init(boolean b) {
		if (engine!=null) return;
		engine=Engine.create();

		local =b;
		try {
			if (local) {
				STORE=EtchStore.create(new File("blockgame-db.etch"));
				
				State genesisState=Init.createState(Lists.of(LOCAL_KEYPAIRS[0].getAccountKey()));
				
				HashMap<Keyword, Object> config=new HashMap<>();
				config.put(Keywords.STORE, STORE);
				config.put(Keywords.RESTORE, true);
				config.put(Keywords.STATE, genesisState);
				config.put(Keywords.KEYPAIR, LOCAL_KEYPAIRS[0]);
				SERVER=API.launchPeer(config);

				PEER_CONVEX = Convex.connect(SERVER);
				PEER_ADDRESS=Init.getGenesisAddress();
				PEER_CONVEX.setAddress(PEER_ADDRESS, LOCAL_KEYPAIRS[0]);
				Result r=PEER_CONVEX.transactSync(Invoke.create(PEER_ADDRESS, 0, Reader.read("(let [addr (create-account "+kp.getAccountKey()+")] (transfer addr 100000000000000) addr)")));
				addr=r.getValue();

				convex=Convex.connect(SERVER);
				convex.setAddress(addr, kp);
				Deploy.doDeploy(convex);
				world=Deploy.world;
				
				
				engine.createPlayer();
				System.out.println("Player created!");

			} else {
				STORE=(EtchStore) Stores.current();
				
				convex =Convex.connect(Utils.toInetSocketAddress("convex.world:18888"));
				world=Address.create(4562);
				addr=Address.create(4564);
				convex.setAddress(addr);
				convex.setKeyPair(kp);
				
			}
		} catch (Throwable t) {
			t.printStackTrace();
			Utils.sneakyThrow(t);
		}
		
		System.out.println("Config complete user="+addr +" world="+world+"");

	}

	public static Convex getConvex() {
		return convex;
	}

	public static Engine getEngine() {
		return engine;
	}


}
