#version 150

in vec3 in_position;
in vec2 in_textureCoords;
in vec3 in_normals;

out vec2 pass_textureCoords;
out vec3 pass_normals;

out vec3 toCamera;

uniform mat4 projectionViewMatrix;
uniform mat4 modelMatrix;
uniform vec4 clipPlane;
uniform vec3 cameraPos;

void main(void){

	vec4 worldPos = modelMatrix * vec4(in_position, 1.0);
	
	gl_ClipDistance[0] = dot(worldPos, clipPlane);

	gl_Position = projectionViewMatrix * worldPos;
	toCamera = normalize(cameraPos - worldPos.xyz);
	
	pass_textureCoords = in_textureCoords;
	pass_normals = in_normals;
}
