#version 150

in vec2 pass_textureCoords;
in vec3 pass_normals;

uniform sampler2D sampler;
uniform vec3 lightDirection;

out vec4 out_color;

const vec2 lightBias = vec2(0.7, 0.6);//just indicates the balance between diffuse and ambient lighting

void main(void){
	vec4 finalColor = texture(sampler, pass_textureCoords);
	
	if (finalColor.rgb == vec3(1.0,0.0,1.0)) {
		discard;
	}
	
	float diffuseLight = max(dot(-lightDirection, pass_normals), 0.0) * lightBias.x + lightBias.y;
	out_color = finalColor * diffuseLight;
	
}
