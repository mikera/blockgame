#version 330

// Attribute
#define FRAG_COLOUR  0

layout (location = FRAG_COLOUR) out vec4 outputColour;

smooth in vec3 theColour;
smooth in vec3 mvVertexNormal;

uniform sampler2D tex;
in vec2 tex_coord;

uniform vec3 vLightDir;

void main()
{
    // Diffuse Light
    float diffuseFactor = max(dot(mvVertexNormal, vLightDir ), 0.0);

	vec4 light = vec4(diffuseFactor*0.5 + 0.5*theColour,1.0);

    outputColour = texture2D(tex, tex_coord)*light;
    // if ((outputColour.a)<=0.0) discard;
}