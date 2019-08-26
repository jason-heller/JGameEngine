package utils;

import org.joml.Vector3f;

import scene.object.Model;

public class ModelBuilder {
	
	public static Model buildQuad(float[] vertices) {
		float[] normals = new float[vertices.length];
		Vector3f normal = getNormal(vertices, 0);
		for(int i = 0; i < 4; i++) {
			normals[i*3+0] = normal.x;
			normals[i*3+1] = normal.y;
			normals[i*3+2] = normal.x;
		}
		
		Model model = Model.create();
		model.bind();
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, new float[] {0,0, 1,0, 0,1, 1,1}, 2);
		model.createAttribute(2, normals, 3);
		model.createIndexBuffer(new int[] {0,1,3,3,1,2});
		model.unbind();
		
		return model;
	}
	
	private static Vector3f getNormal(float[] v, int i) {
		Vector3f p1 = new Vector3f(v[i-9],v[i-8],v[i-7]);
		Vector3f p2 = new Vector3f(v[i-6],v[i-5],v[i-4]);
		Vector3f p3 = new Vector3f(v[i-3],v[i-2],v[i-1]);
		
		return Vector3f.cross(Vector3f.sub(p2, p1), Vector3f.sub(p3, p1)).normalize();
	}
	
	/*private static Model buildVao(float[] vertices, float[] uvs, float[] normals, int[] indices) {
		Model vao = Model.create();
		vao.bind();
		vao.createAttribute(0, vertices, 3);
		vao.createAttribute(1, uvs, 2);
		vao.createAttribute(2, normals, 3);
		vao.createIndexBuffer(indices);
		vao.unbind();
		
		//vao.setVertices(vertices);
		//vao.setIndices(indices);
		
		return vao;
	}*/
	
	public static int[] genIndexPattern(int vertexStripSize) {
		int pointer = 0;
		int[] indices = new int[6*(vertexStripSize-1)*(vertexStripSize-1)];
		for(int gz=0;gz<vertexStripSize-1;gz++){
			for(int gx=0;gx<vertexStripSize-1;gx++){
				int topLeft = (gz*vertexStripSize)+gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz+1)*vertexStripSize)+gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		
		return indices;
	}
}
