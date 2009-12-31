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

package com.kitfox.volume.viewer;

import com.kitfox.volume.VolumeRenderable;
import com.kitfox.volume.viewer.shader.VolumeShader;
import com.kitfox.volume.viewer.shader.VolumeShader.LightingStyle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.swing.event.ChangeEvent;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author kitfox
 */
public class ViewerCube implements DataChangeListener
{
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private float viewerRadius = 7;
    private float viewerYaw = 270;
    private float viewerPitch = 0;
    private VolumeRenderable renderable;
    private Vector3f volumeRadius;
    public static final String PROP_VOLUMERADIUS = "volumeRadius";
    //Opacity of slices should be adjusted so that they have a combined
    // opacity equivilant to the opacity of this number of slices with
    // no adjustment
    protected float opacityReference = 5;
    public static final String PROP_OPACITYREFERENCE = "opacityReference";

    protected Vector3f lightDir;
    public static final String PROP_LIGHTDIR = "lightDir";
    protected Color3f lightColor = new Color3f(1, 1, 1);
    public static final String PROP_LIGHTCOLOR = "lightColor";
    public static final String PROP_NUMPLANES = "numPlanes";

    protected boolean followViewer = true;
    public static final String PROP_FOLLOWVIEWER = "followViewer";
    protected LightingStyle lightingStyle = LightingStyle.NONE;
    public static final String PROP_LIGHTINGSTYLE = "lightingStyle";


    protected VolumeData data;
    public static final String PROP_DATA = "data";

    boolean frontToBack;
    private Point3f viewerPos;
    private Vector3f viewerDir;
    Vector3f planeNormal;
    final WireCube wire = new WireCube();
    final ViewPlaneStack planes = new ViewPlaneStack();
    final VolumeShader shader = new VolumeShader();

    ArrayList<DataChangeListener> listeners = new ArrayList<DataChangeListener>();



    public ViewerCube()
    {
        lightDir = new Vector3f(.5f, .5f, -1);
        lightDir.normalize();
        volumeRadius = new Vector3f(1, 1, 1);

    }

    public void addDataChangeListener(DataChangeListener l)
    {
        listeners.add(l);
    }

    public void removeDataChangeListener(DataChangeListener l)
    {
        listeners.remove(l);
    }

