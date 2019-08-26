package gui.net;

public enum Announcements {
	UNKNOWN("Unknown error occured", true),
	TIMEOUT("Connetion timed out", true),
	FAILURE("Failed to connect", true),
	CONNECTING("Connecting...", false),
	LOADING("Loading...", false);
	
	private String msg;
	private boolean canCancel;
	Announcements(String msg, boolean canCancel) {
		this.msg = msg;
		this.canCancel = canCancel;
	}
	
	public String getMessage() {
		return msg;
	}

	public boolean isCancelable() {
		return canCancel;
	}
}
