package logic.controller.dialogue;

import logic.StatController;
import scene.entity.NPC;

public class DialogueActions {
	public static void doAction(NPC npc, String data) {
		int argsStart = data.indexOf("(")+1;
		String action = data.substring(0,argsStart-1);
		String[] args = data.substring(argsStart,data.length()).split(" ");

		action.replaceAll(" ", "");
		
		if (action.equals("make_hostile")) {
			npc.makeHostile();
		}
		else if (action.equals("give_item")) {
			
		}
		else if (action.equals("story")) {
			StatController.storyProgress.put(args[0], Integer.parseInt(args[1]));
		}
	}

	public static void parseActions(NPC npc, String data) {
		String[] actions = data.split("\\)");
		for(String action : actions) {
			doAction(npc, action);
		}
	}
}