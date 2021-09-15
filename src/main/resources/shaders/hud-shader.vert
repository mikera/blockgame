#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texPos;

out vec2 texCoord;

uniform mat4 MVP;

void main()
{
    gl_Position = MVP * vec4(position, 1.0);
    texCoord = texPos;
}