package scene.world.terrain;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import debug.console.Console;
import logic.collision.BoundingBox;
import logic.collision.CollisionType;
import opengl.Application;
import pipeline.Model;
import pipeline.Resources;
import proceduralgen.GenTerrain;
import scene.entity.DefaultEntity;
import scene.entity.Entity;
import scene.object.StaticEntity;
import scene.world.terrain.biome.Biome;
import scene.world.terrain.region.Region;
import scene.world.terrain.region.RegionType;
import utils.MathUtils;
import utils.ModelBuilder;

public class Chunk {
	public int x, z;

	private int polySize = 128;

	private Model model;

	private float[][] heightmap;
	private Vector2f[] pathPositions = new Vector2f[0];
	private Vector3f[] pathData = new Vector3f[0];
	
	private List<StaticEntity> objects = new ArrayList<StaticEntity>();
	private List<Entity> entities = new ArrayList<Entity>();
	private boolean hasWater = false;

	private Biome[] biomes; // In the cast where the distance between the nearest biomes is < some#, add them all
	private Region[] regions;
	
	private Terrain terrain;
	private byte chunkFlags = 0;
	
	private boolean isLoaded = false;

	private boolean hasHeightmap;

	public Vector3f min, max;

	private boolean isCulled;
	
	public Chunk(Terrain t, int x, int z, int polySize, int stride, String filename) {
		this.x = x;
		this.z = z;
		min = new Vector3f((x*Terrain.chunkSize), -500, (z*Terrain.chunkSize));
		max = new Vector3f((min.x+Terrain.chunkSize),  500, (min.z+Terrain.chunkSize));
		this.polySize = polySize;
		heightmap = new float[stride][stride];
		
		terrain = t;
		
		int centerX = (x*Terrain.chunkSize) + (Terrain.chunkSize/2);
		int centerZ = (z*Terrain.chunkSize) + (Terrain.chunkSize/2);
		biomes = t.getBiomeMap().getClosestBiomes(centerX, centerZ);
		regions = t.getRegionMap().getRegions(centerX, centerZ);
		
		File file = new File(filename+Integer.toString(x)+"_"+Integer.toString(z)+".map");
	
		if (file.exists()) {
			new Thread(() -> {
			DataInputStream is = null;
			
			try {
				is = new DataInputStream(new FileInputStream(file));
				
				// Texture
				//String texture = "";
				//byte textureNameLen = is.readByte();
				//for(int i = 0; i < textureNameLen; i++) {
				//	texture += is.readChar();
				//}
				
				chunkFlags = is.readByte();
				hasHeightmap = (is.readByte()==1);
				boolean left, right, top, bottom, tl, tr, bl, br;
				if (hasHeightmap) {
					left = ((chunkFlags >> 3) & 1)==0;
					right = ((chunkFlags >> 2) & 1)==0;
					top = ((chunkFlags >> 1) & 1)==0;
					bottom = ((chunkFlags) & 1)==0;
					tl = ((chunkFlags >> 7) & 1)==0;
					tr = ((chunkFlags >> 6) & 1)==0;
					bl = ((chunkFlags >> 5) & 1)==0;
					br = ((chunkFlags >> 4) & 1)==0;
					for (int i = 0; i < stride; i++) {
						for (int j = 0; j < stride; j++) {
							int dx = i + (x*(stride-1));
							int dz = j + (z*(stride-1));
							
							heightmap[i][j] = (float) is.readShort();
							
							// Sticks procedurally generated chunks with the non-random ones
							final int transitionSize = 9;// 14 looks nice
							if (i <= transitionSize && left) {
								heightmap[i][j] = MathUtils.sCurveLerp(heightmap[i][j], GenTerrain.getHeightAt(dx, dz, biomes), 1f-(i/(float)transitionSize));
							}
							else if (i >= (stride-1)-transitionSize && right) {
								heightmap[i][j] = MathUtils.sCurveLerp(heightmap[i][j], GenTerrain.getHeightAt(dx, dz, biomes), 1f-(((stride-1)-i)/(float)transitionSize));
							}
							if (j <= transitionSize && top) {
								heightmap[i][j] = MathUtils.sCurveLerp(heightmap[i][j], GenTerrain.getHeightAt(dx, dz, biomes), 1f-(j/(float)transitionSize));
							}
							else if (j >= (stride-1)-transitionSize && bottom) {
								heightmap[i][j] = MathUtils.sCurveLerp(heightmap[i][j], GenTerrain.getHeightAt(dx, dz, biomes), 1f-(((stride-1)-j)/(float)transitionSize));
							}
							
							if (!left&&!right&&!top&&!bottom) {
								if (i <= transitionSize && j <= transitionSize && tl) {
									heightmap[i][j] = MathUtils.sCurveLerp(heightmap[i][j], GenTerrain.getHeightAt(dx, dz, biomes), (1f-(i/(float)transitionSize)) * (1f-(j/(float)transitionSize)));
								}
								else if (i >= (stride-1)-transitionSize && j <= transitionSize && tr) {
									heightmap[i][j] = MathUtils.sCurveLerp(heightmap[i][j], GenTerrain.getHeightAt(dx, dz, biomes), (1f-(((stride-1)-i)/(float)transitionSize)) * (1f-(j/(float)transitionSize)));
								}
								if (i <= transitionSize && j >= (stride-1)-transitionSize && bl) {
									heightmap[i][j] = MathUtils.sCurveLerp(heightmap[i][j], GenTerrain.getHeightAt(dx, dz, biomes), (1f-(i/(float)transitionSize)) * (1f-(((stride-1)-j)/(float)transitionSize)));
								}
								else if (i >= (stride-1)-transitionSize && j >= (stride-1)-transitionSize && br) {
									heightmap[i][j] = MathUtils.sCurveLerp(heightmap[i][j], GenTerrain.getHeightAt(dx, dz, biomes), (1f-(((stride-1)-i)/(float)transitionSize)) * (1f-(j/(float)transitionSize)));
								}
							}
							
							if (heightmap[i][j] < Terrain.waterLevel+4f) { 
								hasWater = true;
							}
						}
					}
				} else {
					GenTerrain.genTerrain(this, biomes, heightmap, x, z);
				}
				// Path data
				
				// TODO: Optimize
				byte totalPaths = is.readByte();
				List<Vector2f> positionsList = new ArrayList<Vector2f>();
				List<Vector3f> dataList = new ArrayList<Vector3f>();

				for(int j = 0; j < totalPaths; j++) {
					byte pathType = is.readByte();
					byte numSegments = is.readByte();
					dataList.add(new Vector3f(Paths.getTextureUnit(pathType), Paths.getWidth(pathType), numSegments));
					
					for(int i = 0; i < numSegments; i++) {
						positionsList.add(new Vector2f(is.readFloat(), is.readFloat()));
					}
				}
				
				this.pathPositions = positionsList.toArray(new Vector2f[0]);
				this.pathData = dataList.toArray(new Vector3f[0]);
				
				byte num;
				// Obj Data
				num = is.readByte();
				for(int i = 0; i < num; i++) {
					int objId = is.readShort();
					int texId = is.readShort();
					byte properties = is.readByte();
					Vector3f position = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
					Vector3f rotation = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
					float scale = is.readFloat();

					
					//t.ioHandler.getModel(objId), t.ioHandler.getTexture(texId), false
					
					CollisionType type = CollisionType.SOLID;
					int colData = (properties & 1) + ((properties >> 1) & 1)*2;
					switch(colData) {
						case 0: type = CollisionType.NONSOLID; break;
						case 1: type = CollisionType.USE_BOUNDING_BOX; break;
						case 2: type = CollisionType.USE_NAV_MESH; break;
					}
					
					DefaultEntity entity = new DefaultEntity(null, Resources.getTexture("default"), type, true);
					
					Application.scene.addObject(entity);
					entity.rotation.set(rotation);
					entity.scale=scale/16f;
					entity.position.set(position);
					
					objects.add(entity);
					t.ioHandler.requestObject(objId, texId, x, z, entity);
					
					/*for(int k = 1; k < 4; k++) {
						for (int j = 1; j < 4; j++) {
							Chunk c = terrain.get(k, j);
							if (c != this && c != null) {
								if (c.fastOBBIntersection(entity.getCollision().getBroadphase())) {
									c.objects.add(entity);
								}
							}
						}
					}*/
				}
				
				// Ent data
				num = is.readByte();
				for(int i = 0; i < num; i++) {
					int id = is.readInt();
					byte propLen = is.readByte();
					String properties = "";
					for(int k = 0; k < propLen; k++) {
						properties += is.readChar();
					}
					Vector3f position = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
					Vector3f rotation = new Vector3f(is.readFloat(), is.readFloat(), is.readFloat());
					float scale = is.readFloat();
					
					
					Matrix4f matrix = new Matrix4f();
					matrix.rotateX(rotation.x);
					matrix.rotateY(rotation.y);
					matrix.rotateZ(rotation.z);
					Entity entity = new Entity(Application.scene, matrix, id);
					Application.scene.addEntity(entity);
					entity.position.set(position);
					entity.scale=scale/16f;
					
					entities.add(entity);
				}
	
				is.close();
			} catch (EOFException e) {
				System.err.println("ERR: Malformed chunk file");
				e.printStackTrace();
				Console.send("quit");
			} catch (IOException e) {
				e.printStackTrace();
			}
			postLoad();
			}).start();
		} else {
			GenTerrain.genTerrain(this, biomes, heightmap, x, z);
			
			postLoad();
		}
	}
	
