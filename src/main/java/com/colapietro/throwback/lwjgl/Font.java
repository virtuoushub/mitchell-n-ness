package com.colapietro.throwback.lwjgl;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.colapietro.throwback.lwjgl.demo.IOUtil.ioResourceToByteBuffer;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.GL_ALPHA;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointKernAdvance;
import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * @author Peter Colapietro.
 */
public class Font {

    private float windowHeight = 600.0f;
    int lineOffset;
    final int lineCount = 1;
    private final int scale  = 0;
    private boolean kerningEnabled = true;
    private boolean lineBoundingBoxEnabled = true;
    private String text;
    private final STBTTFontinfo info;
    private final int ascent;
    private final int descent;
    private final int lineGap;
    private final int fontHeight = 24;
    float lineHeight = fontHeight;
    private final ByteBuffer ttf;
    private long window;
    int BITMAP_WIDTH = 512;
    int BITMAP_HEIGHT = 512;

    public Font() {
        try {
            ttf = ioResourceToByteBuffer("FiraSans-Regular.ttf", 160 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        info = STBTTFontinfo.create();
        if (!stbtt_InitFont(info, ttf)) {
            throw new IllegalStateException("Failed to initialize font information.");
        }
        text = "Bar";
        try (MemoryStack stack = stackPush()) {
            IntBuffer pAscent  = stack.mallocInt(1);
            IntBuffer pDescent = stack.mallocInt(1);
            IntBuffer pLineGap = stack.mallocInt(1);

            stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap);

            ascent = pAscent.get(0);
            descent = pDescent.get(0);
            lineGap = pLineGap.get(0);
        }
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public int getScale() {
        return scale;
    }

    public int getLineOffset() {
        return lineOffset;
    }

    public boolean isKerningEnabled() {
        return kerningEnabled;
    }

    public boolean isLineBoundingBoxEnabled() {
        return lineBoundingBoxEnabled;
    }

    public long getWindow() {
        return window;
    }
    
    public void setWindow(long window) {
        this.window = window;
    }

    void render(STBTTBakedChar.Buffer cdata) {
        float scaleFactor = 1.0f + getScale() * 0.25f;


        glPushMatrix();
        // Zoom
        glScalef(scaleFactor, scaleFactor, 1f);
        // Scroll
        glTranslatef(4.0f, getFontHeight() * 0.5f + 4.0f - getLineOffset() * getFontHeight(), 0f);

        renderText(cdata, BITMAP_WIDTH, BITMAP_HEIGHT);

        glPopMatrix();

        glfwSwapBuffers(getWindow());
    }

    private void renderText(STBTTBakedChar.Buffer cdata, int BITMAP_W, int BITMAP_H) {
        float scale = stbtt_ScaleForPixelHeight(info, getFontHeight());

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint = stack.mallocInt(1);

            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(getWindowHeight() -getFontHeight());

            STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);

            int lineStart = 0;

            int i  = 0;
            int to = text.length();

            glBegin(GL_QUADS);
            while (i < to) {
                i += getCodePoint(text, to, i, pCodePoint);

                int cp = pCodePoint.get(0);
                if (cp == '\n') {
                    if (isLineBoundingBoxEnabled()) {
                        glEnd();
                        renderLineBoundingBox(lineStart, i - 1, y.get(0), scale);
                        glBegin(GL_QUADS);
                    }

                    y.put(0, y.get(0) + (ascent - descent + lineGap) * scale);
                    x.put(0, 0.0f);

                    lineStart = i;
                    continue;
                } else if (cp < 32 || 128 <= cp) {
                    continue;
                }

                stbtt_GetBakedQuad(cdata, BITMAP_W, BITMAP_H, cp - 32, x, y, q, true);
                if (isKerningEnabled() && i < to) {
                    getCodePoint(text, to, i, pCodePoint);
                    x.put(0, x.get(0) + stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0)) * scale);
                }

                glTexCoord2f(q.s0(), q.t0());
                glVertex2f(q.x0(), q.y0());

                glTexCoord2f(q.s1(), q.t0());
                glVertex2f(q.x1(), q.y0());

                glTexCoord2f(q.s1(), q.t1());
                glVertex2f(q.x1(), q.y1());

                glTexCoord2f(q.s0(), q.t1());
                glVertex2f(q.x0(), q.y1());
            }
            glEnd();
            if (isLineBoundingBoxEnabled()) {
                renderLineBoundingBox(lineStart, text.length(), y.get(0), scale);
            }
        }
    }

    private float getWindowHeight() {
        return windowHeight;
    }

    private void renderLineBoundingBox(int from, int to, float y, float scale) {
        glDisable(GL_TEXTURE_2D);
        glPolygonMode(GL_FRONT, GL_LINE);
        glColor(RGB.RED);

        float width = getStringWidth(info, text, from, to, getFontHeight());
        y -= descent * scale;

        glBegin(GL_QUADS);
        glVertex2f(0.0f, y);
        glVertex2f(width, y);
        glVertex2f(width, y - getFontHeight());
        glVertex2f(0.0f, y - getFontHeight());
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glPolygonMode(GL_FRONT, GL_FILL);
//        glColor3f(169f / 255f, 183f / 255f, 198f / 255f); // Text color
        glColor(RGB.WHITE);
    }

    private void glColor(RGB rgb) {
        glColor3f(rgb.red, rgb.green, rgb.blue);
    }

    private float getStringWidth(STBTTFontinfo info, String text, int from, int to, int fontHeight) {
        int width = 0;

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint       = stack.mallocInt(1);
            IntBuffer pAdvancedWidth   = stack.mallocInt(1);
            IntBuffer pLeftSideBearing = stack.mallocInt(1);

            int i = from;
            while (i < to) {
                i += getCodePoint(text, to, i, pCodePoint);
                int cp = pCodePoint.get(0);

                stbtt_GetCodepointHMetrics(info, cp, pAdvancedWidth, pLeftSideBearing);
                width += pAdvancedWidth.get(0);

                if (isKerningEnabled() && i < to) {
                    getCodePoint(text, to, i, pCodePoint);
                    width += stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0));
                }
            }
        }

        return width * stbtt_ScaleForPixelHeight(info, fontHeight);
    }

    private static int getCodePoint(String text, int to, int i, IntBuffer codepointOut) {
        char c1 = text.charAt(i);
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            char c2 = text.charAt(i + 1);
            if (Character.isLowSurrogate(c2)) {
                codepointOut.put(0, Character.toCodePoint(c1, c2));
                return 2;
            }
        }
        codepointOut.put(0, c1);
        return 1;
    }

    STBTTBakedChar.Buffer init(int BITMAP_W, int BITMAP_H) {
        int                   texID = glGenTextures();
        STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(96);

        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        stbtt_BakeFontBitmap(this.ttf, getFontHeight(), bitmap, BITMAP_W, BITMAP_H, 32, cdata);

        glBindTexture(GL_TEXTURE_2D, texID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

//        glClearColor(43f / 255f, 43f / 255f, 43f / 255f, 0f); // BG color
        glColor3f(169f / 255f, 183f / 255f, 198f / 255f); // Text color

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        return cdata;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public void setLineBoundingBoxEnabled(boolean lineBoundingBoxEnabled) {
        this.lineBoundingBoxEnabled = lineBoundingBoxEnabled;
    }

    public void setKerningEnabled(boolean kerningEnabled) {
        this.kerningEnabled = kerningEnabled;
    }

    public boolean getLineBoundingBoxEnabled() {
        return lineBoundingBoxEnabled;
    }

    public boolean getKerningEnabled() {
        return kerningEnabled;
    }
}
