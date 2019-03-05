package logic.collision;

import org.joml.Vector3f;

public class AxisAlignedBox {
	public Vector3f bounds;
	public Vector3f center;
	
	protected float scale;
	
	public boolean intersection(AxisAlignedBox other) {
		if (Math.abs(center.x - other.center.x) <= (bounds.x - other.bounds.x)) return false;
		if (Math.abs(center.y - other.center.y) <= (bounds.y - other.bounds.y)) return false;
		if (Math.abs(center.z - other.center.z) <= (bounds.z - other.bounds.z)) return false;
	    
	    return true;
	}
}
