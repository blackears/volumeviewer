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

package com.kitfox.volume.transfer;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author kitfox
 */
@Deprecated
public class TransferRegion
{
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    protected Color color;
    public static final String PROP_COLOR = "color";
    protected String name;
    public static final String PROP_NAME = "name";
    protected Float area;
    public static final String PROP_AREA = "area";

    public TransferRegion(String name, Rectangle2D.Float area)
    {
        this(name, area, Color.WHITE);
    }

    public TransferRegion(String name, Rectangle2D.Float area, Color color)
    {
        this.color = color;
        this.name = name;
        this.area = area;
    }
    
    /**
     * Get the value of area
     *
     * @return the value of area
     */
    public Float getArea() {
        return area;
    }

    /**
     * Set the value of area
     *
     * @param area new value of area
     */
    public void setArea(Float area) {
        Float oldArea = this.area;
        this.area = area;
        propertyChangeSupport.firePropertyChange(PROP_AREA, oldArea, area);
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, name);
    }

    /**
     * Get the value of color
     *
     * @return the value of color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the value of color
     *
     * @param color new value of color
     */
    public void setColor(Color color) {
        Color oldColor = this.color;
        this.color = color;
        propertyChangeSupport.firePropertyChange(PROP_COLOR, oldColor, color);
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
