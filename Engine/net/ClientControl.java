package net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Listener.ThreadedListener;

import debug.Debug;
import debug.console.Console;
import global.Controls;
import global.Globals;
import gui.net.Announcements;
import net.entity.NetEntity;
import net.entity.PlayerClient;
import net.heller.ReliableUDPQueue;
import net.packets.CmdPacketControl;
import net.packets.GameStateMessage;
import net.packets.Network;
import net.packets.Network.PlayerStatePacket;
import net.packets.Network.ReliablePacket;
import net.packets.Network.StringCmdPacket;
import net.packets.Snapshot;
import net.quakemonkey.ClientDiffHandler;
import net.quakemonkey.DiffClassRegistration;
import opengl.Application;
import opengl.Window;
import scene.Scene;
import scene.entity.Entity;
import scenes.mainmenu.MainMenuScene;
import utils.Colors;
import utils.Input;

public class ClientControl {
	private static final float TICKS_PER_SECOND = 60;
	public static float tickrate = 1f / TICKS_PER_SECOND;

	public static float entityInterpInterval = .1f;
	static Client client;
	public static boolean connected = false;
	private float packetSendTimer = 0f;
	private boolean markedForDisposal = false;

	//private static long lastUdpInTime = Long.MIN_VALUE;
	private static long lastUdpOutTime = Long.MIN_VALUE;
	
	private static int ping = -1;
	private static long roundTripTimeStart = -1;
	private static int pingRequestTimer = 0;
	
	private float timeSinceLastReceivedPacket = 0;

	private List<NetEntity> netEntities = new ArrayList<NetEntity>();

	Map<Byte, PlayerClient> players = new HashMap<Byte, PlayerClient>();
	private ThreadedListener listener;

	private ClientDiffHandler<GameStateMessage> clientDiffHandler;
	private static ReliableUDPQueue rudpQueue;
	private CmdPacketControl cmdPacketControl;
	
	public static ClientControl netObject;
	
	private byte connectionID = -1;
	private Scene scene;
	
	public static int outgoingPayloadSizePerTick = 0;
	public static int incomingPayloadSizePerTick = 0;
	public static int rudpPacketsPerTick = 0;

	public void connect(Scene scene) {
		this.scene = scene;
		client = new Client();

		Network.register(client);
		Kryo kryo = client.getKryo();
		
		DiffClassRegistration.registerClasses(kryo);
		kryo.register(GameStateMessage.class, new GameStateMessage.GameStateSerializer());
		
		client.start();
		listener = new ThreadedListener(new ServerListener());
		client.addListener(listener);
		
		cmdPacketControl = new CmdPacketControl(this);
		rudpQueue = new ReliableUDPQueue(client);

		try {
			client.connect(5000, Network.serverAddress, Network.port, Network.port);
		} catch (IOException ex) {
			System.out.println("Could not connect to " + Network.serverAddress + ":" + Network.port);
			return;
		}

		clientDiffHandler = new ClientDiffHandler<GameStateMessage>(client, GameStateMessage.class, (short) 16);
		clientDiffHandler.addListener(new BiConsumer<Connection, GameStateMessage>() {
			
			@Override
			public void accept(Connection con, GameStateMessage msg) {
				if (msg == null) return;
				
				byte id = (byte)con.getID();
				timeSinceLastReceivedPacket = 0;
				
				PlayerClient player = (PlayerClient)players.get(id);
				
				if (player == null) {
					return;
				}
				
				
				Entity entity = player.getEntity();
				
				if (msg == null || msg.getTime() < player.getLastGameStateTime()) {
					return;
				}
				
				List<Float> pos = msg.getPosition();
				List<Float> rot = msg.getOrientation();
				entity.position.set(pos.get(0), pos.get(1), pos.get(2));
				entity.rotation.set(rot.get(0), rot.get(1), 0f);
				
				byte teamAndHeroByte = msg.getTeamAndClass();
				byte weaponSlotAndAck = msg.getWeaponSlotAndAck();
				
				player.setLastGameStateTime(msg.getTime());
				player.setTeam((teamAndHeroByte & 1));
				player.setHero((byte) (teamAndHeroByte >>> 1));
				player.setWeaponSlot((byte) (weaponSlotAndAck & 0x07));
				
				boolean isPingPacket = ((weaponSlotAndAck & 0x10) != 0);

				if (isPingPacket) {
					ping = (int) (System.currentTimeMillis() - roundTripTimeStart);
					pingRequestTimer = 0;
				}
				
				byte[] projIds = msg.getProjectileIds();
				float[] projPos = msg.getProjectilePos();
				
				player.setProjectileData(projIds, projPos);
				
				if (id == getID()) {
					player.setPing(ping);
				} else {
					player.setPing(msg.getPing());
				}
				
				long ackTime = msg.getAckTime();
				if (ackTime == rudpQueue.udpTime) {
					rudpQueue.ackRecieved();
				}
				
				// TODO: msg.getWeapons() should do something here
				//msg.getWeapons();
				
				player.addSnapshot(new Snapshot(pos, rot));
				NetUtils.checkPredictions(msg, ping);
			}
		});

		connected = true;
	}

