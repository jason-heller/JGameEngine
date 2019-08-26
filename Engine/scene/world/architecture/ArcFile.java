package scene.world.architecture;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import debug.console.Console;
import gr.zdimensions.jsquish.Squish;
import gui.Gui;
import gui.GuiControl;
import logic.collision.BoundingBox;
import logic.collision.CollisionShape;
import logic.collision.Plane;
import logic.controller.SkyboxController;
import opengl.Application;
import opengl.Window;
import pipeline.Resources;
import pipeline.Texture;
import pipeline.util.TextureUtils;
import scene.Scene;
import scene.entity.Warp;
import scene.entity.WarpStyle;
import scene.object.Model;
import scene.object.ObjectControl;
import scene.object.StaticEntity;
import scene.skybox._3D.Skybox3D;
import scene.skybox._3D.SkyboxCamera;
import scene.world.architecture.components.ArcClip;
import scene.world.architecture.components.ArcClipEdge;
import scene.world.architecture.components.ArcEdge;
import scene.world.architecture.components.ArcFace;
import scene.world.architecture.components.ArcTextureData;
import scene.world.architecture.components.ClipType;
import scene.world.architecture.functions.SpawnPoint;
import scene.world.architecture.vis.Bsp;
import scene.world.architecture.vis.BspLeaf;
import scene.world.architecture.vis.BspNode;
import scene.world.architecture.vis.Pvs;
import utils.Colors;
import utils.FileUtils;
import utils.MathUtils;
// Architecture File (for map geom)
public class ArcFile {
	
	private static String data = "";
	private static float steps = 17; // References -> find references (count # times render() is called here)
	private static float completedSteps = 0;
	private static String mapFileName = "";
	private static boolean doRender = true;
	
	private static void render(Scene scene, String text) {
		if (!doRender) return;
		data = text;
		Gui gui = scene.getGui();
		completedSteps++;
		//GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		gui.drawRect(0,0,1280,720,Colors.BLACK);
		gui.drawImage("loading_screen", 0, 0);
		gui.drawRect(1024, 656, 256, 64, Colors.GUI_BACKGROUND_COLOR);
		gui.drawString("Loading map: "+mapFileName, 1029, 656+5, .175f, false);
		gui.drawString(data, 1029, 656+42, .175f, false);
		
		gui.drawRect(1029, 656+25, 246, 12, Colors.GUI_BORDER_COLOR);
		gui.drawRect(1029, 656+25, (int)(246*(completedSteps/steps)), 12, Colors.WHITE);
		
		Window.update();
		GuiControl.render(scene);
		
	}
	
	public static Architecture load(Architecture map, Scene scene, String mapFileName) {
		return load(map, scene, mapFileName, true);
	}
	
