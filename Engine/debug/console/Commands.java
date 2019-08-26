package debug.console;

import java.util.ArrayList;

import debug.Debug;
import global.Globals;
import logic.controller.PlayerController;
import net.ClientControl;
import opengl.GlobalRenderer;
import opengl.Window;
import weapons.Weapons;

@SuppressWarnings("unused") class Commands {
	protected static ArrayList<Command> vars = new ArrayList<Command>();
	
	private static final Command version            				= new Command( "version", "VERSION", Globals.class, CommandType.GETTER, false);
	private static final Command quit            					= new Command( "quit", CommandType.ACTION);
	private static final Command exit            					= new Command( "exit", CommandType.ACTION);
	private static final Command debug            					= new Command( "debug",   "debugMode", Globals.class, CommandType.SETTER, false);
	private static final Command noclip            					= new Command( "noclip",   "toggleNoclip", Command.class, CommandType.METHOD, true);
	private static final Command timescale				          	= new Command( "e_timescale", "timeScale", Window.class, CommandType.SETTER, true).clampInput(1f, 0.1f, 100f);
	private static final Command gravity				          	= new Command( "e_gravity", "gravity", Globals.class, CommandType.SETTER, true);
	private static final Command gfx_water_quality				    = new Command( "gfx_water_quality", "waterQuality", GlobalRenderer.class, CommandType.SETTER, false);
	private static final Command show_chunk_borders		          	= new Command( "show_chunk_borders", "chunkBorders", Debug.class, CommandType.SETTER, true);
	private static final Command fullbright				          	= new Command( "fullbright", "fullbright", Debug.class, CommandType.SETTER, true);
	private static final Command volume					          	= new Command( "volume", "setVolume", Command.class, CommandType.METHOD, false);
	private static final Command warp					          	= new Command( "warp", "warpPlayer", Command.class, CommandType.METHOD, true);
	private static final Command map					        	= new Command( "map", "loadMap", Command.class, CommandType.METHOD, true);
	private static final Command d_ignorebsp			        	= new Command( "map_ignore_bsp", "ignoreBsp", Debug.class, CommandType.SETTER, true);
	private static final Command reset_player			        	= new Command( "reset_player", "resetPlayer", Debug.class, CommandType.METHOD, false);
	private static final Command wireframe							= new Command( "map_show_wireframe", "wireframe", Debug.class, CommandType.SETTER, true);
	private static final Command shadowmap							= new Command( "map_show_shadowmap", "showShadowMap", Debug.class, CommandType.SETTER, true);
	private static final Command spawnplayer						= new Command( "respawn", "spawn", PlayerController.class, CommandType.METHOD, true);
	private static final Command p_friction							= new Command( "p_friction", "friction", PlayerController.class, CommandType.SETTER, true);
	private static final Command p_friction_air						= new Command( "p_friction_air", "airFriction", PlayerController.class, CommandType.SETTER, true);
	private static final Command p_jump_vel							= new Command( "p_jump_velocity", "jumpVelocity", PlayerController.class, CommandType.SETTER, true);
	private static final Command p_max_speed						= new Command( "p_maxspeed", "maxSpeed", PlayerController.class, CommandType.SETTER, true);
	private static final Command p_ground_accel						= new Command( "p_accel_ground", "accelSpeed", PlayerController.class, CommandType.SETTER, true);
	private static final Command p_water_accel						= new Command( "p_accel_water", "waterAccel", PlayerController.class, CommandType.SETTER, true);
	private static final Command fov								= new Command( "fov", "setFov", Command.class, CommandType.METHOD, false);
	private static final Command max_fps							= new Command( "max_fps", "maxFramerate", Globals.class, CommandType.SETTER, false).clampInput(60, 24, 999);
	private static final Command wpn_edit							= new Command( "wep_edit", "weaponEdit", Debug.class, CommandType.SETTER, true);
	private static final Command give								= new Command( "give", "give", Weapons.class, CommandType.METHOD, true);
	private static final Command net_interp							= new Command( "net_interp", "entityInterpInterval", ClientControl.class, CommandType.SETTER, false).clampInput(0.1f, 0.025f, 0.5f);
	private static final Command net_tickrate						= new Command( "net_tickrate", "tickrate", ClientControl.class, CommandType.SETTER, false).clampInput(20, 100, 1).invertInput();
	private static final Command fbo_sample_rate					= new Command( "render_samplerate", "changeSampleRate", GlobalRenderer.class, CommandType.METHOD, false);
	private static final Command fbo_post_gloweffect				= new Command( "render_enable_glow", "enableGlow", Globals.class, CommandType.SETTER, false);
	private static final Command net_packet_log						= new Command( "net_log_packets", "logPackets", Debug.class, CommandType.SETTER, false);
	private static final Command run								= new Command( "run", "execFile", Command.class, CommandType.METHOD, false);
	private static final Command net_graph							= new Command( "net_graph", "netGraph", Debug.class, CommandType.SETTER, false);

	// e_ = engine
	
	public static Command getCommand( String name ) {
		for (int i = 0; i < vars.size(); i++) {
			Command c = vars.get(i);
			if (c.getName().equals(name)) {
				return c;
			}
		}

		return null;
	}
}
