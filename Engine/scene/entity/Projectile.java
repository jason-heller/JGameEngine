package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import logic.collision.BoundingBox;
import logic.collision.Plane;
import logic.controller.PlayerController;
import opengl.Application;
import opengl.Window;
import particles.Particle;
import pipeline.Resources;
import pipeline.Texture;
import scene.Camera;
import scene.Scene;
import scene.object.Model;
import utils.MathUtils;

public class Projectile extends Entity {
	
	protected float size = 1f;
	protected float bouncyness = 1f;
	
	protected float life = 0f, lifespan = Float.MAX_VALUE;
	protected float numBounces = 0f, maxBounces = -1f;
	
	protected boolean destroyOnLastBounce = false;
	
	private float partTimer = 0f;
	protected float explosionRadius = -1f;
	
	protected float spawnDiffX = 0, spawnDiffY = 0, spawnDiffZ = 0;
	
	protected Entity owner = null;
	protected float ownershipTime = -1f;

	public Projectile(Scene scene, Model model, Texture texture, String name) {
		super(scene, model, texture, new Matrix4f(), name);
		this.friction = 1.75f;
		this.bouncyness = 0.5f;
		this.rotation = new Vector3f();
	}
	
	public void setSpawnOffset(float dx, float dy, float dz) {
		spawnDiffX = dx;
		spawnDiffY = dy;
		spawnDiffZ = dz;
	}
	
	public void setOwner(Entity e, float ownershipTime) {
		owner = e;
		this.ownershipTime = ownershipTime;
	}
	
	public void setOwner(Entity e) {
		setOwner(e, -1f);
	}
	
	public Entity getOwner() {
		return owner;
	}
	
	public void launch(Camera camera, float forwardSpeed, float upSpeed) {
		this.position = new Vector3f(PlayerController.getPlayer().position);
		this.position.y += PlayerController.getCameraHeight();
		Vector3f look = camera.getDirectionVector();//MathUtils.getDirection(camera.getViewMatrix());
		position.add(Vector3f.mul(look, 8f));
		this.velocity = Vector3f.mul(look, -forwardSpeed);
		Vector3f bounds = Vector3f.sub(gfx.getModel().max, gfx.getModel().min).div(2f);
		obb = new BoundingBox(position.x, position.y, position.z, bounds.x, bounds.y, bounds.z);
		velocity.y += upSpeed;
		this.updateMatrix();
	}
	
	public void launch(Vector3f position, float yaw, float pitch, float forwardSpeed, float upSpeed) {
		this.position = new Vector3f(position);
		this.position.y += PlayerController.getCameraHeight();
		
		Vector3f forward = MathUtils.eulerToVectorDeg(yaw, pitch);
		Vector3f right = MathUtils.eulerToVectorDeg(yaw-90, 0);
		Vector3f down = MathUtils.eulerToVectorDeg(yaw, pitch+90);

		this.position.add(Vector3f.mul(down, -spawnDiffY));
		this.position.add(Vector3f.mul(forward, -spawnDiffZ));
		this.position.add(Vector3f.mul(right, spawnDiffX));
		
		this.velocity = Vector3f.mul(forward, -forwardSpeed);
		Vector3f bounds = Vector3f.sub(gfx.getModel().max, gfx.getModel().min).div(2f);
		obb = new BoundingBox(this.position.x, this.position.y, this.position.z, bounds.x, bounds.y, bounds.z);
		velocity.y += upSpeed;
		this.updateMatrix();
	}
	
	@Override
	public void update(Scene scene) {
		rotation.set(MathUtils.vectorToEurler(velocity));
		
		life += Window.deltaTime;
		
		if (life >= lifespan) {
			endLife();
		}
		
		//Texture texture, Vector3f position, Vector3f velocity, float gravity, float life, float rotation, float rotationSpeed, float scale
		partTimer += Window.deltaTime;
		if (partTimer >= .06f) {
			partTimer = 0;
			new Particle(Resources.getTexture("smoke"), new Vector3f(position), Vector3f.ZERO, 0f, 100f, 0f, 0f, 2f);
		}
		obb.update(position, rotation, scale);
		super.update(scene);
	}
	
	/* endLife()
	 * @Desc controls what the projectile should do at the end of its life
	 */
	public void endLife() {
		if (explosionRadius >= 0) {
			explode();
			return;
		}
		
		destroy();
	}
	
	/// Custom Collisions ///
	
	@Override
	protected void ceilingCollision(Plane plane) {
		bounceOffPlane(plane);
	}

	@Override
	protected void wallCollision(Plane plane) {
		bounceOffPlane(plane);
	}

	@Override
	protected void floorCollision(Plane plane) {
		bounceOffPlane(plane);
	}
	
	private void bounceOffPlane(Plane plane) {
		Vector3f projectedPoint = Plane.projectPoint(position, plane.normal, -plane.dist);
		projectedPoint.add(Vector3f.mul(plane.normal, size+0.25f));
		position.set(projectedPoint);
		
		
		if (velocity.lengthSquared() < 4 || numBounces == maxBounces) {
			if (destroyOnLastBounce) {
				endLife();
			}
		}
		else {
			velocity.set(MathUtils.reflect(velocity, plane.normal).mul(bouncyness));
			numBounces++;
		}
	}
	
	public void explode() {
		destroy();
		Application.scene.getWorld().createExplosion((byte) owner.getId(), 25, explosionRadius, position.x, position.y, position.z);
	}
}
