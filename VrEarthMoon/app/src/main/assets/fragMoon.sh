precision mediump float;
varying vec4 vDiffuse;
varying vec4 vAmbient;
varying vec4 vSpecular;
varying vec2 vTextureCoor;
uniform sampler2D sTexture;//纹理内容数据

void main() {
    vec4 finalColor = texture2D(sTexture, vTextureCoor);

    gl_FragColor = finalColor * vAmbient + finalColor * vSpecular + finalColor * vDiffuse;
    //gl_FragColor = finalColor;

}
