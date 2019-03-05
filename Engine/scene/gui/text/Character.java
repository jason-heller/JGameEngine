package scene.gui.text;

public class Character {
	private int id;
	private float xTextureCoord;
	private float yTextureCoord;
	private float xMaxTextureCoord;
	private float yMaxTextureCoord;
	private int xOffset;
	private int yOffset;
	private float sizeX;
	private float sizeY;
	private int xAdvance;
	
	public Character(int id, float xTextureCoord, float yTextureCoord, float xTexSize, float yTexSize,
			int xOffset, int yOffset, float sizeX, float sizeY, int xAdvance) {
		this.id = id;
		this.xTextureCoord = xTextureCoord;
		this.yTextureCoord = yTextureCoord;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.xMaxTextureCoord = xTexSize + xTextureCoord;
		this.yMaxTextureCoord = yTexSize + yTextureCoord;
		this.xAdvance = xAdvance;
	}

	public int getId() {
		return id;
	}

	public float getxTextureCoord() {
		return xTextureCoord;
	}

	public float getyTextureCoord() {
		return yTextureCoord;
	}

	public float getXMaxTextureCoord() {
		return xMaxTextureCoord;
	}

	public float getYMaxTextureCoord() {
		return yMaxTextureCoord;
	}

	public int getxOffset() {
		return xOffset;
	}

	public int getyOffset() {
		return yOffset;
	}

	public float getSizeX() {
		return sizeX;
	}

	public float getSizeY() {
		return sizeY;
	}

	public int getxAdvance() {
		return xAdvance;
	}
}

