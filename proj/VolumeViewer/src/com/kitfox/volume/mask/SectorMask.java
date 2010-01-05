/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kitfox.volume.mask;

import static com.kitfox.volume.JAXBHelper.*;

import com.kitfox.xml.schema.volumeviewer.cubestate.SectorMaskType;
import com.sun.opengl.util.BufferUtil;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Vector3f;

/**
 *
 * @author kitfox
 */
public class SectorMask
{
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public static final int Q000 = 1 << 0;
    public static final int Q100 = 1 << 1;
    public static final int Q010 = 1 << 2;
    public static final int Q110 = 1 << 3;
    public static final int Q001 = 1 << 4;
    public static final int Q101 = 1 << 5;
    public static final int Q011 = 1 << 6;
    public static final int Q111 = 1 << 7;

    private int texIdMask;
    boolean dirty = true;

    protected int mask = 0xff;
    public static final String PROP_MASK = "mask";
    protected Vector3f center = new Vector3f(.5f, .5f, .5f);
    public static final String PROP_CENTER = "center";

    public void load(SectorMaskType target)
    {
        setCenter(asVec3f(target.getCenter()));
        setMask(target.getMask());
    }

    public SectorMaskType save()
    {
        SectorMaskType target = new SectorMaskType();

        target.setCenter(asVectorType(center));
        target.setMask(mask);

        return target;
    }

    private void initTexture(GL gl)
    {
        IntBuffer ibuf = BufferUtil.newIntBuffer(1);
        gl.glGenTextures(1, ibuf);
        texIdMask = ibuf.get(0);
    }

    private void loadTexture(GL gl)
    {
        if (texIdMask == 0)
        {
            initTexture(gl);
        }

        //Build mask data
        ByteBuffer data = BufferUtil.newByteBuffer(8 * 4);
        for (int i = 0; i < 8; ++i)
        {
            data.put((byte)0);
            data.put((byte)0);
            data.put((byte)0);
            data.put((byte)((mask & (1 << i)) == 0 ? 0 : 0xff));
        }
        data.rewind();

        //Upload
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);

        gl.glBindTexture(GL.GL_TEXTURE_3D, texIdMask);
        gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_R, GL.GL_CLAMP);
        gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
        gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
        gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);

        gl.glTexImage3D(GL.GL_TEXTURE_3D, 0, GL.GL_RGBA,
                2, 2, 2,
                0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
                data);

        dirty = false;
    }

    public void bindTexture(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();

        if (dirty)
        {
            loadTexture(gl);
        }

        gl.glBindTexture(GL.GL_TEXTURE_3D, texIdMask);
    }

    public void unbindTexture(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        gl.glBindTexture(GL.GL_TEXTURE_3D, 0);
    }

    /**
     * Get the value of center
     *
     * @return the value of center
     */
    public Vector3f getCenter() {
        return new Vector3f(center);
    }

    /**
     * Set the value of center
     *
     * @param center new value of center
     */
    public void setCenter(Vector3f center) {
        Vector3f oldCenter = new Vector3f(this.center);
        this.center.set(center);
        propertyChangeSupport.firePropertyChange(PROP_CENTER, oldCenter, center);
    }

    /**
     * Get the value of mask
     *
     * @return the value of mask
     */
    public int getMask() {
        return mask;
    }

    /**
     * Set the value of mask
     *
     * @param mask new value of mask
     */
    public void setMask(int mask) {
        int oldMask = this.mask;
        this.mask = mask;
        dirty  = true;
        propertyChangeSupport.firePropertyChange(PROP_MASK, oldMask, mask);
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
