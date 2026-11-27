#version 120
uniform vec4 rect; // x, y, w, h in GUI coords
uniform vec2 guiResolution;
const float pad = 2.0; // slack for the AA edge

void main() {
    vec2 p = rect.xy - pad + gl_Vertex.xy * (rect.zw + 2.0 * pad);
    vec2 ndc = p / guiResolution * 2.0 - 1.0;
    gl_Position = vec4(ndc.x, -ndc.y, 0.0, 1.0);
}