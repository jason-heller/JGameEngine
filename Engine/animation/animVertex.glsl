#version 150

in vec3 in_position;
in vec2 in_textureCoords;
in vec3 in_normals;
in int in_boneIndices;

out vec2 pass_textureCoords;
out vec3 pass_normals;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

uniform mat4 bones[10];

void main(void){

	mat4 matrix = viewMatrix * modelMatrix * bones[in_boneIndices];

	gl_Position = projectionMatrix * matrix * vec4(in_position, 1.0);
	
	pass_textureCoords = in_textureCoords;
	pass_normals = in_normals;
}
