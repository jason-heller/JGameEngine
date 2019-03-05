package scene.world.terrain;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import debug.console.Console;
import opengl.Application;
import scene.world.World;
import scene.world.terrain.biome.Biome;
import scene.world.terrain.biome.BiomeMap;
import scene.world.terrain.region.Region;
import scenes.mainmenu.MainMenuScene;
import utils.FileUtils;

public class EnvFileReader {
	public static List<Biome> biomes;
	public static List<Region> regions;

	public static void read(World world, String file) {
		DataInputStream is = null;

		final float scale = 1f;

		biomes = new ArrayList<Biome>();
		regions = new ArrayList<Region>();
		
		try {
			is = new DataInputStream(FileUtils.getInputStream(file));
			
			if (is == null) {
				Console.log("Error: No ENV file");
				Application.changeScene(MainMenuScene.class);
				return;
			}

			boolean[] flags = FileUtils.getFlags(is.readByte());
			world.setFlags(flags);
			
			// Biomes
			short numBiomes = is.readShort();
			for (int i = 0; i < numBiomes; i++) {
				float x = is.readShort() * scale;
				float z = is.readShort() * scale;
				byte type = is.readByte();
				Biome biome = new Biome(x, z, type);
				biomes.add(biome);
				
				//if (type == 6 || type == 7) {
				byte numBorders = is.readByte();
				float[][] borderData = new float[numBorders][3];
				for(int j = 0; j < numBorders; j++) {
					//borderData[j][2] = is.readShort();
					borderData[j][0] = is.readFloat();
					borderData[j][1] = is.readFloat();
					borderData[j][2] = (is.readFloat() * scale) + BiomeMap.TRANSITION_RANGE;
				}
				
				biome.setBorderData(borderData);
				//}
			}
			
			// Regions
			short numRegions = is.readShort();
			for (int i = 0; i < numRegions; i++) {
				float x = is.readShort() * scale;
				float z = is.readShort() * scale;
				float rad = is.readShort() * scale;
				byte type = is.readByte();
				regions.add(new Region(x, z, rad, type));
			}

			// Architectures
			world.setArcOffset(0, 0, 0); //is.readFloat(), is.readFloat(), is.readFloat()
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
