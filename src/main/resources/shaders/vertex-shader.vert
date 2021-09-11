#version 330

// Attribute
#define POSITION    0
#define TEXTURE     1
#define COLOUR      2

layout (location = POSITION) in vec4 position;
layout (location = TEXTURE) in vec2 texture;
layout (location = COLOUR) in vec4 vertex_colour;

out vec2 tex_coord;
smooth out vec4 theColour;

uniform mat4 MVP;

void main()
{
    gl_Position = MVP*position;
    theColour = vertex_colour;
    tex_coord = texture;
}