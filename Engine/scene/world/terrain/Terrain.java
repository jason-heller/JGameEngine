package scene.world.terrain;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import debug.Debug;
import debug.tracers.LineRenderer;
import logic.controller.SkyboxController;
import opengl.Application;
import opengl.shadows.ShadowFrameBuffer;
import opengl.shadows.ShadowMapMasterRenderer;
import pipeline.Resources;
import scene.Camera;
import scene.Scene;
import scene.world.World;
import scene.world.terrain.biome.Biome;
import scene.world.terrain.biome.BiomeMap;
import scene.world.terrain.region.RegionMap;
import scene.world.water.WaterTable;
import utils.MathUtils;

public class Terrain {
	private static Chunk[][] data;
	private BiomeMap biomeMap;
	private RegionMap regionMap;
	private static WaterTable waterTable;
	
	private static TerrainShader shader;
	
	private int x, z;
	private static World world;
	private String baseFile = null, directory = null;
	
	private static int strideSize;
	public static int vertexStripeSize = 32;
	public static float waterLevel = -5.5f;
	public final static int polySize = 8;
	public final static int chunkSize = polySize * (vertexStripeSize-1);
	
	private BufferedImage textureMap, heightmap;
	public TerrainIOHandler ioHandler;
	
	public static final float MAX_HEIGHT = 40;
	
	public boolean isPopulated = false;
	
	private static ShadowMapMasterRenderer shadowRenderer;

