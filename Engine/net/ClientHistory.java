package net;

import org.joml.Vector3f;

import opengl.Application;
import scene.entity.Entity;

public class ClientHistory {
	
	private Entity player;
	private HistorySample[] samples;
	
	private static final int TICKRATE_MS = (1000/Application.TICKS_PER_SECOND);
	
	public ClientHistory(Entity player) {
		this.player = player;
		samples = new HistorySample[Application.TICKS_PER_SECOND*2];
		
	}
	
	public void update() {
		for(int i = samples.length-1; i > 0; i--) {
			samples[i] = samples[i-1];
		}
		
		samples[0] = new HistorySample(player);
	}
	
	public HistorySample getSample(int latency) {
		int position = Math.round(latency / TICKRATE_MS);
		if (position >= samples.length)
			return null;
		if (position < 0)
			return null;
		return samples[position];
	}
}

class HistorySample {
	public Vector3f position;
	
	public HistorySample(Entity player) {
		this.position = new Vector3f(player.position);
	}
}
