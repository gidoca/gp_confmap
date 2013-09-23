#version 150
// Simple fragment shader which does some diffuse shading.
// For simplicity, this shader assumes  that the eye of the viewer always lies 
// in the direction (0,0,1)

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec4 color_g;
in vec3 normal_g;

// Output variable, will be written to the display automatically
out vec4 out_color;

void main()
{		
	out_color = vec4((normal_g + vec3(1, 1, 1)) / 2., 1);
}
