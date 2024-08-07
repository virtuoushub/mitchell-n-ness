package com.colapietro.throwback.lwjgl;

import org.lwjgl.system.MemoryStack;
import org.slf4j.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.colapietro.throwback.lwjgl.GLHelper.glColor;
import static com.colapietro.throwback.lwjgl.demo.IOUtil.ioResourceToByteBuffer;
import static java.lang.Math.round;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_is_hdr_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.stb.STBImageResize.STBIR_EDGE_CLAMP;
import static org.lwjgl.stb.STBImageResize.STBIR_FILTER_MITCHELL;
import static org.lwjgl.stb.STBImageResize.STBIR_RGB;
import static org.lwjgl.stb.STBImageResize.STBIR_RGBA_PM;
import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8_srgb;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * @author Peter Colapietro.
 */
public class Image {
    private static final Logger LOGGER = LoggerFactory.getLogger(Image.class);
    private final int imageWidth;
    final int imageHeight;
    private final ByteBuffer image;
    private final int comp;
    private float windowWidth;
    private float windowHeight;
    boolean lineBoundingBoxEnabled = HelloWorld.boundingBoxesEnabled; // FIXME
    float angle = 0;
    float y = 0;
    float x = 0;
    private boolean wrapAroundEnabled = true;
    float scale = 40.0f;
    float scaleFactor = 1.0f + scale * 0.1f;

    Image(String imagePath) {
        ByteBuffer imageBuffer;
        try {
            imageBuffer = ioResourceToByteBuffer(imagePath, 8 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer imageWidth    = stack.mallocInt(1);
            IntBuffer imageHeight    = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // Use info to read image metadata without decoding the entire image.
            // We don't need this for this demo, just testing the API.
            if (!stbi_info_from_memory(imageBuffer, imageWidth, imageHeight, comp)) {
                throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());
            }

            LOGGER.debug("Image width: " + imageWidth.get(0));
            LOGGER.debug("Image height: " + imageHeight.get(0));
            LOGGER.debug("Image components: " + comp.get(0));
            LOGGER.debug("Image HDR: " + stbi_is_hdr_from_memory(imageBuffer));

            // Decode the image
            image = stbi_load_from_memory(imageBuffer, imageWidth, imageHeight, comp, 0);
            if (image == null) {
                throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
            }

            this.imageWidth = imageWidth.get(0);
            this.imageHeight = imageHeight.get(0);
            this.comp = comp.get(0);
        }
    }

    void createTexture() {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        int format;
        if (comp == 3) {
            if ((imageWidth & 3) != 0) {
                glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (imageWidth & 1));
            }
            format = GL_RGB;
        } else {
            premultiplyAlpha();

            glEnable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

            format = GL_RGBA;
        }

        glTexImage2D(GL_TEXTURE_2D, 0, format, imageWidth, imageHeight, 0, format, GL_UNSIGNED_BYTE, image);

        ByteBuffer input_pixels = image;
        int        input_w      = imageWidth;
        int        input_h      = imageHeight;
        int        mipmapLevel  = 0;
        while (1 < input_w || 1 < input_h) {
            int output_w = Math.max(1, input_w >> 1);
            int output_h = Math.max(1, input_h >> 1);

            ByteBuffer output_pixels = stbir_resize_uint8_srgb(
                    input_pixels, input_w, input_h, input_w * comp,
                    null, output_w, output_h, output_w * comp,
                    comp == 4 ? STBIR_RGBA_PM : STBIR_RGB
            );

            if (mipmapLevel == 0) {
                stbi_image_free(image);
            } else {
                memFree(input_pixels);
            }

            glTexImage2D(GL_TEXTURE_2D, ++mipmapLevel, format, output_w, output_h, 0, format, GL_UNSIGNED_BYTE, output_pixels);

            input_pixels = output_pixels;
            input_w = output_w;
            input_h = output_h;
        }
        if (mipmapLevel == 0) {
            stbi_image_free(image);
        } else {
            memFree(input_pixels);
        }
    }
    
    private void premultiplyAlpha() {
        int stride = imageWidth * 4;
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int i = y * stride + x * 4;

                float alpha = (image.get(i + 3) & 0xFF) / 255.0f;
                image.put(i + 0, (byte)round(((image.get(i + 0) & 0xFF) * alpha)));
                image.put(i + 1, (byte)round(((image.get(i + 1) & 0xFF) * alpha)));
                image.put(i + 2, (byte)round(((image.get(i + 2) & 0xFF) * alpha)));
            }
        }
    }

    void render() {
        glPushMatrix();
        glTranslatef(windowWidth * 0.5f, windowHeight * 0.5f, 0.0f);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glScalef(scaleFactor, scaleFactor, 1f);
        if(wrapAroundEnabled) {
            wrapAround();
        }
        glTranslatef(x, y, 0.0f);
        glRotatef(angle,0,0,1);
        glTranslatef(-imageWidth * 0.5f, -imageHeight * 0.5f, 0.0f);
        glColor(RGB.WHITE);
        glBegin(GL_QUADS);
        {
            glTexCoord2f(0.0f, 0.0f);
            glVertex2f(0.0f, 0.0f);

            glTexCoord2f(1.0f, 0.0f);
            glVertex2f(imageWidth, 0.0f);

            glTexCoord2f(1.0f, 1.0f);
            glVertex2f(imageWidth, imageHeight);

            glTexCoord2f(0.0f, 1.0f);
            glVertex2f(0.0f, imageHeight);
        }
        glEnd();

        if (lineBoundingBoxEnabled) {
            renderLineBoundingBox(imageHeight);
        }
        glPopMatrix();
    }

    private void renderLineBoundingBox(float y) {
        glDisable(GL_TEXTURE_2D);
        glPolygonMode(GL_FRONT, GL_LINE);
        glColor(RGB.RED);

        float width = imageWidth;

        glBegin(GL_QUADS);
        glVertex2f(0.0f, y);
        glVertex2f(width, y);
        glVertex2f(width, y - imageHeight);
        glVertex2f(0.0f, y - imageHeight);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glPolygonMode(GL_FRONT, GL_FILL);
        glColor(RGB.WHITE);
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    private void wrapAround() {
        x = wrapAroundAxis(x, windowWidth);
        y = wrapAroundAxis(y, windowHeight);
    }

    private float wrapAroundAxis(float axis, float axisMax) {
        float scaledAxis;
        if(scaleFactor > 0 || scaleFactor < 0) { //FIXME
            scaledAxis = axis * scaleFactor;
        } else {
            scaledAxis = axis;
        }
        float scaledHalfMax = axisMax / 2.0f;
        if(scaledAxis > scaledHalfMax) {
            scaledAxis = -scaledHalfMax / scaleFactor;
        } else if (scaledAxis < -scaledHalfMax) {
            scaledAxis = scaledHalfMax / scaleFactor;
        } else {
            scaledAxis = axis;
        }
        return scaledAxis;
    }

}
