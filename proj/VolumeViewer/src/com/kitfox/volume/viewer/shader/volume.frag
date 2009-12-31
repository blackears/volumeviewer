/*
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


const int LIGHT_STYLE_NONE = 0;
const int LIGHT_STYLE_PHONG = 1;
const int LIGHT_STYLE_DIFFUSE = 2;

uniform vec3 lightColor;
uniform vec3 lightDir;
uniform vec3 lightHalfDir;
uniform int lightStyle;
uniform float opacityCorrect;

uniform sampler3D texVolume;
uniform sampler2D texXfer;

varying vec3 position;
varying vec3 uv;

void main()
{
//    gl_FragColor = vec4(abs(uv), 1.0);
//    gl_FragColor = vec4(texture3D(texVolume, uv).xyz, 1.0);

//    gl_FragColor = vec4(texture3D(texVolume, uv).aaa, 1.0);
//    gl_FragColor = texture2D(texXfer, uv.xy);

    vec4 vol = texture3D(texVolume, uv);
    float opacityRaw = vol.a;

    //Gradient at current cell
    vec3 grad = vol.rgb * 2.0 - 1.0;
    float gradLen = length(grad);

    vec4 xferCol = texture2D(texXfer, vec2(opacityRaw, gradLen * 2.0));

    float opacityLocal = xferCol.a * opacityRaw;
    opacityLocal = 1.0 - pow(1.0 - opacityLocal, opacityCorrect);
    vec4 color = vec4(xferCol.xyz, opacityLocal);

    if (lightStyle == LIGHT_STYLE_PHONG)
    {
        vec3 normal = normalize(grad);
        float diff = saturate(dot(normal, lightDir));
        float sat = pow(saturate(dot(normal, lightHalfDir)), 20.0);
//        saturate(diff);
//        clamp(diff, 0, 1);
        color = vec4(color.xyz * lightColor * saturate(diff + sat), color.a);
    }
//    gl_FragColor = color;
    gl_FragColor = vec4(color.rgb * color.a, color.a);
}

