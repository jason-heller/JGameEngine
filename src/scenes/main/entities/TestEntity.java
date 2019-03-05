package scenes.main.entities;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import debug.console.Console;
import logic.collision.BoundingBox;
import logic.collision.CollisionShape;
import logic.controller.PlayerController;
import pipeline.Resources;
import scene.Scene;
import scene.entity.Entity;

public class TestEntity extends Entity {
	
	public TestEntity(Scene scene) {
		super(scene, Resources.getModel("cube"), Resources.getTexture("default"), new Matrix4f(), "player");
		Vector3f pos = scene.getCamera().getPosition();
		position.x = pos.x;
		position.y = pos.y;
		position.z = pos.z;
		
		updateMatrix();
		//visible = false;
		obb = new BoundingBox(position, PlayerController.getWidth(), PlayerController.getHeight(), PlayerController.getWidth());
	}
	
	@Override
	public void update(Scene scene) {
		super.update(scene);
	}

}
