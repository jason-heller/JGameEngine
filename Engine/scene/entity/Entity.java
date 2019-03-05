package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import audio.Source;
import global.Globals;
import logic.collision.BoundingBox;
import logic.collision.CollisionShape;
import logic.collision.CollisionType;
import logic.collision.Plane;
import logic.collision.Polygon;
import opengl.Application;
import opengl.Window;
import pipeline.Model;
import pipeline.Resources;
import pipeline.Texture;
import scene.Scene;
import scene.object.StaticEntity;
import scene.object.VisibleObject;
import scene.world.World;
import scene.world.architecture.Architecture;
import scene.world.architecture.components.ArcClip;
import scene.world.architecture.components.ArcFace;
import scene.world.architecture.functions.SpawnPoint;
import scene.world.architecture.vis.Bsp;
import scene.world.architecture.vis.BspLeaf;
import scene.world.terrain.Chunk;
import scene.world.terrain.Terrain;
import scenes.PlayerEntity;
import utils.Input;

public class Entity {
	private Scene scene;

	public Vector3f position = new Vector3f();
	public Vector3f rotation = new Vector3f();
	public Vector3f velocity = new Vector3f();
	protected Vector3f motion = new Vector3f();

	private static final float WALL_FUDGE_FACTOR = 0.05f;
	private static final float FLOOR_STICK_AREA = 1f;

	private static final float SLIDE_ANGLE = .9f;

	private static final float EPSILON = 0.01f;
	private static float stick = 0f;

	private Matrix4f matrix;

	private VisibleObject gfx;
	private boolean tangible = true; // When true, this will collide with heightmaps/other entities
	private boolean active = true;

	private boolean grounded = true;
	private boolean sliding = false;
	private boolean submerged = false;

	protected Source source;

	private float slopeX, slopeZ;
	
	public float maxSpeed = 25f;
	public float friction = 3f;
	public float airFriction = 0f;

	public float scale = 1;

	public boolean visible = true;

	protected BoundingBox obb;

	private boolean locked;
	private boolean persistent, global;

	private boolean previouslyGrounded = true;

	private int id;
	public int texture;

	private boolean climbing;

	

	public Entity(Scene scene, Model model, Texture texture, Matrix4f matrix, String name) {
		this(scene, model, texture, matrix, name.hashCode());
	}

