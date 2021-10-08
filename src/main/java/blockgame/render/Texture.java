package blockgame.render;

import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class Texture {

	int texture;
	int w;
	int h;
	
	public Texture(int texture, int w, int h) {
		this.texture=texture;
		this.w=w;
		this.h=h;
	}
	
	public static Texture createTexture(BufferedImage bi) {
		int w=bi.getWidth();
		int h=bi.getHeight();
		IntBuffer data=BufferUtils.createIntBuffer(w*h);
	         
        int[] argb=new int [w*h];
        bi.getRGB(0, 0, w, h, argb, 0, w);
        for (int i=0; i<argb.length; i++) {
        	int col=argb[i];
        	col=Integer.rotateLeft(col, 8); // rotate to RGBA
        	// col|=0xFF; // Max alpha
        	col=Integer.reverseBytes(col);
        	argb[i]=col;
        }
        
        data.put(argb);
        data.flip();
    
        int id = glGenTextures();
        System.out.println("Loaded texture: "+id);
                
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w,h, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        return new Texture(id,w,h);
    }
	
	public void bind() {
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
 		
	}
	
	/**
	 * Texture tile size
	 */
	public static final float TD = 1.0f / 128;

	
	public static float tx(long texRef) {
		return (texRef & 0xFF) * TD;
	}
	
	public static float ty(long texRef) {
		return ((texRef & 0xFF00) >> 8) * TD;
	}
	



}
