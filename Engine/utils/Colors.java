package utils;

import org.joml.Vector3f;

public class Colors {
	
	public static final float MAX_PIXEL_COLOR = 256*256*256;

	private static final long MARQUEE_SPEED_MS = 500;
	private static final long MARQUEE_COLOR_SIZE = 10;
	
	public static final Vector3f RED = new Vector3f(1,0,0);
	public static final Vector3f ORANGE = new Vector3f(.99f, .44f, 0);
	public static final Vector3f YELLOW = new Vector3f(1,1f,0);
	public static final Vector3f GREEN = new Vector3f(0,1,0);
	public static final Vector3f BLUE = new Vector3f(.078f,0.596f,1);
	public static final Vector3f PINK = new Vector3f(1, .4f, .7f);
	public static final Vector3f INDIGO = new Vector3f(0.54f,0.16f,.88f); // 138-43-226
	public static final Vector3f VIOLET = new Vector3f(0.93f,.51f,0.94f); //	238-130-238
	public static final Vector3f BLACK = new Vector3f(0,0,0);
	public static final Vector3f WHITE = new Vector3f(1,1,1);
	public static final Vector3f SILVER = new Vector3f(0.6f,0.6f,0.6f);
	public static final Vector3f GREY = new Vector3f(.33f,.33f,.33f);
	public static final Vector3f LT_SILVER = new Vector3f(.88f,.88f,.88f);

	public static Vector3f getColor(char c, int pos) {
		switch(c) {
		case 'r': return RED;
		case 'o': return ORANGE;
		case 'y': return YELLOW;
		case 'g': return GREEN;
		case 'b': return BLUE;
		case 'p': return PINK;
		case 'i': return INDIGO;
		case 'v': return VIOLET;
		case 'B':
		case '0': return BLACK;
		case 's': return SILVER;
		case 'R': return hsvToRgb((pos%20)/20.1f,.85f,1f);
		case 'M': return scrollColor(pos);
		case 'A': return alertColor();
		case '8': return LT_SILVER;
		default: return WHITE;
		}
	}
	
	public static Vector3f hsvToRgb(float hue, float saturation, float value) {

	    int h = (int)(hue * 6);
	    float f = hue * 6 - h;
	    float p = value * (1 - saturation);
	    float q = value * (1 - f * saturation);
	    float t = value * (1 - (1 - f) * saturation);

	    switch (h) {
	      case 0: return new Vector3f(value, t, p);
	      case 1: return new Vector3f(q, value, p);
	      case 2: return new Vector3f(p, value, t);
	      case 3: return new Vector3f(p, q, value);
	      case 4: return new Vector3f(t, p, value);
	      case 5: return new Vector3f(value, p, q);
	      default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
	    }
	}
	
	public static Vector3f scrollColor(int pos) {
		pos += MARQUEE_COLOR_SIZE;
		int scrollPos = (int) (((System.currentTimeMillis()%MARQUEE_SPEED_MS)/(float)MARQUEE_SPEED_MS) * (float)MARQUEE_COLOR_SIZE);
	    float alpha = ((pos - (scrollPos%MARQUEE_COLOR_SIZE))%MARQUEE_COLOR_SIZE)/(float)MARQUEE_COLOR_SIZE;
	    return new Vector3f(alpha, alpha, alpha);
	}
	
	public static Vector3f alertColor() {
		float alpha = 0.5f + (float)Math.sin((System.currentTimeMillis()%1000)/250f)*0.5f;
	    return new Vector3f(alpha, alpha/4f, alpha/9f);
	}
}
