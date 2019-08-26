package utils;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternion;
import org.joml.Vector3f;

import animation.Animation;
import animation.components.JointTransform;
import animation.components.Keyframe;
import pipeline.Resources;

public class AniFileLoader {
	public static byte EXPECTED_VERSION = 1; // Version of .MOD files that this game supports
	
	public static void readAniFile(String key, String path) {
		DataInputStream is = null;
		try {
			String fullUrl = "src/res/" + path;
			is = new DataInputStream(new FileInputStream(fullUrl));
			
			String fileExtName = "" + is.readChar() + is.readChar() + is.readChar();
			byte version = is.readByte();
			
			if (version != EXPECTED_VERSION) {
				System.out.println("NOT RIGHT VERSION");
				return;
			}
			
			if (!fileExtName.equals("ANI")) {
				System.out.println("NOT AN ANI FILE");
				return;
			}
			
			byte numAnimations = is.readByte();
			
			for(int i = 0; i < numAnimations; i++) {
				extractAnimationData(key, is);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void extractAnimationData(String key, DataInputStream is) throws IOException {
		short numKeyframes = is.readShort();
		float animationDuration = is.readFloat();
		
		Keyframe[] keyframes = new Keyframe[numKeyframes];

		for(int i = 0; i < numKeyframes; i++) {
			
			float time = is.readFloat();
			byte numTransforms = is.readByte();
			Map<Byte, JointTransform> jointTransforms = new HashMap<Byte, JointTransform>();
			
			for(int j = 0; j < numTransforms; j++) {
				byte index = is.readByte();
				Matrix4f transform = FileUtils.readMatrix4f(is);
				
				jointTransforms.put(index, createJointTransform(transform));
			}
			
			keyframes[i] = new Keyframe(time, jointTransforms);
		}
		
		Resources.addAnimation(key, new Animation(animationDuration, keyframes));
	}
	
	private static JointTransform createJointTransform(Matrix4f mat) {
		Vector3f translation = new Vector3f(mat.m30, mat.m31, mat.m32);
		Quaternion rotation = Quaternion.fromMatrix(mat);
		return new JointTransform(translation, rotation);
	}
}
