package utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import animation.components.Joint;
import animation.components.Skeleton;
import scene.object.Model;

public class ModFileLoader {
	public static byte EXPECTED_VERSION = 1; // Version of .MOD files that this game supports
	
	/** Load a .MOD file
	 * 
	 * @param path the to the file's directory within the res folder, for example, setting this to "weps/gun.mod" would point to a file called "gun.mod" in the res/weps folder
	 * @param saveVertexData setting this to true bakes the vertex data into the model
	 * @return
	 */
	public static Model readModFile(String key, String path, boolean saveVertexData) {
		try {
			return readModFile(key, new DataInputStream(new FileInputStream("src/res/" + path)), saveVertexData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Model readModFile(String key, byte[] data, boolean saveVertexData) {
		return readModFile(key, new DataInputStream(new ByteArrayInputStream(data)), saveVertexData);
	}
	
	public static Model readModFile(String key, DataInputStream is, boolean saveVertexData) {
		Model model = null;
		
		try {
			
			// Header
			String fileExtName = "" + is.readChar() + is.readChar() + is.readChar();
			byte version = is.readByte();
			byte flags = is.readByte();
			
			if (version != EXPECTED_VERSION) {
				return null;
			}
			
			if (!fileExtName.equals("MOD")) {
				return null;
			}
			
			model = (((byte)flags) & (0x01 << 0)) == 1 ?
				  extractModelData(key, is, saveVertexData)
				: extractStaticModelData(is, saveVertexData);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return model;
	}

	private static Model extractModelData(String key, DataInputStream is, boolean saveVertexData) throws IOException {
		int vertexCount = is.readInt();
		int indexCount = is.readInt();
		
		// Mesh data
		float[] vertices = new float[vertexCount*3];
		float[] uvs = new float[vertexCount*2];
		float[] normals = new float[vertexCount*3];
		int[] indices = new int[indexCount];
		int[] jointIds = new int[vertexCount*3];
		float[] weights = new float[vertexCount*3];
		//float furthestPoint = is.readFloat();
		//////
		
		int i;
		for(i = 0; i < vertexCount; i++) {
			vertices[(i*3)+0] 	= is.readFloat();
			vertices[(i*3)+1] 	= is.readFloat();
			vertices[(i*3)+2] 	= is.readFloat();

			/*
			vertices[(i*3)+2] 	= -is.readFloat();
			vertices[(i*3)+1] 	= is.readFloat();
			vertices[(i*3)+0] 	= -is.readFloat();
			 */
			uvs[(i*2)+0] 		= is.readFloat();
			uvs[(i*2)+1] 		= is.readFloat();
			normals[(i*3)+0] 	= is.readFloat();
			normals[(i*3)+1] 	= is.readFloat();
			normals[(i*3)+2] 	= is.readFloat();
			
			jointIds[(i*3)+0] 	= is.readByte();
			jointIds[(i*3)+1] 	= is.readByte();
			jointIds[(i*3)+2] 	= is.readByte();
			weights[(i*3)+0] 	= is.readFloat();
			weights[(i*3)+1] 	= is.readFloat();
			weights[(i*3)+2] 	= is.readFloat();
			
			//indices[i] = is.readInt();
		}
		
		for(i = 0; i < indexCount; i++) {
			indices[i] = is.readInt();
		}
		
		Vector3f min = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
		Vector3f max = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
		
		byte numJoints = is.readByte();
		
		//Resources.addModel("", path);
		Model model = Model.create();
		model.bind();
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, uvs, 2);
		model.createAttribute(2, normals, 3);
		model.createAttribute(3, jointIds, 3);
		model.createAttribute(4, weights, 3);
		model.createIndexBuffer(indices);
		model.setSkeleton(new Skeleton(numJoints, getJoints(is)));
		model.unbind();
		
		model.min = min;
		model.max = max;
		//model.getSkeleton().getRootJoint().getInverseBindTransform().rotateY(90);
		
		if (saveVertexData) {
			model.setVertexData(indices, vertices);
		}
		
		byte numAnimations = is.readByte();
		
		for(int a = 0; a < numAnimations; a++) {
			AniFileLoader.extractAnimationData(key, is);
		}
		
		return model;
	}
	
	private static Model extractStaticModelData(DataInputStream is, boolean saveVertexData) throws IOException {
		int vertexCount = is.readInt();
		int indexCount = is.readInt();
		
		// Mesh data
		float[] vertices = new float[vertexCount*3];
		float[] uvs = new float[vertexCount*2];
		float[] normals = new float[vertexCount*3];
		int[] indices = new int[indexCount];
		//////
		
		int i;
		for(i = 0; i < vertexCount; i++) {
			vertices[(i*3)+0] 	= is.readFloat();
			vertices[(i*3)+1] 	= is.readFloat();
			vertices[(i*3)+2] 	= is.readFloat();
			uvs[(i*2)+0] 		= is.readFloat();
			uvs[(i*2)+1] 		= is.readFloat();
			normals[(i*3)+0] 	= is.readFloat();
			normals[(i*3)+1] 	= is.readFloat();
			normals[(i*3)+2] 	= is.readFloat();
			
			indices[i] = is.readInt();
		}
		
		for(; i < indexCount; i++) {
			indices[i] = is.readInt();
		}
		
		Vector3f min = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
		Vector3f max = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
		
		Model model = Model.create();
		model.bind();
		model.createAttribute(0, vertices, 3);
		model.createAttribute(1, uvs, 2);
		model.createAttribute(2, normals, 3);
		model.createIndexBuffer(indices);
		model.unbind();
		
		model.min = min;
		model.max = max;
		
		if (saveVertexData) {
			model.setVertexData(indices, vertices);
		}
		return model;
	}

	private static Joint getJoints(DataInputStream is) throws IOException {
		byte id = is.readByte();
		String name = FileUtils.readString(is);
		Matrix4f matrix = FileUtils.readMatrix4f(is);
		
		Joint joint = new Joint(id, name, matrix);
		byte numChildren = is.readByte();
		for(int i = 0; i < numChildren; i++) {
			joint.addChild(getJoints(is));
		}
		
		return joint;
	}

	
}
