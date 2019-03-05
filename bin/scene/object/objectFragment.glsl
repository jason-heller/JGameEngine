#version 150

in vec2 pass_textureCoords;
in vec3 pass_normals;

uniform sampler2D sampler;
uniform vec3 lightDirection;
uniform vec3 color;

out vec4 out_color;

const vec2 lightBias = vec2(0.3, 0.7);//just indicates the balance between diffuse and ambient lighting

void main(void){
	vec4 finalColor;
	if (color == vec3(0.0)) {
		finalColor = texture(sampler, pass_textureCoords);
	} else {
		finalColor = vec4(color.xyz, 1.0);
	}
	
	if (finalColor.rgb == vec3(1.0,0.0,1.0) || finalColor.a == 0.0) {
		discard;
	}
	
	vec3 newLightDir = -lightDirection;
	if (newLightDir.y < -.65) {
		newLightDir.y = -.65;
	}
	float diffuseLight = max(dot(newLightDir, pass_normals), 0.0) * lightBias.x + lightBias.y;
	
	if (lightDirection.y > .65) {
		diffuseLight -= lightDirection.y-.65;
	}
	
	out_color = finalColor * diffuseLight;
	
}
