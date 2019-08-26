package logic.collision;

import org.joml.Vector3f;

public class Plane {
	public Vector3f normal;
	public float dist;
	
	public static final byte IN_FRONT = 0x0, BEHIND = 0x1, COPLANAR = 0x2;
	
	public Plane(Vector3f origin, Vector3f normal) {
		this.normal = normal;
		dist = (normal.x*origin.x+normal.y*origin.y
				+normal.z*origin.z);
		
	}
	
	public Plane(Vector3f normal, float equation) {
		this.normal = normal;
		this.dist = equation;
	}
	
	public void set(Vector3f p1, Vector3f p2, Vector3f p3) {
		this.normal = Vector3f.sub(p2, p1).cross(Vector3f.sub(p3, p1));
		dist = (normal.x*p1.x+normal.y*p1.y
				+normal.z*p1.z);
	}
	
	public Plane() {
		this.normal = new Vector3f();
		this.dist = 0;
	}
	
	public boolean isFrontFacingTo(Vector3f direction) {
		double dot = normal.dot(direction);
		return (dot <= 0);
	}
	
	public double signedDistanceTo(Vector3f point) {
		return (point.dot(normal)) + dist;
	}
	
	public Vector3f projectPoint(Vector3f point) {
		return Vector3f.sub(point, Vector3f.mul(normal, (float)signedDistanceTo(point)));
	}
	
	public Vector3f rayIntersection(Vector3f rayOrigin, Vector3f rayDirection) {
	    if (normal.dot(rayDirection) == 0.0f) {
	        return null;
	    }

	    float t = (dist - normal.dot(rayOrigin)) / normal.dot(rayDirection);
	    return Vector3f.add(rayOrigin,rayDirection.mul(t));
	}

	public static Vector3f projectPoint(Vector3f point, Vector3f norm, float dist) {
		return Vector3f.sub(point, Vector3f.mul(norm, (point.dot(norm)) + dist));
	}
	
	public byte classify(Vector3f point, float planeThickness) {
		float fDist = (Vector3f.dot(normal, point) - dist);
		if ( fDist > planeThickness ) { return IN_FRONT; }
		if ( fDist < -planeThickness ) { return BEHIND; }
		return COPLANAR;
	}
	
	public static byte classify(Vector3f point, Vector3f normal, float dist, float planeThickness) {
		float fDist = (Vector3f.dot(normal, point) - dist);
		if ( fDist > planeThickness ) { return IN_FRONT; }
		if ( fDist < -planeThickness ) { return BEHIND; }
		return COPLANAR;
	}

	public void translate(Vector3f offset) {
		Vector3f newPt = Vector3f.add(offset, Vector3f.mul(normal, dist));
		dist = (normal.x*newPt.x+normal.y*newPt.y+normal.z*newPt.z);
	}

	public void set(float a, float b, float c, float d) {
		normal.set(a,b,c);
		
		float len = normal.length();
		normal.div(len);
		
		dist = d/len;
	}
}
