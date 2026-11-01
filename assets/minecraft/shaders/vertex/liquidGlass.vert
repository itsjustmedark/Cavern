#version 120

void main() {

    vec2 position = gl_Vertex.xy;

    gl_Position = vec4(
            position.x * 2.0 - 1.0,
            1.0 - position.y * 2.0,
            0.0,
            1.0
    );
}