/*
 * LightCtrlPanel.java
 * Created on Dec 24, 2009, 11:59:33 AM
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

package com.kitfox.volume.light;

import com.kitfox.volume.ColorChipPanel;
import com.kitfox.volume.viewer.ViewerCube;
import com.kitfox.volume.viewer.shader.VolumeShader.LightingStyle;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author kitfox
 */
public class LightCtrlPanel extends javax.swing.JPanel
        implements PropertyChangeListener
{
    private static final long serialVersionUID = 0;

    LightDirPanel posZPanel = new LightDirPanel();
    LightDirPanel negZPanel = new LightDirPanel();
    ColorChipPanel colorPanel = new ColorChipPanel();

    private ViewerCube cube;

//    protected Color3f chipColor = new Color3f(1, 1, 1);
//    public static final String PROP_CHIPCOLOR = "chipColor";
//    protected Vector3f lightDir = new Vector3f(0, 0, -1);
//    public static final String PROP_LIGHTDIR = "lightDir";

    /** Creates new form LightCtrlPanel */
    public LightCtrlPanel()
    {
        initComponents();

        panel_back.add(negZPanel, BorderLayout.CENTER);
        panel_front.add(posZPanel, BorderLayout.CENTER);
        panel_color.add(colorPanel, BorderLayout.CENTER);

        posZPanel.setPositiveZ(true);

        negZPanel.addPropertyChangeListener(this);
        posZPanel.addPropertyChangeListener(this);
        colorPanel.addPropertyChangeListener(this);
    }

    public void updateFromCube()
    {
        if (cube == null)
        {
            return;
        }

        negZPanel.setLightDir(cube.getLightDir());
        posZPanel.setLightDir(cube.getLightDir());

        negZPanel.setLightColor(cube.getLightColor());
        posZPanel.setLightColor(cube.getLightColor());
        colorPanel.setChipColor(cube.getLightColor());
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getSource() == cube)
        {
            if (ViewerCube.PROP_LIGHTDIR.equals(evt.getPropertyName()))
            {
                updateFromCube();
            }
            if (ViewerCube.PROP_LIGHTCOLOR.equals(evt.getPropertyName()))
            {
                updateFromCube();
            }
            return;
        }

        //Event form sub components
        if (LightDirPanel.PROP_LIGHTDIR.equals(evt.getPropertyName()))
        {
            cube.setLightDir((Vector3f)evt.getNewValue());
        }
        else if (ColorChipPanel.PROP_CHIPCOLOR.equals(evt.getPropertyName()))
        {
            cube.setLightColor((Color3f)evt.getNewValue());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup_lightStyle = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        panel_back = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        panel_front = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        panel_color = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        radio_styleNone = new javax.swing.JRadioButton();
        radio_stylePhong = new javax.swing.JRadioButton();
        radio_styleDiffuse = new javax.swing.JRadioButton();

        panel_back.setLayout(new java.awt.BorderLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Back");

        panel_front.setLayout(new java.awt.BorderLayout());

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Front");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel_back, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
            .addComponent(panel_front, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(panel_back, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel_front, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addContainerGap())
        );

        panel_color.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panel_color.setLayout(new java.awt.BorderLayout());

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Color");

        buttonGroup_lightStyle.add(radio_styleNone);
        radio_styleNone.setSelected(true);
        radio_styleNone.setText("None");
        radio_styleNone.setToolTipText("No lighting");
        radio_styleNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radio_styleNoneActionPerformed(evt);
            }
        });

        buttonGroup_lightStyle.add(radio_stylePhong);
        radio_stylePhong.setText("Phong");
        radio_stylePhong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radio_stylePhongActionPerformed(evt);
            }
        });

        buttonGroup_lightStyle.add(radio_styleDiffuse);
        radio_styleDiffuse.setText("Diffuse");
        radio_styleDiffuse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radio_styleDiffuseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radio_styleNone)
                    .addComponent(radio_stylePhong)
                    .addComponent(radio_styleDiffuse))
                .addContainerGap(73, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radio_styleNone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radio_stylePhong)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radio_styleDiffuse)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                .addGap(56, 56, 56))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel_color, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel_color, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void radio_styleNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_styleNoneActionPerformed
        cube.setLightingStyle(LightingStyle.NONE);
    }//GEN-LAST:event_radio_styleNoneActionPerformed

    private void radio_stylePhongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_stylePhongActionPerformed
        cube.setLightingStyle(LightingStyle.PHONG);
    }//GEN-LAST:event_radio_stylePhongActionPerformed

    private void radio_styleDiffuseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_styleDiffuseActionPerformed
        cube.setLightingStyle(LightingStyle.DIFFUSE);
    }//GEN-LAST:event_radio_styleDiffuseActionPerformed

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
        }
        this.cube = cube;
        if (this.cube != null)
        {
            this.cube.addPropertyChangeListener(this);
        }
        updateFromCube();
    }

//    /**
//     * Get the value of lightDir
//     *
//     * @return the value of lightDir
//     */
//    public Vector3f getLightDir() {
//        return new Vector3f(lightDir);
//    }
//
//    /**
//     * Set the value of lightDir
//     *
//     * @param lightDir new value of lightDir
//     */
//    public void setLightDir(Vector3f lightDir) {
//        if (lightDir.equals(this.lightDir))
//        {
//            return;
//        }
//        Vector3f oldLightDir = new Vector3f(this.lightDir);
//        this.lightDir.set(lightDir);
//        firePropertyChange(PROP_LIGHTDIR, oldLightDir, lightDir);
//
//        posZPanel.setLightDir(lightDir);
//        negZPanel.setLightDir(lightDir);
//    }
//
//    /**
//     * Get the value of chipColor
//     *
//     * @return the value of chipColor
//     */
//    public Color3f getChipColor() {
//        return new Color3f(chipColor);
//    }
//
//    /**
//     * Set the value of chipColor
//     *
//     * @param chipColor new value of chipColor
//     */
//    public void setChipColor(Color3f chipColor)
//    {
//        if (lightDir.equals(this.lightDir))
//        {
//            return;
//        }
//        Color3f oldLightColor = new Color3f(this.chipColor);
//        this.chipColor.set(chipColor);
//        firePropertyChange(PROP_CHIPCOLOR, oldLightColor, chipColor);
//
//        posZPanel.setChipColor(chipColor);
//        negZPanel.setChipColor(chipColor);
//        colorPanel.setChipColor(chipColor);
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup_lightStyle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel panel_back;
    private javax.swing.JPanel panel_color;
    private javax.swing.JPanel panel_front;
    private javax.swing.JRadioButton radio_styleDiffuse;
    private javax.swing.JRadioButton radio_styleNone;
    private javax.swing.JRadioButton radio_stylePhong;
    // End of variables declaration//GEN-END:variables


}
