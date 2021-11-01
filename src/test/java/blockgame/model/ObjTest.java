package blockgame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import blockgame.model.obj.Obj;
import blockgame.model.obj.ObjLoader;

public class ObjTest {

	@Test 
	public void testPlayerLoad() throws IOException {
		ObjLoader loader=new ObjLoader();
		
		Obj o=loader.loadModel("models/player.obj");
		
		assertEquals(96,o.getVertices().size());
	}
}
