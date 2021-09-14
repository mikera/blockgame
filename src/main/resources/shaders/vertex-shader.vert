#version 330

// Attribute
#define POSITION    0
#define NORMAL      1
#define TEXTURE     2
#define COLOUR      3


layout (location = POSITION) in vec3 position;
layout (location = TEXTURE) in vec2 texture;
layout (location = NORMAL) in vec3 normal;
layout (location = COLOUR) in vec4 vertex_colour;

out vec2 tex_coord;
smooth out vec4 theColour;
out vec3 mvVertexNormal;
out vec3 mvVertexPos;

uniform mat4 P;
uniform mat4 MV;

void main()
{
    vec4 mvPos = MV * vec4(position, 1.0);
    
    gl_Position = P * mvPos;
    theColour = vertex_colour;
    tex_coord = texture;
    mvVertexNormal = normalize(MV * vec4(normal, 0.0)).xyz;
    mvVertexPos = mvPos.xyz;
}