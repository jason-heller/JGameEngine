package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import logic.collision.BoundingBox;
import logic.collision.CollisionShape;
import pipeline.Resources;
import scene.Scene;
import scene.object.StaticEntity;
import scene.world.terrain.Terrain;

public class WaterTile extends StaticEntity {
	
	public WaterTile(float x, float z) {//Terrain.waterModel
		super(Resources.getModel("water"), Resources.getTexture("water"), new Matrix4f(), true);
		position = new Vector3f(x,Terrain.waterLevel,z);//matrix.getTranslation();
		rotation = new Vector3f();
		this.scale = Terrain.chunkSize;
		this.collision = new CollisionShape(new BoundingBox(Terrain.chunkSize/2,Terrain.waterLevel,Terrain.chunkSize/2,Terrain.chunkSize,Terrain.chunkSize,Terrain.chunkSize));
	}
	
	public void update(Scene scene) {
		super.updateMatrix();
	}
}
