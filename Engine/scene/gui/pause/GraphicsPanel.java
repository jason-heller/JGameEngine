package scene.gui.pause;

import org.lwjgl.opengl.DisplayMode;

import debug.console.Console;
import global.Globals;
import gui.GuiDropdown;
import gui.GuiPanel;
import gui.GuiSlider;
import gui.GuiSpinner;
import gui.layouts.GuiFlowLayout;
import gui.listeners.MenuListener;
import gui.listeners.SliderListener;
import opengl.Application;
import opengl.Window;

public class GraphicsPanel extends GuiPanel {
	private GuiDropdown menu;
	private GuiSlider fov, fps, chunkRenderDist, particleCount, foliageRadius;
	private GuiSpinner fullscreen, waterQuality;
	
	private DisplayMode[] resolutions;
	private String[] resMenuOptions;
	
	public GraphicsPanel(GuiPanel parent, int x, int y) {
		super(parent);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582/2, 392);
		
		resolutions = Window.getDisplayModes();
		int i = 0;
		resMenuOptions = new String[resolutions.length];
		for(DisplayMode mode : resolutions) {
			resMenuOptions[i++] = mode.getWidth()+"x"+mode.getHeight();
		}
		
		fov = new GuiSlider(x, y, "fov", 60, 105, Globals.fov, 1);
		fov.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Globals.fov = (int) value;
				Application.scene.getCamera().updateProjection();
			}
			
		});
		
		
		fps = new GuiSlider(x, y, "Framerate", 30, 120, Globals.maxFramerate, 1);
		fps.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Globals.maxFramerate = (int) value;
			}
			
		});
		add(fps);
		add(fov);
		
		fullscreen = new GuiSpinner(x,y,"Windowing", Globals.fullscreen?1:0, "Windowed", "Fullscreen");
		fullscreen.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				if (index == 1) {
					Globals.fullscreen = true;
					Window.setDisplayMode(Window.getWidth(), Window.getHeight(), true);
				} else {
					Globals.fullscreen = false;
					Window.setDisplayMode(Window.getWidth(), Window.getHeight(), false);
				}
			}
		});
		add(fullscreen);
		
		menu = new GuiDropdown(0, 0, "resolution", resMenuOptions);
		
		menu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				Window.setDisplayMode(resolutions[index]);
			}
			
		});
		
		addWithoutLayout(menu);
		menu.setPosition(x+324, fullscreen.y-4);
		addSeparator();
		
		chunkRenderDist = new GuiSlider(x, y, "terrain render dist", 3, 15, Globals.chunkRenderDist, 2);
		chunkRenderDist.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Globals.chunkRenderDist = (int) value;
				Application.scene.getTerrain().resize(Globals.chunkRenderDist);
			}
			
		});
		add(chunkRenderDist);
		
		particleCount = new GuiSlider(x, y, "max particles", 0, 300, Globals.maxParticles, 1);
		particleCount.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Globals.maxParticles = (int) value;
			}
			
		});
		add(particleCount);
		
		waterQuality = new GuiSpinner(x,y,"water quality", Globals.waterQuality, "Solid Color", "Only Reflect Skybox", "Simple Reflections", "Reflect Most Objects", "Reflect Everything");
		waterQuality.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				Globals.waterQuality = index;
			}
		});
		add(waterQuality);
		
		foliageRadius = new GuiSlider(x, y, "foliage render distance", 16, 60, Globals.foliageRadius, 1);
		foliageRadius.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {}

			@Override
			public void onRelease(float value) {
				Globals.foliageRadius = (int) value;
				Application.scene.getWorld().getFoliage().setRadius(Globals.foliageRadius);
			}
			
		});
		add(foliageRadius);
	}
}
