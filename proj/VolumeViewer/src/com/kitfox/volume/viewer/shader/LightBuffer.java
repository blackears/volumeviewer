/*
 * Volume Viewer - Display and manipulate 3D volumetric data
 * Copyright © 2009, Mark McKay
 * http://www.kitfox.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kitfox.volume.viewer.shader;

import com.kitfox.volume.viewer.ViewerPanel;
import com.sun.opengl.util.BufferUtil;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Color3f;

/**
 *
 * @author kitfox
 */
public class LightBuffer
{
    private int frameBufAccumId;
    private int renderBufAccumId;

    private int frameBufTexId;
    private int texId;

    boolean dirty = true;

    static final int width = 512;
    static final int height = 512;

    private void checkFramebuffer(GL gl)
    {
        int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
        switch (status)
        {
            case GL.GL_FRAMEBUFFER_COMPLETE_EXT:
                break;
            case GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT:
                throw new RuntimeException("Framebuffer unsupported");
            default:
                throw new RuntimeException("Incomplete buffer " + status);
        }
    }

    private void init(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        
        IntBuffer ibuf = BufferUtil.newIntBuffer(2);

        gl.glGenFramebuffersEXT(2, ibuf);
        frameBufAccumId = ibuf.get(0);
        frameBufTexId = ibuf.get(1);

        gl.glGenTextures(1, ibuf);
        texId = ibuf.get(0);

        gl.glGenRenderbuffersEXT(1, ibuf);
        renderBufAccumId = ibuf.get(0);

        //Create shadow accumulation buffer
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufAccumId);
        gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, renderBufAccumId);
        gl.glRenderbufferStorageMultisampleEXT(GL.GL_RENDERBUFFER_EXT,
                4,
                GL.GL_RGBA,
                width, height);
        gl.glFramebufferRenderbufferEXT(
                GL.GL_FRAMEBUFFER_EXT,
                GL.GL_COLOR_ATTACHMENT0_EXT,
                GL.GL_RENDERBUFFER_EXT,
                renderBufAccumId);

        checkFramebuffer(gl);

        //Create render to texture buffer
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufTexId);

        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT, texId);
        gl.glTexImage2D(GL.GL_TEXTURE_RECTANGLE_EXT, 0, GL.GL_RGBA8,
                width, height,
                0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
                null);
//        gl.glGenerateMipmapEXT(GL.GL_TEXTURE_2D);
        gl.glFramebufferTexture2DEXT(
                GL.GL_FRAMEBUFFER_EXT,
                GL.GL_COLOR_ATTACHMENT0_EXT,
                GL.GL_TEXTURE_RECTANGLE_EXT, texId, 0);

        checkFramebuffer(gl);



        gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT, 0);
        gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, 0);
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);

    }

    public void bind(GLAutoDrawable drawable)
    {
        if (frameBufAccumId == 0)
        {
            init(drawable);
        }
        
        GL gl = drawable.getGL();

//        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufAccumId);

        gl.glPushAttrib(GL.GL_VIEWPORT_BIT);
        gl.glViewport(0, 0, width, height);

        dirty = true;
    }

    public void unbind(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();

        gl.glPopAttrib();
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
    }

    public void clear(GLAutoDrawable drawable, Color3f lightColor)
    {
        bind(drawable);

        GL gl = drawable.getGL();
//        gl.glClearColor(lightColor.x, lightColor.y, lightColor.z, 1);
        gl.glClearColor(lightColor.x, lightColor.y, lightColor.z, 0);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        unbind(drawable);
    }

    /**
     * @return the texId
     */
    public int getTexId()
    {
        return texId;
    }

    public void flushToTexture(GLAutoDrawable drawable)
    {
        if (!dirty)
        {
            return;
        }

        GL gl = drawable.getGL();
            dirty = false;

//            gl.glEnable(GL.GL_BLEND);
//            gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
            gl.glBindFramebufferEXT(GL.GL_READ_FRAMEBUFFER_EXT, frameBufAccumId);
            gl.glBindFramebufferEXT(GL.GL_DRAW_FRAMEBUFFER_EXT, frameBufTexId);
            gl.glBlitFramebufferEXT(0, 0, width, height,
                    0, 0, width, height,
                    GL.GL_COLOR_BUFFER_BIT, GL.GL_NEAREST);
//            gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
            gl.glBindFramebufferEXT(GL.GL_READ_FRAMEBUFFER_EXT, 0);
            gl.glBindFramebufferEXT(GL.GL_DRAW_FRAMEBUFFER_EXT, 0);
//            gl.glGenerateMipmapEXT(GL.GL_TEXTURE_2D);
    }

    public void bindLightTexture(GLAutoDrawable drawable)
    {
        if (frameBufTexId == 0)
        {
            init(drawable);
        }

        GL gl = drawable.getGL();

        flushToTexture(drawable);

        gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT, texId);
//        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
//        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
//        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
//        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);

    }

    public void unbindLightTexture(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT, 0);
    }

    public void dumpLightTexture(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();

        gl.glActiveTexture(GL.GL_TEXTURE0);
        bindLightTexture(drawable);

        byte[] buf = new byte[width * height * 4];
        ByteBuffer bb = BufferUtil.newByteBuffer(buf.length);
        gl.glGetTexImage(GL.GL_TEXTURE_RECTANGLE_EXT, 0, GL.GL_RGBA,
                GL.GL_UNSIGNED_BYTE, bb);
        bb.rewind();
        bb.get(buf);
        byte h = buf[0];

        bb.rewind();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int j = 0; j < height; ++j)
        {
            for (int i = 0; i < width; ++i)
            {
                int r = bb.get() & 0xff;
                int g = bb.get() & 0xff;
                int b = bb.get() & 0xff;
                int a = bb.get() & 0xff;

                int argb = (a << 24)
                        | (r << 16)
                        | (g << 8)
                        | (b << 0);
//                int argb = (255 << 24)
//                        | (a << 16)
//                        | (a << 8)
//                        | (a << 0);

                img.setRGB(i, height - 1 - j, argb);
            }
        }
        try {
            ImageIO.write(img, "png", new File("lightMap.png"));
        } catch (IOException ex) {
            Logger.getLogger(ViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        unbindLightTexture(drawable);
    }

    public static int getLightTextureWidth()
    {
        return width;
    }

    public static int getLightTextureHeight()
    {
        return height;
    }

}
