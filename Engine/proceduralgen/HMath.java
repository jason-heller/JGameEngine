package proceduralgen;

import org.joml.Vector2f;

public class HMath {
	
	public static final double TAU = Math.PI*2;

	public static boolean pointInConvexHull(float x, float y, Vector2f[] vertices) {

		boolean lastResult = pointInsideLine(x, y, vertices[0], vertices[1]);
		
		int len = vertices.length - 1;
		for(int i = 1; i < len; i++) {
			if (lastResult != pointInsideLine(x, y, vertices[i], vertices[i + 1])) {
                return false;
            }
		}

		return pointInsideLine(x, y, vertices[len], vertices[0]);
	}
	
	private static boolean pointInsideLine(float x, float y, Vector2f start, Vector2f end) {
		float vx = start.x - x;
		float vy = start.y - y;
		float ex = start.x - end.x;
		float ey = start.y - end.y;
		
		return ((ex * vy - ey * vx) >= 0f);
	}

	/*public static float[] sort(List<Vector3f> input, Vector3f center) {
		float[] output = new float[input.size() * 3];
		int ind = 0;

		while (input.size() > 1) {
			Vector3f a = input.get(0);
			for (int i = 1; i < input.size(); i++) {
				Vector3f b = input.get(i);
				if (less(a, b, center)) {
					a = b;
				}
			}
			output[ind++] = a.x;
			output[ind++] = a.y;
			output[ind++] = a.z;
			input.remove(a);
		}

		return output;
	}*/

	public static boolean less(Vector2f a, Vector2f b, Vector2f center) {
		if (a.x - center.x >= 0 && b.x - center.x < 0)
			return true;
		if (a.x - center.x < 0 && b.x - center.x >= 0)
			return false;
		if (a.x - center.x == 0 && b.x - center.x == 0) {
			if (a.y - center.y >= 0 || b.y - center.y >= 0)
				return a.y > b.y;
			return b.y > a.y;
		}

		// compute the cross product of vectors (center -> a) x (center -> b)
		float det = (a.x - center.x) * (b.y - center.y) - (b.x - center.x) * (a.y - center.y);
		if (det < 0f)
			return true;
		if (det > 0f)
			return false;

		// points a and b are on the same line from the center
		// check which point is closer to the center
		float d1 = (a.x - center.x) * (a.x - center.x) + (a.y - center.y) * (a.y - center.y);
		float d2 = (b.x - center.x) * (b.x - center.x) + (b.y - center.y) * (b.y - center.y);
		return d1 > d2;
	}

	public static boolean lineInRect(int rx, int ry, int rw, int rh, Vector2f p1, Vector2f p2) {
		Vector2f r1 = new Vector2f(rx,ry);
		Vector2f r2 = new Vector2f(rx,ry+rh);
		Vector2f r3 = new Vector2f(rx+rw,ry+rh);
		Vector2f r4 = new Vector2f(rx+rw,ry);
		
	    if(p1.x > r1.x && p1.x > r2.x && p1.x > r3.x && p1.x > r4.x && p2.x > r1.x && p2.x > r2.x && p2.x > r3.x && p2.x > r4.x ) return false;
	    if(p1.x < r1.x && p1.x < r2.x && p1.x < r3.x && p1.x < r4.x && p2.x < r1.x && p2.x < r2.x && p2.x < r3.x && p2.x < r4.x ) return false;
	    if(p1.y > r1.y && p1.y > r2.y && p1.y > r3.y && p1.y > r4.y && p2.y > r1.y && p2.y > r2.y && p2.y > r3.y && p2.y > r4.y ) return false;
	    if(p1.y < r1.y && p1.y < r2.y && p1.y < r3.y && p1.y < r4.y && p2.y < r1.y && p2.y < r2.y && p2.y < r3.y && p2.y < r4.y ) return false;


	    float f1 = (p2.y-p1.y)*r1.x + (p1.x-p2.x)*r1.y + (p2.x*p1.y-p1.x*p2.y);
	    float f2 = (p2.y-p1.y)*r2.x + (p1.x-p2.x)*r2.y + (p2.x*p1.y-p1.x*p2.y);
	    float f3 = (p2.y-p1.y)*r3.x + (p1.x-p2.x)*r3.y + (p2.x*p1.y-p1.x*p2.y);
	    float f4 = (p2.y-p1.y)*r4.x + (p1.x-p2.x)*r4.y + (p2.x*p1.y-p1.x*p2.y);

	    if(f1<0 && f2<0 && f3<0 && f4<0) return false;
	    if(f1>0 && f2>0 && f3>0 && f4>0) return false;

	    return true;
	}

	public static long szudzik(int x, int y) {
		//NXN -> N pairing funct
		return (y > x) ? ((y*y)+x) : ((x*x)+x+y);
	}
	
	public static boolean lineHullIntersection(float x1,float y1,float x2,float y2,Vector2f[] hull) {
		float dx = (x2-x1);
		float dy = (y1-y2);
		
		for(int i = 1; i < hull.length; i++) {
			if ((dy * (hull[i-1].x - x1) + dx * (hull[i-1].y - y1)) * (dy * (hull[i].x - x1) + dx * (hull[i].y - y1)) < 0) {
				return false;
			}
		}
		
		if (
			(dy*(hull[hull.length-1].x-x1)+dx*(hull[hull.length-1].y-y1))
		   *(dy*(hull[0].x-x1)+dx*(hull[0].y-y1)) < 0
				) {
			return false;
		}
		
		return true;
	}
}
