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

/**
 *
 * @author kitfox
 */
public class WireCube
{
    int wirePointId;
    int wireIndexId;

    private FloatBuffer createWireframeVertices()
    {
        float[] faceVerts = new float[]{
            -1, -1, -1,
             1, -1, -1,
            -1,  1, -1,
             1,  1, -1,
            -1, -1,  1,
             1, -1,  1,
            -1,  1,  1,
             1,  1,  1,

        };

        FloatBuffer buf = BufferUtil.newFloatBuffer(faceVerts.length);
        buf.put(faceVerts);
        buf.rewind();
        return buf;
    }

    private IntBuffer createWireframeIndices()
    {
        int[] indices = new int[]{
            0, 1,
            0, 2,
            1, 3,
            2, 3,

            0, 4,
            1, 5,
            2, 6,
            3, 7,

            4, 5,
            4, 6,
            5, 7,
            6, 7,
        };

        IntBuffer buf = BufferUtil.newIntBuffer(indices.length);
        buf.put(indices);
        buf.rewind();
        return buf;
    }

    private void initVBO(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        IntBuffer ibuf = BufferUtil.newIntBuffer(2);

        gl.glGenBuffers(2, ibuf);
        wireIndexId = ibuf.get(0);
        wirePointId = ibuf.get(1);

        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, wirePointId);
            FloatBuffer arrayBuf = createWireframeVertices();
            gl.glBufferData(GL.GL_ARRAY_BUFFER, arrayBuf.limit() * BufferUtil.SIZEOF_FLOAT, arrayBuf, GL.GL_STATIC_DRAW);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }

        {
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, wireIndexId);
            IntBuffer arrayBuf = createWireframeIndices();
            gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, arrayBuf.limit() * BufferUtil.SIZEOF_INT, arrayBuf, GL.GL_STATIC_DRAW);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    public void render(GLAutoDrawable drawable)
    {
        if (wireIndexId == 0)
        {
            initVBO(drawable);
        }

        GL gl = drawable.getGL();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, wirePointId);
//        gl.glEnableVertexAttribArray(0);
//        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
//        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 3 * BufferUtil.SIZEOF_FLOAT, 0);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, wireIndexId);

        gl.glDrawElements(GL.GL_LINES, 24, GL.GL_UNSIGNED_INT, 0);

//        gl.glDisableVertexAttribArray(0);
//        gl.glVertexAttribPointer(0, 4, GL.GL_FLOAT, false, 0, 0);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        
    }

    public void dispose(GLAutoDrawable drawable)
    {
        if (wirePointId == 0)
        {
            return;
        }

        IntBuffer ibuf = BufferUtil.newIntBuffer(2);
        ibuf.put(0, wireIndexId);
        ibuf.put(1, wirePointId);

        GL gl = drawable.getGL();
        gl.glDeleteBuffers(2, ibuf);

        wireIndexId = wirePointId = 0;
    }
}
