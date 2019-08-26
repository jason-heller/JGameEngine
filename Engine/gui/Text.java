package gui;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import gui.text.Character;
import gui.text.Font;
import utils.Colors;

public class Text implements GuiComponent {
	private String text;
	private float x, y, w, h;
	public float textSize = .3f;
	private boolean centered = false;
	private float opacity = 1f;
	private Font font;
	
	private Image[] letters;
	private boolean temporary = false;
	private int depth = Gui.DEPTH_SEQUENTIAL;
	
	public static final byte ALIGN_LEFT = 0;
	public static final byte ALIGN_RIGHT = 1;
	public static final byte ALIGN_TOP = 2;
	public static final byte ALIGN_BOTTOM = 3;
	
	private int alignment = ALIGN_LEFT;
	private int lineWidth = 1280;
	
	public Text(String text, int x, int y) {
		this(Font.defaultFont, text, x, y, .3f, false);
	}
	
	public Text(String text, int x, int y, float textSize, boolean centered) {
		this(Font.defaultFont, text, x, y, textSize, centered);
	}
	
	public Text(Font font, String text, int x, int y, float textSize, boolean centered) {
		this.font = font;
		this.x = x;
		this.y = y;
		this.textSize = textSize;
		this.centered = centered;
		setText(text);
	}
	
	public Text(Font font, String text, int x, int y, float textSize, int lineWidth, boolean centered, int... offsets) {
		this.font = font;
		this.x = x;
		this.y = y;
		this.textSize = textSize;
		this.lineWidth = lineWidth;
		this.centered = centered;
		
		for(int offset : offsets) 
			this.lineWidth = Math.max(this.lineWidth, offset);
		
		setText(text, offsets);
	}
	
	public void align(byte alignment) {
		switch(this.alignment) {
		case ALIGN_RIGHT:
			for(int i = 0; i < letters.length; i++) {
				letters[i].x += w;
			}
			break;
		case ALIGN_BOTTOM:
			for(int i = 0; i < letters.length; i++) {
				letters[i].y += h;
			}
			break;
		}
		
		this.alignment = alignment;
		
		switch(alignment) {
		case ALIGN_RIGHT:
			for(int i = 0; i < letters.length; i++) {
				letters[i].x -= w;
			}
			break;
		case ALIGN_BOTTOM:
			for(int i = 0; i < letters.length; i++) {
				letters[i].y -= h;
			}
			break;
		}
	}
	
	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}
	
	public void setText(String text, int... offsets) {
		if (text.equals(this.getText())) return;
		List<Image> letterList = new ArrayList<Image>();
		Vector3f color = Colors.WHITE;
		
		float dx = x;
		float dy = y;
		
		int offset = 0;
		
		for(int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '#') {
				if (text.length() > i+1) {
					color = Colors.getColor(text.charAt(i+1), i+(int)((System.currentTimeMillis()/60f)%1000));
				}
				i+=1;
			}
			else if (c == '\t' && offset < offsets.length) {
				dx = x+offsets[offset];
				offset++;
			}
			else if (c == '\n') {
				dx = x;
				dy += font.getCharacter('A').getyOffset()*textSize+20;
			}
			else if (c >= 32 && c <= 126){
				Character character = font.getCharacter(c);
				
				Image newLetter = new Image(font.getTexture(), (dx + character.getxOffset()*textSize), (dy + character.getyOffset()*textSize));
				newLetter.setUvOffset(character.getxTextureCoord(),character.getyTextureCoord(),character.getXMaxTextureCoord(),character.getYMaxTextureCoord());
				newLetter.w = character.getSizeX()*textSize;
				newLetter.h = character.getSizeY()*textSize;
				newLetter.setColor(color);
				
				letterList.add(newLetter);
				
				dx += (character.getxAdvance()*textSize);
				w = Math.max(dx-x, w);
				h = Math.max(dy-y, h);
				
				if ((dx-x) > lineWidth && c==' ') {
					dx = x;
					dy += newLetter.h+20;
				}
			}
		}
		
		int j = 0;
		letters = new Image[letterList.size()];
		for(Image img : letterList) {
			letters[j++] = img;
		}
		
		//if (letters.length > 0) {
			//h += font.getHeight();
		//}
		
		if (centered) {
			for(int i = 0; i < letters.length; i++) {
				letters[i].x -= w/2f;
				letters[i].y -= h/2f;
			}
		}
	}
	
	public String getText() {
		return text;
	}
	
	public Text setDepth(int depth) {
		this.depth = depth;
		GuiControl.updateDepth(this);
		return this;
	}

	public int getDepth() {
		return depth;
	}
	
	public Font getFont() {
		return font;
	}
	
	public Image[] getLetters() {
		return letters;
	}
	
	public void setCentered(boolean centered) {
		this.centered = centered;
	}
	
	public boolean isCentered() {
		return centered;
	}
	
	public float getWidth() {
		return w;
	}
	
	public float getHeight() {
		return h;
	}

	@Override
	public void markAsTemporary() {
		temporary  = true;
	}

	@Override
	public boolean isTemporary() {
		return temporary;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}
	
	public float getOpacity() {
		return opacity;
	}
}
