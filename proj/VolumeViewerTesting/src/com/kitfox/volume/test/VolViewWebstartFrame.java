/*
 * TestFrame.java
 * Created on Dec 23, 2009, 12:05:29 PM
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

package com.kitfox.volume.test;

import com.kitfox.volume.light.LightCtrlPanel;
import com.kitfox.volume.transfer.TransferFnPanel;
import com.kitfox.volume.viewer.DataSamplerImage;
import com.kitfox.volume.mask.SectorPanel;
import com.kitfox.volume.viewer.GLActionSaveBuffer;
import com.kitfox.volume.viewer.ViewerCube;
import com.kitfox.volume.viewer.ViewerPanel;
import com.kitfox.volume.viewer.VolumeData;
import com.kitfox.volume.viewer.VolumeLayoutPanel;
import com.kitfox.volume.viewer.ZipDataLoader;
import com.kitfox.xml.schema.volumeviewer.cubestate.CubeType;
import com.kitfox.xml.schema.volumeviewer.cubestate.NavigatorType;
import com.kitfox.xml.schema.volumeviewer.savefile.ObjectFactory;
import com.kitfox.xml.schema.volumeviewer.savefile.VolumeViewerConfigType;
import com.kitfox.xml.schema.volumeviewer.savefile.WindowLayoutType;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author kitfox
 */
public class VolViewWebstartFrame extends javax.swing.JFrame
{
    private static final long serialVersionUID = 0;

    JFileChooser fileChooser = new JFileChooser();
    {
        FileFilter filter = new FileFilter()
        {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".vvc");
            }

