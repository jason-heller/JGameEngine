package scenes;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import logic.collision.BoundingBox;
import logic.controller.PlayerController;
import pipeline.Resources;
import scene.Scene;
import scene.entity.Entity;

public class PlayerEntity extends Entity {
	
	public PlayerEntity(Scene scene) {
		super(scene, Resources.getModel("quad"), Resources.getTexture("default2"), new Matrix4f(), "player");
		Vector3f pos = scene.getCamera().getPosition();
		position.x = pos.x;
		position.y = pos.y;
		position.z = pos.z;
		this.setGlobal(true);
		
		updateMatrix();
		visible = false;
		obb = new BoundingBox(position, PlayerController.getWidth(), PlayerController.getHeight(), PlayerController.getWidth());

	}
	
	@Override
	public void update(Scene scene) {
		PlayerController.update();
		super.update(scene);
		PlayerController.postUpdate();
		obb.bounds.y = PlayerController.getHeight();
	}

}
