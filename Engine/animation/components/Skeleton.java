package animation.components;

public class Skeleton {
	private int numJoints;
	private Joint rootJoint;
	
	public Skeleton(int numJoints, Joint rootJoint) {
		this.numJoints = numJoints;
		this.rootJoint = rootJoint;
	}
	
	public int getNumJoints() {
		return numJoints;
	}
	
	public Joint getRootJoint() {
		return rootJoint;
	}
}
