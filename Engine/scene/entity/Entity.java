package scene.entity;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import audio.Source;
import global.Globals;
import logic.collision.BoundingBox;
import logic.collision.CollisionShape;
import logic.collision.CollisionType;
import logic.collision.Plane;
import logic.collision.Polygon;
import opengl.Window;
import pipeline.Resources;
import pipeline.Texture;
import scene.Scene;
import scene.object.Model;
import scene.object.StaticEntity;
import scene.object.VisibleObject;
import scene.world.World;
import scene.world.architecture.Architecture;
import scene.world.architecture.components.ArcClip;
import scene.world.architecture.components.ArcEdge;
import scene.world.architecture.components.ArcFace;
import scene.world.architecture.functions.SpawnPoint;
import scene.world.architecture.vis.Bsp;
import scene.world.architecture.vis.BspLeaf;
import scenes.PlayerEntity;

public class Entity {
	private Scene scene;

	public Vector3f position = new Vector3f();
	public Vector3f rotation = new Vector3f();
	public Vector3f velocity = new Vector3f();
	protected Vector3f motion = new Vector3f();

	private static final float WALL_FUDGE_FACTOR = 0.05f;
	private static final float EPSILON = 0.025f;
	private static final float DEFAULT_FRICTION = 3f;

	private Matrix4f matrix;

	protected VisibleObject gfx;
	private boolean tangible = true; // When true, this will collide with heightmaps/other entities
	private boolean active = true;

	private boolean grounded = true;
	private boolean sliding = false;
	private boolean submerged = false;

	protected Source source;
	
	public float maxSpeed = 25f;
	public float friction = DEFAULT_FRICTION;
	public float airFriction = 0f;

	public float scale = 1;

	public boolean visible = true;

	protected BoundingBox obb;

	private boolean locked;
	private boolean persistent, global;

	private boolean previouslyGrounded = true;
	protected boolean ignoreGravity = false;

	private int id;
	public int texture;

	private boolean climbing;

	private Map<Float, ArcFace> intersectedFaces = new HashMap<Float, ArcFace>();

	public Entity(Scene scene, Model model, Texture texture, Matrix4f matrix, String name) {
		this(scene, model, texture, matrix, name.hashCode());
	}

	public Entity(Scene scene, Model model, Texture texture, Matrix4f matrix, int id) {
		this.scene = scene;
		this.matrix = matrix;
		this.id = id;
		gfx = new VisibleObject(model, texture, matrix);
		obb = new BoundingBox(position, scale, scale, scale);
		EntityRenderer.addEntity(this);
		source = new Source();
		source.defaultAttenuation();

	}

	public Entity(Scene scene, Matrix4f matrix, int id) {
		this.scene = scene;
		this.matrix = matrix;
		this.id = id;
		// TODO: this hould look up model/texture based on entity id
		gfx = new VisibleObject(Resources.getModel("cube"), Resources.getTexture("default"), matrix);
		obb = new BoundingBox(position, scale, scale, scale);
		EntityRenderer.addEntity(this);
		source = new Source();
	}

	public void update(Scene scene) {
		if (tangible && !locked && Window.deltaTime < .25f) {
			grounded = false;
			collideWithWorld();
		}

		source.setPosition(position);
		source.setVelocity(velocity);
		
		updateMatrix();
	}

	public void updateMatrix() {
		Matrix4f m = gfx.getMatrix();
		m.identity();
		m.translate(position);
		m.rotate(rotation);
		m.scale(scale);

		if (obb != null)
			obb.update(position, rotation, scale);
	}

	public void accelerate(Vector3f dir, float amount) {
		if (climbing) {
			velocity.y += amount * Window.deltaTime;
			
			velocity.x += dir.x * amount * Window.deltaTime;
			velocity.z += dir.z * amount * Window.deltaTime;
		} else {
			float projVel = Vector3f.dot(velocity, dir); // Vector projection of Current velocity onto accelDir.
			float accelVel = amount * Window.deltaTime; // Accelerated velocity in direction of movment

			// If necessary, truncate the accelerated velocity so the vector projection does
			// not exceed max_velocity
			float speedCap = (submerged && !grounded) ? maxSpeed * 2.8f : maxSpeed;
			if (projVel + accelVel < -speedCap)
				accelVel = -speedCap - projVel;

			if (projVel + accelVel > speedCap)
				accelVel = speedCap - projVel;

			velocity.x += dir.x * accelVel;
			velocity.y += dir.y * accelVel;
			velocity.z += dir.z * accelVel;
		}
	}

