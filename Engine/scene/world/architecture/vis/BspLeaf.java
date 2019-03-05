package scene.world.architecture.vis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import logic.collision.Plane;
import pipeline.Model;
import scene.object.VisibleObject;
import scene.world.architecture.components.ArcClip;
import scene.world.architecture.components.ArcClipEdge;
import scene.world.architecture.components.ArcEdge;
import scene.world.architecture.components.ArcFace;
import scene.world.architecture.components.ArcTextureData;

public class BspLeaf {
	public short clusterId;
	public Vector3f min, max;
	public short firstFace;
	public short numFaces;
	public short waterDataId;

	private VisibleObject[] visibleObjects;
	private int lightmapTexture;
	public List<ArcClip> clips = new ArrayList<ArcClip>();

	// Prior to CS:GO, all BSP files only have one leaf per cluster.
	public void buildModel(Plane[] planes, ArcEdge[] edges, int[] surfEdges, Vector3f[] vertices, ArcFace[] faces,
			short[] leafFaceIndices, ArcTextureData[] textureData, String[] textureList, byte[][] lightmap) {
		// Partition faces by texture
		Map<String, List<ArcFace>> faceMap = new HashMap<String, List<ArcFace>>();

		int lastFace = firstFace + numFaces;
		for (int j = firstFace; j < lastFace; j++) {
			ArcFace face = faces[leafFaceIndices[j]];
			String tex = textureList[textureData[face.texId].textureId];

			if (tex.equals("INVIS"))
				continue;

			if (faceMap.containsKey(tex)) {
				faceMap.get(tex).add(face);
			} else {
				List<ArcFace> list = new ArrayList<ArcFace>();
				list.add(face);
				faceMap.put(tex, list);
			}
		}

		// Build model for each partition

		visibleObjects = new VisibleObject[faceMap.keySet().size()];

		int mdlIndex = 0;
		for (String tex : faceMap.keySet()) {
			// TODO: FINISH
			List<ArcFace> partitionedFaces = faceMap.get(tex);
			int numVerts = 0;
			for (ArcFace face : partitionedFaces) {
				numVerts += face.numEdges - 2;
			}
			numVerts *= 3;

			float[] mdlVerts = new float[numVerts * 3];
			float[] mdlTxtrs = new float[numVerts * 2];
			float[] mdlLgtMp = new float[numVerts * 2];
			float[] mdlNorms = new float[numVerts * 3];

			int v = 0, t = 0, n = 0;

			for (int i = partitionedFaces.size() - 1; i >= 0; i--) {
				ArcFace face = partitionedFaces.get(i);
				int lastEdge = face.firstEdge + face.numEdges;

				for (int j = face.firstEdge + 1; j < lastEdge - 1; j++) {
					// Gross but fast
					Vector3f vert;
					float[][] texVecs;
					Vector3f norm = planes[face.planeId].normal;
					mdlNorms[n++] = norm.x;
					mdlNorms[n++] = norm.y;
					mdlNorms[n++] = norm.z;
					mdlNorms[n++] = norm.x;
					mdlNorms[n++] = norm.y;
					mdlNorms[n++] = norm.z;
					mdlNorms[n++] = norm.x;
					mdlNorms[n++] = norm.y;
					mdlNorms[n++] = norm.z;

					vert = determineVert(vertices, edges, surfEdges, face.firstEdge);
					mdlVerts[v++] = vert.x;
					mdlVerts[v++] = vert.y;
					mdlVerts[v++] = vert.z;

					texVecs = textureData[face.texId].texels;
					mdlTxtrs[t++] = ((texVecs[0][0] * vert.x + texVecs[0][1] * vert.y + texVecs[0][2] * vert.z) + texVecs[0][3]);
					mdlTxtrs[t++] = ((texVecs[1][0] * vert.x + texVecs[1][1] * vert.y + texVecs[1][2] * vert.z) + texVecs[1][3]);

					vert = determineVert(vertices, edges, surfEdges, j);
					mdlVerts[v++] = vert.x;
					mdlVerts[v++] = vert.y;
					mdlVerts[v++] = vert.z;

					texVecs = textureData[face.texId].texels;
					mdlTxtrs[t++] = ((texVecs[0][0] * vert.x + texVecs[0][1] * vert.y + texVecs[0][2] * vert.z) + texVecs[0][3]);
					mdlTxtrs[t++] = ((texVecs[1][0] * vert.x + texVecs[1][1] * vert.y + texVecs[1][2] * vert.z) + texVecs[1][3]);

					vert = determineVert(vertices, edges, surfEdges, j + 1);
					mdlVerts[v++] = vert.x;
					mdlVerts[v++] = vert.y;
					mdlVerts[v++] = vert.z;

					texVecs = textureData[face.texId].texels;
					mdlTxtrs[t++] = ((texVecs[0][0] * vert.x + texVecs[0][1] * vert.y + texVecs[0][2] * vert.z) + texVecs[0][3]);
					mdlTxtrs[t++] = ((texVecs[1][0] * vert.x + texVecs[1][1] * vert.y + texVecs[1][2] * vert.z) + texVecs[1][3]);
				}
			}

			Model model = Model.create();
			model.bind();
			model.createAttribute(0, mdlVerts, 3);
			model.createAttribute(1, mdlTxtrs, 2);
			model.createAttribute(2, mdlNorms, 3);
			model.unbind();

			visibleObjects[mdlIndex] = new VisibleObject(model, tex);

			mdlIndex++;
		}
	}

	private Vector3f determineVert(Vector3f[] vertices, ArcEdge[] edges, int[] surfEdges, int ind) {
		int edgeId = Math.abs(surfEdges[ind]);
		if (surfEdges[ind] < 0) {
			return vertices[edges[edgeId].end];
		}
		return vertices[edges[edgeId].start];
	}

	public VisibleObject[] getVisibleObjects() {
		return visibleObjects;
	}

	public void cleanUp() {
		if (visibleObjects == null)
			return;
		for (VisibleObject visObj : visibleObjects) {
			visObj.getModel().cleanUp();
		}
	}

	public boolean contains(Bsp bsp, Vector3f position) {
		int lastFace = firstFace + numFaces;
		for (int j = firstFace; j < lastFace; j++) {
			if (bsp.planes[bsp.faces[bsp.leafFaceIndices[j]].planeId].classify(position, .001f) == Plane.BEHIND) {
				return false;
			}
		}

		return true;
	}

}
