package scene.world.architecture;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import debug.Debug;
import debug.tracers.LineRenderer;
import global.Globals;
import logic.collision.Plane;
import opengl.Application;
import particles.ParticleEmitter;
import pipeline.Resources;
import pipeline.Texture;
import scene.Camera;
import scene.Scene;
import scene.object.ObjectControl;
import scene.object.StaticEntity;
import scene.object.VisibleObject;
import scene.world.World;
import scene.world.architecture.components.ArcClip;
import scene.world.architecture.components.ArcFace;
import scene.world.architecture.functions.SpawnPoint;
import scene.world.architecture.vis.Bsp;
import scene.world.architecture.vis.BspLeaf;
import scene.world.architecture.vis.Pvs;

public class Architecture {

	private Scene scene;
	private String mapName;
	private byte mapVersion;
	private byte mapCompilerVersion;
	public boolean loaded = false;
	
	public Bsp bsp;
	public Pvs pvs;
	private List<BspLeaf> renderedLeaves = new ArrayList<BspLeaf>();
	private BspLeaf currentLeaf = null;
	
	public Vector3f[] vertices;
	public ArcFace[] faces;
	private List<StaticEntity> entities = new ArrayList<StaticEntity>();
	
	private List<ParticleEmitter> emitters = new ArrayList<ParticleEmitter>();
	private Vector3f sunVector;
	private List<Texture> mapSpecificTextures;
	public Lightmap lightmap;
	private String[] mapSpecTexRefs;
	
	public Architecture(Scene scene) {
		this.scene = scene;	
	}
	
	//public void load(String file) {
	//	ArcFile.load(scene, "res/maps/", file);
	//O}
	
	public void update(Camera camera) {
		if (!loaded) return;
		
		// TODO: Make this only update when changed to a new leaf
		if (Debug.ignoreBsp) {
			for(BspLeaf leaf : bsp.leaves) {
				if (leaf.clusterId == -1) continue;
				for(VisibleObject visObj : leaf.getVisibleObjects()) {
					ObjectControl.renderTriList(camera.getProjectionMatrix(), camera.getViewMatrix(), visObj);
				}
			}
		} else {
			BspLeaf cameraLeaf = bsp.walk(camera.getPosition());
			if (cameraLeaf.clusterId != -1 && cameraLeaf != currentLeaf) {
				currentLeaf = cameraLeaf;
				renderedLeaves.clear();
				
				int[] vis = pvs.getClustersToRender(cameraLeaf);
				
				for(int i = 0; i < bsp.leaves.length; i++) {
					BspLeaf leaf = bsp.leaves[i];
					if (leaf.clusterId == -1) continue;
					if (vis[leaf.clusterId] == 0) continue;
					renderedLeaves.add(leaf);
				}
					
			}
			
			for(BspLeaf leaf : renderedLeaves) {
				for(VisibleObject visObj : leaf.getVisibleObjects()) {
					
					if (!camera.getFrustum().containsBoundingBox(leaf.max, leaf.min)) {continue;}

					ObjectControl.renderTriList(camera.getProjectionMatrix(), camera.getViewMatrix(), visObj);
				}
			}
			
			if (Debug.wireframe) {
				for(BspLeaf leaf : renderedLeaves) {
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
					for(VisibleObject visObj : leaf.getVisibleObjects()) {
						GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
						
						ObjectControl.renderTriListWireframe(camera.getProjectionMatrix(), camera.getViewMatrix(), visObj);
						
					}
					GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
					
					/*for(ArcClip clip : leaf.clips) {
						for(int i = clip.firstEdge; i < clip.firstEdge+clip.numEdges; i++) {
							Plane p = bsp.planes[bsp.clipEdges[i].planeId];
							LineRenderer.render(Application.scene.getCamera(), Vector3f.mul(p.normal, p.dist), Vector3f.add(Vector3f.mul(p.normal, p.dist), new Vector3f(0,32,0)));
						}
					}*/
				}
			}
		}
		
		for (ParticleEmitter pe : emitters) {
			pe.generateParticles(camera);
		}
	}
	
	public void cleanUp() {
		bsp.cleanUp();
		int i = 0;
		for(Texture tex : mapSpecificTextures) {
			tex.delete();
			Resources.removeTextureReference(mapSpecTexRefs[i++]);
		}
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void setProperties(String mapName, byte mapVersion, byte mapCompilerVersion, Vector3f sunVector) {
		this.mapName = mapName;
		this.mapVersion = mapVersion;
		this.sunVector = sunVector;
		this.mapCompilerVersion = mapCompilerVersion;
	}

	public String getMapName() {
		return mapName;
	}

	public byte getMapVersion() {
		return mapVersion;
	}

	public Vector3f getSunVector() {
		return sunVector;
	}

	public byte getMapCompilerVersion() {
		return mapCompilerVersion;
	}

	public void setMapSpecificTextures(List<Texture> mapSpecificTextures, List<String> mapSpecTexRefs) {
		this.mapSpecificTextures = mapSpecificTextures;
		this.mapSpecTexRefs = mapSpecTexRefs.toArray(new String[0]);
	}

	public void addEntity(StaticEntity ent) {
		entities.add(ent);
	}

	public SpawnPoint getSpawn() {
		for(StaticEntity entity : entities) {
			if (entity instanceof SpawnPoint) {
				SpawnPoint spawn = (SpawnPoint)entity;
				if (spawn.getName().equals(Globals.destSpawnName)) {
					return spawn;
				}
			}
		}
		
		return World.DEFAULT_SPAWN_POINT;
	}

	public List<BspLeaf> getRenderedLeaves() {
		return renderedLeaves;
	}

	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}
}
