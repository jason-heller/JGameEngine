package net.entity;

import java.util.ArrayList;
import java.util.List;

import net.packets.Snapshot;
import opengl.Application;
import scene.object.ObjectControl;
import utils.MathUtils;

public class PlayerClient extends NetEntity {

	private String username;
	private int team = -1;
	private int kills, deaths;
	private int ping;
	private int lastUdpTime = 0;
	private byte hero;
	private byte weaponSlot;
	
	private byte[] projectileIds;
	private float[] projectilePos;
	
	private List<NetProjectileEntity> projectiles = new ArrayList<NetProjectileEntity>();
	
	public PlayerClient() {
		super();
		this.username = "Connecting...";
	}

	public void update(Object prevSnapshot, Object nextSnapshot, float timeDiff) {
		
		Snapshot prev = (Snapshot) prevSnapshot;
		Snapshot next = (Snapshot) nextSnapshot;
		if (next == null) return;
		if (prev == null) {
			prev = next;
		}
		entity.position.set(
				MathUtils.lerp(prev.x, next.x, timeDiff), 
				MathUtils.lerp(prev.y, next.y, timeDiff), 
				MathUtils.lerp(prev.z, next.z, timeDiff));
		entity.rotation.y = MathUtils.lerp(prev.yaw, next.yaw, timeDiff);
	}
	
	public void setPing(int ping) {
		this.ping = ping;
	}
	
	public void addDeath() {
		deaths++;
	}
	
	public void addKill() {
		kills++;
	}
	
	public String getName() {
		return username;
	}

	public void setTeam(int team) {
		this.team = team;
	}
	
	public int getTeam() {
		return team;
	}

	public int getKills() {
		return kills;
	}
	
	public int getDeaths() {
		return deaths;
	}
	
	public int getPing() {
		return ping;
	}

	public void setHero(byte hero) {
		this.hero = hero;
	}
	
	public void setKills(short kills) {
		this.kills = kills;
	}
	
	public void setDeaths(short deaths) {
		this.deaths = deaths;
	}

	public void setWeaponSlot(byte weaponSlot) {
		this.weaponSlot = weaponSlot;
	}

	public void setLastGameStateTime(int time) {
		this.lastUdpTime = time;
	}
	
	public long getLastGameStateTime() {
		return this.lastUdpTime;
	}

	public void setName(String name) {
		this.username = name;
	}

	public void setProjectileData(byte[] projIds, float[] projPos) {
		this.projectileIds = projIds;
		this.projectilePos = projPos;
		
		int pos = Math.min(projIds.length, projectiles.size());
		for(int i = 0; i < pos; i++) {
			NetProjectileEntity e = projectiles.get(i);
			e.move(projectileIds[i],
					projectilePos[(i*3)  ],
					projectilePos[(i*3)+1],
					projectilePos[(i*3)+2]);
		}
		
		for(; pos < projIds.length; pos++) {
			NetProjectileEntity npe = new NetProjectileEntity(Application.scene, projectileIds[pos],
					projectilePos[(pos*3)  ],
					projectilePos[(pos*3)+1],
					projectilePos[(pos*3)+2]);
			projectiles.add(npe);
			Application.scene.addObject(npe);
			ObjectControl.addObject(npe);
			
		}
		
		for(pos = projIds.length; pos < projectiles.size(); pos++) {
			NetProjectileEntity npe = projectiles.remove(pos);
			Application.scene.removeObject(npe);
			npe.destroy();
		}
	}
}