	public static Architecture load(Architecture map, Scene scene, String mapFileName, boolean doRender) {
		ArcFile.mapFileName = mapFileName;
		data = "";
		completedSteps = 0;
		render(scene, "");
		DataInputStream in = null;
		String fileName = "src/res/maps/" + mapFileName + ".arc";
	
		map = new Architecture(scene);
		Bsp bsp = new Bsp();
		Pvs pvs = new Pvs();
		Lightmap lightmap = new Lightmap();
		Vector3f sunVector = new Vector3f(.5f,-.5f,0);
		
		try {
			in = new DataInputStream(new FileInputStream(fileName));

			// Header
			String identifier = new String(new byte[] {in.readByte(), in.readByte(), in.readByte()});
			if (!identifier.equals("ARC")) {
				Console.log("Error: Tried to load an "+identifier+" file as an ARC file");
				return null;
			}
			
			byte id = in.readByte();
			byte game = in.readByte();
			
			if (game != 1) {
				Console.log("Error: ARC file is not formatted for this game");
				return null;
			}
			
			byte mapVer = in.readByte();
			String mapName = FileUtils.readString(in);
			
			// Skybox
			render(scene, "Loading skybox");
			List<Texture> mapSpecificTextures = new ArrayList<Texture>();
			List<String> specTexReferences = new ArrayList<String>();
			String skyboxTexture = "";
			skyboxTexture = FileUtils.readString(in);
			if (!skyboxTexture.equals("")) {
				byte compression = in.readByte();
				int width = in.readShort();
				int height = in.readShort();
				int dataLen = in.readInt();
				byte[][] textureData = new byte[6][dataLen];
				for(int face = 0; face < 6; face++) {
					for(int l = 0; l < dataLen; l++) {
						textureData[face][l] = in.readByte();
					}
				}
				
				Squish.CompressionType compType;
				switch(compression) {
				case 3: compType = Squish.CompressionType.DXT3; break;
				case 5: compType = Squish.CompressionType.DXT5; break;
				default: compType = Squish.CompressionType.DXT1;
				}
				
				byte[][] skyboxData = new byte[6][];
				for(int face = 0; face < 6; face++) {
					skyboxData[face] = Squish.decompressImage(null, width, height, textureData[face], compType);
				}
				
				Resources.addTexture("skybox", TextureUtils.createTexture(skyboxData, width, height));
			}
			
			// Baked Lighting
			render(scene, "Loading lightmap");
			int lmDataLenBytes = in.readInt();
			byte[] rgb = new byte[lmDataLenBytes];
			for(int i = 0; i < lmDataLenBytes; i++) {
				rgb[i] = in.readByte();
			}

			// Planes
			render(scene, "Loading planes");
			Plane[] planes = new Plane[in.readInt()];
			for(int i = 0; i < planes.length; ++i) {
				planes[i] = new Plane();
				planes[i].normal = FileUtils.readVec3(in);
				planes[i].dist = in.readFloat();
			}
			bsp.planes = planes;
			
			// Verts
			render(scene, "Loading verticess");
			Vector3f[] verts = new Vector3f[in.readInt()];
			for(int i = 0; i < verts.length; ++i) {
				verts[i] = FileUtils.readVec3(in);
			}
			bsp.vertices = verts;
			
			// Edges
			render(scene, "Loading edges");
			ArcEdge[] edges = new ArcEdge[in.readInt()];
			for(int i = 0; i < edges.length; ++i) {
				edges[i] = new ArcEdge();
				edges[i].start = in.readInt();
				edges[i].end = in.readInt();
			}
			bsp.edges = edges;
			
			// Surfedges
			render(scene, "Loading surface edges");
			int[] surfEdges = new int[in.readInt()];
			for(int i = 0; i < surfEdges.length; ++i) {
				surfEdges[i] = in.readInt();
			}
			bsp.surfEdges = surfEdges;
			
			// faces
			render(scene, "Loading faces");
			ArcFace[] faces = new ArcFace[in.readInt()];
			for(int i = 0; i < faces.length; ++i) {
				faces[i] = new ArcFace();
				faces[i].onNode = in.readByte();
				faces[i].planeId = in.readShort();
				faces[i].firstEdge = in.readInt();
				faces[i].numEdges = in.readShort();
				faces[i].texId = in.readShort();
				faces[i].lmIndex = in.readInt();
				faces[i].lmMins = new float[] {in.readFloat(), in.readFloat()};
				faces[i].lmSizes = new float[] {in.readFloat(), in.readFloat()};
				
				faces[i].lmStyles = new byte[] {in.readByte(), in.readByte(), in.readByte(), in.readByte()};
				
			}
			
			lightmap.create(rgb, faces);
			
			// nodes
			render(scene, "Loading nodes");
			short[] leafIds = new short[in.readInt()];
			for(int i = 0; i < leafIds.length; i++) {
				leafIds[i] = in.readShort();
			}
			bsp.faces = faces;
			bsp.leafFaceIndices = leafIds;
			
			
			BspNode[] nodes = new BspNode[in.readInt()];
			for(int i = 0; i < nodes.length; ++i) {
				nodes[i] = new BspNode();
				nodes[i].planeNum = in.readInt();
				nodes[i].childrenId[0] = in.readInt();
				nodes[i].childrenId[1] = in.readInt();
				nodes[i].min = new Vector3f(in.readShort(), in.readShort(), in.readShort());
				nodes[i].max = new Vector3f(in.readShort(), in.readShort(), in.readShort());
				nodes[i].firstFace = in.readShort();
				nodes[i].numFaces = in.readShort();
			}
			
			bsp.nodes = nodes;
			
			// leafs
			render(scene, "Loading leafs");
			BspLeaf[] leaves = new BspLeaf[in.readInt()];
			int numClusterLeaves = 0;
			for(int i = 0; i < leaves.length; ++i) {
				leaves[i] = new BspLeaf();
				leaves[i].clusterId = in.readShort();
				leaves[i].min = new Vector3f(in.readShort(), in.readShort(), in.readShort());
				leaves[i].max = new Vector3f(in.readShort(), in.readShort(), in.readShort());
				leaves[i].firstFace = in.readShort();
				leaves[i].numFaces = in.readShort();
				leaves[i].waterDataId = in.readShort();
				
				if (leaves[i].clusterId != -1) {
					numClusterLeaves++;
				}
			}
			
			BspLeaf[] clusters = new BspLeaf[numClusterLeaves];
			int j = 0;
			for(int i = 0; i < leaves.length; ++i) {
				if (leaves[i].clusterId != -1) {
					clusters[j++] = leaves[i];
				}
			}
			bsp.leaves = leaves;
			
			// face ids for leafs
			short[] faceIds = new short[in.readInt()];

			for(int i = 0; i < faceIds.length; ++i) {
				faceIds[i] = in.readShort();
			}
			
			// entities
			render(scene, "Loading entities");
			int numEnts = in.readInt();
			
			for(int i = 0; i < numEnts; i++) {
				String name = FileUtils.readString(in);
				byte numTags = in.readByte();
				//System.out.println("NAME: "+name+" ["+numTags+" tags]");
				Map<String, String> tags = new HashMap<String, String>();
				for(byte k = 0; k < numTags; k++) {
					String key = FileUtils.readString(in);
					String val = FileUtils.readString(in);
					tags.put(key, val);
					//System.out.println(key + " " + val);
				}
				if (name.equals("spawn")) {
					SpawnPoint spawn = new SpawnPoint(readVec3(tags,"pos"), readVec3(tags,"rot"), tags.get("label"));
					
					map.addEntity(spawn);
				} else if (name.equals("sun")) {
					float pitch = readFloat(tags, "pitch");
					float yaw = readFloat(tags, "yaw");
					sunVector.set(MathUtils.eulerToVectorDeg(yaw, pitch));
					//e.add("lightcolor", entity.getValue("brightness"));
					//e.add("shadowcolor", entity.getValue("ambient"));
					// rgb = tags.get("rgb") " byter byteg byte b "
				} else if (name.equals("part_emitter")) {
					// TODO: Finish implementing this function
					//GenericParticleEmitter emitter = new GenericParticleEmitter();
					//map.add(emitter);
				} else if (name.equals("warp")) {
					scene.addObject(new Warp(scene, readVec3(tags, "max"), readVec3(tags, "min"),
							readInt(tags, "first_face"), readInt(tags, "num_faces"),
							WarpStyle.values()[readInt(tags, "style")], tags.get("dest_map"), tags.get("dest_spawn")));
				} else if (name.equals("sky3d")) {
					SkyboxCamera c = new SkyboxCamera(
							readVec3(tags,"pos"),
							readFloat(tags,"scale"),
							readInt(tags, "has_fog")!=0,
							readFloat(tags,"fog_start"),
							readFloat(tags,"fog_end"),
							readVec3(tags,"fog_color")
							);
					SkyboxController.setup3DSkybox(c);
					scene.addObject(c);
				}
			}

			// clips
			render(scene, "Loading clips");
			ArcClipEdge[] clipEdges = new ArcClipEdge[in.readShort()];
			for(int i = 0; i < clipEdges.length; i++) {
				clipEdges[i] = new ArcClipEdge();
				clipEdges[i].planeId = in.readShort();
				clipEdges[i].texId = in.readShort();
			}
			bsp.clipEdges = clipEdges;
			
			ArcClip[] clips = new ArcClip[in.readShort()];
			for(int i = 0; i < clips.length; i++) {
				clips[i] = new ArcClip();
				clips[i].id = ClipType.values()[in.readByte()];
				clips[i].firstEdge = in.readInt(); // ptr to clipedges
				clips[i].numEdges = in.readInt();
				//clips[i].faceId = bsp.clipEdges[clips[i].firstEdge].planeId;
				
				// Todo: move this to map compiler
				for(BspLeaf leaf : leaves) {
					for(int k = leaf.firstFace; k < leaf.firstFace+leaf.numFaces; k++) {
						for(int l = clips[i].firstEdge; l < clips[i].firstEdge+clips[i].numEdges; l++) {
							if (faceIds[k] == clipEdges[l].planeId) {
								leaf.clips.add(clips[i]);
								break;
							}
						}
					}
				}
			}
			
			// vis
			render(scene, "Loading BSP tree");
			int numClusters = in.readInt();
			pvs.setNumClusters(numClusters);
			//System.out.println("clusters: "+numClusters);
			
			int[][] clusterPointers = new int[numClusters][2];
			for(int i = 0; i < numClusters; ++i) {
				clusterPointers[i][0] = in.readInt();
				clusterPointers[i][1] = in.readInt();
			}
			
			pvs.setClusterPointers(clusterPointers);
			
			int visLen = in.readInt();
			byte[] visData = new byte[visLen];
			for(int i = 0; i < visLen; ++i) {
				visData[i] = in.readByte();
			}
			
			pvs.setVisData(visData);
			
			// Texture Info
			render(scene, "Loading textures");
			ArcTextureData[] texData = new ArcTextureData[in.readInt()];
			for(int i = 0; i < texData.length; ++i) {
				texData[i] = new ArcTextureData();
				texData[i].textureId = in.readInt();
				texData[i].texels[0][0] = in.readFloat();
				texData[i].texels[0][1] = in.readFloat();
				texData[i].texels[0][2] = in.readFloat();
				texData[i].texels[0][3] = in.readFloat();
				texData[i].texels[1][0] = in.readFloat();
				texData[i].texels[1][1] = in.readFloat();
				texData[i].texels[1][2] = in.readFloat();
				texData[i].texels[1][3] = in.readFloat();
				texData[i].lmVecs[0][0] = in.readFloat();
				texData[i].lmVecs[0][1] = in.readFloat();
				texData[i].lmVecs[0][2] = in.readFloat();
				texData[i].lmVecs[0][3] = in.readFloat();
				texData[i].lmVecs[1][0] = in.readFloat();
				texData[i].lmVecs[1][1] = in.readFloat();
				texData[i].lmVecs[1][2] = in.readFloat();
				texData[i].lmVecs[1][3] = in.readFloat();
			}
			
			// Texture list
			String[] textures = new String[in.readInt()];
			for(int i = 0; i < textures.length; ++i) {
				textures[i] = FileUtils.readString(in);
				/*
				 * STRING name
				 * BYTE compression
				 * BYTE material
				 * SHORT width 
				 * SHORT height
				 * INT dataLen
				 * BYTE[] data
				 */
				byte compression = in.readByte();
				if (!textures[i].equals("INVIS") 
					&& !textures[i].equals("LADDER")
					&& !textures[i].equals("TRIGGER")
					&& compression != 0) {
					/*String texturePath = "maps/" + path + "/" + textures[i] + ".png";
					if (FileUtils.getInputStream("res/"+texturePath) == null) {
						System.err.println("ERR: Nonexistant texture: " + texturePath);
						textures[i] = "default";
					} else {
						mapSpecificTextures.add(Resources.addTexture(textures[i], texturePath));
						specTexRefernces.add(textures[i]);
						
					}*/
					byte material = in.readByte();
					int width = in.readShort();
					int height = in.readShort();
					int dataLen = in.readInt();
					byte[] textureData = new byte[dataLen];
					/*int off = 0;
					int totalRead = 0;
					while(totalRead != -1) {
						totalRead = in.read(textureData, off, dataLen);
						off += totalRead;
					}*/
					for(int l = 0; l < dataLen; l++) {
						textureData[l] = in.readByte();
					}
					Squish.CompressionType compType;
					switch(compression) {
					case 3: compType = Squish.CompressionType.DXT3; break;
					case 5: compType = Squish.CompressionType.DXT5; break;
					default: compType = Squish.CompressionType.DXT1;
					}
					byte[] decompressedData = Squish.decompressImage(null, width, height, textureData, compType);
					Texture t = Resources.addTexture(textures[i], material, decompressedData, width, height);
					mapSpecificTextures.add(t);
					specTexReferences.add(textures[i]);
				}
			}
			
			// Model list
			render(scene, "Loading models");
			int numModels = in.readInt();
			Map<String, Short> modelTextureMap = new HashMap<String, Short>();
			for(int i = 0; i < numModels; i++) {
				byte len = in.readByte();
				if (len != -1) {
					String name = "";
					for(int n = 0; n < len; n++)
						name += in.readChar();
					modelTextureMap.put(name, in.readShort());
					int dataLen = in.readInt();
					byte[] data = new byte[dataLen];
					in.readFully(data);
					Resources.addModel(name, data, true);
				}
			}
			
			// Props
			render(scene, "Loading static entities");
			int numProps = in.readInt();
			for(int i = 0; i < numProps; ++i) {
				String modelName = FileUtils.readString(in);
				Vector3f pos = FileUtils.readVec3(in);
				Vector3f rot = FileUtils.readVec3(in);
				Vector3f light = FileUtils.readVec3(in);
				
				short firstLeaf = in.readShort();
				short numLeafs = in.readShort();
				
				byte solid = in.readByte();
				float fadeMin = in.readFloat();
				float fadeMax = in.readFloat();

				Model model = Resources.getModel(modelName);
				
				if (model == null) {
					model = Resources.getModel("cube");
				}
				
				Short texID = modelTextureMap.get(modelName);
				Texture texture;
				if (texID == null) {
					texture = Resources.getTexture("default");
				}
				else {
					texture = mapSpecificTextures.get(id);
				}
				
				StaticEntity entity = new StaticEntity(model, texture, new Matrix4f());
				entity.position = pos;
				entity.rotation = rot;
				ObjectControl.addObject(entity);

				switch(solid) {
				case 2:	// BBOX
					entity.collision = new CollisionShape(
							new BoundingBox(pos, model.max, model.min));
					break;
					
				case 1: // Tri
					entity.collision = new CollisionShape(model.getVertices());
					break;
					
				default: // None
				}
				
				entity.updateMatrix();
				scene.addObject(entity);
			}
			
			Console.log("Map loaded: "+mapName+" version="+mapVer);
			/*if (Globals.debugMode) {
				System.out.println("planes len: "+planes.length);
				System.out.println("verts len: "+verts.length);
				System.out.println("edges len: "+edges.length);
				System.out.println("surface edges: "+surfEdges.length);
				System.out.println("faces len: "+faces.length);
				System.out.println("nodes len: "+nodes.length);
				System.out.println("leaves len: "+leaves.length);
				System.out.println("cluster#: "+numClusters);
				System.out.println("vis len: "+visData.length);
				System.out.println("texture data len: "+texData.length);
				System.out.println("texture list len: "+textures.length);
			}*/
			
			// If leaf is a cluster (enterable), build model
			for(int i = 0; i < clusters.length; ++i) {
				clusters[i].buildModel(planes, edges, surfEdges, verts, faces, leafIds, texData, textures);
			}
			
			map.setProperties(mapName, mapVer, id);
			map.setSunVector(sunVector);
			map.setMapSpecificTextures(mapSpecificTextures, specTexReferences);
			map.bsp = bsp;
			map.pvs = pvs;
			map.lightmap = lightmap;
			map.faces = faces;
			map.loaded = true;
			
			Skybox3D skybox3D = SkyboxController.getSkybox3D();
			if (skybox3D != null) {
				skybox3D.init(bsp, pvs);
			}
			
			return map;
			
		}
		catch (FileNotFoundException e) {
			Console.log("Tried to load "+mapFileName+", failed");
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static float readFloat(Map<String, String> tags, String string) {
		String data = tags.get(string);
		return data.equals("") ? 0f : Float.parseFloat(data);
	}
	
	private static int readInt(Map<String, String> tags, String string) {
		String data = tags.get(string);
		return data.equals("") ? 0 : Integer.parseInt(data);
	}

	private static Vector3f readVec3(Map<String, String> tags, String string) {
		String[] data = tags.get(string).split(",");
		return new Vector3f(Float.parseFloat(data[0]), Float.parseFloat(data[1]), Float.parseFloat(data[2]));
	}
	
	/*private static void getPolygons(DataInputStream in) throws IOException {
		int numTriangles = in.readInt();
		System.out.println("# polies: "+numTriangles);
		String texture = "default";//FileUtils.readString(in);
		
		float[] vertices 	= new float[numTriangles*3];
		float[] uvs			= new float[numTriangles*2];
		float[] normals		= new float[numTriangles*3];
		
		for (int j = 0; j < numTriangles; j++) {
			vertices[j*3+0] = in.readFloat();
			vertices[j*3+1] = in.readFloat();
			vertices[j*3+2] = in.readFloat();
			
			uvs[j*2+0] = (float)Math.random();in.readFloat();
			uvs[j*2+1] = (float)Math.random();in.readFloat();
			
			normals[j*3+0] = in.readFloat();
			normals[j*3+1] = in.readFloat();
			normals[j*3+2] = in.readFloat();
		}
		
		int numIndices = in.readInt();
		int[] indices = new int[numIndices];
		
		for (int j = 0; j < numIndices; j++) {
			indices[j] = in.readInt();
		}
		
		Model vao = Model.create();
		vao.bind();
		vao.createIndexBuffer(indices);
		vao.createAttribute(0, vertices, 	3);
		vao.createAttribute(1, uvs,		 	2);
		vao.createAttribute(2, normals, 	3);
		vao.unbind();
		vao.setVertexData(indices, vertices);
		
		// SAVE MAP
		Matrix4f mat = new Matrix4f();
		StaticEntity e = new StaticEntity(vao, Resources.getTexture(texture), mat, false);
		e.position = new Vector3f();
		e.rotation = new Vector3f();
		e.collision = new CollisionShape(new float[] {}, mat);
		Application.scene.addObject(e);
	}*/
}