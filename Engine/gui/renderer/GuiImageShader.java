package gui.renderer;

import shader.ShaderProgram;
import shader.UniformBoolean;
import shader.UniformFloat;
import shader.UniformVec3;
import shader.UniformVec4;

public class GuiImageShader extends ShaderProgram {

	private static final String VERTEX_FILE = "gui/renderer/guiVertex.glsl";
	private static final String FRAGMENT_FILE = "gui/renderer/guiFragment.glsl";

	public UniformVec4 translation = new UniformVec4("translation");
	public UniformVec4 offset = new UniformVec4("offset");
	public UniformVec3 color = new UniformVec3("color");
	public UniformBoolean centered = new UniformBoolean("centered");
	public UniformFloat opacity = new UniformFloat("opacity");
	public UniformFloat rotation = new UniformFloat("rot");
	
	public GuiImageShader() {
		super(VERTEX_FILE, FRAGMENT_FILE, "position", "textureCoords");
		super.storeAllUniformLocations(color, translation, offset, centered, opacity, rotation);
	}


}
