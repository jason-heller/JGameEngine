#version 150

in vec2 pass_textureCoords;
in vec3 pass_normals;

uniform sampler2D sampler;
uniform vec3 lightDirection;

out vec4 out_color;

const vec2 lightBias = vec2(0.3, 0.7);//just indicates the balance between diffuse and ambient lighting

void main(void){
	vec4 finalColor = texture(sampler, pass_textureCoords);
	
	if (finalColor.rgb == vec3(1.0,0.0,1.0)) {
		discard;
	}
	
	vec3 newLightDir = -lightDirection;
	if (newLightDir.y < -.45) {
		newLightDir.y = -.45;
	}
	float diffuseLight = max(dot(newLightDir, pass_normals), 0.0) * lightBias.x + lightBias.y;
	
	if (lightDirection.y > .45) {
		diffuseLight -= lightDirection.y-.45;
	}
	
	out_color = finalColor * diffuseLight;
	
}
