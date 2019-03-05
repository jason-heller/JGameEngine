package scene.world.terrain;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec2;
import shader.UniformVec2Array;
import shader.UniformVec3;
import shader.UniformVec3Array;
import shader.UniformVec4;

public class TerrainShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "scene/world/terrain/terrainVertex.glsl";
	private static final String FRAGMENT_SHADER = "scene/world/terrain/terrainFragment.glsl";
	
	public static final int MAX_PATH_SEGMENTS = 15;
	public static final int MAX_PATHS = 15;

	public UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	public UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	protected UniformSampler grass = new UniformSampler("grass");
	protected UniformSampler gravel = new UniformSampler("gravel");
	protected UniformSampler dirt = new UniformSampler("dirt");
	protected UniformSampler lmap = new UniformSampler("lmap");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	public UniformVec2 offset = new UniformVec2("offset");
	public UniformVec4 clipPlane = new UniformVec4("clipPlane");
	public UniformVec2Array trailData = new UniformVec2Array("trailData", MAX_PATH_SEGMENTS);
	public UniformVec3Array trailProperties = new UniformVec3Array("trailProperties", MAX_PATHS);
	public UniformMatrix shadowMatrix  = new UniformMatrix("shadowMatrix");;

	public TerrainShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normals");
		super.storeAllUniformLocations(offset, lightDirection, projectionMatrix, viewMatrix, clipPlane, trailProperties, trailData, grass, gravel, dirt, lmap, shadowMatrix);
	}
}
