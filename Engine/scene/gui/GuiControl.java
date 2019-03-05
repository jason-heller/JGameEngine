package scene.gui;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import global.Globals;
import pipeline.Model;
import pipeline.Resources;
import pipeline.util.TextureUtils;
import scene.gui.renderer.GuiImageShader;
import scene.gui.text.Font;

public class GuiControl {
	private static GuiImageShader shader;
	private static final Model quad = Resources.QUAD2D;
	private static List<GuiComponent> components = new ArrayList<GuiComponent>();
	private static float opacity = 1f;
	
	public static void init() {
		shader = new GuiImageShader();
	}
	
	public static void render() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		shader.start();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		List<GuiComponent> temporaryComponents = new ArrayList<GuiComponent>();
		
		quad.bind(0,1);
		
		for(GuiComponent component : components) {
			if (component instanceof Image) {
				Image image = (Image)component;
				image.gfx.bind(0);
				shader.color.loadVec3(image.getColor());
				shader.opacity.loadFloat(image.getOpacity());
				shader.translation.loadVec4(image.getTransform());
				shader.offset.loadVec4(image.getUvOffset());
				shader.centered.loadBoolean(image.isCentered());
				shader.rotation.loadFloat(image.getRotation());
				GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);		
			} else {
				//GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR);
				Text text = (Text)component;
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, text.getFont().getTexture().id);
				for(int i = 0; i < text.getLetters().length; i++) {
					Image image = text.getLetters()[i];
					shader.color.loadVec3(image.getColor());
					shader.opacity.loadFloat(text.getOpacity());
					shader.translation.loadVec4(image.getTransform());
					shader.offset.loadVec4(image.getUvOffset());
					shader.centered.loadBoolean(image.isCentered());
					shader.rotation.loadFloat(image.getRotation());
					GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
				}//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}
			
			if (component.isTemporary()) {
				temporaryComponents.add(component);
			}
		}
		
		quad.unbind(0,1);
		
		shader.stop();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		components.removeAll(temporaryComponents);
	}
	
	public static void setOpacity(float newOpacity) {
		opacity = newOpacity;
	}
	
	public static float getOpacity() {
		return opacity;
	}
	
	public static void addComponent(GuiComponent component) {
		int depth = component.getDepth();
		
		if (depth == Gui.DEPTH_SEQUENTIAL) {
			int highestDepth = 0;
			
			for(int i = components.size()-1; i >= 0 ; --i) {
				int compDepth = components.get(i).getDepth();
				if (compDepth >= 0) {
					if (compDepth > highestDepth) {
						highestDepth = compDepth;
					} else {
						break;
					}
				}
			}
			component.setDepth(highestDepth);
		}
		
		if (depth < 0) {
			for(int i = components.size()-1; i >= 0 ; --i) {
				int compDepth = components.get(i).getDepth();
				if (compDepth > depth) {
					components.add(i+1, component);
					return;
				}
			}
		} else {
			for(int i = 0; i < components.size(); ++i) {
				int compDepth = components.get(i).getDepth();
				if (compDepth < 0 || compDepth > depth) {
					components.add(i, component);
					return;
				}
			}
		}
		
		components.add(component);
	}
	
	public static void removeComponent(GuiComponent component) {
		components.remove(component);
	}
	
	public static void drawImage(Image image) {
		addComponent(image);
		//image.setOpacity(opacity);
		image.markAsTemporary();
	}
	
	public static Image drawImage(String texture, int x, int y) {
		Image img = new Image(texture, x, y);
		img.setOpacity(opacity);
		img.markAsTemporary();
		addComponent(img);
		return img;
	}
	
	public static Image drawImage(String texture, int x, int y, int w, int h) {
		Image img = new Image(texture, x, y);
		img.w = w;
		img.h = h;
		img.setOpacity(opacity);
		img.markAsTemporary();
		addComponent(img);
		return img;
	}
	
	public static Image drawImage(String texture, int x, int y, int w, int h, Vector3f color) {
		Image img = new Image(texture, x, y);
		img.w = w;
		img.h = h;
		img.setColor(color);
		img.setOpacity(opacity);
		img.markAsTemporary();
		addComponent(img);
		return img;
	}

	public static Text drawText(String text, int x, int y) {
		Text txt = new Text(text, x, y);
		txt.setOpacity(opacity);
		txt.markAsTemporary();
		addComponent(txt);
		return txt;
	}
	
	public static Text drawText(String text, int x, int y, boolean centered) {
		Text txt = new Text(Font.defaultFont, text, x, y, .3f, (Globals.displayWidth/2)-40, centered);
		txt.setOpacity(opacity);
		txt.markAsTemporary();
		addComponent(txt);
		return txt;
	}
	
	public static Text drawText(String text, int x, int y, float fontSize, boolean centered) {
		Text txt = new Text(Font.defaultFont, text, x, y, fontSize, (Globals.displayWidth/2)-40, centered);
		txt.setOpacity(opacity);
		txt.markAsTemporary();
		addComponent(txt);
		return txt;
	}
	
	public static Text drawText(String text, int x, int y, float fontSize, int lineWidth, boolean centered) {
		Text txt = new Text(Font.defaultFont, text, x, y, fontSize, lineWidth, centered);
		txt.setOpacity(opacity);
		txt.markAsTemporary();
		addComponent(txt);
		return txt;
	}
	
	public static Text drawText(Font font, String text, int x, int y, float fontSize, int lineWidth, boolean centered) {
		Text txt = new Text(font, text, x, y, fontSize, lineWidth, centered);
		txt.setOpacity(opacity);
		txt.markAsTemporary();
		addComponent(txt);
		return txt;
	}
	
	public static void cleanUp() {
		shader.cleanUp();
	}

	public static void clear() {
		components.clear();
	}

	public static Text drawText(Text text) {
		addComponent(text);
		text.markAsTemporary();
		return text;
	}

	public static void updateDepth(GuiComponent component) {
		if (components.contains(component)) {
			components.remove(component);
			addComponent(component);
		}
	}

	final static int halfRectWid = 80;
	public static void drawLoadingScreen() {
		drawImage("default", 640-halfRectWid, 350, halfRectWid*2, 30, Vector3f.ZERO).setOpacity(.4f);
		drawText("Loading", 640, 360, .3f, 1280, true);
		render();
	}
}
