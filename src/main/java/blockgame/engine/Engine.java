package blockgame.engine;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import convex.api.Convex;
import convex.core.Result;
import convex.core.data.ACell;
import convex.core.data.AVector;
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
	
	public static Engine create() {
		return new Engine();
	}
	
	public AVector<ACell> loadChunk(long x, long y, long z) {
		x=x&~0xFL;
		y=y&~0xFL;
		z=z&~0xFL;
		Result r;
		try {
			r = convex.querySync(Reader.read("(call #4411 (get-chunk ["+x+" "+y+" "+z+"]))"));
		} catch (TimeoutException | IOException e) {
			throw Utils.sneakyThrow(e);
		}
		if (r.isError()) throw new Error("Bad result: "+r);
		return r.getValue();
	}
	
	
	public static void main(String[] args) throws TimeoutException, IOException {
		Engine e=new Engine();
		System.out.println(e.loadChunk(0,0,0));
	}

}
