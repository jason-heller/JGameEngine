package debug.console;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;

import audio.AudioHandler;
import global.Globals;
import logic.controller.PlayerController;
import opengl.Application;
import scene.Camera;
import scenes.SceneType;
import utils.MathUtils;

class Command {
	private String var_name;
	private String var_value;

	private Class<?> var_class = null;
	private Object var_object = null;
	
	private float valueDef = Float.NaN;
	private float valueMin;
	private float valueMax;
	private static boolean invertInput = false;

	public boolean requiresCheats = false;
	private CommandType cmdType = CommandType.ACTION;
	
	
	public Command( String name, String value, Object object, CommandType cmdType, boolean requiresCheats) {
		Commands.vars.add(this);

		this.var_name = name;
		this.requiresCheats = requiresCheats;
		this.var_object = object;
		this.cmdType = cmdType;
		this.var_value = value;
	}

	public Command( String name, String value, Class<?> object, CommandType cmdType, boolean requiresCheats) {
		Commands.vars.add(this);
		
		this.var_name = name;
		this.requiresCheats = requiresCheats;
		this.var_class = object;
		this.cmdType = cmdType;
		this.var_value = value;
	}

	public Command( String name, String value ) {
		this( name, value, null, CommandType.ACTION, false );
	}
	
	public Command(String name, CommandType action) {
		this( name, "", null, action, false );
	}
	