	public Terrain(World world, int stride, String filename) {
		if (filename != null) {
			this.directory = "src/res/maps/"+filename + "\\";
			this.baseFile = directory + filename;
		}
		if (stride < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
            		stride);

		Terrain.world = world;
		
		biomeMap = new BiomeMap(EnvFileReader.biomes);
		regionMap = new RegionMap(EnvFileReader.regions);
		ioHandler = new TerrainIOHandler();
		ioHandler.init(directory);
		waterTable = new WaterTable();
		
		shader = new TerrainShader();

		Terrain.data = new Chunk[stride][stride];
		strideSize = stride;
		
		try {
			heightmap = ImageIO.read(new File("src/res/heightmap/temp.png"));
			textureMap = ImageIO.read(new File("src/res/heightmap/tex_paint.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Resources.addTexture("grass", "maps/common/snow.png");
		Resources.addTexture("gravel", "maps/common/gravel.png");
		Resources.addTexture("dirt", "maps/common/dirt.png");
		
		shadowRenderer = new ShadowMapMasterRenderer(Application.scene);
	}

	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
	
	public int getStride() {
		return data.length;
	}
	
	public BiomeMap getBiomeMap() {
		return biomeMap;
	}
	
	public RegionMap getRegionMap() {
		return regionMap;
	}
	
	public void shiftX(int dx) {
		byte shiftDir = (byte) Math.signum(dx);
		int shiftStartPos = (shiftDir==1)?1:strideSize-2;
		int shiftEndPos = (shiftDir==1)?strideSize:-1;
		
		
		for(int i = shiftStartPos; i != shiftEndPos; i += shiftDir) {
			for(int j = 0; j < strideSize; j++) {
				if (i == shiftStartPos) {
					data[i-shiftDir][j].destroy();
				}
				data[i-shiftDir][j] = data[i][j];
			}
		}
		
		for(int j = 0; j < strideSize; j++) {
			int i = (shiftDir==1)?strideSize-1:0;
			int nx = data[i][j].getX()+shiftDir;
			int nz = data[i][j].getZ();
			data[i][j] = new Chunk(this, nx, nz, polySize, vertexStripeSize,baseFile);
		}
	}
	
	public void shiftZ(int k) {
		byte shiftDir = (byte) Math.signum(k);
		int shiftStartPos = (shiftDir==1)?1:strideSize-2;
		int shiftEndPos = (shiftDir==1)?strideSize:-1;
		
		for(int i = 0; i < strideSize; i++) {
			for(int j = shiftStartPos; j != shiftEndPos; j += shiftDir) {
				if (j == shiftStartPos) {
					data[i][j-shiftDir].destroy();
				}
				data[i][j-shiftDir] = data[i][j];
			}
		}
		
		for(int i = 0; i < strideSize; i++) {
			int j = (shiftDir==1)?strideSize-1:0;
			int nx = data[i][j].getX();
			int nz = data[i][j].getZ()+shiftDir;
			data[i][j] = new Chunk(this, nx, nz, polySize, vertexStripeSize,baseFile);
		}
	}
	
	public Chunk get(int x, int y) {
		return data[x][y];
	}
	
	public Chunk getAtRealPosition(float x, float z) {
		if (!isPopulated) return null;
		float relx = x - (data[0][0].x*chunkSize);
		float relz = z - (data[0][0].z*chunkSize);
		int tx = (int)Math.floor(relx / chunkSize);
		int tz = (int)Math.floor(relz / chunkSize);
		
		if (tx < 0 || tz < 0 || tx >= data.length || tz >= data[0].length) return null;

		return data[tx][tz];
	}
	
	public Chunk getAtVertexPosition(int x, int z) {
		int relx = x - (data[0][0].x*polySize);
		int relz = z - (data[0][0].z*polySize);
		int tx = (int)Math.floor(relx / chunkSize);
		int tz = (int)Math.floor(relz / chunkSize);
		
		if (tx < 0 || tz < 0 || tx >= data.length || tz >= data[0].length) return null;

		return data[tx][tz];
	}
	
	public Chunk[][] get() {
		return data;
	}

	public void populate(int x, int z) {
		for(int i = 0; i < strideSize; i++) {
			for(int j = 0; j < strideSize; j++) {
				if (data[i][j] != null) {
					data[i][j].destroy();
				}
			}
		}
		this.x = x;
		this.z = z;
		for(int i = 0; i < strideSize; i++) {
			for(int j = 0; j < strideSize; j++) {
				data[i][j] = new Chunk(this, x+(i-((strideSize-1)/2)),z+(j-((strideSize-1)/2)), polySize, vertexStripeSize,baseFile);
			}
		}
		
		isPopulated = true;
	}
	
	public void warp(float newX, float newZ) {
		int camX = (int) Math.floor(newX / chunkSize);
		int camZ = (int) Math.floor(newZ / chunkSize);
		
		populate(camX,camZ);
		world.getFoliage().update(Application.scene);
	}

	public void update(Vector3f currentPosition) {
		//boolean shifted = false;
		int camX = (int) Math.floor(currentPosition.x / chunkSize);
		int camZ = (int) Math.floor(currentPosition.z / chunkSize);
		
		if (!isPopulated) {
			populate(camX,camZ);
			world.getFoliage().update(Application.scene);
			isPopulated = true;
		}
		
		waterLevel = -7.5f + (float)Math.sin(((SkyboxController.getTime()+3.1415f)*MathUtils.TAU)/SkyboxController.DAY_LENGTH)*2f;
		
		if (x != camX) {
			shiftX(camX-x);
			x=camX;
			ioHandler.update(x,z);
			//shifted = true;
		}
		
		if (z != camZ) {
			shiftZ(camZ-z);
			z=camZ;
			ioHandler.update(x,z);
			//shifted = true;
		}
		
		ioHandler.handleObjects();
		
		//return shifted;
	}
	
	public static void renderShadows() {
		Scene scene = Application.scene;
		shadowRenderer.render(scene.getObjects(), scene.getWorld().getTerrain());
		
		if (Debug.showShadowMap) {
			scene.getGui().drawImage("dbgShadow", 1280-256, 0,256,256);
		}
	}
	
	public static void render(Camera c) {
		render(c,0,-1,0,9999,false);
		waterTable.render(c);
	}
	
	public static void render(Camera c, float px, float py, float pz, float pw) {
		render(c,px,py,pz,pw,false);
	}

	public static void render(Camera c, float px, float py, float pz, float pw, boolean drawShadows) {
		if (Debug.chunkBorders) {
			for (Chunk[] chunkStrip : Application.scene.getTerrain().get()) {
				for (Chunk chunk : chunkStrip) {
					LineRenderer.render(Application.scene.getCamera(),
							new Vector3f(chunk.getX() * Terrain.chunkSize, 50, chunk.getZ() * Terrain.chunkSize),
							new Vector3f(chunk.getX() * Terrain.chunkSize, 50,
									Terrain.chunkSize + (chunk.getZ() * Terrain.chunkSize)),
							Vector3f.Z_AXIS);

					LineRenderer.render(Application.scene.getCamera(),
							new Vector3f(Terrain.chunkSize + (chunk.getX() * Terrain.chunkSize), 50,
									(chunk.getZ() * Terrain.chunkSize)),
							new Vector3f(chunk.getX() * Terrain.chunkSize, 50, (chunk.getZ() * Terrain.chunkSize)),
							Vector3f.Z_AXIS);
				}
			}
		}
		
		shader.start();
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		
		shader.grass.loadTexUnit(0);
		shader.gravel.loadTexUnit(1);
		shader.dirt.loadTexUnit(2);
		shader.lmap.loadTexUnit(3);
		
		Resources.getTexture("grass").bind(0);
		Resources.getTexture("gravel").bind(1);
		Resources.getTexture("dirt").bind(2);
		
		//if (drawShadows) {
		shader.shadowMatrix.loadMatrix(shadowRenderer.getToShadowMapSpaceMatrix());
		//GL13.glActiveTexture(GL13.GL_TEXTURE0 + 3);
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowRenderer.getShadowMap());
		Resources.getTexture("dbgShadow").bind(3);
		//}
		
		shader.projectionMatrix.loadMatrix(c.getProjectionMatrix());
		shader.viewMatrix.loadMatrix(c.getViewMatrix());
		shader.lightDirection.loadVec3(Application.scene.getLightDirection());
		shader.clipPlane.loadVec4(px,py,pz,pw);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		for(int i = 0; i < data.length; i++) {
			for(int j = 0; j < data[0].length; j++) {
				if (data[i][j] == null) continue;
				Chunk chunk = data[i][j];
				
				if (!Application.scene.getCamera().getFrustum().containsBoundingBox(chunk.max, chunk.min)) {
					chunk.setCulledFlag(true);
					continue;
				}
				
				chunk.setCulledFlag(false);
				
				if (chunk.getModel() == null && !chunk.isLoaded()) {
					chunk.buildModel();
				}
				
				if (!chunk.isLoaded()) continue;
				
				int pathInd = 0;
				
				Vector2f[] pathPositions = chunk.getPathPositions();
				for(int k = 0; k < pathPositions.length; k++) {
					if (pathInd == TerrainShader.MAX_PATH_SEGMENTS) break;
					
					shader.trailData.loadVec2(pathInd++, pathPositions[k].x, pathPositions[k].y);
				}
				
				pathInd = 0;
				Vector3f[] pathProperties = chunk.getPathData();
				for(int k = 0; k < pathProperties.length; k++) {
					if (pathInd == TerrainShader.MAX_PATHS) break;
					
					shader.trailProperties.loadVec3(pathInd++, pathProperties[k].x, pathProperties[k].y, pathProperties[k].z);
				}
				
				chunk.getModel().bind(0,1,2);
				GL11.glDrawElements(GL11.GL_TRIANGLES, chunk.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
		}
	
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		shader.stop();
	}
	
	public Biome getCurrentBiome(float px, float pz) {
		//final int radius = strideSize/2;
		int left = data[0][0].x * chunkSize;
		int top = data[0][0].z * chunkSize;

		// position of the chunk that px and pz fall in
		int cx = (int) ((px - left)) / chunkSize;
		int cz = (int) ((pz - top)) / chunkSize;

		Chunk chunk = data[cx][cz];
		
		Biome[] biomes = chunk.getBiomes();
		if (biomes == null || biomes.length == 0) return null;
		return biomes[0];
	}

	public Vector3f getPropertiesAt(float px, float pz) {
		if (data[0][0] == null) return null;
		
		// TODO: I think entities cause this to crash when you scroll them off teh chunks
		//final int radius = strideSize/2;
		int left = data[0][0].x*chunkSize;
		int top = data[0][0].z*chunkSize;
		
		// position of the chunk that px and pz fall in
		int cx = (int) ((px-left))/chunkSize;
		int cz = (int) ((pz-top))/chunkSize;
		
		/*if(cx>=Terrain.strideSize)cx--;
		if(cz>=Terrain.strideSize)cz--;
		if(cx<0)cx++;
		if(cz<0)cz++;*/
		
		if (cx>=Terrain.strideSize || cz>=Terrain.strideSize || cx<0 || cz<0) {
			return null;
		}
		
		Chunk chunk = data[cx][cz];
		
		// x & z relative to the chunk
		int rx = (int) ((px-(chunk.x*chunkSize)) / polySize);
		int rz = (int) ((pz-(chunk.z*chunkSize)) / polySize);
		
		if (rx>=Terrain.vertexStripeSize || rz>=Terrain.vertexStripeSize || rx<0 || rz<0) {
			return null;
		}
		
		// x & z relative to their respective polygon
		float polyx = (px-(chunk.x*chunkSize))%polySize;
		float polyz = (pz-(chunk.z*chunkSize))%polySize;
		
		// three 3 positions of polygon
		float y1,y2,y3;
		// output
		float height, slopeComponentX, slopeComponentZ;
		
		// This can be optimized
		if (polyx<polySize-polyz) {
			y1 = chunk.getHeightmap()[rx][rz];
			y2 = chunk.getHeightmap()[rx+1][rz];
			y3 = chunk.getHeightmap()[rx][rz+1];
			
			slopeComponentX = (y2-y1)/polySize;
			slopeComponentZ = (y3-y1)/polySize;
			
			height = MathUtils.barycentric(polyx,polyz,
					new Vector3f(0,y1,0),
					new Vector3f(polySize,y2,0),
					new Vector3f(0,y3,polySize));
		} else {
			y1 = chunk.getHeightmap()[rx+1][rz];
			y2 = chunk.getHeightmap()[rx][rz+1];
			y3 = chunk.getHeightmap()[rx+1][rz+1];
			
			slopeComponentX = (y3-y2)/polySize;
			slopeComponentZ = (y3-y1)/polySize;
			
			height = MathUtils.barycentric(polyx,polyz,
					new Vector3f(polySize,y1,0),
					new Vector3f(0,y2,polySize),
					new Vector3f(polySize,y3,polySize));
		}
		
		//height += Colors.MAX_PIXEL_COLOR/2f;
		//height /= Colors.MAX_PIXEL_COLOR/2f;
		//height *= MAX_HEIGHT;
		
		//slopeComponentX /= Colors.MAX_PIXEL_COLOR;
		//slopeComponentX *= MAX_HEIGHT;
		
		//slopeComponentZ /= Colors.MAX_PIXEL_COLOR;
		//slopeComponentZ *= MAX_HEIGHT;
		
		return new Vector3f(height, slopeComponentX, slopeComponentZ);
	}
	
	public float getTextureData(int dx, int dz) {
		if (dx < 0 || dz < 0 || dx >= heightmap.getWidth() || dz >= heightmap.getHeight()) {
			return 0;
		}
		return textureMap.getRGB(dx, dz);
	}
	
	public void cleanUp() {
		ioHandler.cleanUp();
		shadowRenderer.cleanUp();
	}

	public WaterTable getWaterTable() {
		return waterTable;
	}

	public void resize(int newDist) {
		if (newDist == strideSize) return;
		
		
		Chunk[][] replacement = new Chunk[newDist][newDist];
		
		if (newDist < strideSize) {
			int gap = (strideSize-newDist)/2;
			
			for(int i = 0; i < strideSize; i++) {
				for(int j = 0; j < strideSize; j++) {
					if (i < gap || i >= strideSize-gap || j < gap || j >= strideSize-gap) {
						data[i][j].destroy();
					} else {
						replacement[i-gap][j-gap] = data[i][j];
					}
				}
			}
		} else {
			int gap = (newDist-strideSize)/2;
			
			for(int i = 0; i < newDist; i++) {
				for(int j = 0; j < newDist; j++) {
					if (i < gap || i >= newDist-gap || j < gap || j >= newDist-gap) {
						replacement[i][j] = new Chunk(this, i-((newDist-1)/2),j-((newDist-1)/2), polySize, vertexStripeSize,baseFile);
					} else {
						replacement[i][j] = data[i-gap][j-gap];
					}
				}
			}
		}

		data = replacement;
		strideSize = newDist;
	}
}