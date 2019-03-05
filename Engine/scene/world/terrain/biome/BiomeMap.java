package scene.world.terrain.biome;

import java.util.ArrayList;
import java.util.List;

import scene.world.terrain.Terrain;

public class BiomeMap {
	// Biomes are just points on the map, chunk's biome is determined by the closest point
	private List<Biome> biomes;
	
	public static final float TRANSITION_RANGE = (Terrain.chunkSize);
	public static final int TRANSITION_RANGE_SQUARED = (int) (TRANSITION_RANGE*TRANSITION_RANGE);
	
	
	public BiomeMap(List<Biome> biomes) {
		this.biomes = biomes;
	}
	
	// X and Z are /8
	public Biome[] getClosestBiomes(int x, int z) {
		List<Biome> output = new ArrayList<Biome>();
		
		for(Biome biome : biomes) {
			boolean inside = true;
			for(float[] edge : biome.getBorderData()) {
				float dist = signedDistance(x, z, edge[0], edge[1], edge[2]);//((edge[0]*biome.getX()) + (edge[1]*biome.getZ())) - edge[2];
				if ( dist < 0 ) {
					inside = false;
					break;
				}
			}
			
			if (inside) {
				output.add(biome);
			}
		}
		
		Biome[] outArray = new Biome[output.size()];
		outArray = output.toArray(outArray);
		return outArray;
	}
	
	private static float signedDistance(float x, float y, float nx, float ny, float d) {
		return x*nx + y*ny + d;
	}
}
