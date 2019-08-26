package utils;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MathUtils {
	public static final double TAU = Math.PI*2.0;

	public static float lerp(float s, float t, float amount) {
		return s * (1f-amount) + t * amount;
	}
	
	public static float sCurveLerp(float s, float t, float amount) {
		float amtSqr = amount*amount;
		return lerp(s,t,(-2*(amtSqr*amount)) + (3*amtSqr));
	}
	
	public static Vector3f reflect(Vector3f vector, Vector3f normal) {
		//2*(V dot N)*N - V
		float dp = 2f * Vector3f.dot(vector, normal);
		Vector3f s = Vector3f.mul(normal, dp);
		return Vector3f.sub(vector, s);
	}
	
	public static Vector3f getDirection(Matrix4f matrix) {
		Matrix4f inverse = new Matrix4f();
		matrix.invert(inverse);
		
		return new Vector3f(inverse.m20, inverse.m21, inverse.m22);
	}
	
	public static Matrix4f lookAt(Vector3f eye, Vector3f center, Vector3f up) {
		Vector3f forward = new Vector3f(center).sub(eye).normalize();
		Vector3f side = new Vector3f(forward).cross(up).normalize();
		up = new Vector3f(side).cross(forward);

		Matrix4f matrix = new Matrix4f();
		matrix.m00 = side.x;
		matrix.m01 = side.y;
		matrix.m02 = side.z;
		matrix.m10 = up.x;
		matrix.m11 = up.y;
		matrix.m12 = up.z;
		matrix.m20 = -forward.x;
		matrix.m21 = -forward.y;
		matrix.m22 = -forward.z;
		return matrix;
	}
	
	public static float barycentric(float x, float y, Vector3f p1, Vector3f p2, Vector3f p3) {
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1 = ((p2.z - p3.z) * (x - p3.x) + (p3.x - p2.x) * (y - p3.z)) / det;
		float l2 = ((p3.z - p1.z) * (x - p3.x) + (p1.x - p3.x) * (y - p3.z)) / det;
		float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}

	public static float angleLerp(float start, float end, float amount) {
		start += 360;
		end += 360;
		start %= 360;
		end %= 360;
		
		if (end-start < 180) {
			return lerp(start,end,amount);
		} else {
			return lerp(start+360,end,amount);
		}
	}
	
	public static float pointDirection(float x1, float y1, float z1, float x2, float y2, float z2) {
		float dist = (float) Math.sqrt((x2 - x1) * (x2 - x1)
                + (y2 - y1) * (y2 - y1));
		return (float) Math.atan2(z2-z1,dist);
	}
	
	public static float pointDirection(float x1, float y1, float x2, float y2) {
		float dx, dy;
		dy = y2 - y1;
		dx = x2 - x1;

		return (float) -Math.toDegrees(Math.atan2(dy, dx));
	}
	
	public static Vector3f eulerToVectorDeg(float yaw, float pitch) {
		return eulerToVectorRad((float)Math.toRadians(yaw), (float)Math.toRadians(pitch));
	}

	public static Vector3f eulerToVectorRad(float yaw, float pitch) {
		float xzLen = (float)Math.cos(pitch);
		return new Vector3f(-xzLen * (float)Math.sin(yaw),
							(float)Math.sin(pitch),
							xzLen * (float)Math.cos(yaw));
	}

	public static float clamp(float f, float min, float max) {
		return Math.min(Math.max(f, min), max);
	}

	public static float fastSqrt(float f) {
		return Float.intBitsToFloat(((Float.floatToIntBits(f)-(1<<52))>>1) + (1<<61));
	}

	public static Vector3f vectorToEurler(Vector3f v) {
		return new Vector3f((float)Math.atan(v.y/v.x), (float)Math.acos(v.z/v.length()), 0f);
	}
}
