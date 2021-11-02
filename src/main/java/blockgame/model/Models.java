package blockgame.model;

import java.io.IOException;

import blockgame.model.obj.Obj;
import blockgame.model.obj.ObjLoader;
import convex.core.util.Utils;

public class Models {
	
	private static final ObjLoader LOADER=new ObjLoader();

	public static final Model PLAYER = loadModel("models/player.obj");

	private static Model loadModel(String path) {
		Obj obj;
		try {
			obj = LOADER.loadModel(path);
			return Model.fromObj(obj);
		} catch (IOException e) {
			throw Utils.sneakyThrow(e);
		}
	}

}
