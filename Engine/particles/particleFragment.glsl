#version 330

in vec2 textureCoords1;
in vec2 textureCoords2;
in float blend;

layout(location = 0) out vec4 out_colour;
layout(location = 1) out vec4 out_brightness;

uniform sampler2D particleTexture;

void main(void){

	vec4 color1 = texture(particleTexture, textureCoords1);
	vec4 color2 = texture(particleTexture, textureCoords2);
	vec4 diffuse = mix(color1, color2, blend);
	out_colour = diffuse;
	out_brightness = vec4(0.0);
}