	public Entity(Scene scene, Model model, Texture texture, Matrix4f matrix, int id) {
		this.scene = scene;
		this.matrix = matrix;
		this.id = id;
		gfx = new VisibleObject(model, texture, matrix, true);
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
		gfx = new VisibleObject(Resources.getModel("cube"), Resources.getTexture("default"), matrix, true);
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
		stick = (previouslyGrounded) ? FLOOR_STICK_AREA : 0;

		if (!submerged && !climbing) {
			velocity.y = Math.max(velocity.y - (Globals.gravity * Window.deltaTime), Globals.maxGravity);
		} else {
			stick = 0;
		}

		velocity.add(Vector3f.mul(motion, Window.deltaTime));

		if (!climbing) {
			position.x += (velocity.x) * Window.deltaTime;
			position.z += (velocity.z) * Window.deltaTime;
		}
		
		position.y += (velocity.y) * Window.deltaTime;
		

		if (scene.getWorld().hasHeightmap()) {
			// Water
			Chunk currentChunk = scene.getTerrain().getAtRealPosition(scene.getCamera().getPosition().x,
					scene.getCamera().getPosition().z);
			if (currentChunk != null) {
				if (currentChunk.hasWater() && obb.center.y
						+ (obb.bounds.y / 1.25f) < Terrain.waterLevel) {
					submerged = true;
				} else {
					submerged = false;
				}

				float footPos = obb.center.y;// -obb.bounds.y;
				if (velocity.y < -20 && currentChunk.hasWater()
						&& footPos - (velocity.y * Window.deltaTime) >= Terrain.waterLevel
						&& footPos < Terrain.waterLevel) {
					source.play("splash");
				}
			}

			// Collide with heightmap
			Terrain hmap = scene.getTerrain();
			if (hmap != null) {

				float dx = (position.x - scene.getCamera().getPosition().x);
				float dy = (position.y - scene.getCamera().getPosition().y);
				float dz = (position.z - scene.getCamera().getPosition().z);
				float range = (dx * dx) + (dy * dy) + (dz * dz);
				if (range > Globals.entityDespawnRadSquared) {
					if (!isGlobal()) {
						destroy();
					}
					return;
				}

				Vector3f hmapProperties = hmap.getPropertiesAt(position.x, position.z);

				if (hmapProperties == null) {
					if (!isGlobal()) {
						destroy();
					}
					return;
				}

				collideWithFloor(hmapProperties.x, hmapProperties.y, hmapProperties.z);
			}
		}
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
			if (entity == null)
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
		//BspLeaf leaf = bsp.walk(position);
		
		climbing = false;
		for(BspLeaf leaf : map.getRenderedLeaves()) {
			ArcFace[] faces = bsp.getFaces(leaf);
			for (ArcFace face : faces) {
				faceCollide(bsp, face);
			}
			
			for (ArcClip clip : leaf.clips) {
				switch(clip.id) {
				case PLAYER_CLIP:
					if (this instanceof PlayerEntity) {
						clipCollision(bsp, clip);
					}
					break;
					
				case NPC_CLIP:
					if (this instanceof NPC) {
						//clipCollision(bsp, clip);
					}
					break;
					
				case GRATE:
					//clipCollision(bsp, clip);
					break;
					
				case LADDER:
					if (isInsideClip(bsp, clip)) {
						//position.x -= (velocity.x) * Window.deltaTime;
						//position.y -= (velocity.y) * Window.deltaTime;
						//position.z -= (velocity.z) * Window.deltaTime;
						
						climbing = true;
					}
					break;
					
				case TRIGGER_UNIMPL:
					if (isInsideClip(bsp, clip)) {
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

	private void faceCollide(Bsp bsp, ArcFace face) {
		Plane plane = bsp.planes[face.planeId];
		
		if (plane.normal.y > WALL_FUDGE_FACTOR) {
			if (plane.classify(position, obb.bounds.y) == Plane.COPLANAR && collideWithFace(bsp, face, EPSILON)) {
				collideWithFloor(plane);
			}
		} else if (plane.normal.y <= WALL_FUDGE_FACTOR && plane.normal.y >= -WALL_FUDGE_FACTOR) {
			if (plane.classify(position, obb.bounds.x) == Plane.COPLANAR
				 && collideWithFace(bsp, face, EPSILON)) {
				collideWithWall(plane);
			}
		} else {
			if (plane.classify(position, obb.bounds.y) == Plane.COPLANAR && collideWithFace(bsp, face, EPSILON)) {
				collideWithCeiling(plane);
			}
		}
	}

	private boolean collideWithFace(Bsp bsp, ArcFace face, float edgeThickness) {
		Plane plane = bsp.planes[face.planeId];
		
		for(int i = face.firstEdge; i < face.firstEdge+face.numEdges; i++) {
			int surf = bsp.surfEdges[i];
			int ind = Math.abs(surf);
			Vector3f p1 = bsp.vertices[bsp.edges[ind].start];
			Vector3f p2 = bsp.vertices[bsp.edges[ind].end];
			
			Vector3f vec = (surf<0)?Vector3f.sub(p2, p1):Vector3f.sub(p1, p2).normalize();
			Vector3f edgeNormal = vec.cross(plane.normal);
			float edgeDist = edgeNormal.dot(p1);
			
			if (Plane.classify(position, edgeNormal, edgeDist, edgeThickness) == Plane.BEHIND) {
				return false;
			}
		}
		
		return true;
	}

	private boolean isInsideClip(Bsp bsp, ArcClip clip) {
		int lastEdge = clip.firstEdge + clip.numEdges;
		for (int i = clip.firstEdge; i < lastEdge; i++) {
			if (bsp.clipEdges[i].texId == -1) continue;
			if (bsp.planes[bsp.clipEdges[i].planeId].classify(position, 1f) == Plane.IN_FRONT) {
				return false;
			}
		}

		return true;
	}
	
	private void clipCollision(Bsp bsp, ArcClip clip) {
		if (!isInsideClip(bsp,clip)) return;
		
		int lastEdge = clip.firstEdge + clip.numEdges;
		for (int i = clip.firstEdge; i < lastEdge; i++) {
			Plane plane = bsp.planes[bsp.clipEdges[i].planeId];
			//
			if (bsp.planes[bsp.clipEdges[i].planeId].classify(position, .001f) == Plane.BEHIND) {
				if (plane.normal.y > WALL_FUDGE_FACTOR) {
					if (plane.classify(position, obb.bounds.y) == Plane.COPLANAR) {
						collideWithFloor(plane);
					}
				} else if (plane.normal.y <= WALL_FUDGE_FACTOR && plane.normal.y >= -WALL_FUDGE_FACTOR) {
					if (plane.classify(position, obb.bounds.x) == Plane.COPLANAR) {
						collideWithWall(plane);
					}
				} else {
					if (plane.classify(position, obb.bounds.y) == Plane.COPLANAR) {
						collideWithCeiling(plane);
					}
				}
			}
		}
	}
	
	private void collideWithStaticObject(CollisionShape collision, Matrix4f transform) {
		Polygon[] polygons = collision.getPolygons();
		BoundingBox broadphase = collision.getBroadphase();

		//if (!broadphase.axisAlignedIntersection(obb)) {
		//	return;
		//}
		
		if (collision.getType() == CollisionType.USE_BOUNDING_BOX) {
			collideWithOBB(broadphase);
			return;
		}
		
		Polygon[] walls = new Polygon[3];
		Polygon[] floors = new Polygon[2];
		Polygon ceil = null;
		int wi = 0, fi = 0;

		for (Polygon untransformedPolygon : polygons) {

			Polygon polygon = new Polygon(new Vector3f(untransformedPolygon.p1),
					new Vector3f(untransformedPolygon.p2), new Vector3f(untransformedPolygon.p3));
			polygon.transform(transform);
			
			

			if (polygon.normal.y > WALL_FUDGE_FACTOR) {
				if (fi < 2 && obb.intersects(polygon) && velocity.y <= 0f) {
					floors[fi++] = polygon;
				}
			}
			else if (wi < 3 && polygon.normal.y <= WALL_FUDGE_FACTOR && polygon.normal.y >= -WALL_FUDGE_FACTOR) {
				if (obb.intersects(polygon)) {
					walls[wi++] = polygon;
					
				}
			} else {
				if (obb.intersects(polygon)) {
					ceil = polygon;
				}
			}
		}
		
		if (fi != 0)
			collideWithFloor(floors[0].getPlane());
		
		for(int i = 1; i < fi; i++) {
			if (obb.intersects(floors[i]) && velocity.isFacing(floors[i].normal))
				collideWithFloor(floors[i].getPlane());
		}
		
		for(int i = 0; i < wi; i++) {
			if (obb.intersects(walls[i]) && velocity.isFacing(walls[i].normal))
				collideWithWall(walls[i].getPlane());
		}
		
		if (ceil != null) {
			if (obb.intersects(ceil) && velocity.isFacing(ceil.normal))
				collideWithCeiling(ceil.getPlane());
		}
	}

	private void collideWithOBB(BoundingBox otherObb) {
		Vector3f vec = obb.intersects(otherObb);
		if (vec != null) {
			this.position.add(vec);
			if (vec.y < 0f) {
				grounded = true;
				position.y -= velocity.y * Window.deltaTime;
				velocity.y = 0;
			} else {
				this.position.sub(vec);
			}
		}
	}
	
	private void collideWithCeiling(Plane plane) {
		Vector3f projectedPoint = Plane.projectPoint(position, plane.normal, -plane.dist);
		
		projectedPoint.add(Vector3f.mul(plane.normal, obb.bounds.y));
		position.set(projectedPoint.x, projectedPoint.y, projectedPoint.z);
		velocity.set(Vector3f.sub(
				Plane.projectPoint(Vector3f.add(position, velocity), plane.normal, -plane.dist), position));
		position.y -= velocity.y * Window.deltaTime;
		if (velocity.y > 0) {
			velocity.y = 0;
		}
	}

	private void collideWithWall(Plane plane) {
		Vector3f projectedPoint = Plane.projectPoint(position, plane.normal, -plane.dist-.015f);
		
		projectedPoint.add(Vector3f.mul(plane.normal, obb.bounds.x));
		projectedPoint.y = position.y;
		position.set(projectedPoint.x, position.y, projectedPoint.z);
		velocity.set(Vector3f.sub(
				Plane.projectPoint(Vector3f.add(position, velocity), plane.normal, -plane.dist), position));
	}

	private void collideWithFloor(Plane plane) {
		if (position.isFacing(plane.normal)) return;
		Vector3f projectedPoint = Plane.projectPoint(position, plane.normal, -plane.dist);
		
		float outVec = Vector3f.mul(obb.bounds, plane.normal).length();
		projectedPoint.add(Vector3f.mul(plane.normal, outVec-EPSILON));
		position.set(projectedPoint.x, projectedPoint.y, projectedPoint.z);
		
		if (plane.normal.y < SLIDE_ANGLE) {
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
		} else {
			grounded = true;
			velocity.y = 0;
		}
		
		obb.update(position, rotation, scale);
	}

	private void collideWithFloor(float height, float slopeX, float slopeZ) {
		this.slopeX = slopeX;
		this.slopeZ = slopeZ;

		float sxAbs = Math.abs(slopeX);
		float szAbs = Math.abs(slopeZ);

		if ((position.y - scale) < height + stick) {
			position.y = height + scale;
			if (sxAbs < SLIDE_ANGLE && szAbs < SLIDE_ANGLE) {
				velocity.y = 0f;
			}
			grounded = true;

			// Slide
			if (sxAbs > SLIDE_ANGLE) {
				if (!submerged) {
					if (Math.signum(slopeX) == Math.signum(velocity.x)) {
						velocity.x -= .45f * Math.signum(slopeX);
					} else {
						float slopeDir = Math.signum(slopeX);
						velocity.x += (-((friction * 2f * 1) * Window.deltaTime) * slopeDir);
					}
				}
				sliding = true;
			}

			if (szAbs > SLIDE_ANGLE) {
				if (!submerged) {
					if (Math.signum(slopeZ) == Math.signum(velocity.z)) {
						velocity.z -= .45f * Math.signum(slopeZ);
					} else {
						float slopeDir = Math.signum(slopeZ);
						velocity.z += (-((friction * 2f * 1) * Window.deltaTime) * slopeDir);
					}
				}
				sliding = true;
			}

			if (sxAbs <= SLIDE_ANGLE && szAbs <= SLIDE_ANGLE) {
				sliding = false;
			}
		}
	}

	public void destroy() {
		EntityRenderer.removeEntity(this);
		Application.scene.removeEntity(this);
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
			} else if (sliding) {
				float jumpSpeed = Math.max(velocity.length() / 6f, height / 2f);
				velocity.x = (-slopeX) * jumpSpeed;
				velocity.z = (-slopeZ) * jumpSpeed;
				velocity.y = height;

			} else {
				velocity.y = height;
				position.y += 0.5f;
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
		upwarp(Application.scene.getWorld());
		updateMatrix();
	}

	public void upwarp(World world) {
		float minRaise = Float.POSITIVE_INFINITY;
		if (world.hasHeightmap() && world.getTerrain().isPopulated) {
			Vector3f data = world.getTerrain().getPropertiesAt(position.x, position.z);
			if (data != null) {
				float y = data.x;
				
				if (y < minRaise)
					minRaise = y;
			} else {
				// Hacky solution
				minRaise = -9999;
			}
		}
		
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
