package debug.console;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import global.Globals;
import opengl.Window;

public class DevScript {
	public static List<DevScript> activeScripts = Collections.synchronizedList(new ArrayList<DevScript>());
	
	private int start, end, startLine = -1, tickLine = -1;
	private String scriptName;
	private List<String> lines;
	
	Map<String, Float> floats = new HashMap<String, Float>();
	Map<String, Integer> labels = new HashMap<String, Integer>();
	
	NumberFormat floatFormat;
	
	private boolean active = false;
	
	public DevScript(String scriptName, List<String> lines) {
		end = -1;
		this.scriptName = scriptName;
		this.lines = lines;
		
		floatFormat = DecimalFormat.getInstance();
		floatFormat.setMinimumFractionDigits(0);
		floatFormat.setMaximumFractionDigits(2);
		
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			line = line.replaceAll("\t", "");
			if (startLine == -1 && line.toLowerCase().contains("script_start")) {
				startLine = i+1;
			}
			
			if (tickLine == -1 && line.toLowerCase().contains("script_tick")) {
				tickLine = i+1;
			}
			
			if (line.startsWith("@")) {
				labels.put(line.substring(1), i);
				continue;
			}
		}
		
		if (tickLine == -1)
			tickLine = 0;
	}
	
	public static void tick() {
		Iterator<DevScript> iter = activeScripts.iterator();
		while(iter.hasNext()) {
			DevScript script = iter.next();
			boolean complete = script.runTick();
			if (complete) {
				iter.remove();
			}
		}
	}
	
	public boolean runTick() {
		return execute(tickLine);
	}
	
	public boolean execute() {
		return execute(startLine);
	}
	
	public boolean execute(String label) {
		Integer pos = labels.get(label);
		
		if (pos != null) {
			return execute(pos.intValue());
		}
		else {
			err("Tried to run from nonexistant label "+label, -1);
		}
		
		return false;
	}
	
	private boolean execute(int pos) {
		if (!active && startLine != -1) {
			activeScripts.add(this);
			active = true;
		}
		
		if (pos < 0) 
			pos = 0;
		
		for(int i = pos; i < lines.size(); i++) {
			String line = lines.get(i);
			line = line.replaceAll("\t", "");
			
			if (i == end) {
				end = -1;
			}
			
			if (line.length() == 0 || line.toLowerCase().equals("script_tick"))
				continue;
			
			if (line.toLowerCase().equals("script_end")) {
				active = false;
				return true;
			}
			
			if (line.toLowerCase().equals("return")) {
				return false;
			}
			
			if (line.contains("log ")) {
				String cmdLine = sanitize(line.replace("log ", ""));
				
				Console.log(cmdLine);
				continue;
			}
			
			if (line.startsWith("@")) {
				continue;
			}
			
			if (line.startsWith("$")) {
				// Math
				handleMath(line.substring(1), i);
				continue;
			}
			if (line.toLowerCase().contains("goto")) {
				String rhs = line.split(" ")[1];
				
				boolean byLabel = false;
				for(String label : labels.keySet()) {
					if (rhs.equals(label)) {
						i = labels.get(label);

						byLabel = true;
						break;
					}
				}
				
				if (byLabel)
					continue;
				
				try {
					i = Integer.parseInt(rhs)-2;
				}
				catch(Exception e) {
					err("'goto' command has invalid arguments",i);
				}
				continue;
			}
			
			if (line.toLowerCase().indexOf("if ") == 0) {
				try {
					boolean condition = ifCondition(line);
					
					start = -1;
					end = -1;
					
					int skips = 0;
					
					for(int j = i; j < lines.size(); j++) {
						if (lines.get(j).contains("{")) {
							if (start == -1)
									start = j;
							else
								skips++;
						}
						
						if (end == -1 && lines.get(j).contains("}")) {
							if (skips == 0)
								end = j;
							else
								skips--;
						}
					}
					
					if (condition && start != -1) {
						i = start-1;
						lines.set(start, lines.get(start).substring(lines.get(start).indexOf("{")+1));
						lines.set(end, lines.get(end).substring(lines.get(end).indexOf("}")+1));
						end = -1;
					}
					else {
						i = end;
					}
					
				}
				catch(ClassNotFoundException e) {
					err("Error with 'if' command, condition looks for nonexistant var",i);
				}
				catch(Exception e) {
					err("Error with 'if' command",i);
				}
				continue;
			}
			
			String cmdLine = sanitize(line);
			Console.send(cmdLine);
		}
		return false;
	}

	private String sanitize(String cmdLine) {
		if (cmdLine.contains("$")) {
			for(String var : floats.keySet()) {
				String fullVar = "$"+var;
				while(cmdLine.contains(fullVar)) {
					cmdLine = cmdLine.replace(fullVar,
							floatFormat.format(floats.get(var)));
				}
			}
		}
		return cmdLine;
	}

	private boolean ifCondition(String line) throws NumberFormatException, ClassNotFoundException {
		boolean condition = false;
		String[] parts = line.split(" ");
		
		if (parts[1].contains("!=")) {
			String[] cond = parts[1].split("!=");
			condition = !getVar(cond[0]).equals(cond[1]);
		}
		else if (parts[1].contains("=")) {
			if (parts[1].contains(">")) {
				String[] cond = parts[1].split(">=");
				condition = Float.parseFloat(getVar(cond[0]))
						>= Float.parseFloat(cond[1]);
			}
			else if (parts[1].contains("<")) {
				String[] cond = parts[1].split("<=");
				condition = Float.parseFloat(getVar(cond[0]))
						<= Float.parseFloat(cond[1]);
			}
			else {
				String[] cond = parts[1].split("==");
				condition = getVar(cond[0]).equals(cond[1]);								
			}
		}
		else if (parts[1].contains(">")) {
			String[] cond = parts[1].split(">");
			condition = Float.parseFloat(getVar(cond[0]))
					> Float.parseFloat(cond[1]);
		}
		else if (parts[1].contains("<")) {
			String[] cond = parts[1].split("<");
			condition = Float.parseFloat(getVar(cond[0]))
					< Float.parseFloat(cond[1]);
		}
		
		return condition;
	}
	
	private String getVar(String cond) throws ClassNotFoundException {
		if (cond.charAt(0) == '$') {
			return floats.get(cond.substring(1)).toString();
		}
		
		String[] condLHS = cond.split(":");
		return Command.getVariable(Class.forName(condLHS[0]), condLHS[1]);
	}

	private void handleMath(String line, int i) {
		line = line.replaceAll(" ", "");
		String var = null;
		for(int j = 0; j < line.length(); j++) {
			switch(line.charAt(j)) {
			case '=':
			case '+':
			case '-':
			case '*':
			case '/':
			case '%':
				var = line.substring(0, j);
				line = line.substring(j);
				break;
			default:
				continue;
			}
			break;
		}
		
		if (var == null || var.length() == 0) {
			err("Math syntax error", i);
			return;
		}
		
		Float value = floats.get(var);
		if (value == null && line.charAt(0)!='=') {
			err(var + " is not defined", i);
			return;
		}
		
		String rightHandSide = line.substring(1);
		Float rhsValue = null;
		if (rightHandSide.equals("delta_time")) {
			rhsValue = new Float(Window.deltaTime);
		}
		else if (rightHandSide.equals("fps")) {
			rhsValue = new Float(Window.framerate);
		}
		else if (rightHandSide.equals("gravity")) {
			rhsValue = new Float(Globals.gravity);
		}
		else if (rightHandSide.equals("fov")) {
			rhsValue = new Float(Globals.fov);
		}
		else {
			try {
				rhsValue = Float.parseFloat(rightHandSide);
			}
			catch (Exception e) {
				err("Math syntax error", i);
				return;
			}
		}
		
		switch(line.charAt(0)) {
		case '+':
			floats.put(var, value.floatValue() + rhsValue.floatValue());
			break;
			
		case '-':
			floats.put(var, value.floatValue() - rhsValue.floatValue());
			break;
			
		case '*':
			floats.put(var, value.floatValue() * rhsValue.floatValue());
			break;
			
		case '/':
			floats.put(var, value.floatValue() / rhsValue.floatValue());
			break;
			
		case '%':
			floats.put(var, value.floatValue() % rhsValue.floatValue());
			break;
			
		case '=':
			floats.put(var, rhsValue);
			break;
			
		default:
			err("Math syntax error", i);
			return;
		}
	}

	private void err(String s, int i) {
		System.err.println("["+scriptName+"] Error at line "+i+": "+s);
	}
}
