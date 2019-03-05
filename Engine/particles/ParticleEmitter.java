package particles;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import opengl.Window;
import pipeline.Resources;
import pipeline.Texture;
import scene.Camera;
 
public class ParticleEmitter {
 
	private static final float MAX_RANGE = 1000;
	
    private float pps, averageSpeed, gravityComplient, averageLifeLength, averageScale;
    private Texture texture;
 
    private float speedError, lifeError, scaleError = 0;
    private boolean randomRotation = false;
    private float rotation = 0f;
    private Vector3f origin, direction;
    private float directionDeviation = 0;
    
    private int texStart = 0, texEnd = 0;
    
    private float range = 0;
 
    private Random random = new Random();
 
    public ParticleEmitter(Texture texture, float pps, float speed, float gravityComplient, float lifeLength, float scale) {
        this.pps = pps;
        this.averageSpeed = speed;
        this.gravityComplient = gravityComplient;
        this.averageLifeLength = lifeLength;
        this.averageScale = scale;
        this.texture = texture;
        this.texStart = 0;
		this.texEnd = texture.getNumAtlasRows()*texture.getNumAtlasRows();
    }
    
    public ParticleEmitter(String texture, float pps, float speed, float gravityComplient, float lifeLength, float scale) {
        this(Resources.getTexture(texture), pps, speed, gravityComplient, lifeLength, scale);
    }
 
    /**
     * @param direction - The average direction in which particles are emitted.
     * @param deviation - A value between 0 and 1 indicating how far from the chosen direction particles can deviate.
     */
    public void setOrigin(Vector3f origin) {
    	this.origin = origin;
    }
    
    public void setRotation(float rotation) {
    	this.rotation = rotation;
    }
    
    public void setDirection(Vector3f direction, float deviation) {
        this.direction = new Vector3f(direction);
        this.directionDeviation = (float) (deviation * Math.PI);
    }
 
    public void setTextureAtlasRange(int start, int end) {
		this.texStart = start;
		this.texEnd = end;
	}
    
    public void randomizeRotation() {
        randomRotation = true;
    }
 
    /**
     * @param error
     *            - A number between 0 and 1, where 0 means no error margin.
     */
    public void setSpeedError(float error) {
        this.speedError = error * averageSpeed;
    }
 
    /**
     * @param error
     *            - A number between 0 and 1, where 0 means no error margin.
     */
    public void setLifeError(float error) {
        this.lifeError = error * averageLifeLength;
    }
 
    /**
     * @param error
     *            - A number between 0 and 1, where 0 means no error margin.
     */
    public void setScaleError(float error) {
        this.scaleError = error * averageScale;
    }
 
    public void generateParticles(Camera camera) {
    	range = Vector3f.distance(camera.getPosition(), origin);
    	if (range > MAX_RANGE) return;
        float delta = Window.deltaTime;
        float particlesToCreate = pps * delta;
        int count = (int) Math.floor(particlesToCreate);
        float partialParticle = particlesToCreate % 1;
        for (int i = 0; i < count; i++) {
            emitParticle(origin);
        }
        if (Math.random() < partialParticle) {
            emitParticle(origin);
        }
    }
 
    private void emitParticle(Vector3f center) {
        Vector3f velocity = null;
        if(direction!=null){
            velocity = generateRandomUnitVectorWithinCone(direction, directionDeviation);
        }else{
            velocity = generateRandomUnitVector();
        }
        velocity.normalize();
        velocity.scale(generateValue(averageSpeed, speedError));
        float scale = generateValue(averageScale, scaleError);
        float lifeLength = generateValue(averageLifeLength, lifeError);
        new Particle(texture, new Vector3f(center), velocity, gravityComplient, lifeLength, generateRotation(), rotation, scale, texStart, texEnd);
    }
 
    private float generateValue(float average, float errorMargin) {
        float offset = (random.nextFloat() - 0.5f) * 2f * errorMargin;
        return average + offset;
    }
 
    private float generateRotation() {
        if (randomRotation) {
            return random.nextFloat() * 360f;
        } else {
            return 0f;
        }
    }
    
    private static Vector3f generateRandomUnitVectorWithinCone(Vector3f coneDirection, float angle) {
    	float cosAngle = (float) Math.cos(angle);
        Random random = new Random();
        float theta = (float) (random.nextFloat() * 2f * Math.PI);
        float y = cosAngle + (random.nextFloat() * (1 - cosAngle));
        float rootOneMinusZSquared = (float) Math.sqrt(1 - y * y);
        float x = (float) (rootOneMinusZSquared * Math.cos(theta));
        float z = -(float) (rootOneMinusZSquared * Math.sin(theta));
 
        Vector4f direction = new Vector4f(x, y, z, 1);
        if (coneDirection.x != 0 || coneDirection.z != 0 || (coneDirection.y != 1 && coneDirection.y != -1)) {
            Vector3f rotateAxis = Vector3f.cross(coneDirection, new Vector3f(0, 1, 0));
            rotateAxis.normalize();
            float rotateAngle = (float) Math.acos(Vector3f.dot(coneDirection, new Vector3f(0, 1, 0)));
            Matrix4f rotationMatrix = new Matrix4f();
            rotationMatrix.rotate((float) -Math.toDegrees(rotateAngle), rotateAxis);
            direction = Matrix4f.transform(rotationMatrix, direction);
        } else if (coneDirection.y == -1) {
            direction.y *= -1;
        }
        return new Vector3f(direction);
    }
     
    private Vector3f generateRandomUnitVector() {
        float theta = (float) (random.nextFloat() * 2f * Math.PI);
        float y = (random.nextFloat() * 2) - 1;
        float rootOneMinusZSquared = (float) Math.sqrt(1 - y * y);
        float x = (float) (rootOneMinusZSquared * Math.cos(theta));
        float z = (float) (rootOneMinusZSquared * Math.sin(theta));
        return new Vector3f(x, y, z);
    }
 
}