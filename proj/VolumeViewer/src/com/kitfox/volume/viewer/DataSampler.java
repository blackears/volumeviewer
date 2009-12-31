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

/**
 * Treats data source as 1x1x1 cube.  Samples data at given point to
 * construct renderable data.
 *
 * @author kitfox
 */
abstract public class DataSampler
{
    abstract public float getValue(float x, float y, float z);
    abstract public float getDx(float x, float y, float z);
    abstract public float getDy(float x, float y, float z);
    abstract public float getDz(float x, float y, float z);

    public Histogram createHistogram(int numMagSamples, int xSpan, int ySpan, int zSpan, DataSampler sampler)
    {
        Histogram hist = new Histogram(256, numMagSamples);

        for (int k = 0; k < zSpan; ++k)
        {
            float dk = (float)k / zSpan;

            for (int j = 0; j < ySpan; ++j)
            {
                float dj = (float)j / ySpan;

                for (int i = 0; i < xSpan; ++i)
                {
                    float di = (float)i / xSpan;

                    float value = sampler.getValue(di, dj, dk);
                    int x = (int)(value * 255 + .5f);

                    //All d* should be on [-.5 .5]
                    float dx = sampler.getDx(di, dj, dk) * 2;
                    float dy = sampler.getDy(di, dj, dk) * 2;
                    float dz = sampler.getDz(di, dj, dk) * 2;
                    float mag = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
                    hist.add(x, (int)(mag * numMagSamples));
                }
            }
        }

        return hist;
    }
}
