#version 330

// Attribute
#define POSITION    0
#define TEXTURE     1
#define COLOUR      2

layout (location = POSITION) in vec4 position;
layout (location = TEXTURE) in vec2 texCoord_buffer;
layout (location = COLOUR) in vec4 vertex_colour;

varying vec2 tex_coord;
smooth out vec4 theColour;

void main()
{
    gl_Position = position;
    gl_Position.z = (1000.0-position.z)/2000.0;
    theColour = vertex_colour;
    tex_coord = texCoord_buffer;
}