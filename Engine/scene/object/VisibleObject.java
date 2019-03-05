package scene.object;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import animation.Animation;
import animation.Pose;
import pipeline.Model;
import pipeline.Resources;
import pipeline.Texture;

public class VisibleObject {
	protected Texture texture;
	protected Model model;
	private Matrix4f matrix;
	private Pose pose = null;
	private boolean loaded;
	
	public VisibleObject(Model model, Texture texture, Matrix4f matrix, boolean dontRender) {
		this.setModel(model);
		this.setTexture(texture);
		this.setMatrix(matrix);
		
		if (!dontRender && model != null) {
			ObjectControl.addObject(this);
		}
	}
	
	public VisibleObject(Model model, String texture) {
		this(model, Resources.getTexture(texture), new Matrix4f(), true);
	}

	public void setAnimation(Animation animation) {
		pose = new Pose(animation);
	}
	
	public void animate() {
		pose.update();
	}
	
	public Pose getPose() {
		return pose;
	}

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Matrix4f getMatrix() {
		return matrix;
	}

	public void setMatrix(Matrix4f matrix) {
		this.matrix = matrix;
	}
	
	public void setMatrix(Vector3f position) {
		this.matrix.translate(position);
	}
	
	public void destroy() {
		ObjectControl.removeObject(this);
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	
	public boolean isLoaded() {
		return loaded;
	}
}
