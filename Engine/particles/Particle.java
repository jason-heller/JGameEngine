package particles;

import org.joml.Vector2f;
import org.joml.Vector3f;

import opengl.Window;
import pipeline.Texture;
import scene.Camera;

public class Particle {
	private Vector3f position, velocity;
	private float gravity;
	private float life, rotation, rotationSpeed, scale;
	private float elapsedTime = 0;
	
	private Texture texture;
	
	private int texStart, texEnd;
	private Vector2f textureOffset1 = new Vector2f(), textureOffset2 = new Vector2f();
	private float blend;
	private float distance;
	
	private boolean alive = true;

	public Particle(Texture texture, Vector3f position, Vector3f velocity, float gravity, float life, float rotation, float rotationSpeed, float scale) {
		this(texture, position, velocity, gravity, life, rotation, 0, scale, 0, texture.getTextureAtlasRows()*texture.getTextureAtlasRows());
	}
	
	public Particle(Texture texture, Vector3f position, Vector3f velocity, float gravity, float life, float rotation, float rotationSpeed, float scale, int texStart, int texEnd) {
		this.position = position;
		this.velocity = velocity;
		this.gravity = gravity;
		this.life = life;
		this.rotation = rotation;
		this.rotationSpeed = rotationSpeed;
		this.scale = scale;
		this.texture = texture;
		this.texStart = texStart;
		this.texEnd = texEnd;
		updateTextureCoordInfo();
		ParticleHandler.add(this);
	}
	
	public void setTextureAtlasRange(int start, int end) {
		this.texStart = start;
		this.texEnd = end;
	}
	
	protected boolean update(Camera camera) {
		position.add(Vector3f.mul(velocity,Window.deltaTime*135));
		rotation += rotationSpeed*Window.deltaTime*1000f;
		velocity.y -= gravity*Window.deltaTime*60;
		
		elapsedTime+=Window.deltaTime*135f;
		
		distance = Vector3f.sub(new Vector3f(camera.getPosition()), position).lengthSquared();
		
		updateTextureCoordInfo();
		if (elapsedTime > life) {
			alive = false;
			return false;
		}
		
		return true;
	}
	
	private void updateTextureCoordInfo() {
		float lifeFactor = elapsedTime / life;
		int numStages = texEnd - texStart;
		float progression = texStart + (lifeFactor * numStages);
		int index1 = (int)progression;
		int index2 = index1 < numStages ? index1 + 1 : index1;
		blend = lifeFactor;
		setTextureOffset(textureOffset1, index1);
		setTextureOffset(textureOffset2, index2);
	}
	
	private void setTextureOffset(Vector2f offset, int index) {
		int column = index % texture.getTextureAtlasRows();
		int row = index / texture.getTextureAtlasRows();
		offset.x = (float)column / texture.getTextureAtlasRows();
		offset.y = (float)row / texture.getTextureAtlasRows();
	}

	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getVelocity() {
		return velocity;
	}

	public float getGravity() {
		return gravity;
	}

	public float getLife() {
		return life;
	}

	public float getRotation() {
		return rotation;
	}

	public float getScale() {
		return scale;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public void setVelocity(Vector3f velocity) {
		this.velocity = velocity;
	}

	public void setGravity(float gravity) {
		this.gravity = gravity;
	}

	public void setLife(float life) {
		this.life = life;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public Texture getTexture() {
		return texture;
	}

	public Vector2f getTextureOffset1() {
		return textureOffset1;
	}

	public Vector2f getTextureOffset2() {
		return textureOffset2;
	}

	public float getBlend() {
		return blend;
	}

	public float getDistance() {
		return distance;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setActive(Vector3f position, Vector3f velocity, float gravity,
			float life, float rotation, float scale) {
		this.alive = true;
		this.position = position;
		this.velocity = velocity;
		this.gravity = gravity;
		this.life = life;
		this.rotation = rotation;
		this.scale = scale;
	}
}
