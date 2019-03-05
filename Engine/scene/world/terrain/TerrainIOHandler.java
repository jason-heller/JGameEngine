package scene.world.terrain;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import logic.collision.CollisionShape;
import opengl.Application;
import pipeline.Model;
import pipeline.Texture;
import pipeline.util.ModelUtils;
import pipeline.util.TextureUtils;
import scene.object.ObjectControl;
import scene.object.StaticEntity;

public class TerrainIOHandler {
	
	private Map<Integer, TempModel> activeObjects = new HashMap<Integer, TempModel>();
	private Map<Integer, TempTexture> activeTextures = new HashMap<Integer, TempTexture>();
	private String modelFileLocations = "", textureFileLocations = "";
	
	private List<TempObject> objectQueue = new ArrayList<TempObject>();

	public void init(String directory) {
		modelFileLocations = directory + "\\mdl\\";
		textureFileLocations = (directory + "tex/").replace("src/", "");
	}
	
	// Every time the chunks shift, this should be called
	// x = map xpos, z = map zpos
	public void update(int x, int z) {
		Iterator<Integer> it = activeObjects.keySet().iterator();
	    while (it.hasNext()) {
	        TempModel m = activeObjects.get(it.next());
	        
	        int stride = Application.scene.getTerrain().getStride();
	        if (Math.abs(m.x - x) >= stride || Math.abs(m.z - z) >= stride) {
	        	it.remove();
	        	
	        	
	        }
	    }
	}

	public void requestObject(int objId, int texId, int x, int z, StaticEntity e) {
		objectQueue.add(new TempObject(objId, texId, x, z, e));
	}
	
	public void handleObjects() {
		for(int i = 0; i < objectQueue.size(); i++) {
			TempObject obj = objectQueue.get(i);
			if (obj == null) continue;
			if (!activeObjects.containsKey(obj.model)) {
				TempModel model = new TempModel(ModelUtils.loadStaticModel(new File(modelFileLocations+obj.model+".cm"), true, true),
						obj.x, obj.z);
				activeObjects.put(obj.model, model);
			}
			
			if (!activeTextures.containsKey(obj.tex)) {
				TempTexture tex = new TempTexture(TextureUtils.createTexture(textureFileLocations+obj.tex+".png"),
						obj.x, obj.z);
				activeTextures.put(obj.tex, tex);
			}
			
			obj.entity.setModel(activeObjects.get(obj.model).model);
			obj.entity.setTexture(activeTextures.get(obj.tex).texture);
			obj.entity.setLoaded(true);
			ObjectControl.addObject(obj.entity);
		}
		
		objectQueue.clear();
	}
	
	/*public void removeObject(int id) {
		activeObjects.get(id).cleanUp();
		activeObjects.remove(id);
	}*/
	
	public void cleanUp() {
		for(TempModel m : activeObjects.values()) {
			m.cleanUp();
		}
		activeObjects.clear();
		
		for(TempTexture m : activeTextures.values()) {
			m.cleanUp();
		}
		activeTextures.clear();
	}

	public Model getModel(int id) {
		return activeObjects.get(id).model;
	}

	public Texture getTexture(int id) {
		return activeTextures.get(id).texture;
	}

	
}

class TempModel {
	public Model model;
	public int x, z;
	
	public TempModel(Model model, int x, int z) {
		this.model = model;
		this.x = x;
		this.z = z;
	}
	
	public void cleanUp() {
		model.cleanUp();
	}
}

class TempTexture {
	public Texture texture;
	public int x, z;
	
	public TempTexture(Texture texture, int x, int z) {
		this.texture = texture;
		this.x = x;
		this.z = z;
	}
	
	public void cleanUp() {
		texture.delete();
	}
}

class TempObject {
	public StaticEntity entity;
	int model;
	int tex;
	public int x, z;
	
	public TempObject(int model, int  tex, int x, int z, StaticEntity entity) {
		this.model = model;
		this.tex = tex;
		this.entity = entity;
		this.x = x;
		this.z = z;
	}
}
