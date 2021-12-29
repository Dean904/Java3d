#version 330

in  vec3 exColour;
in  vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler;

void main()
{
    //fragColor =  vec4(exColour, 1.0);
    fragColor = texture(texture_sampler, outTexCoord)  + vec4(exColour, 1.0);
}
