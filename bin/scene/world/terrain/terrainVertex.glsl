#version 150

in vec3 in_position;
in vec2 in_textureCoords;
in vec3 in_normals;

out vec2 pass_textureCoords;
out vec3 pass_normals;
out vec3 pass_position;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec2 offset;
uniform vec4 clipPlane;

/* Shadows */
const float shadowDist = 200.0;
const float transitionDist = 15.0;

uniform mat4 shadowMatrix;
out vec4 shadowCoords;
/***********/

void main(void){

	vec4 worldSpace = vec4(in_position.x+offset.x, in_position.y, in_position.z+offset.y, 1.0);
	
	gl_ClipDistance[0] = dot(worldSpace, clipPlane);
	gl_Position = projectionMatrix*viewMatrix * worldSpace;
	
	pass_textureCoords = in_textureCoords;
	pass_normals = in_normals;
	pass_position = in_position;
	
	vec4 posRelativeToCam = viewMatrix*worldSpace;
	shadowCoords = shadowMatrix * worldSpace;
	float dist = length(posRelativeToCam);
	dist -= (shadowDist - transitionDist);
	dist /= transitionDist;
	shadowCoords.w = clamp(1.0-dist, 0.0, 1.0);
}
