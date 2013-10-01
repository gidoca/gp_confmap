#version 150
// Default vertex shader

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

uniform float maxvalence;

// Input vertex attributes passed from the main program to shader 
// The position variable corresponds to data passed using
// glDisplayable.addElement(float[], Semantic.POSITION, 3);
in vec4 position;

in float curvature;

// Output variables are passed to the fragment shader, or, if existent to the geometry shader.
// They have to be declared as in variables in the next shader.
out vec4 frag_color;

void main()
{
	float curv_log = log(1 + curvature/10);
	frag_color = vec4(curv_log - 1,
					 curv_log,
					 1 - curv_log,
					 0);
	if (curv_log > 1) {
		//frag_color = vec4(1,1,1,0);
		frag_color.y = 2 - curv_log;
	}
	frag_color = clamp(frag_color, 0, 1);
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position, this variable has to be computed
	// either in the vertex shader or in the geometry shader, if present.
	gl_Position = projection * modelview * position;
}
