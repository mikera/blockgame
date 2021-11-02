package blockgame.model.obj;

/*
* The MIT License (MIT)
*
* Copyright (c) 2014 Matthew 'siD' Van der Bijl
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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_FLAT;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;

public class Obj extends Object {

    private final List<Vector3f> vertices;
    private final List<Vector2f> textureCoords;
    private final List<Vector3f> normals;
    private final HashMap<String,List<Face>> objects;
    private boolean enableSmoothShading;

    public Obj(List<Vector3f> vertices, List<Vector2f> textureCoords,
            List<Vector3f> normals, HashMap<String,List<Face>> objects, boolean enableSmoothShading) {
        super();

        this.vertices = vertices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.objects = objects;
        this.enableSmoothShading = enableSmoothShading;
    }

    public Obj() {
        this(new ArrayList<Vector3f>(), new ArrayList<Vector2f>(),
                new ArrayList<Vector3f>(), new HashMap<>(), true);
    }

    public void enableStates() {
        if (this.isSmoothShadingEnabled()) {
            GL11.glShadeModel(GL_SMOOTH);
        } else {
            GL11.glShadeModel(GL_FLAT);
        }
    }

    public boolean hasTextureCoordinates() {
        return this.getTextureCoordinates().size() > 0;
    }

    public boolean hasNormals() {
        return this.getNormals().size() > 0;
    }

    public List<Vector3f> getVertices() {
        return this.vertices;
    }

    public List<Vector2f> getTextureCoordinates() {
        return this.textureCoords;
    }

    public List<Vector3f> getNormals() {
        return this.normals;
    }

    public HashMap<String,List<Face>> getFaces() {
        return this.objects;
    }

    public boolean isSmoothShadingEnabled() {
        return this.enableSmoothShading;
    }

    public void setSmoothShadingEnabled(boolean isSmoothShadingEnabled) {
        this.enableSmoothShading = isSmoothShadingEnabled;
    }

    public static class Face extends Object {

        private final int[] vertexIndices;
        private final int[] normalIndices;
        private final int[] textureCoordinateIndices;

        public boolean hasNormals() {
            return this.normalIndices != null;
        }

        public boolean hasTextureCoords() {
            return this.textureCoordinateIndices != null;
        }

        public int[] getVertices() {
            return this.vertexIndices;
        }

        public int[] getTextureCoords() {
            return this.textureCoordinateIndices;
        }

        public int[] getNormals() {
            return this.normalIndices;
        }

        public Face(int[] vertexIndices, int[] textureCoordinateIndices,
                int[] normalIndices) {
            super();

            this.vertexIndices = vertexIndices;
            this.normalIndices = normalIndices;
            this.textureCoordinateIndices = textureCoordinateIndices;
        }

        @Override
        public String toString() {
            return String.format("Face[vertexIndices%s normalIndices%s textureCoordinateIndices%s]",
                    Arrays.toString(vertexIndices), Arrays.toString(normalIndices), Arrays.toString(textureCoordinateIndices));
        }
    }

	public Vector3f getVertex(int vi) {
		return getIndexed(vertices,vi);
	}
	
	public Vector3f getNormal(int ni) {
		return getIndexed(normals,ni);
	}

	public Vector2f getTextureCoordinate(int ti) {
		return getIndexed(textureCoords,ti);
	}

	private <T> T getIndexed(List<T> vals, int ix) {
		// Note obj indexing from 1, negative means from end of list
		if (ix>0) {
			return vals.get(ix-1);
		} else if (ix<0) {
			return vals.get(vals.size()+ix);
		} else {
			return null;
		}
	}



}