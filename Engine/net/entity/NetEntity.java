package net.entity;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.ClientControl;
import opengl.Application;
import scene.Scene;
import scene.entity.Entity;
import utils.MathUtils;

public abstract class NetEntity {
	private static final long SNAPSHOT_LIFE = 1000;		// How long to hold onto a snapshot in milliseconds
	protected Entity entity;
	protected Scene scene;
	
	private long prevSnapTime = Long.MIN_VALUE;
	private long nextSnapTime = Long.MAX_VALUE;
	private long clientRenderingTime = 0;
	
	protected Map<Long, Object> snapshots = new ConcurrentHashMap<Long, Object>();
	
	public NetEntity() {
		scene = Application.scene;
		
		entity = new NetPlayerEntity(scene);
		scene.addEntity(entity);
	}
	
	public void updateSnapTimes() {
		long msInterp = (long)(ClientControl.entityInterpInterval*1000);
		clientRenderingTime = System.currentTimeMillis() - msInterp;
		prevSnapTime = Long.MIN_VALUE;
		nextSnapTime = Long.MAX_VALUE;
		
		Iterator<Entry<Long, Object>> it = snapshots.entrySet().iterator();
	    while (it.hasNext()) {
	        long l = (long)((Map.Entry<Long, Object>) it.next()).getKey();
	        
	        if (l < clientRenderingTime - SNAPSHOT_LIFE) {
				it.remove();
				continue;
			}
	        
	        if (l > prevSnapTime && l < clientRenderingTime)
				prevSnapTime = l;
			
			if (l < nextSnapTime && l > clientRenderingTime)
				nextSnapTime = l;
	    }
	}
	
	public void update() {
		updateSnapTimes();
		
		if (prevSnapTime == Long.MIN_VALUE || nextSnapTime == Long.MAX_VALUE)
			return;
		
		if (nextSnapTime != prevSnapTime) {
			float snapTimeDiff = (clientRenderingTime-prevSnapTime)/(float)(nextSnapTime-prevSnapTime);
			snapTimeDiff = MathUtils.clamp(snapTimeDiff, 0, 1);
			update(snapshots.get(prevSnapTime), snapshots.get(nextSnapTime), snapTimeDiff);
		}
	}
	
	public void addSnapshot(Object object) {
		snapshots.put(System.currentTimeMillis(), object);
	}
	
	public abstract void update(Object prevSnapshot, Object nextSnapshot, float timeDiff);
	
	public void remove() {
		scene.removeEntity(entity);
	}
	
	public Entity getEntity() {
		return entity;
	}
}
