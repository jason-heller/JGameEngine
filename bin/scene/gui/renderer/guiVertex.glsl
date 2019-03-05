#version 150

in vec2 position;
in vec2 textureCoords;

out vec2 pass_textureCoords;

uniform vec4 translation;
uniform vec4 offset;
uniform bool centered;
uniform float rot;


void main(void){

	vec2 centering = vec2(0.5,0.5);

	if (centered) {
		centering = vec2(0, 1);
	}

	if (offset == vec4(0.0)) {
		pass_textureCoords = textureCoords;
	} else {
		switch(gl_VertexID) {
		case 0: pass_textureCoords = offset.xw; break;
		case 1: pass_textureCoords = offset.zw; break;
		case 2: pass_textureCoords = offset.xy; break;
		case 3: pass_textureCoords = offset.zy; break;
		}
	}

	mat2 rotationMatrix = mat2(
		cos(rot), -sin(rot),
		sin(rot),  cos(rot));
		
	//if (translation.z == -1.0) {
		gl_Position = vec4((vec2((position*rotationMatrix) + centering)*translation.zw) + translation.xy * vec2(2.0, -2.0), 0.0, 1.0);
	//} else {
	//	gl_Position = vec4(rotationMatrix * vec2(((position + centering)*translation.zw)) + vec2(translation.xy), 0.0, 1.0); //
	//}
}
