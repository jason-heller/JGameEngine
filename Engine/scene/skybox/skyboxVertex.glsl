#version 150

in vec3 in_position;
out vec3 pass_uvs;
out float lightAmt;
out float lightY;

uniform vec3 lightDir;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void){
	lightAmt = dot(-lightDir,normalize(in_position));
	//if (lightAmt > .99) {
	//	lightAmt = 3;
	//}
	
	lightY = lightDir.y;

	gl_Position = projectionMatrix * viewMatrix * vec4(in_position*4.0, 1.0);
	pass_uvs = vec3(in_position.x,in_position.y,in_position.z);

}
