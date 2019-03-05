#version 150

in vec3 pass_uvs;
uniform samplerCube sampler;
out vec4 out_colour;
uniform vec3 viewDir;
in float lightAmt;
in float lightY;

uniform vec3 color;
uniform vec3 sunColor;

void main(void){
	vec4 finalColor = texture(sampler, pass_uvs);
	
	vec4 bgColor = vec4(mix(color, sunColor, lightAmt),1.0);

	if (finalColor.a == 1.0 && lightY > 0.0) {// 
		out_colour = mix(bgColor, finalColor, lightY);
	} else {
		out_colour = bgColor;
	}
	
}
