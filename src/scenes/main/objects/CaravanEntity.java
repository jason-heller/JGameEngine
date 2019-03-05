package scenes.main.objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import logic.collision.BoundingBox;
import logic.collision.CollisionShape;
import pipeline.Resources;
import scene.Scene;
import scene.object.StaticEntity;

public class CaravanEntity extends StaticEntity {
	
	public CaravanEntity(Scene scene) {
		super(Resources.getModel("caravan"), Resources.getTexture("caravan"), new Matrix4f(), false);
		this.position = new Vector3f(scene.getCamera().getPosition());
		collision = new CollisionShape(new BoundingBox(position, 7, 5, 4));
		//this.collision = new CollisionShape(Resources.getModel("caravan").getVertices());
	}
	
	@Override
	public void update(Scene scene) {
		super.update(scene);
	}

}