	public static String getVariable(Class<?> A, String name) {
		try {
			try {
				return A.getField(name).get(null).toString();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			Console.log("Console error: No such var " + A.toString() + "." + name);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		return "ERR";
	}
	
	public static String setVariable(Class<?> A, String name, String value, float valueDef, float valueMin, float valueMax) {
		try {
			try {
				Field field = A.getField(name);

				if (field.getType().isAssignableFrom(Float.TYPE)) {
					
					if (!Float.isNaN(valueDef) && value.toLowerCase().equals("default") || value.toLowerCase().equals("d")) {
						field.set(null, valueDef);
					} else {
						float v = Float.parseFloat(value);
						if (!Float.isNaN(valueDef)) {
							v = MathUtils.clamp(v, valueMin, valueMax);
						}
						
						field.set(null, invertInput?1f/v:v);
					}
				}
				if (field.getType().isAssignableFrom(Boolean.TYPE)) {
					if (value.equals("")) {
						Boolean v = (Boolean) field.get(new Boolean(false));
						field.set(null, !v.booleanValue());
					} else if (value.equals("1")) {
						field.set(null, invertInput?false:true);
					} else if (value.equals("0")) {
						field.set(null, invertInput?true:false);
					} else {
						boolean b = Boolean.parseBoolean(value);
						field.set(null, invertInput?!b:b);
					}
				}
				if (field.getType().isAssignableFrom(String.class)) {
					field.set(null, value);
				}
				if (field.getType().isAssignableFrom(Integer.TYPE)) {
					if (!Float.isNaN(valueDef) && value.toLowerCase().equals("default") || value.toLowerCase().equals("d")) {
						field.set(null, (int)valueDef);
					} else {
						int v = Integer.parseInt(value);
						if (!Float.isNaN(valueDef)) {
							v = (int)MathUtils.clamp(v, valueMin, valueMax);
						}
						field.set(null, invertInput?1/v:v);
					}
				}
				
				return A.getField(name).get(null).toString();
			} catch (IllegalAccessException e) {
				Console.printStackTrace(e);
			} catch (NumberFormatException e) {
				Console.log("Incorrect parameters for " + A.toString() + "." + name);
			}
		} catch (NumberFormatException e) {
			Console.printStackTrace(e);
		} catch (NoSuchFieldException e) {
			Console.log("No such var " + A.toString() + "." + name);
		} catch (SecurityException e) {
			Console.printStackTrace(e);
		}
		
		return "ERR";
	}
	
	@SuppressWarnings("unused")
	private String invokeMethod() {
		if (var_class == null) {
			return invokeMethod(var_object, getValue());
		}
		else {
			return invokeMethod(var_class, getValue());
		}
	}
	
	private static Method getMethod(Class<?> A, String methodName, Object ... args) throws NoSuchMethodException, SecurityException {
		Method m = null;
		
		Class<?>[] types = new Class[args.length];
		
		for(int i = 0; i < types.length; i++) {
			types[i] = args[i].getClass();
		}
		
		m = A.getMethod(methodName, types);
		
		return m;
	}

	public static String invokeMethod(Class<?> A, String methodName, Object ... args) {
		try {
			Method m = null;
			m = getMethod(A, methodName, args);

			Object s = m.invoke(null, args);
			if (s == null) return null;
			else return s.toString();
		} catch (NoSuchMethodException | SecurityException e) {
			Console.log("Console error: No such method " + A.toString() + "." + methodName);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String invokeMethod(Object o, String methodName, Object ... args) {
		try {
			Method m = getMethod(o.getClass(), methodName, args);
			
			Object s = m.invoke(o, args);
			
			if (s == null) return null;
			else return s.toString();
		} catch (NoSuchMethodException | SecurityException e) {
			Console.log("Console error: No such method " + o.getClass().toString() + "." + methodName);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@SuppressWarnings("all")
	public void execute(String[] args) {
		if (var_class == Console.class) {
			var_object = Application.scene;
		}
		
		if (cmdType == CommandType.GETTER) {
			Console.log(getVariable(var_class, getValue()));
		}
		
		if (cmdType == CommandType.SETTER) {
			if (args.length != 0) {
				setVariable(var_class, getValue(), args[0], valueDef, valueMin, valueMax);
			} else {
				setVariable(var_class, getValue(), "", valueDef, valueMin, valueMax);
			}
		}
		
		if (cmdType == CommandType.METHOD) {
			String output = null;
			
			if (var_class != null && var_class != Console.class) {
				output = invokeMethod(var_class, getValue(), args);
			}
			else {
				output = invokeMethod(var_object, getValue(), args);
			}
			
			if (output != null)
				Console.log(output);
		}
		
		if (var_name.equals("quit") || var_name.equals("exit")) {
			Application.close();
		}
		
	}

	public String getName() {
		return this.var_name;
	}

	public String getValue() {
		return this.var_value;
	}
	
	public static void setVolume(String value) {
		try {
			Globals.volume = Float.parseFloat(value);
		} catch (NumberFormatException e) {
			Console.log("Error: "+value+" cannot be cast to float");
		} catch (NullPointerException e) {
			return;
		}
		AudioHandler.changeMasterVolume();
		
	}
	
	public static void warpPlayer(String x, String y, String z) {
		try {
			PlayerController.getPlayer().position.set(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z));
			Application.scene.getCamera().setPosition(PlayerController.getPlayer().position);
			
			Console.doSceneTick();
		} catch (NumberFormatException e) {
			Console.log("Error: Argument "+e.getLocalizedMessage()+" cannot be cast to float");
		} catch (NullPointerException e) {
			return;
		}
		
	}
	
	public static void warpPlayer(String data) {
		try {
			Class<?> scene = SceneType.get(data);
			if (scene != null) {
				Application.changeScene(scene);
			} else {
				Console.log("Error: No such scene " + data);
			}
		} catch (NumberFormatException e) {
			Console.log("Error: Argument "+e.getLocalizedMessage()+" cannot be cast to float");
		} catch (NullPointerException e) {
			return;
		}
		
	}
	
	public static void loadMap(String data) {
		try {
			Application.loadMap(data);
		} catch (NullPointerException e) {
			return;
		}
	}
	
	public static void log(String data) {
		Console.log(data);
	}
	
	public static void execFile(String data) {
		try {
			File file = new File(data);
			if (!file.exists()) {
				Console.log("Tried to execute \""+data+"\", could not file file.");
			}
			else {
				List<String> lines = Files.readAllLines(file.toPath());
				
				DevScript ds = new DevScript(data, lines);
				ds.execute();
			}
		} catch (NullPointerException e) {
			return;
		} catch (IOException e) {
			Console.printStackTrace(e);
		}
		
	}
	
	public static void toggleNoclip() {
		if (Application.scene.getCamera().getControlStyle() == Camera.FIRST_PERSON) {
			Application.scene.getCamera().setControlStyle(Camera.SPECTATOR);
		} else {
			Application.scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
		}
	}
	
	public static void setFov(String data) {
		try {
			Globals.fov = Integer.parseInt(data);
			Application.scene.getCamera().updateProjection();
		} catch (NumberFormatException e) {
			Console.log("Error: Argument "+e.getLocalizedMessage()+" cannot be cast to int");
		}
	}

	public Command clampInput(float defaultValue, float minValue, float maxValue) {
		valueDef = defaultValue;
		valueMin = minValue;
		valueMax = maxValue;
		return this;
	}

	/**
	 * For Setter Commands:
	 * If the input is boolean, this swaps the flag
	 * If the input is an int/float, this will set the variable to 1/input
	 * @return this object
	 */
	public Command invertInput() {
		invertInput  = true;
		return this;
	}
}

