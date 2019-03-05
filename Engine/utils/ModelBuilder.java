package utils;

import org.joml.Vector3f;

import pipeline.Model;
import proceduralgen.GenTerrain;
import scene.world.terrain.Terrain;
import scene.world.terrain.biome.Biome;

public class ModelBuilder {
	
	private static int[] indices = genIndexPattern(Terrain.vertexStripeSize);

	public static Model buildHeightmap(int x, int z, Biome[] biomes, float[][] chunkHeightmap,float polySize) {
		return buildHeightmap(x,z,biomes,chunkHeightmap,(byte)0,false,polySize);
	}
	
	public static Model buildHeightmap(int x, int z, Biome[] biomes, float[][] chunkHeightmap, byte chunkFlags, boolean hasHeightmap, float polySize) {
		float[] vertices, uvs, normals;
		int v, u, n;
		final int vertexStripSize = chunkHeightmap.length;
		final float heightmapSize = polySize * (vertexStripSize-1);
		
		vertices = new float[(3 * vertexStripSize*vertexStripSize)];
		uvs = new float[(2 * vertexStripSize*vertexStripSize)];
		normals = new float[vertices.length];
		
		//int relX = (x*(vertexStripSize-1));
		//int relZ = (z*(vertexStripSize-1));
		Vector3f normal = new Vector3f();
		
		v = u = n = 0;
		for(int i = 0; i < vertexStripSize; i++) {
			for(int j = 0; j < vertexStripSize; j++) {
				
				float nx = (x*heightmapSize) + (j*polySize);
				float nz = (z*heightmapSize) + (i*polySize);
				
				Vector3f pos = new Vector3f(nx, chunkHeightmap[j][i], nz);
				vertices[v++] = pos.x;
				vertices[v++] = pos.y;
				vertices[v++] = pos.z;
			
				float ty = 1 - (j%2); //j
				float tx = (i%2); // i
				
				uvs[u++] = tx;
				uvs[u++] = ty;

				normal = getNormal(chunkHeightmap,biomes,j,i,x*(vertexStripSize-1),z*(vertexStripSize-1),chunkFlags,hasHeightmap);
				normals[n++] = normal.x;
				normals[n++] = normal.y;
				normals[n++] = normal.z;
			}
		}
		
		Model vao = buildVao(vertices, uvs, normals, indices);
		return vao;
	}
	
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
	
	// Todo: optimize
	private static Vector3f getNormal(float[][] data, Biome[] biomes, int x, int z, int nx, int nz,byte chunkFlags, boolean hasHeightmap) {
		float hL = getHeight(data, biomes,x-1,z,nx,nz,chunkFlags,hasHeightmap);
		float hR = getHeight(data, biomes,x+1,z,nx,nz,chunkFlags,hasHeightmap);
		float vL = getHeight(data, biomes,x,z-1,nx,nz,chunkFlags,hasHeightmap);
		float vR = getHeight(data, biomes,x,z+1,nx,nz,chunkFlags,hasHeightmap);
		
		return new Vector3f(hL - hR,
				2f,
				vL - vR).normalize();
	}
	
	private static float getHeight(float[][] data, Biome[] biomes, int x, int z, int nx, int nz,byte chunkFlags,boolean hasHeightmap) {
		if (x < 0 || z < 0 || x >= Terrain.vertexStripeSize || z >= Terrain.vertexStripeSize) {
			boolean l = ((chunkFlags >> 3) & 1)==1;
			boolean r = ((chunkFlags >> 2) & 1)==1;
			boolean t = ((chunkFlags >> 1) & 1)==1;
			boolean b = ((chunkFlags >> 0) & 1)==1;
			if (hasHeightmap) {
				if (chunkFlags != 0 && !l && !r && !t && !b) {
					/*if (x<0 && z<0 && ((chunkFlags >> 7) & 1)==1) {
						return data[(x<0)?0:(x>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:x)][(z<0)?0:(z>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:z)];
					}
					else if (x>=Terrain.vertexStripeSize && ((chunkFlags >> 6) & 1)==1) {
						return data[(x<0)?0:(x>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:x)][(z<0)?0:(z>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:z)];
					}
					
					if (z<0 && ((chunkFlags >> 5) & 1)==1) {
						return data[(x<0)?0:(x>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:x)][(z<0)?0:(z>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:z)];
					}
					else if (z>=Terrain.vertexStripeSize && ((chunkFlags >> 4) & 1)==1) {
						return data[(x<0)?0:(x>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:x)][(z<0)?0:(z>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:z)];
					}*/
					if (x<0||x>=Terrain.vertexStripeSize||z<0||z>=Terrain.vertexStripeSize) {
						return data[0][0];//data[(x<0)?0:(x>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:x)][(z<0)?0:(z>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:z)];
					}
				} else {
				
					if (x<0 && l) {
						return data[0][(z<0)?0:(z>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:z)];
					}
					else if (x>=Terrain.vertexStripeSize && r) {
						return data[Terrain.vertexStripeSize-1][(z<0)?0:(z>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:z)];
					}
					
					if (z<0 && t) {
						return data[(x<0)?0:(x>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:x)][0];
					}
					else if (z>=Terrain.vertexStripeSize && b) {
						return data[(x<0)?0:(x>=Terrain.vertexStripeSize?Terrain.vertexStripeSize-1:x)][Terrain.vertexStripeSize-1];
					}
				}
			}
			
			return GenTerrain.getHeightAt(nx+x, nz+z,biomes);
		}
		else {
			float height = data[x][z];
			//height += Colors.MAX_PIXEL_COLOR/2f;
			//height /= Colors.MAX_PIXEL_COLOR/2f;
			//height *= 40;
			return height;
		}
	}

	private static Model buildVao(float[] vertices, float[] uvs, float[] normals, int[] indices) {
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
	}
	
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
