package particles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import global.Globals;
import pipeline.Resources;
import pipeline.Texture;
import scene.Camera;

public class ParticleHandler {
	private static Map<Texture, List<Particle>> particles = new LinkedHashMap<Texture, List<Particle>>();
	private static ParticleRenderer renderer;
	private static int particleCount = 0;
	
	public static void init(Camera camera) {
		renderer = new ParticleRenderer(camera);
		Resources.addTexture("particles", "particles/particles.png", GL11.GL_TEXTURE_2D, true, 32);
		Resources.addTexture("smoke", "particles/smoke.png", GL11.GL_TEXTURE_2D, true, 8);
	}
	
	public static void update(Camera camera) {
		Iterator<Entry<Texture, List<Particle>>> mapIterator = particles.entrySet().iterator();
		while(mapIterator.hasNext()) {
			Entry<Texture, List<Particle>> entry = mapIterator.next();
			List<Particle> list = entry.getValue();
			
			Iterator<Particle> iter = list.iterator();
			while(iter.hasNext()) {
				Particle p = iter.next();
				if (p.isAlive()) {
					boolean alive = p.update(camera);
					if (!alive) {
						iter.remove();
						particleCount--;
					}
				}
			}
			
			//if (!entry.getKey().isAdditive()) {
				sortParticles(list);
			//}
		}
	}
	
	public static void render(Camera camera) {
		renderer.render(particles, camera);
	}
	
	private static void sortParticles(List<Particle> list) {
        for (int i = 1; i < list.size(); i++) {
            Particle item = list.get(i);
            if (item.getDistance() > list.get(i - 1).getDistance()) {
                sortUpHighToLow(list, i);
            }
        }
    }
 
    private static void sortUpHighToLow(List<Particle> list, int i) {
        Particle item = list.get(i);
        int attemptPos = i - 1;
        while (attemptPos != 0 && list.get(attemptPos - 1).getDistance() < item.getDistance()) {
            attemptPos--;
        }
        list.remove(i);
        list.add(attemptPos, item);
    }
	
	public static void add(Particle p) {
		if (particleCount > Globals.maxParticles+1) {
			//particles.remove(entry.getKey(),entry.getValue());
			//particleCount--;
			return;
		}
		List<Particle> list = particles.get(p.getTexture());

		if (list == null) {
			list = new ArrayList<Particle>();	
			particles.put(p.getTexture(), list);
			//list.add(p);
		}
		/*else if (numAlive < list.size()) {
			for(Particle particle : list) {
				if (!particle.isAlive()) {
					particle.setActive(p.getPosition(), p.getVelocity(), p.getGravity(), p.getLife(), p.getRotation(), p.getScale());
				}
			}
		}
		else {*/
			list.add(p);
			particleCount++;
		//}
	}
	
	public static void cleanUp() {
		renderer.cleanup();
	}
}