	public int getId() {
		return id;
	}

	private void collideWithWorld() {

		if (!submerged && !climbing && !ignoreGravity) {
			velocity.y = Math.max(velocity.y - (Globals.gravity * Window.deltaTime), Globals.maxGravity);
		}

		velocity.add(Vector3f.mul(motion, Window.deltaTime));

		if (!climbing) {
			position.x += (velocity.x) * Window.deltaTime;
			position.z += (velocity.z) * Window.deltaTime;
		}
		
		position.y += (velocity.y) * Window.deltaTime;
		

		if (scene.getWorld().hasArchitecture()) {
			collideWithMapGeom(scene.getWorld().getArchitecture());
		}

		for (Entity entity : EntityRenderer.getEntities()) {
			if (entity.obb == null || entity == this)
				continue;
			collideWithOBB(entity.obb);
		}

		for (int i = 0; i < scene.getObjects().size(); i++) {
			StaticEntity entity = scene.getObjects().get(i);
			if (entity == null || entity.collision == null)
				continue;
			if (entity.collision.getType() == CollisionType.NONSOLID)
				continue;
			collideWithStaticObject(entity.getCollision(), entity.getMatrix());
		}

		// Friction
		if ((!sliding && previouslyGrounded) || submerged) {
			float speed = velocity.length();
			if (speed != 0) {
				float drop = speed * friction * Window.deltaTime;
				if (submerged) {
					drop /= 2;
					grounded = false;
				}
				float offset = Math.max(speed - drop, 0) / speed;
				velocity.mul(offset); // Scale the velocity based on friction.
			}
		} else if (climbing) {
			float speed = Math.abs(velocity.y);
			if (speed != 0) {
				float drop = speed * friction * Window.deltaTime;
				float offset = Math.max(speed - drop, 0) / speed;
				velocity.y *= offset; 
				velocity.x = Math.signum(velocity.x) * velocity.y;
				velocity.z = Math.signum(velocity.z) * velocity.y;
			}
		}
		
		else if (airFriction != 0f && !sliding && !submerged) {
			float speed = new Vector2f(velocity.x,velocity.z).length();
			if (speed != 0f) {
				float drop = speed * airFriction * Window.deltaTime;
				float offset = Math.max(speed - drop, 0) / speed;
				velocity.set(velocity.x*offset,velocity.y,velocity.z*offset ); // Scale the velocity based on friction.
			}
		}

		previouslyGrounded = grounded;
	}

	private void collideWithMapGeom(Architecture map) {
		Bsp bsp = map.bsp;
		
		climbing = false;
		intersectedFaces.clear();
		
		for(BspLeaf leaf : map.getRenderedLeaves()) {
			if (leaf.isNearBbox(this.getObb())) {
				ArcFace[] faces = bsp.getFaces(leaf);
				
				for (ArcFace face : faces) {
					float dist = collideWithFace(bsp, face);
					if (dist != Float.MAX_VALUE) {
						intersectedFaces.put(dist, face);
						
						if (intersectedFaces.size() > 4) break;
					}
				}
				
				for (ArcClip clip : leaf.clips) {
					switch(clip.id) {
					case PLAYER_CLIP:
						if (this instanceof PlayerEntity) {
							clipCollision(bsp, clip);
						}
						break;
						
					case NPC_CLIP:
						break;
						
					case GRATE:
						//clipCollision(bsp, clip);
						break;
						
					case LADDER:
						if (intersectsClip(bsp, clip)) {
							//position.x -= (velocity.x) * Window.deltaTime;
							//position.y -= (velocity.y) * Window.deltaTime;
							//position.z -= (velocity.z) * Window.deltaTime;
							
							climbing = true;
						}
						break;
						
					case TRIGGER_UNIMPL:
						if (intersectsClip(bsp, clip)) {
							// TODO: this
						}
						break;
						
					default:
						clipCollision(bsp, clip);
						break;
					}
				}

			}
		}
		
		int iterations = 0;
		while(iterations < 4) {
			float shortest = Float.MAX_VALUE;
			for(float dist : intersectedFaces.keySet()) {
				if (dist < shortest) {
					shortest = dist;
				}
			}
			if (shortest != Float.MAX_VALUE) {
				if (collideWithFace(bsp, intersectedFaces.get(shortest)) != Float.MAX_VALUE) {
					faceCollide(bsp, intersectedFaces.get(shortest));
					intersectedFaces.remove(shortest);
				}
			} else {
				break;
			}
			iterations++;
		}
	}

