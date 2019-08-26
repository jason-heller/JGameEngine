package scene.skybox._3D;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import scene.object.StaticEntity;
import scene.world.architecture.vis.Bsp;
import scene.world.architecture.vis.BspLeaf;
import scene.world.architecture.vis.Pvs;

public class SkyboxCamera extends StaticEntity {

	float scale;
	private float fogStart, fogEnd;
	private boolean hasFog;
	private Vector3f fogColor;
	
	private BspLeaf cameraLeaf;
	private List<BspLeaf> renderedLeaves = new ArrayList<BspLeaf>();
	
	public SkyboxCamera(Vector3f position, float scale, boolean hasFog, float fogStart, float fogEnd,
			Vector3f fogColor) {
		super(null, null, new Matrix4f());
		this.position = position;
		this.rotation = new Vector3f();
		this.scale = scale;
		this.hasFog = hasFog;
		this.fogColor = fogColor;
		this.fogStart = fogStart;
		this.fogEnd = fogEnd;
	}
	
	public void updateLeaf(Bsp bsp, Pvs pvs) {
		cameraLeaf = bsp.walk(position);
		int[] vis = pvs.getClustersToRender(cameraLeaf);
		
		for(int i = 0; i < bsp.leaves.length; i++) {
			BspLeaf leaf = bsp.leaves[i];
			if (leaf.clusterId == -1) continue;
			if (vis[leaf.clusterId] == 0) continue;
			renderedLeaves.add(leaf);
		}
	}
	
	public List<BspLeaf> getRenderedLeaves() {
		return renderedLeaves;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getScale() {
		return scale;
	}

	public float getFogStart() {
		return fogStart;
	}

	public float getFogEnd() {
		return fogEnd;
	}

	public boolean isHasFog() {
		return hasFog;
	}

	public Vector3f getFogColor() {
		return fogColor;
	}

}
