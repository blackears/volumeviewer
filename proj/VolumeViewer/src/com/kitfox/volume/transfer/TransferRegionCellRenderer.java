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
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 *
 * @author kitfox
 */
@Deprecated
public class TransferRegionCellRenderer extends JLabel implements ListCellRenderer
{
    private static final long serialVersionUID = 1;

    final int side = 10;

    class ColorIcon implements Icon
    {
        private static final long serialVersionUID = 1;

        final Color color;

        ColorIcon(Color color)
        {
            this.color = color;
        }

        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            g.setColor(color);
            g.fillRect(x, y, side, side);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, side, side);
        }

        public int getIconWidth()
        {
            return side;
        }

        public int getIconHeight()
        {
            return side;
        }
    };

    public TransferRegionCellRenderer()
    {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
        Color bg = isSelected ? uid.getColor("List.selectionBackground")
                : uid.getColor("List.background");
        setBackground(bg);


        TransferRegion region = (TransferRegion)value;
        setText(region == null ? "<error>" : region.getName());

        Color fg = region.getColor();
        setIcon(new ColorIcon(fg));

        return this;
    }

}
