package pipeline.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import animation.Keyframe;
import pipeline.Model;
import pipeline.Resources;
import utils.FileUtils;

public class ModelUtils {
	public static Model loadObj(String path, boolean saveData) {
		
		List<float[]> vertices = new ArrayList<float[]>();
		List<float[]> uvs = new ArrayList<float[]>();
		List<float[]> normals = new ArrayList<float[]>();
		
		List<int[]> indices = new ArrayList<int[]>();
		List<Byte> boneIndices = new ArrayList<Byte>();
		List<Integer> indexOrder = new ArrayList<Integer>();
		Vector3f max = new Vector3f(-1000000,-1000000,-1000000);
		Vector3f min = new Vector3f(1000000,1000000,1000000);
		
		boolean hasTexture = false;
		
		BufferedReader reader;
		
		try {
			reader = FileUtils.getReader(path);
			String line = reader.readLine();
			
			while (line != null) {
				String[] data = line.split(" ");
				
				if (data.length > 2) {
					if (data[0].equals("v")) {
						float[] vertex = new float[] {Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3])};
						vertices.add(vertex);
						min.x = Math.min(min.x, vertex[0]);
						min.y = Math.min(min.y, vertex[1]);
						min.z = Math.min(min.z, vertex[2]);
						max.x = Math.max(max.x, vertex[0]);
						max.y = Math.max(max.y, vertex[1]);
						max.z = Math.max(max.z, vertex[2]);
					}
					else if (data[0].equals("vt")) {
						float[] uvCoord = new float[] {Float.parseFloat(data[1]), Float.parseFloat(data[2])};
						hasTexture = true;
						uvs.add(uvCoord);
					}
					else if (data[0].equals("vn")) {
						float[] normal = new float[] {Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3])};
						normals.add(normal);
					}
					else if (data[0].equals("f")) {
						if (data.length > 4) { 
							int[] curIndices = new int[4];
							
							for(byte i = 1; i < data.length; i++) {
								String[] faceData = data[i].split("/");
								
								int[] index;
								if (hasTexture) index = new int[] {Integer.parseInt(faceData[0])-1, Integer.parseInt(faceData[1])-1, Integer.parseInt(faceData[2])-1};
								else index = new int[] {Integer.parseInt(faceData[0])-1, 0, Integer.parseInt(faceData[2])-1};
	
								int indexPosition = -1;
								
								for(int j = 0; j < indices.size(); j++) {
									int[] check = indices.get(j);
									if (check[0] == index[0] && check[1] == index[1] && check[2] == index[2]) {
										indexPosition = j;
										break;
									}
								}
								
								if (indexPosition == -1) {
									indices.add(index);
									curIndices[i-1] = indices.size()-1;
								}
								else {
									curIndices[i-1] = indexPosition;
								}
							}
							
							indexOrder.add(curIndices[0]);
							indexOrder.add(curIndices[1]);
							indexOrder.add(curIndices[3]);
							indexOrder.add(curIndices[3]);
							indexOrder.add(curIndices[1]);
							indexOrder.add(curIndices[2]);
						}
						else {
							for(byte i = 1; i < data.length; i++) {
								String[] faceData = data[i].split("/");
								int[] index = new int[] {Integer.parseInt(faceData[0])-1, Integer.parseInt(faceData[1])-1, Integer.parseInt(faceData[2])-1};
								
								int indexPosition = -1;
								
								for(int j = 0; j < indices.size(); j++) {
									int[] check = indices.get(j);
									if (check[0] == index[0] && check[1] == index[1] && check[2] == index[2]) {
										indexPosition = j;
										break;
									}
								}
								
								if (indexPosition == -1) {
									indices.add(index);
									indexOrder.add(indices.size()-1);	//index[0]
								}
								else {
									//indices.add(indices.get(indexPosition));
									indexOrder.add(indexPosition);	//indices.get(indexPosition)[0]
								}
							}
						}
					}
					else if (data[0].equals("b")) {
						boneIndices.add(Byte.parseByte(data[1]));
					}
				}
				
				line = reader.readLine();
			}
			
			reader.close();
			
			float[] vertexArray = new float[indices.size()*3];
			float[] uvArray = new float[indices.size()*2];
			float[] normalArray = new float[indices.size()*3];
			byte[] boneArray = new byte[boneIndices.size()]; 
			int[] indexArray = new int[indexOrder.size()];
			
			for(int i = 0; i < indexArray.length; i++) {
				indexArray[i] = indexOrder.get(i);//indices.get(indexOrder.get(i))[0];
			}
			
			for(int i = 0; i < vertexArray.length/3; i ++) {
				float[] vertex = vertices.get(indices.get(i)[0]);
				vertexArray[i*3+0] = vertex[0];
				vertexArray[i*3+1] = vertex[1];
				vertexArray[i*3+2] = vertex[2];
				
				float[] uv = uvs.get(indices.get(i)[1]);
				uvArray[i*2+0] = uv[0];
				uvArray[i*2+1] = 1-uv[1];
				
				float[] normal = normals.get(indices.get(i)[2]);
				normalArray[i*3+0] = normal[0];
				normalArray[i*3+1] = normal[1];
				normalArray[i*3+2] = normal[2];
				
				if (boneArray.length > 0) {
					boneArray[i] = boneIndices.get(i);
				}
			}

			Model model = Model.create();
			model.bind();
			model.createIndexBuffer(indexArray);
			model.createAttribute(0, vertexArray, 3);
			model.createAttribute(1, uvArray, 2);
			model.createAttribute(2, normalArray, 3);
			if (boneArray.length > 0) {
				model.createAttribute(3, boneArray, 1);
			}
			model.unbind();
			
			if (saveData) {
				model.setVertexData(indexArray, vertexArray);
			}
			
			return model;
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static Model quad2DModel() {
		Model model = Model.create();
		model.bind();
		model.createAttribute(0, new float[] {-0.5f,-0.5f, 0.5f,-0.5f, -0.5f,0.5f, 0.5f,0.5f}, 2);
		model.createAttribute(1, new float[] {0,0, 1,0, 0,1, 1,1}, 2);
		model.unbind();
		return model;
	}
	
	public static Model loadAnimatedModel(String mdlKey, File file) {
		if (file==null) return null;
		Model model = null;
		DataInputStream is;
		try {
			is = new DataInputStream(new FileInputStream(file));
			int numAnimations = is.readByte();
			model = loadModel(is, true, false);
			
			for(int anim = 0; anim < numAnimations; anim++) {
				loadAnimation(mdlKey,is);
			}
			
			is.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return model;
	}
	
	public static void loadAnimation(File file) {
		if (file==null) return;
		DataInputStream is;
		try {
			is = new DataInputStream(new FileInputStream(file));
			int numAnimations = is.readByte();
			
			for(int anim = 0; anim < numAnimations; anim++) {
				loadAnimation("",is);
			}
			
			is.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void loadAnimation(String mdlKey,DataInputStream is) throws IOException {
		short totalFrames 	= is.readShort();
		int animationEnd	= is.readInt();
		byte totalGroups	= is.readByte();
		
		String name = "";
		byte numChars = is.readByte();
		for (int i = 0; i < numChars; i++) {
			name += is.readChar();
		}

		Keyframe[] keyframes = new Keyframe[totalFrames];
		float[] times = new float[totalFrames+1];
		
		for(int i = 0; i < totalFrames; i++) {
			times[i] = is.readFloat();
			Matrix4f[] matrices = new Matrix4f[totalGroups];
			byte[] interps = new byte[totalGroups];
			
			for(int j = 0; j < totalGroups; j++) {
				matrices[j] = new Matrix4f(
						is.readFloat(),is.readFloat(),is.readFloat(),is.readFloat(),
						is.readFloat(),is.readFloat(),is.readFloat(),is.readFloat(),
						is.readFloat(),is.readFloat(),is.readFloat(),is.readFloat(),
						is.readFloat(),is.readFloat(),is.readFloat(),is.readFloat()
						);
				
				interps[j] = is.readByte();
			}
			
			keyframes[i] = new Keyframe(matrices, interps);
		}
		
		times[totalFrames] = animationEnd;
		
		Resources.addAnimation(mdlKey+"_"+name, times, keyframes);
	}

	public static Model loadStaticModel(File file, boolean saveData, boolean compressed) {
		if (file==null) return null;
		Model model = null;
		DataInputStream is;
		try {
			is = new DataInputStream(new FileInputStream(file));
			
			System.out.println("src/res/"+file.getPath() + ", "+ compressed);
			
			model = (!compressed) ? loadModel(is, false, saveData) : loadCompressedModel(is, saveData);
			
			is.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return model;
	}

	private static Model loadModel(DataInputStream is, boolean animated, boolean saveData) throws IOException {
		float[] vertices, uvs, normals;
		int[] indices, groups = null;
		
		int vertexCount = is.readInt();
		System.out.println(vertexCount);
		
		vertices 	= new float[vertexCount*3];
		uvs			= new float[vertexCount*2];
		normals		= new float[vertices.length];
		if (animated) {
			groups = new int[vertexCount];
		}
		
		for(int i = 0; i < vertexCount; i++) {
			vertices[(i*3)+0] = is.readFloat();
			vertices[(i*3)+1] = is.readFloat();
			vertices[(i*3)+2] = is.readFloat();
			
			uvs[(i*2)+0] = is.readFloat();
			uvs[(i*2)+1] = is.readFloat();
			
			normals[(i*3)+0] = is.readFloat();
			normals[(i*3)+1] = is.readFloat();
			normals[(i*3)+2] = is.readFloat();
			
			if (animated) {
				groups[i] = is.readInt();
			}
		}
		
		int indexCount		= is.readInt();
		indices = new int[indexCount];
		
		for(int i = 0; i < indexCount; i++) {
			indices[i] = is.readInt();
		}
		
		Model model = Model.create();
		model.bind();
		model.createIndexBuffer(indices);
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, uvs, 2);
		model.createAttribute(2, normals, 3);
		if (animated) {
			model.createAttribute(3, groups, 1);
			
		}
		model.unbind();
		
		if (saveData) {
			model.setVertexData(indices, vertices);
		}
		
		return model;
	}
	
	private static Model loadCompressedModel(DataInputStream is, boolean saveData) throws IOException {
		float[] vertices, uvs, normals;
		int[] indices;
		
		boolean hasNavMesh = false;
		int vertexCount = is.readShort();
		if (vertexCount < 0) {
			vertexCount = -vertexCount;
			hasNavMesh = true;
		}
		
		vertices 	= new float[vertexCount*3];
		uvs			= new float[vertexCount*2];
		normals		= new float[vertices.length];
		
		Vector3f max = new Vector3f(-1000000,-1000000,-1000000);
		Vector3f min = new Vector3f(1000000,1000000,1000000);
		
		for(int i = 0; i < vertexCount; i++) {
			//vertices[(i*3)+0] = is.readShort()/100f;
			//vertices[(i*3)+1] = is.readShort()/100f;
			//vertices[(i*3)+2] = is.readShort()/100f;
			vertices[(i*3)+0] = is.readFloat();
			vertices[(i*3)+1] = is.readFloat();
			vertices[(i*3)+2] = is.readFloat();
			min.x = Math.min(min.x, vertices[(i*3)+0]);
			min.y = Math.min(min.y, vertices[(i*3)+1]);
			min.z = Math.min(min.z, vertices[(i*3)+2]);
			max.x = Math.max(max.x, vertices[(i*3)+0]);
			max.y = Math.max(max.y, vertices[(i*3)+1]);
			max.z = Math.max(max.z, vertices[(i*3)+2]);
			
			/*uvs[(i*2)+0] = is.readByte()/255f;
			uvs[(i*2)+1] = is.readByte()/255f;
			
			normals[(i*3)+0] = is.readByte()/127f;
			normals[(i*3)+1] = is.readByte()/127f;
			normals[(i*3)+2] = is.readByte()/127f;*/
			
			uvs[(i*2)+0] = is.readFloat();
			uvs[(i*2)+1] = is.readFloat();
			
			normals[(i*3)+0] = is.readFloat();
			normals[(i*3)+1] = is.readFloat();
			normals[(i*3)+2] = is.readFloat();
		}
		
		int indexCount		= is.readShort();
		indices = new int[indexCount];
		
		for(int i = 0; i < indexCount; i++)
			indices[i] = is.readShort();
		
		Model model = Model.create();
		model.bind();
		model.createIndexBuffer(indices);
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, uvs, 2);
		model.createAttribute(2, normals, 3);
		model.unbind();
		
		if (hasNavMesh) {

			vertexCount = is.readShort();
			vertices 	= new float[vertexCount*3];
			
			max = new Vector3f(-1000000,-1000000,-1000000);
			min = new Vector3f(1000000,1000000,1000000);
			
			for(int i = 0; i < vertexCount; i++) {
				vertices[(i*3)+0] = is.readFloat();
				vertices[(i*3)+1] = is.readFloat();
				vertices[(i*3)+2] = is.readFloat();
				min.x = Math.min(min.x, vertices[(i*3)+0]);
				min.y = Math.min(min.y, vertices[(i*3)+1]);
				min.z = Math.min(min.z, vertices[(i*3)+2]);
				max.x = Math.max(max.x, vertices[(i*3)+0]);
				max.y = Math.max(max.y, vertices[(i*3)+1]);
				max.z = Math.max(max.z, vertices[(i*3)+2]);
			}
			
			indexCount		= is.readShort();
			indices = new int[indexCount];
			
			for(int i = 0; i < indexCount; i++) {
				indices[i] = is.readShort();
			}
			
			model.setVertexData(indices, vertices);
			model.setHeight(max.y-min.y);
		} else if (saveData) {
			model.setVertexData(indices, vertices);
			model.setHeight(max.y-min.y);
		}
		
		
		
		return model;
	}

	
}
