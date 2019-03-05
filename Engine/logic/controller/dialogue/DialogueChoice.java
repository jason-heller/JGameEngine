package logic.controller.dialogue;

public class DialogueChoice {
	private String text;
	private int position;
	private byte skillTestResult = 0;
	// Todo add data for required stats
	
	public DialogueChoice(String text, int position) {
		this.text = text;
		this.position = position;
	}
	
	public String getText() {
		return text;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setSkillTestResult(byte result) {
		this.skillTestResult = result;
	}
	
	public byte getSkillTestResult() {
		return skillTestResult;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}
