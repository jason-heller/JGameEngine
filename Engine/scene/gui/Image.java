package scene.gui;

import org.joml.Vector3f;
import org.joml.Vector4f;

import global.Globals;
import opengl.Window;
import pipeline.Resources;
import pipeline.Texture;
import utils.Colors;

public class Image implements GuiComponent {
	
	public Texture gfx;
	public float x, y, w, h;
	
	private Vector3f color;
	private float opacity = 1f;
	private int numRows;
	private Vector4f uvOffset = new Vector4f(0,0,0,0);
	private boolean centered = false;
	private float rotation;
	
	private float index;
	private int animationCap;
	private int depth = Gui.DEPTH_SEQUENTIAL;
	private boolean temporary = false;
	
	public Image(String texture, float x, float y) {
		gfx = Resources.getTexture(texture);
		this.x = x;
		this.y = y;
		this.w = gfx.size;
		this.h = gfx.size;
		color = Colors.WHITE;
	}
	
	public Image(Texture texture, float x, float y) {
		gfx = texture;
		this.x = x;
		this.y = y;
		this.w = gfx.size;
		this.h = gfx.size;
		color = Colors.WHITE;
	}
	
	public Image(Texture texture, float x, float y, float w, float h) {
		gfx = texture;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		color = Colors.WHITE;
	}
	
	public void setIndex(float index) {
		this.index = index;
		float size = 1f/numRows;
		uvOffset = new Vector4f(getXOffset(), getYOffset(), size, size);
	}
	
	public void setAnimCap(int cap) {
		animationCap = cap;
	}
	
	public void addIndexOffset(float dx, float dy) {
		this.index += dx + (dy*numRows);
		float size = 1f/numRows;
		uvOffset = new Vector4f(getXOffset(), getYOffset(), size, size);
	}
	
	public Vector4f getUvOffset() {
		return uvOffset;
	}
	
	public Texture getTexture() {
		return gfx;
	}
	
	public void incIndex(float incrementation) {
		index=Math.min(index+incrementation*Window.deltaTime,animationCap);
		float size = 1f/numRows;
		uvOffset = new Vector4f(getXOffset(), getYOffset(), size, size);
	}
	
	public Image setUvOffset(Vector4f offset) {
		this.uvOffset = offset;
		return this;
	}
	
	private float getYOffset() {
		int row = ((int)index)/numRows;
		return (float)row/(float)numRows;
	}

	private float getXOffset() {
		int col = ((int)index)%numRows;
		return (float)col/(float)numRows;
	}
	
	public Vector3f getColor() {
		return color;
	}
	public Image setColor(Vector3f color) {
		this.color = color;
		return this;
	}
	public float getOpacity() {
		return opacity;
	}
	public Image setOpacity(float opacity) {
		this.opacity = opacity;
		return this;
	}
	public float getNumRows() {
		return numRows;
	}
	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}
	public boolean isCentered() {
		return centered;
	}
	public Image setCentered(boolean centered) {
		this.centered = centered;
		return this;
	}
	public float getRotation() {
		return rotation;
	}
	public Image setRotation(float rotation) {
		this.rotation = rotation;
		return this;
	}

	public Vector4f getTransform() {
		float scaledWidth = (w/Globals.guiWidth)*Globals.displayWidth;
		float scaledHeight = (h/Globals.guiHeight)*Globals.displayHeight;
		float scaledX = (x/Globals.guiWidth)*Globals.displayWidth;
		float scaledY = (y/Globals.guiHeight)*Globals.displayHeight;
		
		float width  = scaledWidth/(Globals.displayWidth/2f);
		float height = scaledHeight/(Globals.displayHeight/2f);
		//float rotCos = (float)Math.cos(rotation);
		//float rotSin = (float)Math.sin(rotation);
		//float rx = rotCos * width + (-rotSin) * height;
		//float ry = rotSin * width + rotCos    * height;
		return new Vector4f(
				-0.5f + (scaledX/(Globals.displayWidth)),
				-(0.5f - (height/2f)) + (scaledY/(Globals.displayHeight)),
				width,
				height
				);
	}
	
	/**
	 * Marks this component as temporary
	 */
	public void markAsTemporary() {
		temporary  = true;
	}
	
	/**
	 * @return whether or not this is a temporary component
	 */
	public boolean isTemporary() {
		return temporary;
	}
	
	public Image setDepth(int depth) {
		this.depth = depth;
		GuiControl.updateDepth(this);
		return this;
	}

	public int getDepth() {
		return depth;
	}

	public void setUvOffset(float x, float y, float z, float w) {
		this.uvOffset = new Vector4f(x,y,z,w);
	}

	public void setSize(int w, int h) {
		this.w = w;
		this.h = h;
	}

}
