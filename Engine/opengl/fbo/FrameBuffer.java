package opengl.fbo;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class FrameBuffer {
	private int width;
	private int height;
	private int fbo;

	private int textureBuffers[];
	private int depthBufferTexture = -1, depthBuffer = -1;
	
	private boolean multisampled = false;
	private int numTargets = 1;

	public FrameBuffer(int width, int height) {
		this(width, height, true, true, false, false, 1);
	}

	public FrameBuffer(int width, int height, boolean hasTextureBuffer, boolean hasDepthBuffer,
			boolean hasDepthBufferTexture, boolean multisampled, int numTargets) {
		this.width = width;
		this.height = height;
		this.multisampled = multisampled;
		this.numTargets = numTargets;
		this.textureBuffers = new int[numTargets];
		fbo = createFbo();

		if (hasTextureBuffer)
			if (multisampled) {
				for(int i = 0; i < numTargets; i++)
					textureBuffers[i] = FboUtils.createMultisampleColorAttachment(
							width, height, GL30.GL_COLOR_ATTACHMENT0 + i);
			}
			else {
				for(int i = 0; i < numTargets; i++)
					textureBuffers[i] = FboUtils.createTextureAttachment(width, height);
			}
		if (hasDepthBuffer)
			depthBuffer = FboUtils.createDepthBufferAttachment(width, height, multisampled);
		if (hasDepthBufferTexture)
			depthBufferTexture = FboUtils.createDepthTextureAttachment(width, height);

		unbind();
	}

	private int createFbo() {
		int frameBuffer = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		determineDrawBuffers();
		GL11.glReadBuffer(GL11.GL_NONE);
		return frameBuffer;
	}

	private void determineDrawBuffers() {
		IntBuffer drawBuffer = BufferUtils.createIntBuffer(numTargets);
		for(int i = 0; i < numTargets; i++)
			drawBuffer.put(GL30.GL_COLOR_ATTACHMENT0 + i);
		
		drawBuffer.flip();
		GL20.glDrawBuffers(drawBuffer);
		
	}
	
	public static void bind(int frameBuffer, int width, int height) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameBuffer);
		GL11.glViewport(0, 0, width, height);
	}

	public void bind() {
		bind(fbo, width, height);
	}

	public void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}
	
	public int getTextureBuffer() {
		return textureBuffers[0];
	}

	public int getTextureBuffer(int index) {
		return textureBuffers[index];
	}

	public int getDepthBuffer() {
		return depthBuffer;
	}

	public int getDepthBufferTexture() {
		return depthBufferTexture;
	}

	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_FLOAT,
					(ByteBuffer) null);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT16, width, height, 0,
					GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
	}

	public void bindTextureBuffer(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	public void bindTextureBuffer(int unit, int textureBufferIndex) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureBuffers[textureBufferIndex]);
	}

	public void bindDepthBuffer(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthBuffer);
	}

	public void unbindBuffer() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void cleanUp() {
		GL30.glDeleteFramebuffers(fbo);
		if (hasTextureBuffer()) {
			for(int i : textureBuffers)
				GL11.glDeleteTextures(i);
		}
		if (hasDepthBuffer())
			GL30.glDeleteRenderbuffers(depthBuffer);
		if (hasDepthBufferTexture())
			GL11.glDeleteTextures(depthBufferTexture);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean hasTextureBuffer() {
		return (textureBuffers != null);
	}
	
	public boolean hasDepthBuffer() {
		return (depthBuffer != -1);
	}

	public boolean hasDepthBufferTexture() {
		return (depthBufferTexture != -1);
	}
	
	public boolean isMultisampled() {
		return multisampled;
	}
	
	public int getId() {
		return fbo;
	}

	public void resolve(FrameBuffer output) {
			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fbo);
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, output.getId());
			
			GL30.glBlitFramebuffer(0, 0, 1280, 720, 0, 0, 1280, 720,
					GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
			unbind();
	}
}
