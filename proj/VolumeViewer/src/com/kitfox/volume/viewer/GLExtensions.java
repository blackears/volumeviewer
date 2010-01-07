/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kitfox.volume.viewer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

/**
 *
 * @author kitfox
 */
public class GLExtensions
{
    static GLExtensions instance;
    
    private final boolean shaderOk;
    private final boolean multisampleOk;
    private final boolean shadowLightOk;

    private GLExtensions(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        String ext = gl.glGetString(GL.GL_EXTENSIONS);

        multisampleOk = ext.contains("GL_ARB_multisample");
        shadowLightOk = ext.contains("GL_ARB_framebuffer_object")
                && ext.contains("GL_ARB_texture_rectangle");
        shaderOk = ext.contains("GL_ARB_vertex_program")
                && ext.contains("GL_ARB_vertex_shader")
                && ext.contains("GL_ARB_fragment_program")
                && ext.contains("GL_ARB_fragment_shader")
                && ext.contains("GL_ARB_multitexture");

        /*
         GL_ARB_multisample
         GL_ARB_framebuffer_object
         GL_ARB_texture_rectangle
         GL_ARB_vertex_buffer_object
         GL_ARB_vertex_program
         GL_ARB_fragment_program
         GL_ARB_fragment_shader
         GL_ARB_shader_objects
         GL_ARB_multitexture
         */
        
//       System.err.println("Gl version" + gl.glGetString(GL.GL_VERSION));
    }

    public static void update(GLAutoDrawable drawable)
    {
        instance = new GLExtensions(drawable);

    }

    public static GLExtensions inst()
    {
        return instance;
    }

    /**
     * @return the shaderOk
     */
    public boolean isShaderOk() {
        return shaderOk;
    }

    /**
     * @return the multisampleOk
     */
    public boolean isMultisampleOk() {
        return multisampleOk;
    }

    /**
     * @return the shadowLightOk
     */
    public boolean isShadowLightOk() {
        return shadowLightOk;
    }
}
