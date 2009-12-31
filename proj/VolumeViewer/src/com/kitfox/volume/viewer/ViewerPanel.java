/*
 * ViewerPanel.java
 * Created on Dec 23, 2009, 12:17:55 PM
 *
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

package com.kitfox.volume.viewer;

import com.sun.opengl.util.BufferUtil;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author kitfox
 */
public class ViewerPanel extends GLCanvas
        implements GLEventListener, PropertyChangeListener, DataChangeListener
{
    private static final long serialVersionUID = 0;
    
    private float maxUserRadius = 10;
    private float minUserRadius = 3;
    private float cameraAngularVel = 1f; //Degress per pixel
    private float cameraZoomVel = .4f;
    private float fovY = 30;
    private float aspect = 1;
    private float nearPlane = .25f;
    private float farPlane = 1000;

    private ViewerCube cube;

    int volumeFrameBufId;
    int volumeTexId;
    int volumeRenderBufId;

    /** Creates new form ViewerPanel */
    public ViewerPanel()
    {
        super(getCapabilities());
//        setOpaque(false);

        addGLEventListener(this);

        initComponents();
    }

    private static GLCapabilities getCapabilities()
    {
        GLCapabilities cap = new GLCapabilities();

        cap.setAlphaBits(8);
        return cap;
    }

    public void init(GLAutoDrawable drawable)
    {
//        GLCapabilities cap = getCapabilities();

        //Debug
        drawable.setGL(new DebugGL(drawable.getGL()));

        GL gl = drawable.getGL();
        System.err.println("Initializing GL Thread");
        System.err.println(gl.glGetString(GL.GL_EXTENSIONS));
        System.err.println("Gl version" + gl.glGetString(GL.GL_VERSION));

        // Enable VSync
        gl.setSwapInterval(1);

        gl.glShadeModel(GL.GL_FLAT);

        {
            IntBuffer ibuf = BufferUtil.newIntBuffer(1);

            gl.glGenFramebuffersEXT(1, ibuf);
            volumeFrameBufId = ibuf.get(0);

            gl.glGenRenderbuffersEXT(1, ibuf);
            volumeRenderBufId = ibuf.get(0);

            gl.glGenTextures(1, ibuf);
            volumeTexId = ibuf.get(0);
        }
    }

    float[] colArr = new float[3];

    public void display(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();

        setViewerCamera(gl, viewWidth, viewHeight);

        {
            IntBuffer ibuf = BufferUtil.newIntBuffer(1);
            gl.glGetIntegerv(GL.GL_ALPHA_BITS, ibuf);
            int alphaBits = ibuf.get(0);
            alphaBits += 0;
        }

        {
            //Clear
            Color bg = getBackground();
            bg.getColorComponents(colArr);
    //        gl.glClearColor(colArr[0], colArr[1], colArr[2], 0.0f);
            gl.glClearColor(0, 0, 0, 0.0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
//            gl.glClear(GL.GL_COLOR_BUFFER_BIT);

            //Draw view cube
            ViewerCube curCube = cube;
            if (curCube != null)
            {
                curCube.render(drawable);
            }
        }

    }

    public void display_(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();

        setViewerCamera(gl, viewWidth, viewHeight);

        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, volumeFrameBufId);
        gl.glViewport(0, 0, viewWidth, viewHeight);
//        gl.glPushAttrib(GL.GL_VIEWPORT_BIT);
//        gl.glViewport(0, 0, getWidth(), getHeight());

//        setHUDCamera(gl, viewWidth, viewHeight);
//        gl.glLoadIdentity();

        {
            //Clear
            Color bg = getBackground();
            bg.getColorComponents(colArr);
    //        gl.glClearColor(colArr[0], colArr[1], colArr[2], 0.0f);
            gl.glClearColor(0, 1, 0, 0.0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
//            gl.glClear(GL.GL_COLOR_BUFFER_BIT);

            //Draw view cube
//            {
//                gl.glColor3f(1, 0, 1);
//
//                GLU glu = new GLU();
//                gl.glLoadIdentity();
//                glu.gluLookAt(
//                        0, 0, -7,
//                        0, 0, 0,
//                        0, 1, 0);
//
//                GLUT glut = new GLUT();
////                glut.glutWireTeapot(1);
//                glut.glutSolidTeapot(1);
//            }
            ViewerCube curCube = cube;
            if (curCube != null)
            {
                curCube.render(drawable);
            }
//            gl.glBegin(GL.GL_QUADS);
//            {
//                gl.glTexCoord2f(0, 0); gl.glVertex2f(-1.9f, -1.9f);
//                gl.glTexCoord2f(1, 0); gl.glVertex2f(5.9f, -1.9f);
//                gl.glTexCoord2f(1, 1); gl.glVertex2f(5.9f, 11.9f);
//                gl.glTexCoord2f(0, 1); gl.glVertex2f(-1.9f, 11.9f);
//            }
//            gl.glEnd();
        }

//        gl.glPopAttrib();
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);


        setHUDCamera(gl, viewWidth, viewHeight);
        gl.glLoadIdentity();

        gl.glEnable(GL.GL_TEXTURE_RECTANGLE_EXT);
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
        gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT, volumeTexId);

        {
            byte[] buf = new byte[viewWidth * viewHeight * 4];
            ByteBuffer bb = BufferUtil.newByteBuffer(buf.length);
            gl.glGetTexImage(GL.GL_TEXTURE_RECTANGLE_EXT, 0, GL.GL_RGBA,
                    GL.GL_UNSIGNED_BYTE, bb);
            bb.rewind();
            bb.get(buf);
            byte h = buf[0];

            bb.rewind();
            BufferedImage img = new BufferedImage(viewWidth, viewHeight, BufferedImage.TYPE_INT_ARGB);
            for (int j = 0; j < viewHeight; ++j)
            {
                for (int i = 0; i < viewWidth; ++i)
                {
                    int argb = ((bb.get() * 0xff) << 0)
                            | ((bb.get() * 0xff) << 24)
                            | ((bb.get() * 0xff) << 16)
                            | ((bb.get() * 0xff) << 8);

                    img.setRGB(i, j, argb);
                }
            }
            try {
                ImageIO.write(img, "png", new File("frame.png"));
            } catch (IOException ex) {
                Logger.getLogger(ViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

            gl.glClearColor(0, 0, 0, 1f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

//        gl.glColor3f(1, 0, 0);
        gl.glBegin(GL.GL_QUADS);
        {
            gl.glTexCoord2f(0, 0); gl.glVertex2f(-.9f, -.9f);
            gl.glTexCoord2f(viewWidth, 0); gl.glVertex2f(1, -1);
            gl.glTexCoord2f(viewWidth, viewHeight); gl.glVertex2f(.9f, .9f);
            gl.glTexCoord2f(0, viewHeight); gl.glVertex2f(-1, 1);
//            gl.glTexCoord2f(1, 0); gl.glVertex2f(1, -1);
//            gl.glTexCoord2f(1, 1); gl.glVertex2f(.9f, .9f);
//            gl.glTexCoord2f(0, 1); gl.glVertex2f(-1, 1);

        }
        gl.glEnd();
        gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT, 0);


//        System.err.println("display");
    }

    int viewWidth;
    int viewHeight;

    private void setViewerCamera(GL gl, int width, int height)
    {
        GLU glu = new GLU();

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        if (height <= 0)
        {
            height = 1;
        }
        final float scrnAspect = (float)width / height;
        glu.gluPerspective(fovY, scrnAspect * aspect, nearPlane, farPlane);


        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    private void setHUDCamera(GL gl, int width, int height)
    {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        GL gl = drawable.getGL();

        viewWidth = width;
        viewHeight = height;

        gl.glViewport(x, y, width, height);

//        System.err.println("reshape");

        {
            //Create render to texture buffer
            gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, volumeFrameBufId);

            gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT, volumeTexId);
            gl.glTexImage2D(GL.GL_TEXTURE_RECTANGLE_EXT, 0, GL.GL_RGBA8,
                    width, height,
                    0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
                    null);
            gl.glFramebufferTexture2DEXT(
                    GL.GL_FRAMEBUFFER_EXT,
                    GL.GL_COLOR_ATTACHMENT0_EXT,
                    GL.GL_TEXTURE_RECTANGLE_EXT, volumeTexId, 0);


            gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, volumeRenderBufId);
            gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT,
                    GL.GL_DEPTH_COMPONENT,
                    width, height);
            gl.glFramebufferRenderbufferEXT(
                    GL.GL_FRAMEBUFFER_EXT,
                    GL.GL_DEPTH_ATTACHMENT_EXT,
                    GL.GL_RENDERBUFFER_EXT,
                    volumeRenderBufId);

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


            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
            gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, 0);
            gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
        }
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
    {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                formMouseWheelMoved(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    MouseEvent oldMouse;
    float startYaw;
    float startPitch;
    //cube

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        if (cube == null)
        {
            return;
        }

        oldMouse = evt;
        startYaw = cube.getViewerYaw();
        startPitch = cube.getViewerPitch();
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        
    }//GEN-LAST:event_formMouseReleased

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        if (cube == null)
        {
            return;
        }

        int dx = evt.getX() - oldMouse.getX();
        int dy = evt.getY() - oldMouse.getY();

        float newYaw = startYaw + dx * cameraAngularVel;
        float newPitch = startPitch - dy;
        newPitch = Math.min(Math.max(newPitch, -85), 85);

        cube.setViewerYaw(newYaw);
        cube.setViewerPitch(newPitch);
        repaint();
    }//GEN-LAST:event_formMouseDragged

    private void formMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_formMouseWheelMoved
        if (cube == null)
        {
            return;
        }

        float newRad = cube.getViewerRadius() + evt.getWheelRotation() * cameraZoomVel;
        newRad = Math.max(Math.min(newRad, maxUserRadius), minUserRadius);

        cube.setViewerRadius(newRad);
        repaint();
    }//GEN-LAST:event_formMouseWheelMoved

    /**
     * @return the maxUserRadius
     */
    public float getMaxUserRadius() {
        return maxUserRadius;
    }

    /**
     * @param maxUserRadius the maxUserRadius to set
     */
    public void setMaxUserRadius(float maxUserRadius) {
        this.maxUserRadius = maxUserRadius;
    }

    /**
     * @return the minUserRadius
     */
    public float getMinUserRadius() {
        return minUserRadius;
    }

    /**
     * @param minUserRadius the minUserRadius to set
     */
    public void setMinUserRadius(float minUserRadius) {
        this.minUserRadius = minUserRadius;
    }

    /**
     * @return the cameraAngularVel
     */
    public float getCameraAngularVel() {
        return cameraAngularVel;
    }

    /**
     * @param cameraAngularVel the cameraAngularVel to set
     */
    public void setCameraAngularVel(float cameraAngularVel) {
        this.cameraAngularVel = cameraAngularVel;
    }

    /**
     * @return the fovY
     */
    public float getFovY() {
        return fovY;
    }

    /**
     * @param fovY the fovY to set
     */
    public void setFovY(float fovY) {
        this.fovY = fovY;
    }

    /**
     * @return the aspect
     */
    public float getAspect() {
        return aspect;
    }

    /**
     * @param aspect the aspect to set
     */
    public void setAspect(float aspect) {
        this.aspect = aspect;
    }

    /**
     * @return the nearPlane
     */
    public float getNearPlane() {
        return nearPlane;
    }

    /**
     * @param nearPlane the nearPlane to set
     */
    public void setNearPlane(float nearPlane) {
        this.nearPlane = nearPlane;
    }

    /**
     * @return the farPlane
     */
    public float getFarPlane() {
        return farPlane;
    }

    /**
     * @param farPlane the farPlane to set
     */
    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
    }

    /**
     * Degress per pixel
     * @return the cameraZoomVel
     */
    public float getCameraZoomVel() {
        return cameraZoomVel;
    }

    /**
     * @param cameraZoomVel the cameraZoomVel to set
     */
    public void setCameraZoomVel(float cameraZoomVel) {
        this.cameraZoomVel = cameraZoomVel;
    }

    /**
     * @return the cube
     */
    public ViewerCube getCube()
    {
        return cube;
    }

    /**
     * @param cube the cube to set
     */
    public void setCube(ViewerCube cube)
    {
        if (this.cube != null)
        {
            this.cube.removePropertyChangeListener(this);
            this.cube.removeDataChangeListener(this);
        }
        this.cube = cube;
        if (this.cube != null)
        {
            this.cube.addPropertyChangeListener(this);
            this.cube.addDataChangeListener(this);
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        repaint();
    }

    public void dataChanged(ChangeEvent evt) {
        repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
