package scene.world.architecture.vis;

import org.joml.Vector3f;

import logic.collision.BoundingBox;
import logic.collision.Plane;
import scene.world.architecture.components.ArcClipEdge;
import scene.world.architecture.components.ArcEdge;
import scene.world.architecture.components.ArcFace;

public class Bsp {
	private static final float EPSILON = 0.0001f;
	
	public Plane[] planes;
	public BspNode[] nodes;
	public BspLeaf[] leaves;
	public ArcFace[] faces;

	public Vector3f[] vertices;
	public ArcEdge[] edges;
	public int[] surfEdges;
	public short[] leafFaceIndices;

	public ArcClipEdge[] clipEdges;
	
	public void cleanUp() {
		for(BspLeaf leaf : leaves) {
			leaf.cleanUp();
		}
	}

	// The children[] members are the two children of this node; if positive, they are node indices; if negative, 
	// the value (-1-child) is the index into the leaf array (e.g., the value -100 would reference leaf 99). 
	public BspLeaf walk(Vector3f position) {
		int nextNode = 0;
		BspNode node;
		
		while(nextNode > -1) {
			node = nodes[nextNode];
			
			if (planes[node.planeNum].classify(position, EPSILON) == Plane.IN_FRONT) {
				nextNode = node.childrenId[0];
			} else {
				nextNode = node.childrenId[1];
			}
		}
		
		return leaves[-1-nextNode];
	}

	public Plane[] getPlanes(BspLeaf leaf) {
		if (leaf.clusterId == -1) {
			return new Plane[] {};
		}
		
		Plane[] leafPlanes = new Plane[leaf.numFaces];
		
		for(int i = 0; i < leaf.numFaces; i++) {
			leafPlanes[i] = planes[faces[leafFaceIndices[leaf.firstFace+i]].planeId];
		}
		
		return leafPlanes;
	}

	public ArcFace[] getFaces(BspLeaf leaf) {
		if (leaf.clusterId == -1) {
			return new ArcFace[] {};
		}
		
		ArcFace[] leafPlanes = new ArcFace[leaf.numFaces];
		
		for(int i = 0; i < leaf.numFaces; i++) {
			leafPlanes[i] = faces[leafFaceIndices[leaf.firstFace+i]];
		}
		
		return leafPlanes;
	}

	public boolean obbHullIntersection(BoundingBox obb, int firstFace, int numFaces) {
		int lastFace = firstFace + numFaces;
		for (int i = firstFace; i < lastFace; i++) {
			if (planes[faces[i].planeId].classify(obb.center, .001f) == Plane.IN_FRONT) {
				return false;
			}
		}

		return true;
	}
	
}