	private void postLoad() {
		for(Region r : regions) {
			if (r.getType() == RegionType.LAKE) {
				//hasWater = true;
			}
		}
		
		if (hasWater) { 
			terrain.getWaterTable().add(this);
		}
		
	}
	
	void buildModel() {
		model = ModelBuilder.buildHeightmap(x, z, biomes, heightmap, chunkFlags, hasHeightmap, polySize);
		isLoaded = true;
	}

	public Chunk(int x, int z, int polySize, int stride) {
		
		this.x = x;
		this.z = z;
		this.polySize = polySize;
		heightmap = new float[stride][stride];
		GenTerrain.genTerrain(this, biomes, heightmap, x, z);

		model = ModelBuilder.buildHeightmap(x, z, biomes, heightmap, polySize);

	}
	
	public Model getModel() {
		return model;
	}
	
	public Vector3f getPathData(int i) {
		return pathData[i];
	}

	public void destroy() {
		if (model != null && isLoaded) {
			model.cleanUp();
		}
		
		if (hasWater) {
			terrain.getWaterTable().remove(this);
		}
		
		for(StaticEntity object: objects) {
			object.destroy();
			Application.scene.removeObject(object);
		}
	}

	public int getPolygonSize() {
		return this.polySize;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}
	
	public Biome[] getBiomes() {
		return biomes;
	}
	
