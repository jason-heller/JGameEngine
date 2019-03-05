package animation;

import debug.console.Console;

public class Animation {

	private float[] keyframeTimes;
	private Keyframe[] frames;
	private int numGroups;

	public Animation(float[] keyframeTimes, Keyframe[] frames) {
		this.keyframeTimes = keyframeTimes;
		this.frames = frames;
		
		numGroups = frames[0].getTransforms().length;
	}
	
	public int size() {
		return keyframeTimes.length;
	}

	public Keyframe[] getKeyframes() {
		return frames;
	}

	public float[] getKeyframeTimes() {
		return keyframeTimes;
	}

	public int getNumGroups() {
		return numGroups;
	}

	public int[] getNeighboringFrames(float time) {
		int prevTime = -1;
		int nextTime = -1;
		for(int i = 0; i < keyframeTimes.length; i++) {
			if (keyframeTimes[i] <= time && (prevTime == -1 || keyframeTimes[prevTime] <= keyframeTimes[i])) {
				prevTime = i;
			}
			
			if (keyframeTimes[i] >= time && (nextTime == -1 || keyframeTimes[nextTime] >= keyframeTimes[i])) {
				nextTime = i;
			}
		}
		
		return new int[] {prevTime,nextTime};
	}

	public float getEndTime() {
		return keyframeTimes[keyframeTimes.length-1];
	}

	public void printDebugInfo() {
		System.err.println("Animation info:\n"+frames.length + " frames\n"+numGroups+" groups\nFrame times:");
		Console.log("Animation info:\n"+frames.length + " frames\n"+numGroups+" groups\nFrame times:");
		for(float time : keyframeTimes) {
			System.err.print(" "+time);
			Console.log(""+time);
		}
		System.err.println("\n");
	}
}