	private void faceCollide(Bsp bsp, ArcFace face) {
		Plane plane = bsp.planes[face.planeId];
		
		if (plane.normal.y > WALL_FUDGE_FACTOR) {
			floorCollision(plane);
		} else if (plane.normal.y <= WALL_FUDGE_FACTOR && plane.normal.y >= -WALL_FUDGE_FACTOR) {
			wallCollision(plane);
		} else {
			ceilingCollision(plane);
		}
	}
	
	private float collideWithFace(Bsp bsp, ArcFace face) {
		return faceIntersection(bsp, face.firstEdge, face.numEdges, face.planeId);
	}
	
	private float faceIntersection(Bsp bsp, int firstEdge, int numEdges, int planeId) {
		int lastEdge = firstEdge + numEdges;
		Plane plane = bsp.planes[planeId];
		
		boolean squish = false;
		if (plane.normal.y <= WALL_FUDGE_FACTOR
		&& plane.normal.y >= -WALL_FUDGE_FACTOR) {
			squish = true;
			obb.bounds.y /= 1.25f;
		}
		
		for(int i = firstEdge + 1; i < lastEdge - 1; i++) {
			
			Vector3f p1 = determineVert(bsp.vertices, bsp.edges, bsp.surfEdges, firstEdge);
			Vector3f p2 = determineVert(bsp.vertices, bsp.edges, bsp.surfEdges, i);
			Vector3f p3 = determineVert(bsp.vertices, bsp.edges, bsp.surfEdges, i + 1);
			
			Polygon p = new Polygon(p1,p2,p3);
			
			if (obb.intersects(p)) {
				
				Vector3f projectedPoint = Plane.projectPoint(obb.center, plane.normal, -plane.dist);
				
				float outVec = Vector3f.mul(obb.bounds, plane.normal).length();
				projectedPoint.add(Vector3f.mul(plane.normal, outVec));

				if (squish) {
					obb.bounds.y *= 1.25f;
				}
				return   (projectedPoint.x-obb.center.x)*(projectedPoint.x-position.x)
						   + (projectedPoint.y-obb.center.y)*(projectedPoint.y-obb.center.y)
						   + (projectedPoint.z-obb.center.z)*(projectedPoint.z-obb.center.z);
			}
		}
		if (squish) {
			obb.bounds.y *= 1.25f;
		}
		return Float.MAX_VALUE;
	}
	
	

	private Vector3f determineVert(Vector3f[] vertices, ArcEdge[] edges, int[] surfEdges, int ind) {
		int edgeId = Math.abs(surfEdges[ind]);
		if (surfEdges[ind] < 0) {
			return vertices[edges[edgeId].end];
		}
		return vertices[edges[edgeId].start];
	}

	private boolean intersectsClip(Bsp bsp, ArcClip clip) {
		return faceIntersection(bsp, clip.firstEdge, clip.numEdges, bsp.clipEdges[clip.firstEdge].planeId) != Float.MAX_VALUE;
	}
	
	private void clipCollision(Bsp bsp, ArcClip clip) {
		if (!intersectsClip(bsp,clip)) return;
		
		int lastEdge = clip.firstEdge + clip.numEdges;
		for (int i = clip.firstEdge; i < lastEdge; i++) {
			Plane plane = bsp.planes[bsp.clipEdges[i].planeId];
			
			if (plane.normal.y > WALL_FUDGE_FACTOR) {
				//floorCollision(plane);
			} else if (plane.normal.y <= WALL_FUDGE_FACTOR && plane.normal.y >= -WALL_FUDGE_FACTOR) {
				//wallCollision(plane);
			} else {
				//ceilingCollision(plane);
			}
		}
	}
	
	private void collideWithStaticObject(CollisionShape collision, Matrix4f transform) {
		//Polygon[] polygons = collision.getPolygons();
		BoundingBox broadphase = collision.getBroadphase();

		if (collision.getType() == CollisionType.USE_BOUNDING_BOX) {
			collideWithOBB(broadphase);
			return;
		}
		
		// TODO: triangle collisions
	}

	private void collideWithOBB(BoundingBox otherObb) {
		Vector3f vec = obb.intersects(otherObb);
		if (vec != null) {
			// TODO: this method sucks
			/*if (vec.y >= 0f && vec.y > vec.x && vec.y > vec.z) {
				grounded = true;
				position.y -= velocity.y * Window.deltaTime;
				velocity.y = 0;
				
			} else {
				this.position.add(vec);
			}*/
		}
	}
	
