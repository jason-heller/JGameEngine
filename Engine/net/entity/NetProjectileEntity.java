package net.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import opengl.Window;
import particles.Particle;
import pipeline.Resources;
import pipeline.Texture;
import scene.Scene;
import scene.object.Model;
import scene.object.StaticEntity;
import weapons.projectiles.Projectiles;

public class NetProjectileEntity extends StaticEntity {
	
	public int id;
	private float partTimer = 0f;
	
	public NetProjectileEntity(Scene scene, int id, float x, float y, float z) {
		super(getModelById(id), getTextureById(id), new Matrix4f());

		this.id = id;
		
		// TODO: get owner, set direction to owner's yaw/pitch here
		position = new Vector3f(x,y,z);
		rotation = new Vector3f();
		updateMatrix();
	}
	
	private static Model getModelById(int id) {
		return Projectiles.getModelById(id);
	}
	
	private static Texture getTextureById(int id) {
		return Projectiles.getTextureById(id);
	}
	
	public void move(int id, float x, float y, float z) {
		if (this.id != id) {
			this.id = id;
			setModel(getModelById(id));
			setDiffuse(getTextureById(id));
		}
		
		// TODO: set rotation based on prev position
		
		position.set(x,y,z);
	}

	@Override
	public void update(Scene scene) {
		super.update(scene);
		
		partTimer  += Window.deltaTime;
		if (partTimer >= .06f) {
			partTimer = 0;
			new Particle(Resources.getTexture("smoke"), new Vector3f(position), Vector3f.ZERO, 0f, 100f, 0f, 0f, 2f);
		}
	}

}
