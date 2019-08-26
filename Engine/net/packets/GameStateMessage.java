package net.packets;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class GameStateMessage {
	private byte id;

	private int time;
	private long ack;
	
	private List<Float> position;
	private List<Float> orientation;
	private byte teamAndClass; // CCCCCCCT (T = team (0-1), C = class (0-128)
	private byte weaponSlot; // UUUUUWWW (U = unused, W = weapon slot)
	private List<Short> weapons;
	
	private byte numProjectiles;
	private byte[] projectileIds;
	private float[] projectilePos;
	
	private short ping;

	public GameStateMessage() {
		// default public constructor
	}

	public GameStateMessage(int time, byte id, List<Float> position, List<Float> orientation,
			byte teamAndClass, byte weaponSlotAndAck, List<Short> weapons,
			byte numProjectiles, byte[] projectileIds, float[] projectilePos, short ping, long ack) {
		this.time = time;
		this.ack = ack;
		this.id = id;
		this.position = position;
		this.orientation = orientation;
		this.teamAndClass = teamAndClass;
		this.weaponSlot = weaponSlotAndAck;
		this.weapons = weapons;
		this.numProjectiles = numProjectiles;
		this.projectileIds = projectileIds;
		this.projectilePos = projectilePos;
		this.ping = ping;
		
	}

	public byte getId() {
		return id;
	}
	
	public List<Float> getPosition() {
		return position;
	}

	public List<Float> getOrientation() {
		return orientation;
	}

	public byte getTeamAndClass() {
		return teamAndClass;
	}

	public byte getWeaponSlotAndAck() {
		return weaponSlot;
	}

	public List<Short> getWeapons() {
		return weapons;
	}

	public short getPing() {
		return ping;
	}
	
	public int getTime() {
		return time;
	}

	public static class GameStateSerializer extends Serializer<GameStateMessage> {
		@Override
		public GameStateMessage read(Kryo kryo, Input input, Class<GameStateMessage> cls) {
			int time = input.readInt();
			byte id = input.readByte();
			
			List<Float> position = new ArrayList<Float>(),
					orientation = new ArrayList<Float>();
			List<Short> weapons = new ArrayList<Short>();

			position.add(input.readFloat());
			position.add(input.readFloat());
			position.add(input.readFloat());
			orientation.add(input.readFloat());
			orientation.add(input.readFloat());
			
			byte teamAndClass 		= input.readByte();
			byte weaponSlotAndAck 	= input.readByte();
			
			short ping 	= input.readShort();
			long ack 	= input.readLong();

			byte numProjectiles = input.readByte();
			
			byte[] projectileIds = new byte[numProjectiles];
			float[] projectilePos = new float[numProjectiles*3];
			for (int i = 0; i < numProjectiles; ++i) {
				projectileIds[i] 		= input.readByte();
				projectilePos[i*3] 		= input.readFloat();
				projectilePos[(i*3)+1]	= input.readFloat();
				projectilePos[(i*3)+2] 	= input.readFloat();
			}
			
			int n = input.readByte();
			for (int i = 0; i < n; ++i) {
				weapons.add(input.readShort());
			}
			
			
			
			return new GameStateMessage(time, id, position, orientation, teamAndClass, weaponSlotAndAck,
					weapons, numProjectiles, projectileIds, projectilePos, ping, ack);
		}

		@Override
		public void write(Kryo kryo, Output output, GameStateMessage object) {
			
			output.writeInt(object.time);
			output.writeByte(object.id);
			
			output.writeFloat(object.position.get(0));
			output.writeFloat(object.position.get(1));
			output.writeFloat(object.position.get(2));

			output.writeFloat(object.orientation.get(0));
			output.writeFloat(object.orientation.get(1));
			
			output.writeByte(object.teamAndClass);
			output.writeByte(object.weaponSlot);
			
			output.writeShort(object.ping);
			output.writeLong(object.ack);
			
			int n = object.numProjectiles;
			output.writeByte(n);
			for(int i = 0; i < n; ++i) {
				output.writeByte(object.projectileIds[i]);
				output.writeFloat(object.projectilePos[i*3]);
				output.writeFloat(object.projectilePos[(i*3)+1]);
				output.writeFloat(object.projectilePos[(i*3)+2]);
			}
			
			n = object.weapons.size();
			output.writeByte(n);
			for (int i = 0; i < n; ++i) {
				output.writeShort(object.weapons.get(i));
			}
			
			
		}
	}
	
	@Override
	public String toString() {
		return "Snapshot {"+
				this.getTime()+", "+
				this.getId()+", "+
				listFloat(this.getPosition())+", "+
				listFloat(this.getOrientation())+", "+
				String.format("%8s", Integer.toBinaryString(this.getTeamAndClass() & 0xFF)).replace(' ', '0')+", "+
				String.format("%8s", Integer.toBinaryString(this.getWeaponSlotAndAck() & 0xFF)).replace(' ', '0')+", "+
				listShort(this.getWeapons())+", "+
				this.getPing()+
				"}";
	}

	private String listFloat(List<Float> arr) {
		String s = "[";
		for(float f : arr)
			s += ((int)f+",");
		s += "]";
		return s;
	}
	
	private String listShort(List<Short> arr) {
		String s = "[";
		for(short f : arr)
			s += (f+",");
		s += "]";
		return s;
	}
	
	public long getAckTime() {
		return ack;
	}

	public int getNumProjectiles() {
		return this.numProjectiles;
	}
	
	public byte[] getProjectileIds() {
		return this.projectileIds;
	}
	
	public float[] getProjectilePos() {
		return this.projectilePos;
	}
}