            @Override
            public String getDescription() {
                return "Volume Viewer config file (*.vvc)";
            }
        };
        fileChooser.setFileFilter(filter);
        fileChooser.setCurrentDirectory(new File("."));
    }

    JFileChooser fileChooserPng = new JFileChooser();
    {
        FileFilter filter = new FileFilter()
        {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "PNG image file (*.png)";
            }
        };
        fileChooser.setFileFilter(filter);
        fileChooser.setCurrentDirectory(new File("."));
    }

    class DemoAction extends AbstractAction
    {
        private static final long serialVersionUID = 0;

        final URL url;

        DemoAction(String name, URL url)
        {
            super(name);
            this.url = url;
        }

        public void actionPerformed(ActionEvent e)
        {
            load(url);
        }
    }

    class DataLoader implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent evt)
        {
            try {
                DataSamplerImage tex = ZipDataLoader.createSampler(dataSource.getDataSource());
                System.err.println("Loaded images");
                VolumeData data =
                        new VolumeData(tex.getxSpan(), tex.getySpan(), tex.getzSpan(), tex);
                System.err.println("Sampled data");
                cube.setData(data);

                xferPanel.setVolumeData(data);
            } catch (IOException ex) {
                Logger.getLogger(VolViewWebstartFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    DataLoader loader = new DataLoader();
    DataSource dataSource = new DataSource();

    JDialog dlgDataSourcePanel = new JDialog();
    JDialog dlgXferPanel = new JDialog();
    JDialog dlgLightPanel = new JDialog();
    JDialog dlgLayoutPanel = new JDialog();
    JDialog dlgSectorPanel = new JDialog();
    JDialog dlgAboutPanel = new JDialog();

    TransferFnPanel xferPanel = new TransferFnPanel();
    final URI helpContents;

    DataPanel dataPanel = new DataPanel();
    ViewerPanel viewer = new ViewerPanel();
    LightCtrlPanel lightPanel = new LightCtrlPanel();
    VolumeLayoutPanel layoutPanel = new VolumeLayoutPanel();
    SectorPanel octantPanel = new SectorPanel();
    AboutPanel aboutPanel = new AboutPanel();

    ViewerCube cube = new ViewerCube();

//    private static final String NS = "http://xml.kitfox.com/schema/volumeViewer/saveFile";


    /** Creates new form TestFrame */
    public VolViewWebstartFrame()
    {
        initComponents();

        menu_demo.add(new DemoAction("Head Edges",
                VolViewWebstartFrame.class.getResource("/headEdges.vvc")));
        menu_demo.add(new DemoAction("Head Organs",
                VolViewWebstartFrame.class.getResource("/headOrgans.vvc")));
        menu_demo.add(new DemoAction("Hollow Side View",
                VolViewWebstartFrame.class.getResource("/hollowSideView.vvc")));
        menu_demo.add(new DemoAction("Sectioned View",
                VolViewWebstartFrame.class.getResource("/sectionedView.vvc")));
        menu_demo.add(new DemoAction("Clear Head",
                VolViewWebstartFrame.class.getResource("/clearHead.vvc")));
        menu_demo.add(new DemoAction("Opaque",
                VolViewWebstartFrame.class.getResource("/opaque.vvc")));
        menu_demo.add(new DemoAction("Skull",
                VolViewWebstartFrame.class.getResource("/skull.vvc")));
        menu_demo.add(new DemoAction("Cut Skull",
                VolViewWebstartFrame.class.getResource("/cutSkull.vvc")));


        URI uri = null;
        try {
            uri = new URI("http://volumeviewer.kenai.com");
        } catch (URISyntaxException ex) {
            Logger.getLogger(AboutPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        helpContents = uri;

        getContentPane().add(viewer, BorderLayout.CENTER);

        dataPanel.setDataSource(dataSource);
        dataSource.addPropertyChangeListener(loader);

        viewer.setCube(cube);
        lightPanel.setCube(cube);
        layoutPanel.setCube(cube);
        octantPanel.setCube(cube);

        buildWindows();

        setSize(640, 480);

//        dataSource.setDataSource(VolViewWebstartFrame.class.getResource("/mrbrain-8bit.zip"));

        File file = new File("../../www/mrbrain-8bit.zip");

        if (file.exists())
        {
            //For local testing only
            try
            {
                URL url = file.toURI().toURL();
                dataSource.setDataSource(url);
            } catch (MalformedURLException ex) {
                Logger.getLogger(VolViewWebstartFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            //By default, load one of the demos
            load(VolViewWebstartFrame.class.getResource("/headEdges.vvc"));
        }
    }

    private void buildWindows()
    {
        dlgXferPanel.add(xferPanel, BorderLayout.CENTER);
        dlgXferPanel.pack();
        dlgXferPanel.setLocation(640, 0);
        dlgXferPanel.setTitle("Paint");
        dlgXferPanel.setVisible(true);

        dlgLightPanel.add(lightPanel, BorderLayout.CENTER);
        dlgLightPanel.pack();
        dlgLightPanel.setLocation(640, 480);
        dlgLightPanel.setTitle("Light");
        dlgLightPanel.setVisible(true);

        dlgSectorPanel.add(octantPanel, BorderLayout.CENTER);
        dlgSectorPanel.pack();
        dlgSectorPanel.setLocation(840, 480);
        dlgSectorPanel.setTitle("Clip Sectors");
        dlgSectorPanel.setVisible(true);

        dlgLayoutPanel.add(layoutPanel, BorderLayout.CENTER);
        dlgLayoutPanel.pack();
        dlgLayoutPanel.setLocation(0, 480);
        dlgLayoutPanel.setTitle("Layout");
        dlgLayoutPanel.setVisible(true);

        dlgDataSourcePanel.add(dataPanel, BorderLayout.CENTER);
        dlgDataSourcePanel.pack();
        dlgDataSourcePanel.setLocation(20, 480);
        dlgDataSourcePanel.setTitle("Data Source");
        dlgDataSourcePanel.setVisible(false);

        dlgAboutPanel.add(aboutPanel, BorderLayout.CENTER);
        dlgAboutPanel.pack();
        dlgAboutPanel.setLocation(20, 480);
        dlgAboutPanel.setTitle("About the Author");
        dlgAboutPanel.setVisible(false);


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        menu_file = new javax.swing.JMenu();
        cm_fileLoad = new javax.swing.JMenuItem();
        cm_fileSave = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        cm_fileSaveSnapshot = new javax.swing.JMenuItem();
        menu_demo = new javax.swing.JMenu();
        menu_window = new javax.swing.JMenu();
        cm_winDataSource = new javax.swing.JMenuItem();
        cm_winXferFn = new javax.swing.JMenuItem();
        cm_winLighting = new javax.swing.JMenuItem();
        cm_winLayout = new javax.swing.JMenuItem();
        cm_winOctant = new javax.swing.JMenuItem();
        menu_help = new javax.swing.JMenu();
        cm_helpContents = new javax.swing.JMenuItem();
        cm_helpAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        menu_file.setText("File");

        cm_fileLoad.setText("Load...");
        cm_fileLoad.setToolTipText("Load a config file");
        cm_fileLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cm_fileLoadActionPerformed(evt);
            }
        });
        menu_file.add(cm_fileLoad);

        cm_fileSave.setText("Save...");
        cm_fileSave.setToolTipText("Save configuration");
        cm_fileSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cm_fileSaveActionPerformed(evt);
            }
        });
        menu_file.add(cm_fileSave);
        menu_file.add(jSeparator1);

        cm_fileSaveSnapshot.setText("Save Snapshot...");
        cm_fileSaveSnapshot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cm_fileSaveSnapshotActionPerformed(evt);
            }
        });
        menu_file.add(cm_fileSaveSnapshot);

        jMenuBar1.add(menu_file);

        menu_demo.setText("Demo");
        jMenuBar1.add(menu_demo);

        menu_window.setText("Window");

        cm_winDataSource.setText("Data Source");
        cm_winDataSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cm_winDataSourceActionPerformed(evt);
            }
        });
        menu_window.add(cm_winDataSource);

        cm_winXferFn.setText("Paint");
        cm_winXferFn.setToolTipText("Paint colors onto the rendered volume");
        cm_winXferFn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cm_winXferFnActionPerformed(evt);
            }
        });
        menu_window.add(cm_winXferFn);

        cm_winLighting.setText("Lighting");
        cm_winLighting.setToolTipText("Adjust lighting values");
        cm_winLighting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cm_winLightingActionPerformed(evt);
            }
        });
        menu_window.add(cm_winLighting);

        cm_winLayout.setText("Layout");
        cm_winLayout.setToolTipText("Adjust the rendered shape");
        cm_winLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cm_winLayoutActionPerformed(evt);
            }
        });
        menu_window.add(cm_winLayout);

        cm_winOctant.setText("Clip Sectors");
        cm_winOctant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cm_winOctantActionPerformed(evt);
            }
        });
        menu_window.add(cm_winOctant);

        jMenuBar1.add(menu_window);

        menu_help.setText("Help");

        cm_helpContents.setText("Contents");
        cm_helpContents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cm_helpContentsActionPerformed(evt);
            }
        });
        menu_help.add(cm_helpContents);

        cm_helpAbout.setText("About");
        cm_helpAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cm_helpAboutActionPerformed(evt);
            }
        });
        menu_help.add(cm_helpAbout);

        jMenuBar1.add(menu_help);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cm_winXferFnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cm_winXferFnActionPerformed
        dlgXferPanel.setVisible(true);
    }//GEN-LAST:event_cm_winXferFnActionPerformed

    private void cm_winLightingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cm_winLightingActionPerformed
        dlgLightPanel.setVisible(true);
    }//GEN-LAST:event_cm_winLightingActionPerformed

    private void cm_winLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cm_winLayoutActionPerformed
        dlgLayoutPanel.setVisible(true);
    }//GEN-LAST:event_cm_winLayoutActionPerformed

    private void cm_winOctantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cm_winOctantActionPerformed
        dlgSectorPanel.setVisible(true);
    }//GEN-LAST:event_cm_winOctantActionPerformed

    private void cm_winDataSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cm_winDataSourceActionPerformed
        dlgDataSourcePanel.setVisible(true);
    }//GEN-LAST:event_cm_winDataSourceActionPerformed

    private void cm_helpContentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cm_helpContentsActionPerformed
        try {
            Desktop.getDesktop().browse(helpContents);
        } catch (IOException ex) {
            Logger.getLogger(AboutPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_cm_helpContentsActionPerformed

    private void cm_helpAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cm_helpAboutActionPerformed
        dlgAboutPanel.setVisible(true);
    }//GEN-LAST:event_cm_helpAboutActionPerformed

    private void cm_fileLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cm_fileLoadActionPerformed
        int res = fileChooser.showOpenDialog(this);

        if (res != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        File file = fileChooser.getSelectedFile();
        if (!file.exists())
        {
            return;
        }

        try {
            load(file.toURI().toURL());
        } catch (MalformedURLException ex) {
            Logger.getLogger(VolViewWebstartFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_cm_fileLoadActionPerformed

    private void cm_fileSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cm_fileSaveActionPerformed
        int res = fileChooser.showSaveDialog(this);

        if (res != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        File file = fileChooser.getSelectedFile();
        if (!file.getName().endsWith(".vvc"))
        {
            file = new File(file.getParentFile(), file.getName() + ".vvc");
        }

        //Export
        ObjectFactory fact = new ObjectFactory();
        JAXBElement<VolumeViewerConfigType> value = fact.createVolumeViewerConfig(save());

        try {
            JAXBContext context = JAXBContext.newInstance(
                    CubeType.class, NavigatorType.class, VolumeViewerConfigType.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            FileWriter writer = new FileWriter(file);
            marshaller.marshal(value, writer);
            writer.close();

            System.err.println("Saved config file " + file.getAbsolutePath());
        } catch (JAXBException ex) {
            Logger.getLogger(VolViewWebstartFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VolViewWebstartFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_cm_fileSaveActionPerformed

    private void cm_fileSaveSnapshotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cm_fileSaveSnapshotActionPerformed
        int res = fileChooserPng.showSaveDialog(this);

        if (res != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        File file = fileChooserPng.getSelectedFile();
        if (!file.getName().endsWith(".png"))
        {
            file = new File(file.getParentFile(), file.getName() + ".png");
        }

        //Do actual image fetch in GL thread
        GLActionSaveBuffer action = new GLActionSaveBuffer(file, viewer);
        viewer.postGLAction(action);
        
        //Flush GL queue
        viewer.repaint();

    }//GEN-LAST:event_cm_fileSaveSnapshotActionPerformed

    private void load(URL url)
    {
        try {
            JAXBContext context = JAXBContext.newInstance(
                    CubeType.class, NavigatorType.class, VolumeViewerConfigType.class);
            StreamSource source = new StreamSource(url.openStream());
            Unmarshaller unmarshaller = context.createUnmarshaller();

            JAXBElement<VolumeViewerConfigType> ele =
                    unmarshaller.unmarshal(source, VolumeViewerConfigType.class);

            load(ele.getValue());

            System.err.println("Loaded config file " + url.toExternalForm());
        } catch (JAXBException ex) {
            Logger.getLogger(VolViewWebstartFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VolViewWebstartFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void load(VolumeViewerConfigType conf)
    {
        for (WindowLayoutType win: conf.getWindowList())
        {
            if ("dlgDataSourcePanel".equals(win.getName()))
            {
                loadWindow(win, dlgDataSourcePanel);
            }
            else if ("dlgXferPanel".equals(win.getName()))
            {
                loadWindow(win, dlgXferPanel);
            }
            else if ("dlgLightPanel".equals(win.getName()))
            {
                loadWindow(win, dlgLightPanel);
            }
            else if ("dlgLayoutPanel".equals(win.getName()))
            {
                loadWindow(win, dlgLayoutPanel);
            }
            else if ("dlgSectorPanel".equals(win.getName()))
            {
                loadWindow(win, dlgSectorPanel);
            }
            else if ("dlgAboutPanel".equals(win.getName()))
            {
                loadWindow(win, dlgAboutPanel);
            }
        }
        dataSource.load(conf.getDataSource());
        viewer.load((NavigatorType)conf.getNavigator());

        cube.load((CubeType)conf.getCube());
    }

    private void loadWindow(WindowLayoutType win, JDialog dlg)
    {
        dlg.setBounds(win.getX(), win.getY(), win.getWidth(), win.getHeight());
        dlg.setVisible(win.isVisible());
    }

    private VolumeViewerConfigType save()
    {
        VolumeViewerConfigType target = new VolumeViewerConfigType();

        target.setCube(cube.save());
        saveWindow(target, dlgDataSourcePanel, "dlgDataSourcePanel");
        saveWindow(target, dlgXferPanel, "dlgXferPanel");
        saveWindow(target, dlgLightPanel, "dlgLightPanel");
        saveWindow(target, dlgLayoutPanel, "dlgLayoutPanel");
        saveWindow(target, dlgSectorPanel, "dlgSectorPanel");
        saveWindow(target, dlgAboutPanel, "dlgAboutPanel");
        target.setDataSource(dataSource.save());

        target.setNavigator(viewer.save());

        return target;
    }

    private void saveWindow(VolumeViewerConfigType target, JDialog dlg, String id)
    {
        WindowLayoutType win = new WindowLayoutType();
        
        win.setHeight(dlg.getHeight());
        win.setName(id);
        win.setVisible(dlg.isVisible());
        win.setWidth(dlg.getWidth());
        win.setX(dlg.getX());
        win.setY(dlg.getY());

        target.getWindowList().add(win);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[])
    {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VolViewWebstartFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem cm_fileLoad;
    private javax.swing.JMenuItem cm_fileSave;
    private javax.swing.JMenuItem cm_fileSaveSnapshot;
    private javax.swing.JMenuItem cm_helpAbout;
    private javax.swing.JMenuItem cm_helpContents;
    private javax.swing.JMenuItem cm_winDataSource;
    private javax.swing.JMenuItem cm_winLayout;
    private javax.swing.JMenuItem cm_winLighting;
    private javax.swing.JMenuItem cm_winOctant;
    private javax.swing.JMenuItem cm_winXferFn;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenu menu_demo;
    private javax.swing.JMenu menu_file;
    private javax.swing.JMenu menu_help;
    private javax.swing.JMenu menu_window;
    // End of variables declaration//GEN-END:variables

}
