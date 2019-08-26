package gui;

public interface GuiComponent {
	void markAsTemporary();
	boolean isTemporary();
	int getDepth();
	GuiComponent setDepth(int depth);

}
