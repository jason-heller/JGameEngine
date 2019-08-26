package net;

import java.util.List;

import org.joml.Vector3f;

import logic.controller.NetSceneData;
import logic.controller.PlayerController;
import net.packets.CmdPacketControl;
import net.packets.GameStateMessage;
import net.packets.Network.ShootPacket;
import net.packets.Network.StringCmdPacket;

public class NetUtils {

	private static Vector3f actualPosition = null;
	
	public static void checkPredictions(GameStateMessage msg, int ping) {
		if (PlayerController.getPlayer() == null) return;
		
		HistorySample history = PlayerController.getPlayer().getHistory().getSample(ping);
		List<Float> pos = msg.getPosition();
		if (history == null) {
			
			PlayerController.getPlayer().position.set(pos.get(0), pos.get(1), pos.get(2));
			//ClientControl.debugEnt.position.set(PlayerController.getPlayer().position);
			return;
		}
		Vector3f prevPosition = history.position;
		actualPosition = new Vector3f(pos.get(0), pos.get(1), pos.get(2));
		//ClientControl.debugEnt.position.set(prevPosition);
		
		if (!(prevPosition.x == actualPosition.x && prevPosition.y == actualPosition.y && prevPosition.z == actualPosition.z)) {
			//PlayerController.error = Vector3f.sub(actualPosition, PlayerController.getPlayer().position);
			PlayerController.getPlayer().position.set(actualPosition);
		} else {
			actualPosition = null;
		}
	}
	
	public static void updatePositionAdjustment() {
		if (actualPosition == null) return;
		PlayerController.getPlayer().position.set(actualPosition);
	}

	public static void chooseTeam(int team) {
		NetSceneData.team = team;
		if (NetSceneData.hero != -1) {
			StringCmdPacket packet = new StringCmdPacket();
			packet.command = CmdPacketControl.buildString("TEAMHERO", (byte) ((NetSceneData.hero << 1) + (NetSceneData.team)));
			ClientControl.addToRUDPQueue(packet);
		}
	}

	public static void chooseClass(int hero) {
		NetSceneData.hero = hero;
		if (NetSceneData.hero != -1) {
			StringCmdPacket packet = new StringCmdPacket();
			packet.command = CmdPacketControl.buildString("TEAMHERO", (byte) ((NetSceneData.hero << 1) + (NetSceneData.team)));
			ClientControl.addToRUDPQueue(packet);
		}
		
	}

	public static void onFirePacket(byte flags) {
		ShootPacket packet = new ShootPacket();
		packet.flags = flags;
		ClientControl.addToRUDPQueue(packet);
	}
}
