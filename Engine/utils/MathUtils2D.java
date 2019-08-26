package utils;

import org.joml.Vector2f;

public class MathUtils2D {
	
	public static Vector2f lineIntersection(Vector2f line1Start, Vector2f line1End, Vector2f line2Start, Vector2f line2End) {
		float a1 = line1End.y - line1Start.y;
		float b1 = line1Start.x - line1End.x;
		float c1 = a1 * line1Start.x + b1 * line1Start.y;
 
		float a2 = line2End.y - line2Start.y;
		float b2 = line2Start.x - line2End.x;
		float c2 = a2 * line2Start.x + b2 * line2Start.y;
 
		float delta = a1 * b2 - a2 * b1;
        return new Vector2f((b2 * c1 - b1 * c2) / delta, (a1 * c2 - a2 * c1) / delta);
    }
	
	// Only works without zero-slope or no-slope
	public static Vector2f lineIntersection(float m1, float b1, float m2, float b2) {
		float x = (b2 - b1) / (m1 - (m2));
	    float y = m1 * x + b1;

	    return new Vector2f(x, y);
	}

	public static float distance(float x, float y, float x2, float y2) {
		return (float)Math.sqrt(distanceSquared(x,y,x2,y2));
	}

	public static float distanceSquared(float x, float y, float x2, float y2) {
		float dx = x2-x;
		float dy = y2-y;
		
		return dx*dx + dy*dy;
	}
}
