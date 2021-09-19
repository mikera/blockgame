#version 330

// Attribute
#define FRAG_COLOUR  0

layout (location = FRAG_COLOUR) out vec4 outputColour;

smooth in vec4 theColour;

uniform sampler2D tex;
in vec2 tex_coord;

void main()
{

    outputColour = texture2D(tex, tex_coord);
    // if ((outputColour.a)<=0.0) discard;
}