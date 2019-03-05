package debug;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import logic.collision.BoundingBox;
import logic.collision.CollisionShape;
import logic.controller.SkyboxController;
import pipeline.Resources;
import scene.Scene;
import scene.object.StaticEntity;

public class DebugEntity extends StaticEntity {
	
	public DebugEntity(Scene scene) {
		super(Resources.getModel("cube"), Resources.getTexture("default"), new Matrix4f(), false);
		this.position = new Vector3f(scene.getCamera().getPosition());
		this.rotation = new Vector3f(0,0,0);
		this.collision = new CollisionShape(model.getVertices());
		this.scale = 2f;
		this.updateMatrix();
	}


}
