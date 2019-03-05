package animation;

import org.joml.Matrix4f;
import org.joml.Quaternion;
import org.joml.Vector3f;

import opengl.Application;
import opengl.Window;

public class Pose {
	// This is used to calculate the matrices for an actively animated object
	
	private float animationTimer = 0f;
	private boolean isActive = false;
	private int currentKeyframe = 0;
	private Animation animation;

	private Matrix4f[] matrices;
	
	// Frame 0 is the starting frame
	
	// Last entry in keyframeTimes should be the time the animation ends
	
	// Example
	/*
	 
	keyframeTimes = {0, 10, 20, 30}
	positions = {0}{[0,0,0],   [10,10,10],   [20,20,20] },    	{1}{[0,0,0],   [10,10,10],   [20,20,20]	}
	rotations = {0}{[0,0,0],   [0,60,0],     [0,0,0]	}, 		{1}{]0,0,0],   [0,60,0],     [0,0,0]	}
	interpFactor = {0}{0, 0, 0}, {1}{.3, .2, 0}
	parents 	=  {0}{0, 0, 0}. {1}{0, 0, 0}
	
	*/
	// Notice how keyframeTimes has one extra element over the rest (the rest go by threes while it goes by four)
	
	public Pose(Animation animation) {
		this.animation = animation;
		
		matrices = new Matrix4f[animation.getNumGroups()];
		for(int i = 0; i < matrices.length; i++) {
			matrices[i] = new Matrix4f();
		}
	}
	
	public void startAnimation() {
		startAnimation(0);
	}
	
	public void startAnimation(float startingTime) {
		isActive = true;
		currentKeyframe++;
		animationTimer = startingTime;
	}
	
	public void stopAnimation() {
		animationTimer = 0;
		isActive = false;
		currentKeyframe = 0;
		
		for(int i = 0; i < animation.getNumGroups(); i++) {
			matrices[i].set(animation.getKeyframes()[0].getTransforms()[i]);
		}
	}
	
	public void update() {
		if (!isActive) return;
		
		animationTimer += Window.deltaTime*60;
		
		if (animationTimer*10 >= animation.getEndTime()) {
			stopAnimation();
			return;
		}
		
		int[] times = animation.getNeighboringFrames(animationTimer);
		
		Keyframe start, end;
		start = animation.getKeyframes()[times[0]];
		if (times[1] == animation.getKeyframes().length) {
			end	= animation.getKeyframes()[0];
		} else {
			end	= animation.getKeyframes()[times[1]];
		}
		
		float baseLerp = (animationTimer - (float)animation.getKeyframeTimes()[times[0]])/animation.getKeyframeTimes()[times[1]];
		
		if (animationTimer >= animation.getKeyframeTimes()[times[1]]) {
			currentKeyframe++;
		}
		
		for(int i = 0; i < animation.getNumGroups(); i++) {
			Matrix4f mStart = start.getTransforms()[i];
			Matrix4f mEnd	= end.getTransforms()[i];
			
			Quaternion qStart 	= Quaternion.fromMatrix(mStart);
			Quaternion qEnd 	= Quaternion.fromMatrix(mEnd);
			
			Vector3f tStart = mStart.getTranslation();
			Vector3f tEnd	= mEnd.getTranslation();
			
			float lerp = baseLerp;
			if (start.getInterpolations()[i] == 1) {
				lerp = baseLerp*baseLerp;
			}
			else if (start.getInterpolations()[i] == 2 && baseLerp>0) {
				lerp = (float)Math.sqrt(baseLerp);
			}
			
			Quaternion q = new Quaternion();
			Quaternion.slerp(qStart, qEnd, lerp, q);
			Vector3f v = Vector3f.lerp(tStart, tEnd, lerp);
			
			matrices[i].identity();
			matrices[i].translate(v);
			matrices[i].rotate(q);
			//matrices[i].set(mEnd);
		}
	}
	
	public Matrix4f[] getMatrices() {
		return matrices;
	}

	public boolean isActive() {
		return isActive;
	}
}
