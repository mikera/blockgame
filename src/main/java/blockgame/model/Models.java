package blockgame.model;

import java.io.IOException;

import blockgame.assets.Assets;
import blockgame.model.obj.Obj;
import blockgame.model.obj.ObjLoader;
import blockgame.render.Texture;
import convex.core.util.Utils;

public class Models {

	private static final ObjLoader LOADER=new ObjLoader();

	public static final Model PLAYER = loadModel("models/player.obj");

	// Created lazily on first use: requires a current GL context
	private static Texture playerTex=null;

	private static synchronized Texture playerTexture() {
		if (playerTex==null) playerTex=Texture.createTexture(Assets.player);
		return playerTex;
	}

	private static Model loadModel(String path) {
		Obj obj;
		try {
			obj = LOADER.loadModel(path);
			Model model= Model.fromObj(obj);
			model.setTexture(Models::playerTexture);
			return model;
		} catch (IOException e) {
			throw Utils.sneakyThrow(e);
		}
	}

}
