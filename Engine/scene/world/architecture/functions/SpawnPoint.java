package scene.world.architecture.functions;

import org.joml.Vector3f;

import scene.object.StaticEntity;

public class SpawnPoint extends StaticEntity {
	private String name = "";
	
	public SpawnPoint(Vector3f position, Vector3f rotation, String name) {
		this.position = position;
		this.rotation = rotation;
		this.name = name;
	}

	public String getName() {
		return name;
	}
}