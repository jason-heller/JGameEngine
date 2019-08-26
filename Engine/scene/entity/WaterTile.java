package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import pipeline.Resources;
import scene.Scene;
import scene.object.StaticEntity;

public class WaterTile extends StaticEntity {
	
	public WaterTile(float x, float y, float z) {//Terrain.waterModel
		super(Resources.getModel("water"), Resources.getTexture("water"), new Matrix4f());
		position = new Vector3f(x,y,z);//matrix.getTranslation();
		rotation = new Vector3f();
		//this.collision = new CollisionShape(new BoundingBox(Terrain.chunkSize/2,Terrain.waterLevel,Terrain.chunkSize/2,Terrain.chunkSize,Terrain.chunkSize,Terrain.chunkSize));
	}
	
	public void update(Scene scene) {
		super.updateMatrix();
	}
}
