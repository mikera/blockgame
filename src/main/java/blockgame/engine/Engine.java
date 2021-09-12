package blockgame.engine;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import convex.api.Convex;
import convex.core.Result;
import convex.core.data.ACell;
import convex.core.data.Address;
import convex.core.lang.Reader;
import convex.core.util.Utils;

/**
 * Main off-chain game engine
 */
public class Engine {

	private Convex convex=null;
	
	private Engine() {
		try {
			convex=Convex.connect(Utils.toInetSocketAddress("convex.world:18888"));
			convex.setAddress(Address.create(4411));
		} catch (IOException|TimeoutException e ) {
			throw Utils.sneakyThrow(e);
		}
	}
	
	
	public ACell loadChunk(long x, long y, long z) throws TimeoutException, IOException {
		Result r=convex.querySync(Reader.read("(call #4411 (get-chunk nil))"));
		if (r.isError()) throw new Error("BAd result: "+r);
		return r.getValue();
	}
	
	
	public static void main(String[] args) throws TimeoutException, IOException {
		Engine e=new Engine();
		System.out.println(e.loadChunk(0,0,0));
	}
}
