package opengl;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

import global.Globals;

public class Window {
	private static long lastFrameTime;
	private static float aspectRatio;
	private static long lastFramerate;
	
	public static float deltaTime;
	private static int frameTime;
	public static float framerate;
	public static float timeScale = 1f;

	/**
	 * Creates the display for the game
	 */
	public static void create() {
		//Globals.displayWidth  = Settings.getInt("display width");
		//Globals.displayHeight = Settings.getInt("display height");
		//Globals.maxFramerate = Settings.getInt("target fps");
		//Globals.fov = Settings.getInt("fov");
		
		try {
			//resize(Globals.displayWidth, Globals.displayHeight);
			Display.create(new PixelFormat(),
					new ContextAttribs(3, 3).withForwardCompatible(true).withProfileCore(true));
			Display.setTitle(Globals.windowTitle);
			Display.setInitialBackground(1, 1, 1);
			Display.setVSyncEnabled(false);
			//Display.setLocation(0, 0);
			//Display.setResizable(true);
			setDisplayMode(Window.getWidth(), Window.getHeight(), Globals.fullscreen);

			GL11.glEnable(GL13.GL_MULTISAMPLE);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.err.println("Couldn't create display!");
			System.exit(-1);
		}
		GL11.glViewport(0, 0, Globals.viewportWidth, Globals.viewportHeight);
		lastFrameTime = getCurrentTime();
		lastFramerate = lastFrameTime;
	}
	
	public static DisplayMode[] getDisplayModes() {
		try {
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			List<DisplayMode> prunedModes = new ArrayList<DisplayMode>();
			prunedModes.add(Display.getDesktopDisplayMode());
			for(DisplayMode m : modes) {
				if (m.getWidth() < 640 || m.getHeight() < 480) continue;
				float ratio = m.getWidth()/(float)m.getHeight();
				if (ratio != 4f/3f && ratio != 16f/9f) continue;
				boolean add = true;
				for(DisplayMode m2 : prunedModes) {
					if (m2.getWidth() == m.getWidth() && m2.getHeight() == m.getHeight()) {
						add = false;
						break;
					}
				}
				
				if (add)
					prunedModes.add(m);
			}
			
			return prunedModes.toArray(new DisplayMode[0]);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void update() {
		if (Display.wasResized()) GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		
		// Update Display
		Display.sync(Globals.maxFramerate);
		Display.update();
		
		// Get delta time
		long currentFrameTime = getCurrentTime();
		deltaTime = ((currentFrameTime - lastFrameTime) / 1000f) * timeScale;
		lastFrameTime = currentFrameTime;
		
		// Get framerate
		if (currentFrameTime - lastFramerate > 1000) {
			framerate = (frameTime);
			frameTime = 0;
			lastFramerate += 1000;
        }
		frameTime++;
	}
	
	public static void resize(int width, int height) {
        try {
            DisplayMode targetDisplayMode = new DisplayMode(width,height);
            Display.setDisplayMode(targetDisplayMode);
            aspectRatio = width / (float)height;
        } catch (LWJGLException e) {
            System.out.println("Unable to setup mode ["+width+","+height+"]");
        }
    }
	
	public static void setFullscreen(boolean isFullscreen) {
		if (Display.isFullscreen() == isFullscreen)
			return;
		
		try {
            Display.setFullscreen(isFullscreen);
        } catch (LWJGLException e) {
            System.out.println("Unable to setup mode [fs="+isFullscreen+"]");
        }
	}
	
	/**
	 * Closes the game's display
	 */
	public static void destroy() {
		Display.destroy();
	}
	
	private static long getCurrentTime() {
		return Sys.getTime() * 1000 / Sys.getTimerResolution();
	}

	public static float getAspectRatio() {
		return aspectRatio;
	}

	public static int getWidth() {
		return Display.getDisplayMode().getWidth();
	}

	public static int getHeight() {
		return Display.getDisplayMode().getHeight();
	}
	
	public static void setDisplayMode(int width, int height, boolean fullscreen) {
        // return if requested DisplayMode is already set
                if ((Display.getDisplayMode().getWidth() == width) && 
            (Display.getDisplayMode().getHeight() == height) && 
            (Display.isFullscreen() == fullscreen)) {
            return;
        }
         
        try {
            DisplayMode targetDisplayMode = null;
             
            if (fullscreen) {
                DisplayMode[] modes = Display.getAvailableDisplayModes();
                int freq = 0;
                 
                for (int i=0;i<modes.length;i++) {
                    DisplayMode current = modes[i];
                     
                    if ((current.getWidth() == width) && (current.getHeight() == height)) {
                        if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
                            if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
                                targetDisplayMode = current;
                                freq = targetDisplayMode.getFrequency();
                            }
                        }
 
                        // if we've found a match for bpp and frequence against the 
                        // original display mode then it's probably best to go for this one
                        // since it's most likely compatible with the monitor
                        if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel()) &&
                            (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
                            targetDisplayMode = current;
                            break;
                        }
                    }
                }
            } else {
                targetDisplayMode = new DisplayMode(width,height);
            }
             
            if (targetDisplayMode == null) {
                System.out.println("Failed to find value mode: "+width+"x"+height+" fs="+fullscreen);
                return;
            }
 
            Display.setDisplayMode(targetDisplayMode);
            Display.setFullscreen(fullscreen);
             
        } catch (LWJGLException e) {
            System.out.println("Unable to setup mode "+width+"x"+height+" fullscreen="+fullscreen + e);
        }
    }

	public static void setDisplayMode(DisplayMode displayMode) {
		try {
			Display.setDisplayMode(displayMode);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	public static void refresh() {
		deltaTime = 0;
		lastFrameTime = getCurrentTime();
		if (Display.wasResized()) GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		
		// Update Display
		Display.sync(Globals.maxFramerate);
		Display.update();
	}
}
