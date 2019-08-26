package gui.pause;

import org.lwjgl.opengl.DisplayMode;

import global.Globals;
import gui.GuiDropdown;
import gui.GuiPanel;
import gui.GuiSlider;
import gui.GuiSpinner;
import gui.layouts.GuiFlowLayout;
import gui.listeners.MenuListener;
import gui.listeners.SliderListener;
import opengl.Application;
import opengl.GlobalRenderer;
import opengl.Window;

public class GraphicsPanel extends GuiPanel {
	private GuiDropdown resolution;
	private GuiSlider fov, fps, particleCount;
	private GuiSpinner fullscreen, waterQuality, sampleRate;
	
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
		
		resolution = new GuiDropdown(0, 0, "resolution", resMenuOptions);
		
		resolution.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				Window.setDisplayMode(resolutions[index]);
			}
			
		});
		
		addWithoutLayout(resolution);
		resolution.setPosition(x+324, fullscreen.y-4);
		addSeparator();
		
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
		
		sampleRate = new GuiSpinner(0, 0, "Anti-Aliasing", 0, "None", "2x", "4x");
		sampleRate.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				GlobalRenderer.changeSampleRate(""+(index*2));
			}
			
		});
		
		add(sampleRate);
		
		sampleRate = new GuiSpinner(0, 0, "Glow Effects", Globals.enableGlow?1:0, "Off", "On");
		sampleRate.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				Globals.enableGlow = index==1?true:false;
			}
			
		});
		
		add(sampleRate);
	}
}
