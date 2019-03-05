#version 150

in vec3 in_position;
in vec2 in_textureCoords;
in vec3 in_normals;
in vec4 in_positionAndScale;

out vec2 pass_textureCoords;
out vec3 pass_normals;

uniform mat4 projectionViewMatrix;

void main(void){

	vec3 position = (in_position*in_positionAndScale.w);
	position.y *= (in_positionAndScale.w/2.75);
	gl_Position = projectionViewMatrix * (vec4(position + in_positionAndScale.xyz, 1.0));

	pass_textureCoords = in_textureCoords;
	pass_normals = in_normals;
}
