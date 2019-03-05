package scene.world.architecture;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import debug.console.Console;
import logic.collision.Plane;
import logic.controller.SkyboxController;
import opengl.Application;
import pipeline.Resources;
import pipeline.Texture;
import scene.Scene;
import scene.entity.NPC;
import scene.entity.Warp;
import scene.entity.WarpStyle;
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
import utils.FileUtils;
// Architecture File (for map geom)
public class ArcFile {

	public static Architecture load(Architecture map, Scene scene, String path, String mapFileName, Vector3f offset) {
		DataInputStream in = null;
		String fileName = "src/res/maps/" + path + "/" + mapFileName + ".arc";
	
		map = new Architecture(Application.scene);
		Bsp bsp = new Bsp();
		Pvs pvs = new Pvs();
		//Lightmap lightmap = new Lightmap();
		Vector3f sunVector = new Vector3f(0,1,0);
		
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

			// Planes
			Plane[] planes = new Plane[in.readInt()];
			for(int i = 0; i < planes.length; ++i) {
				planes[i] = new Plane();
				planes[i].normal = FileUtils.readVec3(in);
				planes[i].dist = in.readFloat();
				planes[i].translate(offset);
			}
			bsp.planes = planes;
			
			// Verts
			Vector3f[] verts = new Vector3f[in.readInt()];
			for(int i = 0; i < verts.length; ++i) {
				verts[i] = FileUtils.readVec3(in).add(offset);
			}
			bsp.vertices = verts;
			
			// Edges
			ArcEdge[] edges = new ArcEdge[in.readInt()];
			for(int i = 0; i < edges.length; ++i) {
				edges[i] = new ArcEdge();
				edges[i].start = in.readInt();
				edges[i].end = in.readInt();
			}
			bsp.edges = edges;
			
			// Surfedges
			int[] surfEdges = new int[in.readInt()];
			for(int i = 0; i < surfEdges.length; ++i) {
				surfEdges[i] = in.readInt();
			}
			bsp.surfEdges = surfEdges;
			
			// faces
			ArcFace[] faces = new ArcFace[in.readInt()];
			for(int i = 0; i < faces.length; ++i) {
				faces[i] = new ArcFace();
				faces[i].onNode = in.readByte();
				faces[i].planeId = in.readShort();
				faces[i].firstEdge = in.readInt();
				faces[i].numEdges = in.readShort();
				faces[i].texId = in.readShort();
				faces[i].lmIndex = in.readInt();
				faces[i].lmMins = new int[] {in.readInt(), in.readInt()};
				faces[i].lmSizes = new int[] {in.readInt(), in.readInt()};
				faces[i].lmStyles = new byte[] {in.readByte(), in.readByte(), in.readByte(), in.readByte()};
			}
			
			// nodes
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
				nodes[i].min = new Vector3f(in.readShort(), in.readShort(), in.readShort()).add(offset);
				nodes[i].max = new Vector3f(in.readShort(), in.readShort(), in.readShort()).add(offset);
				nodes[i].firstFace = in.readShort();
				nodes[i].numFaces = in.readShort();
			}
			
			bsp.nodes = nodes;
			
			// leafs
			BspLeaf[] leaves = new BspLeaf[in.readInt()];
			int numClusterLeaves = 0;
			for(int i = 0; i < leaves.length; ++i) {
				leaves[i] = new BspLeaf();
				leaves[i].clusterId = in.readShort();
				leaves[i].min = new Vector3f(in.readShort(), in.readShort(), in.readShort()).add(offset);
				leaves[i].max = new Vector3f(in.readShort(), in.readShort(), in.readShort()).add(offset);
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
			int numEnts = in.readInt();
			
			for(int i = 0; i < numEnts; i++) {
				String name = FileUtils.readString(in);
				byte numTags = in.readByte();
				System.out.println("NAME: "+name+" ["+numTags+" tags]");
				Map<String, String> tags = new HashMap<String, String>();
				
				for(byte k = 0; k < numTags; k++) {
					String key = FileUtils.readString(in);
					String val = FileUtils.readString(in);
					tags.put(key, val);
					System.out.println(key + " " + val);
				}
				
				if (name.equals("spawn")) {
					SpawnPoint spawn = new SpawnPoint(readVec3(tags,"pos").add(offset), readVec3(tags,"rot"), tags.get("label"));
					
					map.addEntity(spawn);
				} else if (name.equals("sun")) {
					sunVector.set(0, readFloat(tags, "yaw"), readFloat(tags, "pitch"));
					// rgb = tags.get("rgb") " byter byteg byte b "
					SkyboxController.setTime(sunVector);
				} else if (name.equals("part_emitter")) {
					// TODO: Finish implementing this function
					//GenericParticleEmitter emitter = new GenericParticleEmitter();
					//map.add(emitter);
				} else if (name.equals("warp")) {
					scene.addObject(new Warp(scene, readVec3(tags, "max"), readVec3(tags, "min"),
							readInt(tags, "first_face"), readInt(tags, "num_faces"),
							WarpStyle.values()[readInt(tags, "style")], tags.get("dest_map"), tags.get("dest_spawn")));
				} else if (name.equals("npc_spawn")) {
					int maxSpawns = readInt(tags, "max_spawn");
					if (maxSpawns == 5) {
						// Todo: this
					}
					
					NPC npc = new NPC(scene, tags.get("character"), readVec3(tags,"pos").add(offset), readVec3(tags,"rot"));
					scene.addEntity(npc);
					npc.position.y += npc.getObb().bounds.y;
				}
			}

			// clips
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
			int numClusters = in.readInt();
			pvs.setNumClusters(numClusters);
			System.out.println("clusters: "+numClusters);
			
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
			
			// Baked Lighting
			byte[][] rgb = new byte[in.readInt()/4][3];
			
			for(int i = 0; i < rgb.length; i++) {
				byte r = in.readByte();
				byte g = in.readByte();
				byte b = in.readByte();
				in.readByte();
				//byte exp = (byte)Math.pow(2, in.readByte());
				rgb[i] = new byte[] {r, g, b};
			}
			//lightmap.setSamples(rgb);
			// Texture Info
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
			}
			
			// Texture list
			List<Texture> mapSpecificTextures = new ArrayList<Texture>();
			List<String> specTexRefernces = new ArrayList<String>();
			String[] textures = new String[in.readInt()];
			for(int i = 0; i < textures.length; ++i) {
				textures[i] = FileUtils.readString(in);
				
				if (!textures[i].equals("INVIS") 
					&& !textures[i].equals("LADDER")
					&& !textures[i].equals("TRIGGER")
						&& Resources.getTexture(textures[i]) == null) {
					String texturePath = "maps/" + path + "/" + textures[i] + ".png";
					if (FileUtils.getInputStream("res/"+texturePath) == null) {
						System.err.println("ERR: Nonexistant texture: " + texturePath);
						textures[i] = "default";
					} else {
						mapSpecificTextures.add(Resources.addTexture(textures[i], texturePath));
						specTexRefernces.add(textures[i]);
						
					}
				}
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
				clusters[i].buildModel(planes, edges, surfEdges, verts, faces, leafIds, texData, textures, rgb);
			}
			
			map.setProperties(mapName, mapVer, id, sunVector);
			map.setMapSpecificTextures(mapSpecificTextures, specTexRefernces);
			map.bsp = bsp;
			map.pvs = pvs;
			//map.lightmap = lightmap;
			map.faces = faces;
			map.loaded = true;
			
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
