#version 330

in vec2 pass_textureCoords;
in vec3 pass_normals;

uniform sampler2D diffuse;
uniform sampler2D specular;
uniform float specularity;

in vec3 toCamera;
uniform vec3 lightDirection;
uniform vec3 color;

layout(location=0) out vec4 out_color;
layout(location=1) out vec4 out_brightness;

const vec2 lightBias = vec2(0.3, 0.7);//just indicates the balance between diffuse and ambient lighting

void main(void){
	vec4 diffuseTexture;
	if (color == vec3(0.0)) {
		diffuseTexture = texture(diffuse, pass_textureCoords);
	} else {
		diffuseTexture = vec4(color.xyz, 1.0);
	}
	
	if (diffuseTexture.rgb == vec3(1.0,0.0,1.0) || diffuseTexture.a == 0.0) {
		discard;
	}
	
	float diffuseLight = max(dot(lightDirection, pass_normals), 0.0) * lightBias.x + lightBias.y;
	float specularLight = 0.0;
	
	out_brightness = vec4(0.0);
	vec4 specularVec = vec4(0.0);
	if (specularity > 0.0) {
		vec4 specularTexture = texture(specular, pass_textureCoords);
	
		vec3 lightReflectDir = reflect(-lightDirection, pass_normals);
		float specularFactor = dot(lightReflectDir, toCamera);
		specularFactor = max(specularFactor, 0.0);
		float damped = pow(specularFactor, 10);
		specularLight = specularTexture.r*damped;
		
		specularVec = vec4(specularLight, specularLight, specularLight, 1.0);
		
		if (specularTexture.g > 0.0) {
			//out_brightness = diffuseTexture + specularVec;
		}
	}
	
	out_color = specularVec + (diffuseTexture * diffuseLight);
	
}
