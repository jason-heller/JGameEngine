package proceduralgen;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

import be.humphreys.simplevoronoi.Cell;
import be.humphreys.simplevoronoi.GraphEdge;
import be.humphreys.simplevoronoi.GraphSegment;
import be.humphreys.simplevoronoi.IntersectionGraphEdge;
import be.humphreys.simplevoronoi.Site;
import be.humphreys.simplevoronoi.Voronoi;

public class InfiniteVoronoi {
	public static final float NO_SITE = 0.4230428f;

	public static final float INTERSECTION_SIZE = 256f;
	private static final float OVERLAP = 270f;
	
	private Voronoi voronoi;
	//public List<Crossing> crossings = new ArrayList<Crossing>();
	
	public static int minX;

	public static int minY;
	private int size;

	public static int scaledSize;
	private int scale;
	
	private List<GraphEdge> edges;
	//private List<Vehicle> vehicles;
	private float[] sitePosX, sitePosY;
	private int realSize = 0;
	private byte type;
	
	public static final byte PATH_RAILROAD = 0;
	public static final byte PATH_INTERSTATE = 1;

	private long seed;

	private int relX, relY;
	
	public InfiniteVoronoi(byte type, int x, int y, int size, int scale, String seed) {
		this.size = size;
		this.scale = scale;
		this.type = type;
		
		this.seed = 1l + seed.hashCode();
		scaledSize = size*scale;
		voronoi = new Voronoi(12f);
		
		int total = size*size;
		sitePosX = new float[total];
		sitePosY = new float[total];
		
		//vehicles = new ArrayList<Vehicle>();
		
		rebuild(x, y);
		
		populate();
	}
	
	private void populate() {
		
		/*if (type == PATH_RAILROAD) {
			for(GraphEdge edge : edges) {
				if (Math.random() < .95) {
					//float vx = (edge.x1 + edge.x2) / 2f;
					//float vy = (edge.y1 + edge.y2) / 2f;
					vehicles.add(new Vehicle(edge.x2, edge.y2, edge, null));
					//Console.log("New vehicle");
				}
			}
		}*/
	}

	public void update(float x, float y) {
		int nx = (int) Math.floor(x/scale);
		int ny = (int) Math.floor(y/scale);
		
		//vehicleTick();
		
		if (relX != nx || relY != ny) {
			/*List<GraphEdge> crossings = new ArrayList<GraphEdge>();
			for(GraphEdge e : edges) {
				if (e.crossings != null) {
					crossings.add(e);
				}
			}*/
			//crossings.clear();
			rebuild(nx, ny);
			populate();
			/*for(GraphEdge e : crossings) {
				if (!edges.contains(e)) {
					for(GraphEdge e2 : edges) {
						if (e.equals(e2)) {
							if (e2.crossings == null) e2.crossings = new ArrayList<Crossing>();
							e2.crossings.addAll(e.crossings);
							break;
						}
					}
				}
			}*/
			System.gc();
		}
	}

	/*private void vehicleTick() {
		
		List<Crossing> removeThese = new ArrayList<Crossing>(crossings);
		
		for(int i = 0; i < vehicles.size(); i++) {
			Vehicle vehicle = vehicles.get(i);
			//GraphEdge edge = vehicle.edge;
	
			//float s = Chunk.CHUNK_SIZE*BigChunk.CHUNKS_PER_BIGCHUNK;
			float rx = vehicle.x;
			float ry = vehicle.y;
			if (rx < minX || ry < minX || rx > minX+scaledSize || ry > minX+scaledSize) {
				vehicles.remove(i);
				i--;
				//Console.log("vehicle left range");
			}
			
			if (vehicle.getEntity() != null) {
				for(Crossing crossing : crossings) {
					float dx = (crossing.x-vehicle.x);
					float dy = (crossing.y-vehicle.y);
					float dist = (float)Math.sqrt(dx*dx + dy*dy);
					
					// Honk
					if (vehicle.getEntity() != null && vehicle.getEntity() instanceof TrainEntity) {
						TrainEntity e = (TrainEntity)vehicle.getEntity();
						//float xxx = (float) Math.sqrt(distSqr);
						if (dist < TrainEntity.CROSSING_HONK1_DIST
						&& dist > TrainEntity.CROSSING_HONK1_DIST-100) {
							e.blowHorn(1);
						}
						
						else if (dist < TrainEntity.CROSSING_HONK2_DIST
						&& dist > TrainEntity.CROSSING_HONK2_DIST-100) {
							e.blowHorn(10);
						}
					}
					
					//Console.log(""+(distSqr-Crossing.TRIGGER_DIST_SQUARED));
					if (dist < Crossing.TRIGGER_DIST) {
						crossing.active = true;
						crossing.c1.activate();
						crossing.c2.activate();
						removeThese.remove(crossing);
					}
				}
			}
			
			for(Crossing crossing : removeThese) {
				crossing.active = false;
				crossing.c1.deactivate();
				crossing.c2.deactivate();
			}
			
			vehicle.move(this);
		}
			
			/*if (edge.crossings != null) {
				List<Crossing> removeThese = new ArrayList<Crossing>(edge.crossings);

				for(int k = 0; k < edge.crossings.size(); k++) {
					Crossing crossing = edge.crossings.get(k);
					float dx = (crossing.x-vehicle.x);
					float dy = (crossing.y-vehicle.y);
					float dist = (float)Math.sqrt(dx*dx + dy*dy);
					
					// Honk
					if (vehicle.getEntity() != null && vehicle.getEntity() instanceof TrainEntity) {
						TrainEntity e = (TrainEntity)vehicle.getEntity();
						//float xxx = (float) Math.sqrt(distSqr);
						if (dist < TrainEntity.CROSSING_HONK1_DIST
						&& dist > TrainEntity.CROSSING_HONK1_DIST-100) {
							e.blowHorn(1);
						}
						
						else if (dist < TrainEntity.CROSSING_HONK2_DIST
						&& dist > TrainEntity.CROSSING_HONK2_DIST-100) {
							e.blowHorn(10);
						}
					}
					
					//Console.log(""+(distSqr-Crossing.TRIGGER_DIST_SQUARED));
					if (dist < Crossing.TRIGGER_DIST) {
						crossing.active = true;
						crossing.c1.activate();
						crossing.c2.activate();
						removeThese.remove(crossing);
					}
				}
				
				for(Crossing crossing : removeThese) {
					crossing.active = false;
					crossing.c1.deactivate();
					crossing.c2.deactivate();
				}

			}
			
			vehicle.move(this);
		}*/
	//}

