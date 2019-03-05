package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import logic.collision.BoundingBox;
import logic.collision.CollisionShape;
import logic.collision.CollisionType;
import pipeline.Model;
import pipeline.Texture;
import scene.Scene;
import scene.object.StaticEntity;

public class DefaultEntity extends StaticEntity {
	
	private CollisionType collisionType;
	
	public DefaultEntity(Model model, Texture texture, CollisionType collisionType, boolean dontRender) {
		super(model, texture, new Matrix4f(), dontRender);
		position = new Vector3f();//matrix.getTranslation();
		rotation = new Vector3f();
		
		this.collisionType = collisionType;
	}
	
	public void setModel(Model model) {
		super.setModel(model);
		if (model == null) return;
		collision = new CollisionShape(model.getVertices());
		collision.setType(collisionType);
		
	}
	
	public void update(Scene scene) {
		super.updateMatrix();
	}

}
