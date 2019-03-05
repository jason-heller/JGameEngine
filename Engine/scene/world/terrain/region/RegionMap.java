package scene.world.terrain.region;

import java.util.ArrayList;
import java.util.List;

public class RegionMap {
	// Biomes are just points on the map, chunk's biome is determined by the closest point
	private List<Region> regions;
	
	public RegionMap(List<Region> regions) {
		this.regions = regions;
		regions.add(new Region(128,128,512,(byte) 1));
	}
	
	// X and Z are /8
	public Region[] getRegions(int x, int z) {
		List<Region> output = new ArrayList<Region>();
		
		for(Region region : regions) {
			long regionDist = (int)region.distanceSquared(x,z);
			if (regionDist < region.getRadius()) {
				output.add(region);
			}
		}
		
		Region[] outArray = new Region[output.size()];
		outArray = output.toArray(outArray);
		return outArray;
	}
}
