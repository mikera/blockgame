#version 330

in vec2 texCoord;
in vec3 mvPos;
out vec4 fragColour;

uniform sampler2D texSampler;
uniform vec4 colour;

void main()
{
    fragColour = colour * texture(texSampler, texCoord);
}