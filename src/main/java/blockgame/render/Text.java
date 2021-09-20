package blockgame.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.FloatBuffer;

import blockgame.assets.Assets;

public class Text {
	
	static FloatBuffer fb=FloatBuffer.allocate(10000);

	static Texture texture=Texture.createTexture(Assets.font);
	static int textVBO;
	
	public static final int FLOATS_PER_VERTEX=3+2; // position + texture

	
	private static FloatBuffer addChar(FloatBuffer fb, float x, float y, float s, int ch) {
		float TD=1.0f/16.0f;
		float tx=(ch&0xF)*TD;
		float ty=((ch&0xF0)>>4)*TD;
		
		
		// Vertices in square numbered clockwise 0,1,2,3
		// 0,1,3
		fb.put(x).put(y).put(0).put(tx).put(ty);
		fb.put(x+s).put(y).put(0).put(tx+TD).put(ty);
		fb.put(x).put(y+s).put(0).put(tx).put(ty+TD);
		// 3,1,2
		fb.put(x).put(y+s).put(0).put(tx).put(ty+TD);
		fb.put(x+s).put(y).put(0).put(tx+TD).put(ty);
		fb.put(x+s).put(y+s).put(0).put(tx+TD).put(ty+TD);
		
		return fb;
	}
	
	public static void init() {
		textVBO = glGenBuffers();
	}
	
	public static void draw() {
		Text.texture.bind();

		fb.flip();
		int n=fb.remaining();
		
		FloatBuffer vertexBuffer = memAllocFloat(n);
		vertexBuffer.put(fb);
		vertexBuffer.flip();

		glBindBuffer(GL_ARRAY_BUFFER, textVBO);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		memFree(vertexBuffer);
		
		int stride=FLOATS_PER_VERTEX*4; // stride in bytes
		
		// define vertex format, should be after glBindBuffer
		// position is location 0
		glVertexAttribPointer(HUD.h_vs_inputPosition,3,GL_FLOAT,false,stride,0L); // Note: stride in bytes
        glEnableVertexAttribArray(HUD.h_vs_inputPosition);
        
        // texture is location 1
        glVertexAttribPointer(HUD.h_vs_texturePosition,2,GL_FLOAT,false,stride,12L); // Note: stride in bytes
        glEnableVertexAttribArray(HUD.h_vs_texturePosition);

		glDrawArrays(GL_TRIANGLES, 0, n/FLOATS_PER_VERTEX);
		
		fb.clear();
	}
	
	public static void addText(float x, float y,String s) {
		float size=32f;
		String[] ss=s.split("\n");
		int lines=ss.length;
		for (int j=0; j<lines; j++) {
			String t=ss[j];
			int n=t.length();
			for (int i=0; i<n; i++) {
				addChar(fb,x+size*i*0.75f,y+size*j,30f,(int)t.charAt(i));
			}
		}
	}
}
