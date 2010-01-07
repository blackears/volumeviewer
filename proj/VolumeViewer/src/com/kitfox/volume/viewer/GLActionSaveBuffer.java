/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kitfox.volume.viewer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.opengl.GLAutoDrawable;

/**
 *
 * @author kitfox
 */
public class GLActionSaveBuffer implements GLAction
{
    final File file;
    final ViewerPanel viewer;

    public GLActionSaveBuffer(File file, ViewerPanel viewer)
    {
        this.file = file;
        this.viewer = viewer;
    }

    public void run(GLAutoDrawable drawable)
    {
        BufferedImage img = viewer.dumpBuffer(viewer);

        try {
            ImageIO.write(img, "png", file);
        } catch (IOException ex) {
            Logger.getLogger(ViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.err.println("Saved snapshot to " + file);
    }

}
