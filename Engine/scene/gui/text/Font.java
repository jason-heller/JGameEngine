package scene.gui.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import pipeline.Resources;
import pipeline.Texture;
import utils.FileUtils;

public class Font {
	
	private static final int PAD_TOP = 0;
	private static final int PAD_LEFT = 1;
	private static final int PAD_BOTTOM = 2;
	private static final int PAD_RIGHT = 3;
	
	protected static final float LINE_HEIGHT = 0.03f;
	protected static final int SPACE_ASCII = 32;

	private static final int DESIRED_PADDING = 3;
	public static Font defaultFont = new Font("verdana");
	
	private Map<Integer, Character> metaData = new HashMap<Integer, Character>();

	private BufferedReader reader;
	private Map<String, String> values = new HashMap<String, String>();

	private int[] padding;

	private int paddingWidth;
	private int paddingHeight;
	private float spaceWidth;
	
	private Texture fontTexture;

	public Font(String pathNoExtension) {
		load("res/" + pathNoExtension + ".fnt");
		fontTexture = Resources.addTexture("verdana", pathNoExtension + ".png", GL11.GL_TEXTURE_2D, true, false, 1f, false, false, 0);
	}
	
	private void load(String path) {
		try {
			// Open file
			reader = FileUtils.getReader(path);
			
			// Load padding data
			readLine();
			this.padding = getValuesOfVariable("padding");
			this.paddingWidth = padding[PAD_LEFT] + padding[PAD_RIGHT];
			this.paddingHeight = padding[PAD_TOP] + padding[PAD_BOTTOM];
			
			// Line sizes
			readLine();
			//int lineHeightPixels = Integer.parseInt(values.get("lineHeight")) - paddingHeight;
			
			int imageWidth = Integer.parseInt(values.get("scaleW"));
			
			// Character data
			readLine();
			readLine();
			while (readLine()) {
				Character c = loadCharacter(imageWidth);
				if (c != null) {
					metaData.put(c.getId(), c);
				}
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Couldn't read font file!");
		}
	}
	
	private boolean readLine() {
		values.clear();
		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e1) {
		}
		if (line == null) {
			return false;
		}
		for (String part : line.split(" ")) {
			String[] valuePairs = part.split("=");
			if (valuePairs.length == 2) {
				values.put(valuePairs[0], valuePairs[1]);
			}
		}
		return true;
	}
	
	private Character loadCharacter(int imageSize) {
		int id = Integer.parseInt(values.get("id"));
		if (id == SPACE_ASCII) {
			this.spaceWidth = (Integer.parseInt(values.get("xadvance")) - paddingWidth);
			return new Character(id, 0, 0, 0, 0, 0, 0, spaceWidth, 0, (int) spaceWidth);
		}
		if (id == 9) {
			return null;
		}
		float xTex = ((float) Integer.parseInt(values.get("x")) + (padding[PAD_LEFT] - DESIRED_PADDING)) / imageSize;
		float yTex = ((float) Integer.parseInt(values.get("y")) + (padding[PAD_TOP] - DESIRED_PADDING)) / imageSize;
		int width = Integer.parseInt(values.get("width")) - (paddingWidth - (2 * DESIRED_PADDING));
		int height = Integer.parseInt(values.get("height")) - ((paddingHeight) - (2 * DESIRED_PADDING));
		float quadWidth = width;
		float quadHeight = height;
		float xTexSize = (float) width / imageSize;
		float yTexSize = (float) height / imageSize;
		int xOff = (Integer.parseInt(values.get("xoffset")) + padding[PAD_LEFT] - DESIRED_PADDING);
		int yOff = (Integer.parseInt(values.get("yoffset")) + (padding[PAD_TOP] - DESIRED_PADDING));
		int xAdvance = (Integer.parseInt(values.get("xadvance")) - paddingWidth);
		return new Character(id, xTex, yTex, xTexSize, yTexSize, xOff, yOff, quadWidth, quadHeight, xAdvance);
	}
	
	private int[] getValuesOfVariable(String variable) {
		String[] numbers = values.get(variable).split(",");
		int[] actualValues = new int[numbers.length];
		for (int i = 0; i < actualValues.length; i++) {
			actualValues[i] = Integer.parseInt(numbers[i]);
		}
		return actualValues;
	}
	
	public Texture getTexture() {
		return fontTexture;
	}
	
	public float getSpaceWidth() {
		return spaceWidth;
	}
	
	public Character getCharacter(int ascii) {
		return metaData.get(ascii);
	}

	public int getNumCharacters() {
		return metaData.size();
	}

	public int getHeight() {
		return paddingHeight;
	}

	public int getWidth() {
		return paddingWidth;
	}
}