	public List<GraphSegment> getEdgesInRegion(int left, int top, int size, int offset, int width, List<Vector2f[]> infra) {
		List<GraphSegment> culledEdges = new ArrayList<GraphSegment>();
		for(GraphEdge edge : edges) {
			List<Vector2f> col = edge.intersection(left, top, size);
			if (col.size() == 2) {
				culledEdges.add(new GraphSegment(col, edge));
			}
			//else {
			col.clear();
			col = edge.intersection(left-offset, top-offset, size+(offset*2));
			//}
			
			if (col.size() >= 2) {
				Vector2f p1 = col.get(0);
				Vector2f p2 = col.get(1);
				float dx = (p1.y - p2.y);
				float dy = (p2.x - p1.x);

				double l = Math.sqrt(dx*dx + dy*dy);
				dx /= l;
				dy /= l;
				dx *= width;
				dy *= width;
				float ddx = (p2.x - p1.x);
				float ddy = (p2.y - p1.y);
				//l = Math.sqrt(ddx*ddx + ddy*ddy);
				ddx /= l;
				ddy /= l;
				float is = InfiniteVoronoi.INTERSECTION_SIZE + OVERLAP;
				ddx *= is;
				ddy *= is;
				p1.x -= ddx;
				p1.y -= ddy;
				p2.x += ddx;
				p2.y += ddy;
				
				Vector2f[] out = new Vector2f[] {
						new Vector2f((p1.x+dx),(p1.y+dy)),
						new Vector2f((p1.x-dx),(p1.y-dy)),
						new Vector2f((p2.x-dx),(p2.y-dy)),
						new Vector2f((p2.x+dx),(p2.y+dy))
						};
				infra.add(out);
			}
		}
		
		return culledEdges;
	}
	
	private void rebuild(int x, int y) {
		relX = x;
		relY = y;
		minX = (relX*scale) - (int)(scaledSize/2.5f);
		minY = (relY*scale) - (int)(scaledSize/2.5f);

		int ind = 0;
		for(int i = 0; i < size; i ++) {
			for(int j = 0; j < size; j ++) {
				//if (GenTerrain.getMountainHeight(i+relX,j+relY) > -.25f) {
					sitePosX[ind] = minX + (i*scale) + (Noise.noise2d(i+relX, j+relY,  seed) * scale);
					sitePosY[ind] = minY + (j*scale) + (Noise.noise2d(i+relX, j+relY, -seed) * scale);
					ind++;
				//}
			}
		}
		
		realSize = ind;
		
		//edges.clear();
		edges = voronoi.generateVoronoi(sitePosX, sitePosY, realSize, minX, minX + scaledSize, minY, minY + scaledSize);
	}

	public Site[] getSites() {
		return voronoi.getSites();
	}

	public List<GraphEdge> getEdges() {
		return this.edges;
	}

	public List<IntersectionGraphEdge> getIntersectionPts(int x, int z) {
		return voronoi.getIntersectionPts(x, z);
	}

	public Cell[] getCells() {
		return voronoi.getCells();
	}
}