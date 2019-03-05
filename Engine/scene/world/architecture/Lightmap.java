package scene.world.architecture;

public class Lightmap {
	private byte[][] samples;
	
	public void setSamples(byte[][] samples) {
		this.samples = samples;
	}
	
	public byte[][] getSamples() {
		return samples;
	}
}
