package logic.collision;

import org.joml.Vector3f;

public class Ray {
	public Vector3f normal;
	public Vector3f origin;
	
	private static final float EPSILON = 0.00001f;
	
	public Ray(Vector3f origin, Vector3f normal) {
		this.normal = normal;
		this.origin = origin;
	}
	
	public Vector3f intersection(Polygon polygon) {
		Vector3f I = new Vector3f();
        Vector3f    u, v, n;
        Vector3f    dir, w0;
        float     r, a, b;
        
        u = new Vector3f(polygon.p2);
        u.sub(polygon.p1);
        v = new Vector3f(polygon.p3);
        v.sub(polygon.p1);
        n = Vector3f.cross(u, v);
        
        if (n.length() == 0) {
            return null;
        }
        
        dir = new Vector3f(normal);
        w0 = new Vector3f(origin);
        w0.sub(polygon.p1);
        a = -(new Vector3f(n).dot(w0));
        b = new Vector3f(n).dot(dir);
        
        if ((float)Math.abs(b) < EPSILON) {
            return null;
        }
        
        r = a / b;
        if (r < 0.0) {
            return null;
        }
        
        I = new Vector3f(origin);
        I.x += r * dir.x;
        I.y += r * dir.y;
        I.z += r * dir.z;
        
        return I;
    }
}