	public Region[] getRegions() {
		return regions;
	}

	public float[][] getHeightmap() {
		return heightmap;
	}
	
	public List<StaticEntity> getObjects() {
		return objects;
	}
	
	// Incomplete
	public boolean fastOBBIntersection(BoundingBox obb) {
		Vector2f[] shape1corners = new Vector2f[] {
				new Vector2f(x*Terrain.chunkSize,z*Terrain.chunkSize),
				new Vector2f((x+1)*Terrain.chunkSize,z*Terrain.chunkSize),
				new Vector2f(x*Terrain.chunkSize,(z+1)*Terrain.chunkSize),
				new Vector2f((x+1)*Terrain.chunkSize,(z+1)*Terrain.chunkSize)
		};
		Vector2f[] shape2corners = new Vector2f[] {
				new Vector2f(obb.center.x+obb.bounds.x,obb.center.z+obb.bounds.z),
				new Vector2f(obb.center.x-obb.bounds.x,obb.center.z+obb.bounds.z),
				new Vector2f(obb.center.x+obb.bounds.x,obb.center.z-obb.bounds.z),
				new Vector2f(obb.center.x-obb.bounds.x,obb.center.z-obb.bounds.z)
		};
		
		Vector2f minMax1 = new Vector2f(), minMax2 = new Vector2f();
		minMax1=axisTest(Vector2f.X_AXIS, shape1corners);
		minMax2=axisTest(Vector2f.X_AXIS, shape2corners);
		if (!overlaps(minMax1.x, minMax1.y, minMax2.x, minMax2.y)) return false;
		minMax1=axisTest(Vector2f.Y_AXIS, shape1corners);
		minMax2=axisTest(Vector2f.Y_AXIS, shape2corners);
		if (!overlaps(minMax1.x, minMax1.y, minMax2.x, minMax2.y)) return false;

		return true;
	}
	
	private Vector2f axisTest(Vector2f axis, Vector2f[] corners) {
		float minAlong=99999, maxAlong=-99999;
		  for(int i = 0; i < corners.length; i++) {
		    float dotVal = corners[i].dot( axis );
		    if(dotVal < minAlong)  minAlong=dotVal;
		    if(dotVal > maxAlong)  maxAlong=dotVal;
		  }
		  
		  return new Vector2f(minAlong, maxAlong);
	}
	
	private boolean overlaps(float min1, float max1, float min2, float max2) {
		return (min2 <= min2 && min2 <= max1) || (min2 <= min1 && min1 <= max2);
	}

	public Vector3f[] getPathData() {
		return pathData;
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	public Vector2f[] getPathPositions() {
		return pathPositions;
	}

	public void setCulledFlag(boolean b) {
		this.isCulled = b;
	}
	
	public boolean isCulled() {
		return isCulled;
	}

	public boolean hasWater() {
		return hasWater;
	}

	public void setHasWaterFlag(boolean b) {
		hasWater = b;
	}
}
