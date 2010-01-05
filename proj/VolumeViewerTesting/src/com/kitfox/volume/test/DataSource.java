/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kitfox.volume.test;

import com.kitfox.xml.schema.volumeviewer.savefile.DataSourceType;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kitfox
 */
public class DataSource
{

    protected URL dataSource;
    public static final String PROP_DATASOURCE = "dataSource";


    public void load(DataSourceType dataSource)
    {
        try {
            setDataSource(new URL(dataSource.getUrl()));
        } catch (MalformedURLException ex) {
            Logger.getLogger(DataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DataSourceType save()
    {
        DataSourceType target = new DataSourceType();

        target.setUrl(dataSource.toExternalForm());

        return target;
    }


    /**
     * Get the value of dataSource
     *
     * @return the value of dataSource
     */
    public URL getDataSource() {
        return dataSource;
    }

    /**
     * Set the value of dataSource
     *
     * @param dataSource new value of dataSource
     */
    public void setDataSource(URL dataSource) {
        URL oldDataSource = this.dataSource;
        this.dataSource = dataSource;
        propertyChangeSupport.firePropertyChange(PROP_DATASOURCE, oldDataSource, dataSource);
    }
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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
