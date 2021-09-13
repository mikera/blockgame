package blockgame;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import blockgame.engine.Engine;
import blockgame.engine.Engine.HitResult;

public class EngineTest {
	@Test public void testIntersect() {
		HitResult hr=new HitResult();
		
		Engine.intersect(new Vector3f(0.5f,0.5f,0.5f), new Vector3f(1,0,0), (x,y,z)-> (x>=3)?x:null, hr);
		assertEquals(3,hr.hit);
		assertEquals(2.5,hr.distance);
		assertEquals(4,hr.face);
		assertEquals(3,hr.x);
		assertEquals(0,hr.y);
		assertEquals(0,hr.z);
		
		Engine.intersect(new Vector3f(0.5f,0.5f,0.5f), new Vector3f(1,0.3f,0), (x,y,z)-> (x>=3)?x:null, hr);
		assertEquals(3,hr.hit);
		assertEquals(4,hr.face);
		assertEquals(3,hr.x);
		assertEquals(1,hr.y);
		assertEquals(0,hr.z);

		Engine.intersect(new Vector3f(0.5f,0.5f,0.5f), new Vector3f(-1,0,0), (x,y,z)-> (x>=3)?x:null, hr);
		assertEquals(null,hr.hit);
		assertEquals(2,hr.face);
		assertEquals(0,hr.y);
		assertEquals(0,hr.z);
		
		Engine.intersect(new Vector3f(0.5f,0.5f,0.5f), new Vector3f(0,0,-1), (x,y,z)-> (z<=-3)?z:null, hr);
		assertEquals(-3,hr.hit);
		assertEquals(2.5,hr.distance);
		assertEquals(0,hr.face);
		assertEquals(0,hr.x);
		assertEquals(0,hr.y);
		assertEquals(-3,hr.z);
	}
}
