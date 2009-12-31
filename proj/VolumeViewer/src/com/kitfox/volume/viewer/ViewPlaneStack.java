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

import com.sun.opengl.util.BufferUtil;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 *
 * @author kitfox
 */
public class ViewPlaneStack
{
    private int numPlanes = 5;
    private Vector3f normal = new Vector3f(1, 0, 0);
    private Vector3f boxRadius = new Vector3f(1, 1, 1);
    boolean dirty = true;

    PolygonSet polySet = new PolygonSet();

    int numVerts;
    int[] polyVertCounts;
    int[] polyOffset;

    int planesPointId;


    private void layout()
    {
        polySet.clear();
        numVerts = 0;
        polyVertCounts = new int[numPlanes];
        polyOffset = new int[numPlanes];

        float radius = boxRadius.length();
        int numDiv = numPlanes + 1;
        float distBtwPlanes = 2 * radius / numDiv;

        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f right = new Vector3f(1, 0, 0);
        Vector3f center = new Vector3f();
        Vector3f tangent = new Vector3f();
        Vector3f binormal = new Vector3f();

        Point3f[] points = new Point3f[4];
        for (int i = 0; i < numPlanes; ++i)
        {
            float d = -radius + (i + 1) * distBtwPlanes;

            center.set(normal);
            center.scale(d);

            if (Math.abs(normal.x) < Math.abs(normal.y))
            {
                tangent.cross(normal, right);
            }
            else
            {
                tangent.cross(normal, up);
            }
            tangent.normalize();
            binormal.cross(normal, tangent);

            tangent.scale(radius);
            binormal.scale(radius);

            //Create square within which the radius of the box could be
            // inscribed
            points[0] = new Point3f(center);
            points[0].sub(tangent);
            points[0].sub(binormal);

            points[1] = new Point3f(center);
            points[1].add(tangent);
            points[1].sub(binormal);

            points[2] = new Point3f(center);
            points[2].add(tangent);
            points[2].add(binormal);

            points[3] = new Point3f(center);
            points[3].sub(tangent);
            points[3].add(binormal);

            //Create polygon
            Polygon poly = new Polygon(points);
            polySet.add(poly);
        }

        //Clip to sides of cube
        polySet.clip(new Vector4f(1, 0, 0, boxRadius.x));
        polySet.clip(new Vector4f(-1, 0, 0, boxRadius.x));
        polySet.clip(new Vector4f(0, 1, 0, boxRadius.y));
        polySet.clip(new Vector4f(0, -1, 0, boxRadius.y));
        polySet.clip(new Vector4f(0, 0, 1, boxRadius.z));
        polySet.clip(new Vector4f(0, 0, -1, boxRadius.z));
    }

    private void buildPlanes(GL gl)
    {
        layout();

        numVerts = 0;
        polyVertCounts = new int[polySet.size()];
        for (int i = 0; i < polySet.size(); ++i)
        {
            Polygon poly = polySet.get(i);

            int size = poly.getNumVerts();
            polyVertCounts[i] = size;
            polyOffset[i] = numVerts;
            numVerts += size;
        }

        //Allocate buffers
        if (planesPointId == 0)
        {
            IntBuffer ibuf = BufferUtil.newIntBuffer(1);

            gl.glGenBuffers(1, ibuf);
            planesPointId = ibuf.get(0);
        }

        //Upload buffer
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, planesPointId);

            //Build fans
            FloatBuffer arrayBuf = BufferUtil.newFloatBuffer(numVerts * 6);
            for (int i = 0; i < polySet.size(); ++i)
            {
                Polygon poly = polySet.get(i);
//                poly.appendFan(arrayBuf);
                poly.appendFanWithTexCoord(arrayBuf, boxRadius);
            }
            arrayBuf.rewind();

            gl.glBufferData(GL.GL_ARRAY_BUFFER, arrayBuf.limit() * BufferUtil.SIZEOF_FLOAT, arrayBuf, GL.GL_DYNAMIC_DRAW);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }


        dirty = false;
    }

    public void dispose(GLAutoDrawable drawable)
    {
        if (planesPointId == 0)
        {
            return;
        }

        IntBuffer ibuf = BufferUtil.newIntBuffer(1);
        ibuf.put(0, planesPointId);

        GL gl = drawable.getGL();
        gl.glDeleteBuffers(1, ibuf);

        planesPointId = 0;
    }

    public void render(GLAutoDrawable drawable, boolean frontToBack)
    {
        GL gl = drawable.getGL();

        if (dirty)
        {
            buildPlanes(gl);
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, planesPointId);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL.GL_FLOAT, BufferUtil.SIZEOF_FLOAT * 6, 0);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glTexCoordPointer(3, GL.GL_FLOAT, BufferUtil.SIZEOF_FLOAT * 6, BufferUtil.SIZEOF_FLOAT * 3);

//        int offset = 0;
        //Slices are arranged from furthest from light to closest
        for (int i = 0; i < polyVertCounts.length; ++i)
        {
//            int idx = frontToBack ? polyVertCounts.length - 1 - i : i;
            int idx = polyVertCounts.length - 1 - i;
//            int idx = i;
//            int count = polyVertCounts[frontToBack ? i : polyVertCounts.length - 1 - i];
            int count = polyVertCounts[idx];
            int offset = polyOffset[idx];
            gl.glDrawArrays(GL.GL_TRIANGLE_FAN, offset, count);
//            offset += count;
        }

        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }

    private void renderWireframe(GLAutoDrawable drawable)
    {
        //For debugguing
        GL gl = drawable.getGL();

        if (dirty)
        {
            buildPlanes(gl);
        }
        
        for (int i = 0; i < polySet.size(); ++i)
        {
            Polygon poly = polySet.get(i);

            int size = poly.verts.size();
            for (int j = 0; j < size; ++j)
            {
                Point3f p0 = poly.verts.get(j);
                Point3f p1 = poly.verts.get(j == size - 1 ? 0 : j + 1);

                gl.glBegin(GL.GL_LINES);
                {
                    gl.glVertex3f(p0.x, p0.y, p0.z);
                    gl.glVertex3f(p1.x, p1.y, p1.z);
                }
                gl.glEnd();
            }
        }
    }

    /**
     * @return the numPlanes
     */
    public int getNumPlanes() {
        return numPlanes;
    }

    /**
     * @param numPlanes the numPlanes to set
     */
    public void setNumPlanes(int numPlanes) {
        if (this.numPlanes == numPlanes)
        {
            return;
        }
        this.numPlanes = numPlanes;
        dirty = true;
    }

    /**
     * @return the normal
     */
    public Vector3f getNormal() {
        return new Vector3f(normal);
    }

    /**
     * @param normal the normal to set
     */
    public void setNormal(Vector3f normal) {
        if (this.normal.equals(normal))
        {
            return;
        }
        this.normal.set(normal);
        dirty = true;
    }

    /**
     * @return the boxRadius
     */
    public Vector3f getBoxRadius() {
        return new Vector3f(boxRadius);
    }

    /**
     * @param boxRadius the boxRadius to set
     */
    public void setBoxRadius(Vector3f boxRadius) {
        if (this.boxRadius.equals(boxRadius))
        {
            return;
        }
        this.boxRadius.set(boxRadius);
        dirty = true;
    }

}
