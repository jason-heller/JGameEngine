package net.entity;

import org.joml.Matrix4f;

import logic.collision.BoundingBox;
import logic.controller.PlayerController;
import pipeline.Resources;
import scene.Scene;
import scene.entity.Entity;

public class NetPlayerEntity extends Entity {
	
	public NetPlayerEntity(Scene scene) {
		super(scene, Resources.getModel("player"), Resources.getTexture("default"), new Matrix4f(), "net_player");
		
		updateMatrix();
		obb = new BoundingBox(position, PlayerController.getWidth(), PlayerController.getHeight(), PlayerController.getWidth());
	}
	
	@Override
	public void update(Scene scene) {
		super.update(scene);
	}

}
