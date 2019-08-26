package scene.world.architecture.vis;

import org.joml.Vector3f;

public class BspNode {
	public int planeNum;
	public int[] childrenId = new int[2];
	public Vector3f min, max;
	public short firstFace;
	public short numFaces;
}
