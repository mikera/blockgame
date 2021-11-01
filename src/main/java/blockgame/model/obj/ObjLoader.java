
/*
* The MIT License (MIT)
*
* Copyright (c) 2014 Matthew 'siD' Van der Bijl, 2021 Mike Anderson
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*
*/
package blockgame.model.obj;

import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_SHININESS;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

/**
 * OBJloader class. Loads in Wavefront .obj file in to the program.
 *
 * @author Matthew Van der Bijl, Mike Anderson
 */
public class ObjLoader extends Object {

    /**
     * Constructs a new <code>OBJLoader</code>.
     */
    public ObjLoader() {
        super();
    }

    public int createDisplayList(Obj model) {
        int displayList = GL11.glGenLists(1);
        GL11.glNewList(displayList, GL_COMPILE);
        {
            this.render(model);
        }
        GL11.glEndList();
        return displayList;
    }

    /**
     * Renders a given <code>Obj</code> file.
     *
     * @param model the <code>Obj</code> file to be rendered
     */
    public void render(Obj model) {
        GL11.glMaterialf(GL_FRONT, GL_SHININESS, 120);
        GL11.glBegin(GL_TRIANGLES);
        {
        	for (Map.Entry<String,List<Obj.Face>> e:model.getFaces().entrySet()) {
	            for (Obj.Face face : e.getValue()) {
	                Vector3f[] normals = {
	                    model.getNormals().get(face.getNormals()[0] - 1),
	                    model.getNormals().get(face.getNormals()[1] - 1),
	                    model.getNormals().get(face.getNormals()[2] - 1)
	                };
	                Vector2f[] texCoords = {
	                    model.getTextureCoordinates().get(face.getTextureCoords()[0] - 1),
	                    model.getTextureCoordinates().get(face.getTextureCoords()[1] - 1),
	                    model.getTextureCoordinates().get(face.getTextureCoords()[2] - 1)
	                };
	                Vector3f[] vertices = {
	                    model.getVertices().get(face.getVertices()[0] - 1),
	                    model.getVertices().get(face.getVertices()[1] - 1),
	                    model.getVertices().get(face.getVertices()[2] - 1)
	                };
	                {
	                    GL11.glNormal3f(normals[0].x, normals[0].y, normals[0].z);
	                    GL11.glTexCoord2f(texCoords[0].x, texCoords[0].y);
	                    GL11.glVertex3f(vertices[0].x, vertices[0].y, vertices[0].z);
	                    GL11.glNormal3f(normals[1].x, normals[1].y, normals[1].z);
	                    GL11.glTexCoord2f(texCoords[1].x, texCoords[1].y);
	                    GL11.glVertex3f(vertices[1].x, vertices[1].y, vertices[1].z);
	                    GL11.glNormal3f(normals[2].x, normals[2].y, normals[2].z);
	                    GL11.glTexCoord2f(texCoords[2].x, texCoords[2].y);
	                    GL11.glVertex3f(vertices[2].x, vertices[2].y, vertices[2].z);
	                }
	            }
        	}
        }
        GL11.glEnd();
    }

    /**
     * @param file the file to be loaded
     * @return the loaded <code>Obj</code>
     * @throws java.io.FileNotFoundException thrown if the Obj file is not found
     */
    public Obj loadModel(File file) throws FileNotFoundException {
        return this.loadModel(new Scanner(file));
    }

    /**
     * @param stream the stream to be loaded
     * @return the loaded <code>Obj</code>
     */
    public Obj loadModel(InputStream stream) {
        return this.loadModel(new Scanner(stream));
    }

    /**
     * @param sc the <code>Obj</code> to be loaded
     * @return the loaded <code>Obj</code>
     */
    public Obj loadModel(Scanner sc) {
        Obj model = new Obj();
        String o="Default";
        while (sc.hasNextLine()) {
            String ln = sc.nextLine();
            if (ln == null || ln.equals("") || ln.startsWith("#")) {
            	// Skip empty lines
            } else {
                String[] split = ln.split(" ");
                switch (split[0]) {
                    case "v":
                        model.getVertices().add(new Vector3f(
                                Float.parseFloat(split[1]),
                                Float.parseFloat(split[2]),
                                Float.parseFloat(split[3])
                        ));
                        break;
                    case "vn":
                        model.getNormals().add(new Vector3f(
                                Float.parseFloat(split[1]),
                                Float.parseFloat(split[2]),
                                Float.parseFloat(split[3])
                        ));
                        break;
                    case "vt":
                        model.getTextureCoordinates().add(new Vector2f(
                                Float.parseFloat(split[1]),
                                Float.parseFloat(split[2])
                        ));
                        break;
                    case "f":
                    	
                    	List<Obj.Face> faces=model.getFaces().get(o);
                    	if (faces==null) {
                    		faces=new ArrayList<>();
                    		model.getFaces().put(o, faces);
                    	}
                        faces.add(new Obj.Face(
                                new int[]{
                                    Integer.parseInt(split[1].split("/")[0]),
                                    Integer.parseInt(split[2].split("/")[0]),
                                    Integer.parseInt(split[3].split("/")[0])
                                },
                                new int[]{
                                    Integer.parseInt(split[1].split("/")[1]),
                                    Integer.parseInt(split[2].split("/")[1]),
                                    Integer.parseInt(split[3].split("/")[1])
                                },
                                new int[]{
                                    Integer.parseInt(split[1].split("/")[2]),
                                    Integer.parseInt(split[2].split("/")[2]),
                                    Integer.parseInt(split[3].split("/")[2])
                                }
                        ));
                        break;
                    case "o":
                        o=split[1];
                        break;
                    case "s":
                        model.setSmoothShadingEnabled(!ln.contains("off"));
                        break;
                    case "usemtl":
                        // Ignore for now
                        break;
                    default:
                        System.err.println("[OBJ] Unknown Line: " + ln);
                }
            }
        }
        sc.close();
        return model;
    }

	public Obj loadModel(String path) throws IOException {
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
			if (inputStream == null) throw new IOException("Resource not found: " + path);
			return loadModel(inputStream);
		} 
	}
}
