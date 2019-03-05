#version 150

in vec2 pass_textureCoords;
in vec3 pass_normals;
in vec3 pass_position;

uniform sampler2D grass;
uniform sampler2D gravel;
uniform sampler2D dirt;
uniform vec3 lightDirection;

const int MAX_TRAILS = 15;

uniform vec3[15] trailProperties;
uniform vec2[MAX_TRAILS] trailData;

out vec4 out_color;

/* Lighting */
const vec2 lightBias = vec2(0.3, 0.7);//just indicates the balance between diffuse and ambient lighting

in vec4 shadowCoords;
uniform sampler2D lmap;

/* TRAIL DATA NOTES */
// x,y = position
// z = width, 0 = dont draw
// w = texture

float DistanceSquared(vec2 p, vec2 q) {
    float dX = q.x - p.x;
	float dY = q.y - p.y;
    return dX * dX + dY * dY;
}

float Dist(vec2 point, vec4 line) {
	float distSq = DistanceSquared(line.xy, line.zw);
	float t = dot((point - line.xy), (line.zw - line.xy)) / distSq;
    if ( t < 0.0  ){
        return (DistanceSquared(point, line.xy));
    } else if ( t > 1.0 ) {
        return (DistanceSquared(point, line.zw));
    }
    
    vec2 proj = line.xy + ( ( line.zw - line.xy ) * t );
    return (DistanceSquared(point, proj));
}

void main(void){
	vec4 finalColor = vec4(0.0,0.0,0.0,1.0);
	
	if (pass_position.y < 0.0) {
		if (pass_position.y < -16.0) {
			vec4 grassCol = texture(dirt, pass_textureCoords);	
			vec4 gravelCol = texture(gravel, pass_textureCoords);	
		
			finalColor = mix(grassCol, gravelCol, min(-((pass_position.y+16)/5.0),1.0));
		} else {
			vec4 grassCol = texture(grass, pass_textureCoords);	
			vec4 gravelCol = texture(dirt, pass_textureCoords);	
		
			finalColor = mix(grassCol, gravelCol, min(-(pass_position.y/5.0),1.0));
		}
	} else {
		vec4 grassCol = texture(grass, pass_textureCoords);	
		
		float d = 0.0, dNew = 0.0, dOrig = 0.0;
		int trailGfx = int(trailProperties[0].x), trailMix = -1;
		float width = trailProperties[0].y;
		bool hit = false;
		float dist = 99999.9;
		int counter = 0, propertyId = 0;
		
		for(int i = 0; i < MAX_TRAILS-1; i++) {
			dist = Dist(pass_position.xz, vec4(trailData[i].xy, trailData[i+1].xy))/width;
			
			
			if (dist < 50) {
				d = max(d, 1.0-(dist/50));
				if (trailMix == -1 && propertyId != 0) { // this hsould check if the trailGfx has changed this is temp
					trailMix = trailGfx;
					trailGfx = int(trailProperties[propertyId].x);
					
				}
		
				if (trailMix != -1) {
					dNew = d;
				} else {
					dOrig = d;
				}
			}
			
			counter++;
			
			if (counter == trailProperties[propertyId].z-1) {
				counter = 0;
				propertyId++;
				
				width = trailProperties[propertyId].y;
				i++;
			}
		}
		
		vec4 trailCol, trailCol2;
		switch(trailGfx) {
			case 1:  trailCol = texture(dirt, pass_textureCoords); break;
			case 2:  trailCol = texture(gravel, pass_textureCoords); break;
			default: trailCol = texture(gravel, pass_textureCoords);
		}
		
		if (trailMix == -1) {
			trailCol2 = vec4(0.0);
		} else {
			switch(trailMix) {
				case 1:  trailCol2 = texture(dirt, pass_textureCoords); break;
				case 2:  trailCol2 = texture(gravel, pass_textureCoords); break;
				default: trailCol2 = texture(gravel, pass_textureCoords);
			}
		}
		
		
		
		if (trailCol2.a != 0.0) {// != 0.0
			finalColor = mix(mix(grassCol, trailCol, dNew), trailCol2, dOrig);
		} else {
			finalColor = mix(grassCol, trailCol, dOrig);
		}
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
	
	// Shadows
	float nearestLight = texture(lmap, shadowCoords.xy).r;

	float lightFactor = 1.0;
	if (shadowCoords.z > nearestLight + 0.01) {
		lightFactor = 1.0 - (shadowCoords.w*0.8);
	}

	out_color *= min(lightFactor,diffuseLight);
}