    protected void fireRefresh()
    {
        ChangeEvent evt = new ChangeEvent(this);
        for (int i = 0; i < listeners.size(); ++i)
        {
            listeners.get(i).dataChanged(evt);
        }
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void clearCache()
    {
        viewerPos = null;
        viewerDir = null;
        planeNormal = null;
    }

    private void buildCache()
    {
        //Yaw transform
        viewerPos = new Point3f(
                (float)Math.sin(Math.toRadians(viewerYaw)),
                0,
                (float)Math.cos(Math.toRadians(viewerYaw)));

        //Add pitch
        AxisAngle4f axis = new AxisAngle4f(
                -viewerPos.z, 0, viewerPos.x, (float)Math.toRadians(viewerPitch));
        Matrix4f rot = new Matrix4f();
        rot.set(axis);
        rot.transform(viewerPos);

        //Set back by radius
        viewerPos.scale(viewerRadius);
        viewerDir = new Vector3f(viewerPos);
        viewerDir.normalize();

        //Determine plane direction
        planeNormal = new Vector3f(lightDir);

        if (followViewer)
        {
            Vector3f viewerDir = new Vector3f(getViewerPos());
            viewerDir.normalize();

            if (viewerDir.dot(lightDir) > 0)
            {
                //Light behind viewer
                planeNormal.add(viewerDir);
                planeNormal.normalize();
                frontToBack = true;
            }
            else
            {
                //Light in front of viewer
                planeNormal.sub(viewerDir);
                planeNormal.normalize();
                frontToBack = false;
            }
//planeNormal.set(viewerDir);
        }
    }

    private Vector3f getPlaneNormal()
    {
        if (planeNormal == null)
        {
            buildCache();
        }
        return planeNormal;
    }

    private Point3f getViewerPos()
    {
        if (viewerPos == null)
        {
            buildCache();
        }
        return viewerPos;
    }

    private Vector3f getViewerDir()
    {
        if (viewerDir == null)
        {
            buildCache();
        }
        return viewerDir;
    }

    public void render(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        GLU glu = new GLU();

        gl.glShadeModel(GL.GL_FLAT);
//        gl.glEnable(GL.GL_DEPTH_TEST);

        Point3f pos = getViewerPos();

        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluLookAt(
                pos.x, pos.y, pos.z,
                0, 0, 0,
                0, 1, 0);

        //Draw bounds
        gl.glColor3f(1, .5f, 1);
        wire.render(drawable);

        if (data != null)
        {

//            {
//                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
//                planes.setBoxRadius(volumeRadius);
//                planes.setNormal(getPlaneNormal());
//                planes.render(drawable, frontToBack);
//                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
//            }

//        if (true)
//        {
//            gl.glPopMatrix();
//            return;
//        }

            gl.glActiveTexture(GL.GL_TEXTURE0);
            data.bindTexture3d(drawable);
            shader.setTexVolumeId(0);

            gl.glActiveTexture(GL.GL_TEXTURE1);
            data.bindTextureXfer(drawable);
            shader.setTexXferId(1);

            shader.setLightingStyle(lightingStyle);
            shader.setLightColor(lightColor);
            shader.setLightDir(lightDir);
            shader.setViewDir(getViewerDir());
//            shader.setOpacityCorrect(1 - (float)Math.pow(1 - .9f, 100f / planes.getNumPlanes()));
//            shader.setOpacityCorrect(1f / planes.getNumPlanes());
            shader.setOpacityCorrect(opacityReference / planes.getNumPlanes());

            shader.bind(drawable, frontToBack);

            planes.setBoxRadius(volumeRadius);
            planes.setNormal(getPlaneNormal());
            planes.render(drawable, frontToBack);
System.err.println("Ftb: " + frontToBack);

            gl.glActiveTexture(GL.GL_TEXTURE0);
            shader.unbind(drawable);
        }

        gl.glPopMatrix();
    }

    /**
     * @return the renderable
     */
    public VolumeRenderable getRenderable() {
        return renderable;
    }

    /**
     * @param renderable the renderable to set
     */
    public void setRenderable(VolumeRenderable renderable) {
        this.renderable = renderable;
    }

    /**
     * @return the volumeRadius
     */
    public Vector3f getVolumeRadius()
    {
        return new Vector3f(volumeRadius);
    }

    /**
     * @param volumeRadius the volumeRadius to set
     */
    public void setVolumeRadius(Vector3f volumeRadius)
    {
        Vector3f oldVolumeRadius = new Vector3f(this.volumeRadius);
        this.volumeRadius.set(volumeRadius);
        clearCache();
        propertyChangeSupport.firePropertyChange(PROP_VOLUMERADIUS, oldVolumeRadius, volumeRadius);
    }

    /**
     * @return the viewerRadius
     */
    public float getViewerRadius() {
        return viewerRadius;
    }

    /**
     * @param viewerRadius the viewerRadius to set
     */
    public void setViewerRadius(float viewerRadius) {
        this.viewerRadius = viewerRadius;
        clearCache();
    }

    /**
     * @return the viewerYaw
     */
    public float getViewerYaw() {
        return viewerYaw;
    }

    /**
     * @param viewerYaw the viewerYaw to set
     */
    public void setViewerYaw(float viewerYaw) {
        this.viewerYaw = viewerYaw;
        clearCache();
    }

    /**
     * @return the viewerPitch
     */
    public float getViewerPitch() {
        return viewerPitch;
    }

    /**
     * @param viewerPitch the viewerPitch to set
     */
    public void setViewerPitch(float viewerPitch) {
        this.viewerPitch = viewerPitch;
        clearCache();
    }

    /**
     * Get the value of lightColor
     *
     * @return the value of lightColor
     */
    public Color3f getLightColor()
    {
        return new Color3f(lightColor);
    }

    /**
     * Set the value of lightColor
     *
     * @param lightColor new value of lightColor
     */
    public void setLightColor(Color3f lightColor)
    {
        Color3f oldLightColor = new Color3f(this.lightColor);
        this.lightColor.set(lightColor);
        propertyChangeSupport.firePropertyChange(PROP_LIGHTCOLOR, oldLightColor, lightColor);
    }

    /**
     * Get the value of lightDir
     *
     * @return the value of lightDir
     */
    public Vector3f getLightDir() {
        return new Vector3f(lightDir);
    }

    /**
     * Set the value of lightDir
     *
     * @param lightDir new value of lightDir
     */
    public void setLightDir(Vector3f lightDir) {
        Vector3f oldLightDir = new Vector3f(this.lightDir);
        this.lightDir.set(lightDir);
        clearCache();
        propertyChangeSupport.firePropertyChange(PROP_LIGHTDIR, oldLightDir, lightDir);
    }

    /**
     * Get the value of data
     *
     * @return the value of data
     */
    public VolumeData getData() {
        return data;
    }

    /**
     * Set the value of data
     *
     * @param data new value of data
     */
    public void setData(VolumeData data) {
        if (this.data != null)
        {
            this.data.removeDataChangeListener(this);
        }
        VolumeData oldData = this.data;
        this.data = data;
        if (this.data != null)
        {
            this.data.addDataChangeListener(this);
        }
        propertyChangeSupport.firePropertyChange(PROP_DATA, oldData, data);
    }

    public void setNumPlanes(int numPlanes)
    {
        int oldNumPlanes = planes.getNumPlanes();
        planes.setNumPlanes(numPlanes);
        propertyChangeSupport.firePropertyChange(PROP_LIGHTDIR, oldNumPlanes, numPlanes);
    }

    public int getNumPlanes()
    {
        return planes.getNumPlanes();
    }

    public void dataChanged(ChangeEvent evt)
    {
        fireRefresh();
    }

    /**
     * Get the value of followViewer
     *
     * @return the value of followViewer
     */
    public boolean isFollowViewer() {
        return followViewer;
    }

    /**
     * Set the value of followViewer
     *
     * @param followViewer new value of followViewer
     */
    public void setFollowViewer(boolean followViewer) {
        boolean oldFollowViewer = this.followViewer;
        this.followViewer = followViewer;
        clearCache();
        propertyChangeSupport.firePropertyChange(PROP_FOLLOWVIEWER, oldFollowViewer, followViewer);
    }

    /**
     * Get the value of lightingStyle
     *
     * @return the value of lightingStyle
     */
    public LightingStyle getLightingStyle() {
        return lightingStyle;
    }

    /**
     * Set the value of lightingStyle
     *
     * @param lightingStyle new value of lightingStyle
     */
    public void setLightingStyle(LightingStyle lightingStyle) {
        LightingStyle oldLightingStyle = this.lightingStyle;
        this.lightingStyle = lightingStyle;
        propertyChangeSupport.firePropertyChange(PROP_LIGHTINGSTYLE, oldLightingStyle, lightingStyle);
    }

    /**
     * Get the value of opacityReference
     *
     * @return the value of opacityReference
     */
    public float getOpacityReference() {
        return opacityReference;
    }

    /**
     * Set the value of opacityReference
     *
     * @param opacityReference new value of opacityReference
     */
    public void setOpacityReference(float opacityReference) {
        float oldOpacityReference = this.opacityReference;
        this.opacityReference = opacityReference;
        propertyChangeSupport.firePropertyChange(PROP_OPACITYREFERENCE, oldOpacityReference, opacityReference);
    }

}
