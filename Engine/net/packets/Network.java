package net.packets;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {
	public static int port = 54555;
	public static String serverAddress = "127.0.0.1";

	// This registers objects that are going to be sent over the network.
	static public void register (EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		kryo.register(PlayerStatePacket.class);
		kryo.register(StringCmdPacket.class);
		
		kryo.register(HitscanPacket.class);
		
		kryo.register(ShootPacket.class);
		
		kryo.register(ReliablePacket.class);
	}
	
	static public class PlayerStatePacket {
		public long time;
		public byte controlFlags1;	// WASDjcPS
		public byte controlFlags2;	// 123456UU
		public float yaw, pitch;
		// WASD = movement keys
		// j = jump
		// c = crouch
		// P = primary fire
		// S = secondary fire
		
		// 123456 = weapon slot held
		// U = unused
	}
	
	// Reliable packets
	static public class StringCmdPacket extends ReliablePacket {
		public String command;
		
		@Override
		public String toString() {
			return "StrCmd Packet: "+command;
		}
	}
	
	static public class HitscanPacket extends ReliablePacket {
		public byte owner;
		public short id;
		public byte dmg;
		public float x,y,z,yaw,pitch;
	}
	
	static public class ShootPacket extends ReliablePacket {
		public byte flags;
		
		@Override
		public String toString() {
			return "Shoot packet "+flags;
		}
	}
	
	static public class ReliablePacket {
		public long time;
	}
}
