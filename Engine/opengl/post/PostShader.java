package opengl.post;

import opengl.Window;
import opengl.fbo.FboUtils;
import opengl.fbo.FrameBuffer;
import shader.ShaderProgram;

public abstract class PostShader extends ShaderProgram {
	protected static final String VERTEX_SHADER = "opengl/post/glsl/vertex.glsl";
	
	private FrameBuffer fbo;
	
	public PostShader(String vertexFile, String fragmentFile, int width, int height, String... inVariables) {
		super(vertexFile, fragmentFile, inVariables);
		fbo = FboUtils.createTextureFbo(width, height);
	}
	
	public PostShader(String vertexFile, String fragmentFile, String... inVariables) {
		super(vertexFile, fragmentFile, inVariables);
		fbo = FboUtils.createTextureFbo(Window.getWidth(), Window.getHeight());
	}
	
	public void bindFbo() {
		fbo.bind();
	}
	
	public void unbindFbo() {
		fbo.unbind();
	}
	
	public abstract void loadUniforms();
	
	@Override
	public void cleanUp() {
		super.cleanUp();
		fbo.cleanUp();
	}

	public FrameBuffer getFbo() {
		return fbo;
	}
}
