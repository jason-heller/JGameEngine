package scenes;

import org.joml.Matrix4f;

import logic.collision.BoundingBox;
import logic.controller.PlayerController;
import net.ClientHistory;
import pipeline.Resources;
import scene.Scene;
import scene.entity.Entity;

public class PlayerEntity extends Entity {
	
	private ClientHistory history;
	
	public PlayerEntity(Scene scene) {
		super(scene, Resources.getModel("quad"), Resources.getTexture("default2"), new Matrix4f(), "player");
		position.x = 0;
		position.y = 100;
		position.z = 0;
		this.setGlobal(true);
		
		updateMatrix();
		visible = false;
		obb = new BoundingBox(position, PlayerController.getWidth(), PlayerController.getHeight(), PlayerController.getWidth());
	
		history = new ClientHistory(this);
	}
	
	@Override
	public void update(Scene scene) {
		PlayerController.update();
		super.update(scene);
		PlayerController.postUpdate();
		obb.bounds.y = PlayerController.getHeight();
		history.update();
	}

	public ClientHistory getHistory() {
		return history;
	}
}
