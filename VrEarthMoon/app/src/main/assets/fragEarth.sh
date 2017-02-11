precision mediump float;
varying vec2 vTextureCoor;
varying vec4 vDiffuse;
varying vec4 vAmbient;
varying vec4 vSpecular;

uniform sampler2D sTextureDay;
uniform sampler2D sTextureNight;

void main() {
    vec4 finalColorDay;
    vec4 finalColorNight;

    finalColorDay = texture2D(sTextureDay, vTextureCoor);
    finalColorDay = finalColorDay * vAmbient + finalColorDay * vDiffuse + finalColorDay * vSpecular;

    finalColorNight = texture2D(sTextureNight, vTextureCoor);
    finalColorNight = finalColorNight  * vec4(0.5, 0.5, 0.5, 1.0);

    if(vDiffuse.x > 0.21) {
        gl_FragColor = finalColorDay;
    } else if(vDiffuse.x < 0.05) {
        gl_FragColor = finalColorNight;
    } else {
        float t = (vDiffuse.x - 0.05) / 0.16;
        gl_FragColor = t * finalColorDay + (1.0 - t) * finalColorNight;
    }
}
