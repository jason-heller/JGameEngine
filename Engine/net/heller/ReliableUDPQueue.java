package net.heller;

import java.util.LinkedList;
import java.util.Queue;

import com.esotericsoftware.kryonet.Client;

import debug.Debug;
import debug.console.Console;
import net.ClientControl;
import net.packets.Network.ReliablePacket;

public class ReliableUDPQueue {
	public long udpTime = Long.MIN_VALUE;
	
	private Queue<ReliablePacket> cmdList = new LinkedList<ReliablePacket>();
	private Client client;
	
	public ReliableUDPQueue(Client client) {
		this.client = client;
	}
	
	public void add(ReliablePacket packet) {
		cmdList.add(packet);
		
		if (Debug.logPackets) Console.log("Sent: "+packet.toString());
	}
	
	public void tick() {
		if (!cmdList.isEmpty()) {
			ReliablePacket packet = cmdList.peek();
			packet.time = udpTime;
			client.sendUDP(packet);
			ClientControl.rudpPacketsPerTick++;
		}
	}
	
	public void ackRecieved() {
		if (!cmdList.isEmpty()) {
			Object o = cmdList.remove();

			udpTime++;

			if (Debug.logPackets) Console.log("Acked: "+o.toString());
		}
	}
}