	public void update(Scene scene) {
		packetSendTimer += Window.deltaTime;
		timeSinceLastReceivedPacket += Window.deltaTime;
		if (timeSinceLastReceivedPacket > 1) {
			scene.getGui().drawRect(1280-164, 0, 164, 64, Colors.DK_VIOLET);
			scene.getGui().drawString("Server not responding!\nWill disconnect in "+(5-(int)timeSinceLastReceivedPacket)+"sec", 1280-160, 4, .2f, false);
			if (timeSinceLastReceivedPacket > 5) {
				Globals.announcement = Announcements.TIMEOUT;
				Application.changeScene(MainMenuScene.class);
			}
		}
		
		if (packetSendTimer >= tickrate) {
			tick(scene);
			packetSendTimer -= tickrate;

			/*
			 * for(NetEntity e : netEntities) { e.updateSnapTimes(); }
			 * 
			 * for(NetEntity e : players.values()) { e.updateSnapTimes(); }
			 */
		}

		for (NetEntity e : netEntities) {
			e.update();
		}

		for (NetEntity e : players.values()) {
			e.update();
		}
	}
	
	public void tick(Scene scene) {
		
		if (connected) {
			pingRequestTimer++;
			boolean requestPing = (pingRequestTimer == TICKS_PER_SECOND);
			
			if (requestPing)
				roundTripTimeStart = System.currentTimeMillis();
			
			rudpQueue.tick();
			
			PlayerStatePacket packet = new PlayerStatePacket();
			packet.controlFlags1 = fromBits(Input.isDown(Controls.get("walk foward")),
					Input.isDown(Controls.get("walk left")), Input.isDown(Controls.get("walk backward")),
					Input.isDown(Controls.get("walk right")), Input.isDown(Controls.get("jump")),
					Input.isDown(Controls.get("sneak")), Input.isMouseDown(0), Input.isMouseDown(1));

			packet.controlFlags2 = fromBits(Input.isDown("reload"), false, false, false, false, false, 
					requestPing, cmdPacketControl.sendAckFlag());

			packet.time = lastUdpOutTime;
			packet.yaw = scene.getCamera().getYaw();
			packet.pitch = scene.getCamera().getPitch();
			client.sendUDP(packet);
			lastUdpOutTime++;
			requestPing = false;
			
			if (Debug.netGraph) {
				Debug.packetsIn = incomingPayloadSizePerTick;
				Debug.packetsOut = outgoingPayloadSizePerTick;
				Debug.logPayload(incomingPayloadSizePerTick, rudpPacketsPerTick);
			}
			outgoingPayloadSizePerTick = 0;
			incomingPayloadSizePerTick = 0;
			rudpPacketsPerTick = 0;
		}
	}
	
	private byte fromBits(boolean... bs) {
		String s = "";

		for (boolean b : bs)
			s += b ? "1" : "0";

		return (byte) Integer.parseInt(s, 2);
	}

	public void disconnect() {
		if (!connected)
			return;
	
		markedForDisposal = true;
		client.removeListener(listener);
		client.stop();

		try {
			client.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Client getClient() {
		return client;
	}

	public class ServerListener extends Listener {

		public void connected(Connection connection) {
			StringCmdPacket login = new StringCmdPacket();
			login.command = "JOIN/"+Globals.playerName;
			rudpQueue.add(login);
		}

		public void received(Connection connection, Object object) {
			if (markedForDisposal)
				return;
			
			if (object instanceof StringCmdPacket) {
				cmdPacketControl.received(scene, (StringCmdPacket)object);
			}
		}

		public void disconnected(Connection connection) {
			disconnect();
			markedForDisposal = true;
			return;
		}
	}
	
	public Collection<PlayerClient> getPlayers() {
		return players.values();
	}

	public static void addToRUDPQueue(ReliablePacket packet) {
		rudpQueue.add(packet);
	}

	public PlayerClient addPlayer(byte id, String name) {
		PlayerClient pc = new PlayerClient();
		pc.setName(name);
		players.put(id, pc);
		return pc;
	}
	
	public void removePlayer(byte id) {
		PlayerClient player = players.get(id);
		if (player != null) {
			players.remove(id);
			Console.log(player.getName() + " disconnected");
		}
	}

	public void setID(byte id) {
		this.connectionID = id;
	}
	
	public byte getID() {
		return connectionID;
	}

	public PlayerClient getPlayer(byte id2) {
		return players.get(connectionID);
	}

	public boolean isConnected() {
		return client.isConnected();
	}
}
