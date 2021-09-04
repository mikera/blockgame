#version 330

// Attribute
#define FRAG_COLOUR  0

layout (location = FRAG_COLOUR) out vec4 outputColour;

smooth in vec4 theColour;
uniform sampler2D tex;
varying vec2 tex_coord;

void main()
{
    outputColour = theColour * texture2D(tex, tex_coord);
    if ((outputColour.a)<=0.0) discard;
    // outputColour = theColour * vec4(1.0f, 1.0f, 1.0f, gl_FragCoord.x/500)*texture2D(tex, tex_coord);
}