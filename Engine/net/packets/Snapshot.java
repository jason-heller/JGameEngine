package net.packets;

import java.util.List;

public class Snapshot {
	public float x, y, z, yaw, pitch;
	
	public Snapshot(List<Float> pos, List<Float> rot) {
		x = pos.get(0);
		y = pos.get(1);
		z = pos.get(2);
		yaw = rot.get(0);
		pitch = rot.get(1);
	}
}
