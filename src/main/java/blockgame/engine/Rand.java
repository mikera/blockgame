package blockgame.engine;

public class Rand {

	public static long xorshift64(long x) {
		x ^= x << 13;
		x ^= x >> 7;
		x ^= x << 17;
		return x;
	}

	public static int rint(int max, long seedx) {
		long x=76756;
		x=xorshift64(x^seedx);
		return Math.floorMod((int)xorshift64(x),max);
	}

	public static int rint(int max, long seedx, long seedy) {
		long x=seedx*17+seedy;
		x=xorshift64(x^seedx);
		x=xorshift64(x^seedy);
		return Math.floorMod((int)xorshift64(x),max);
	}

	public static int rint(int max, long seedx, long seedy, long seedz) {
		long x=seedx*5787591+seedy*19+seedz;
		x=xorshift64(x^seedx);
		x=xorshift64(x^seedy);
		x=xorshift64(x^seedz);
		return Math.floorMod((int)xorshift64(x),max);
	}

}
