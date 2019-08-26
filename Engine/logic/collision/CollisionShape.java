package logic.collision;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CollisionShape {
	private CollisionType type = CollisionType.SOLID;
	private Polygon[] polygons = null;
	private BoundingBox broadphase = null;

	
	public CollisionShape(float[] vertices, Matrix4f matrix) {
		int j = 0;
		try {
			polygons = new Polygon[vertices.length/9];
		} catch (NullPointerException e) {
			System.err.println("ERR: This OBJ needs to have it's vertex data saved!");
			return;
		}
		Vector3f max = new Vector3f(-99999,-99999,-99999);
		Vector3f min = new Vector3f(99999,99999,99999);
		for(int i = 0; i < vertices.length; i += 9) {
			polygons[j++] = new Polygon(
					vertices[i],vertices[i+1],vertices[i+2],
					vertices[i+3],vertices[i+4],vertices[i+5],
					vertices[i+6],vertices[i+7],vertices[i+8]);
			max.max(polygons[j-1].getMax());
			min.min(polygons[j-1].getMin());
			polygons[j-1].applyMatrix(matrix);
			
		}
		broadphase = new BoundingBox(new Vector3f(),max,min);
	}
	
	public CollisionShape(float[] vertices) {
		this(vertices, new Matrix4f());
	}

	
	public CollisionShape(BoundingBox broadphase) {
		this.broadphase = broadphase;
		this.type = CollisionType.USE_BOUNDING_BOX;
	}
	
	public CollisionShape() {
		this.broadphase = null;
		this.type = CollisionType.NONSOLID;
	}
	
	public void setType(CollisionType type) {
		this.type = type;
	}
	
	public CollisionType getType() {
		return type;
	}
	
	public BoundingBox getBroadphase() {
		return broadphase;
	}
	
	public Polygon[] getPolygons() {
		return polygons;
	}
	
	public void update(Vector3f position, Vector3f rotation, float scale) {
		broadphase.update(position, rotation, scale);
	}
}
