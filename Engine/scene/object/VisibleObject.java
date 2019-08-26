package scene.object;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import animation.Animator;
import pipeline.Resources;
import pipeline.Texture;

public class VisibleObject {
	protected Model model;
	
	protected Texture diffuse;
	protected Texture specular;
	
	private Matrix4f matrix;
	
	private Animator animator = null;
	
	public VisibleObject(Model model, Texture diffuse, Matrix4f matrix) {
		this.setModel(model);
		this.setDiffuse(diffuse);
		this.setMatrix(matrix);
		
		if (model != null && model.isAnimated()) {
			this.animator = new Animator(model, this);
		}
	}
	
	public VisibleObject(Model model, Texture diffuse) {
		this(model, diffuse, new Matrix4f());
	}
	
	public VisibleObject(Model model, String diffuse) {
		this(model, Resources.getTexture(diffuse), new Matrix4f());
	}
	
	public VisibleObject(String model, String diffuse) {
		this(Resources.getModel(model), Resources.getTexture(diffuse), new Matrix4f());
	}
	
	public void setSpecular(Texture specular) {
		this.specular = specular;
	}
	
	public Texture getSpecular() {
		return specular;
	}
	
	public Animator getAnimator() {
		return animator;
	}
	
	public boolean hasSpecular() {
		return specular != null;
	}

	public boolean isAnimated() {
		return (animator != null);
	}
	
	public Texture getDiffuse() {
		return diffuse;
	}

	public void setDiffuse(Texture diffuse) {
		this.diffuse = diffuse;
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
		if (isAnimated()) {
			animator.destroy();
		} else {
			ObjectControl.removeObject(this);
		}
	}
	
	// Convenience for animation
	public void play(String animation) {
		animator.start(animation, true);
	}
	
	public void play(String animation, int startFrame, int endFrame) {
		animator.start(animation, true, startFrame, endFrame);
	}
	
	public void replay(String animation) {
		animator.start(animation, true);
	}
	
	public void stop() {
		animator.stop();
	}
	
	public void pause() {
		animator.pause();
	}
	
	public void unpause() {
		animator.unpause();
	}
	
	public void loop(String animation) {
		animator.loop(animation);
	}
}
