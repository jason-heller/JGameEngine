package opengl.shadows;

import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import opengl.Application;
import pipeline.Model;
import scene.object.StaticEntity;
import scene.object.VisibleObject;
import scene.world.terrain.Chunk;

class ShadowMapEntityRenderer {

	private Matrix4f projectionViewMatrix;
	private ShadowShader shader;
	//private List<Tree> trees;
	//private Vao lowPolyTree = ObjLoader.load(new InnerFile(Settings.RES_FOLDER, "object/tree_lowpoly.obj"));

	/**
	 * @param shader
	 *            - the simple shader program being used for the shadow render
	 *            pass.
	 * @param projectionViewMatrix
	 *            - the orthographic projection matrix multiplied by the light's
	 *            "view" matrix.
	 */
	protected ShadowMapEntityRenderer(ShadowShader shader, Matrix4f projectionViewMatrix) {
		this.shader = shader;
		this.projectionViewMatrix = projectionViewMatrix;
	}

	/**
	 * Renders entieis to the shadow map. Each model is first bound and then all
	 * of the entities using that model are rendered to the shadow map.
	 * 
	 * @param entities
	 *            - the entities to be rendered to the shadow map.
	 */
	protected void render(List<StaticEntity> entities) {
		for (VisibleObject entity : entities) {
			if (entity.getModel() == null ) continue;

			Model rawModel = entity.getModel();
			bindModel(rawModel);
			prepareInstance(entity);
			GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		}
		
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
	
	protected void render(Chunk[][] chunks) {
		shader.mvpMatrix.loadMatrix(projectionViewMatrix);
		for(Chunk[] chunkBatch : chunks) {
			for (Chunk chunk : chunkBatch) {
				if (chunk==null || !chunk.isLoaded()) continue;
				Model rawModel = chunk.getModel();
				bindModel(rawModel);
				GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
		}
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}

	/**
	 * Binds a raw model before rendering. Only the attribute 0 is enabled here
	 * because that is where the positions are stored in the VAO, and only the
	 * positions are required in the vertex shader.
	 * 
	 * @param rawModel
	 *            - the model to be bound.
	 */
	private void bindModel(Model rawModel) {
		GL30.glBindVertexArray(rawModel.id);
		GL20.glEnableVertexAttribArray(0);
	}

	/**
	 * Prepares an entity to be rendered. The model matrix is created in the
	 * usual way and then multiplied with the projection and view matrix (often
	 * in the past we've done this in the vertex shader) to create the
	 * mvp-matrix. This is then loaded to the vertex shader as a uniform.
	 * 
	 * @param entity
	 *            - the entity to be prepared for rendering.
	 */
	private void prepareInstance(VisibleObject entity) {
		entity.getTexture().bind(0);
		Matrix4f modelMatrix = entity.getMatrix();
		Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, modelMatrix, null);
		shader.mvpMatrix.loadMatrix(mvpMatrix);
	}
}
