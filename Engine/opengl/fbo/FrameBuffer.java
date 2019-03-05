package opengl.fbo;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public class FrameBuffer {
	private int width;
	private int height;
	private int fbo;

	private int textureBuffer = -1, depthBufferTexture = -1, depthBuffer = -1;

	public FrameBuffer(int width, int height) {
		this(width, height, true, true, false);
	}

	public FrameBuffer(int width, int height, boolean hasTextureBuffer, boolean hasDepthBuffer, boolean hasDepthBufferTexture) {
		this.width = width;
		this.height = height;
		fbo = createFbo(hasTextureBuffer ? GL30.GL_COLOR_ATTACHMENT0 : GL11.GL_NONE);

		if (hasTextureBuffer)
			textureBuffer = FboUtils.createTextureAttachment(width, height);
		if (hasDepthBuffer)
			depthBuffer = FboUtils.createDepthBufferAttachment(width, height);
		if (hasDepthBufferTexture)
			depthBufferTexture = FboUtils.createDepthTextureAttachment(width, height);

		unbind();
	}

	private static int createFbo(int drawBuffer) {
		int frameBuffer = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		GL11.glDrawBuffer(drawBuffer);
		GL11.glReadBuffer(GL11.GL_NONE);
		return frameBuffer;
	}

	/*public static int createDepthBuffer(int width, int height) {
		int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT16, width, height, 0, GL11.GL_DEPTH_COMPONENT,
				GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture, 0);
		return texture;
	}

	public static int createTextureBuffer(int width, int height) {
		int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE,
				(ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture, 0);
		return texture;
	}*/

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
		return textureBuffer;
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
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureBuffer);
	}

	public void bindDepthBuffer(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureBuffer);
	}

	public void unbindBuffer() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void cleanUp() {
		GL30.glDeleteFramebuffers(fbo);
		if (hasTextureBuffer())
			GL11.glDeleteTextures(textureBuffer);
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
		return (textureBuffer != -1);
	}
	
	public boolean hasDepthBuffer() {
		return (depthBuffer != -1);
	}

	public boolean hasDepthBufferTexture() {
		return (depthBufferTexture != -1);
	}
}
