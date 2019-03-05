package proceduralgen;


import org.joml.Vector3f;

import debug.tracers.LineRenderer;
import opengl.Application;
import scene.world.terrain.Chunk;
import scene.world.terrain.Terrain;
import scene.world.terrain.biome.Biome;
import scene.world.terrain.biome.BiomeMap;
import scene.world.terrain.biome.BiomeType;
import utils.MathUtils;

public class GenTerrain {
	private static final float ISLAND_CHUNK_RADIUS = 24;	// Num chunks from origin to reach edge of island
	private static final int OCEAN_DEPTH = 64;				// Base y level of ocean
	private static final float GROUND_LEVEL = 0f;			// Min ground height on land
	private static final float OCEAN_TRANSITION_LENGTH = 2;	// How many chunks it takes for the ground to turn to ocean fully
	private static final float HILL_MAX_HEIGHT = 32;
	
	private static SimplexNoise hillNoise = new SimplexNoise(14414);
	
	public static void genTerrain(Chunk chunk, Biome[] biomes, float[][] heightmap, int x, int z) {
		int relX = (x*(heightmap.length-1));
		int relZ = (z*(heightmap[0].length-1));
		
		for(int i = 0; i < heightmap.length; i++) {
			for(int j = 0; j < heightmap[0].length; j++) {
				
				heightmap[j][i] = getHeightAt((relX+j), (relZ+i), biomes);
				
				if (heightmap[j][i] < Terrain.waterLevel+4f) chunk.setHasWaterFlag(true);
			}
		}
	}

	public static float getHeightAt(int a, int b, Biome[] biomes) {
		float baseHeight;
		
		float distFromOrig = MathUtils.fastSqrt(a*a + b*b) / Terrain.vertexStripeSize;
		float heightScale = distFromOrig-ISLAND_CHUNK_RADIUS;
		
		if (heightScale <= 0) {
			baseHeight = getGroundHeight(a,b);
		} else {
			 baseHeight = (MathUtils.sCurveLerp(getGroundHeight(a,b), -OCEAN_DEPTH, MathUtils.clamp(heightScale/OCEAN_TRANSITION_LENGTH, 0, 1f)));
		}

		float height = 1f;
		float heightDifference = -999f, transitionRange = -999f;
		Biome heightAffectingBiome = null;
		
		for(Biome biome : biomes) {
			if (biome.getType() == BiomeType.RIVER) {
				heightDifference = -100;
				transitionRange = 400;
				heightAffectingBiome = biome;
				break;
			}
			else if (biome.getType() == BiomeType.MOUNTAIN) {
				heightDifference = 330;
				transitionRange = 650;
				heightAffectingBiome = biome;
				break;
			}
		}
		
		if (heightAffectingBiome != null) {
			for(float[] border : heightAffectingBiome.getBorderData()) {
				
				float pointX = a * Terrain.polySize;
				float pointZ = b * Terrain.polySize;
				float dist = ((pointX*border[0] + pointZ*border[1]) + border[2]) - (transitionRange - BiomeMap.TRANSITION_RANGE);
				
				if (dist <= transitionRange) {
					if (dist > 0f) {
						float ratio = dist / transitionRange;
						height = Math.min(height, ratio);
					} else {
						return baseHeight;
					}
				}
			}
			
			if (heightDifference >= 0) {
				return baseHeight + MathUtils.sCurveLerp
						(0, heightDifference, height);
			} else {
				return baseHeight - MathUtils.sCurveLerp
						(0, -heightDifference, height);
			}
		}
		
		return baseHeight;
	}

	private static float getGroundHeight(int a, int b) {
		return GROUND_LEVEL + (HILL_MAX_HEIGHT + (hillNoise.octavedNoise(a,b, 1, 1.9f, .0075f)*HILL_MAX_HEIGHT));
	}
}
