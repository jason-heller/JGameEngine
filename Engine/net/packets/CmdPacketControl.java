package net.packets;

import debug.console.Console;
import net.ClientControl;
import net.entity.PlayerClient;
import net.packets.Network.StringCmdPacket;
import scene.Scene;

public class CmdPacketControl {

	private ClientControl client;
	private boolean ackFlag = false;

	public CmdPacketControl(ClientControl client) {
		this.client = client;
	}
	
	public void received(Scene scene, StringCmdPacket object) {
		Console.log(object.command);
		String[] in = object.command.split("/");
		String cmd = sanitizeCmd(in[0]);
		String[] args = sanitizeArgs(in);
		ackFlag = true;
		
		
		if (cmd.equals("CONNECT")) {
			byte id = toByte(args[0]);
			String name = args[1];
			
			PlayerClient pc = client.addPlayer(id, name);
			
			if (args.length > 2 && args[2].equals("self")) {
				client.setID(id);
				pc.getEntity().visible = false;
			}
			
			Console.log(name + " connected");
		}
		
		
		else if (cmd.equals("DISCONNECT")) {
			byte id = toByte(args[0]);
			
			client.removePlayer(id);
		}
		
		else if (cmd.equals("XPLODE")) {
			byte id = toByte(args[0]);
			int dmg = toInt(args[1]);
			float radius = toFloat(args[2]);
			float x = toFloat(args[3]);
			float y = toFloat(args[4]);
			float z = toFloat(args[5]);
			
			scene.getWorld().createExplosion(id, dmg, radius, x, y, z);
		}
		
		else if (cmd.equals("KILLEVENT")) {
			byte killer = toByte(args[0]);
			byte victim = toByte(args[1]);
			
			/*if (packet.victim == connectionId) {
				NetSceneData.deaths++;
			} else if (packet.killer == connectionId) {
				NetSceneData.kills++;
			} else {
				// some shit
			}

			if (victim != null) {
				victim.lastUdpTime = Integer.MIN_VALUE;
				victim.addDeath();
			}

			if (killer != null) {
				killer.addKill();
			}*/
		}
		
		
		else {
			Console.send(object.command);
		}
	}
	
	public static String buildString(Object ... objects) {
		String s = "";
		for(int i = 0; i < objects.length; i++) {
			s += objects[i].toString() + ((i==objects.length-1)?"":"/");
		}
		return s;
	}

	private String[] sanitizeArgs(String[] in) {
		String[] args = new String[in.length-1];
		for(int i = 0, n = args.length; i < n; i++) {
			args[i] = in[i+1];
		}
		
		return args;
	}

	private String sanitizeCmd(String in) {
		return in.toUpperCase();
	}
	
	private byte toByte(String s) {
		return Byte.parseByte(s);
	}
	
	private int toInt(String s) {
		return Integer.parseInt(s);
	}
	
	private float toFloat(String s) {
		return Float.parseFloat(s);
	}

	public boolean sendAckFlag() {
		if (ackFlag) {
			ackFlag = false;
			return true;
		}
		else {
			ackFlag = false;
			return false;
		}
	}
}