	protected void ceilingCollision(Plane plane) {
		if (velocity.y > 0) {
			velocity.y = 0;
		}
	}

	protected void wallCollision(Plane plane) {
		Vector3f projectedPoint = Plane.projectPoint(position, plane.normal, -plane.dist);
		
		projectedPoint.add(Vector3f.mul(plane.normal, obb.bounds.x+EPSILON));
		position.set(projectedPoint.x, position.y, projectedPoint.z);
		velocity.set(Vector3f.sub(
				Plane.projectPoint(Vector3f.add(position, velocity), plane.normal, -plane.dist), position));
		obb.update(position, rotation, scale);
	}

	

	protected void floorCollision(Plane plane) {
		//if (velocity.y > 0f) return;
		Vector3f projectedPoint = //Plane.projectPoint(position, plane.normal, -plane.dist);
		plane.rayIntersection(position, new Vector3f(0,-1,0));
		//float outVec = Vector3f.mul(obb.bounds, plane.normal).length();
		//projectedPoint.add(Vector3f.mul(plane.normal, outVec));
		position.set(projectedPoint.x, projectedPoint.y+obb.bounds.y-EPSILON, projectedPoint.z);

		/*if (plane.normal.y < SLIDE_ANGLE) {
			Vector3f pseudoSurf = new Vector3f(plane.normal.x, -plane.normal.y, plane.normal.z);
			velocity.add( Vector3f.mul(pseudoSurf, (1f-plane.normal.y) ) );
			position.add(Vector3f.mul(velocity,Window.deltaTime));
			
			if (!sliding) {
				velocity.add( Vector3f.mul(pseudoSurf, -velocity.y ) );
			}
			
			velocity.y = 0f;
			sliding = true;
			grounded = false;
			
			projectedPoint = Plane.projectPoint(position, plane.normal, -plane.dist);
			projectedPoint.add(Vector3f.mul(plane.normal, outVec+EPSILON));
			position.set(projectedPoint.x, projectedPoint.y, projectedPoint.z);
		} else {*/
			grounded = true;
			velocity.y = 0;
		//}
		
		obb.update(position, rotation, scale);
	}

	public void destroy() {
		EntityRenderer.removeEntity(this);
		scene.removeEntity(this);
		source.delete();
	}

	public void deactivate() {
		active = false;
	}

	public void activate() {
		active = true;
	}

	public boolean isActive() {
		return active;
	}

	public Matrix4f getMatrix() {
		return matrix;
	}

	public boolean isGrounded() {
		return grounded;
	}

	public boolean isSliding() {
		return sliding;
	}
	
	public BoundingBox getObb() {
		return obb;
	}

	public void jump(float height) {
		//if (grounded || (submerged && velocity.y < 0)) {
			if (climbing) {
				velocity.x = (-velocity.x);
				velocity.z = (-velocity.z);
				velocity.y = height;
				climbing = false;
				grounded = false;
				sliding = false;
				
				previouslyGrounded = false;
			} else {
				velocity.y = height;
				position.y += 1.5f;
				grounded = false;
				sliding = false;
				previouslyGrounded = false;
			}
		//}
	}

	public VisibleObject getGfx() {
		return gfx;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public void lock() {
		locked = true;
	}

	public void unlock() {
		locked = false;
	}

	public boolean isSubmerged() {
		return submerged;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public boolean isGlobal() {
		return global;
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public void warpTo(SpawnPoint spawn) {
		position.set(spawn.position);
		rotation.set(spawn.rotation);
		
		position.y += obb.bounds.y/2f;
		upwarp(scene.getWorld());
		updateMatrix();
	}

	public void upwarp(World world) {
		float minRaise = Float.POSITIVE_INFINITY;

		if (world.hasArchitecture()) {
			Architecture map = world.getArchitecture();
			Bsp bsp = map.bsp;
			BspLeaf leaf = bsp.walk(Vector3f.add(position, new Vector3f(0,5,0)));
			
			ArcFace[] faces = bsp.getFaces(leaf);
			for (ArcFace face : faces) {
				Plane plane = bsp.planes[face.planeId];
			
				if (plane.normal.y > WALL_FUDGE_FACTOR) {
					float y = plane.projectPoint(position).y;
					
					if (y < position.y-1)
						break;
					
					if (y < minRaise)
						minRaise = y;
				}
			}
		}
		
		if (minRaise != Float.POSITIVE_INFINITY) {
			position.y = minRaise + scale;
		}
	}

	public Source getSource() {
		return source;
	}

	public boolean isClimbing() {
		return climbing;
	}
}
