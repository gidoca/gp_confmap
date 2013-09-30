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

in float valence;

// Output variables are passed to the fragment shader, or, if existent to the geometry shader.
// They have to be declared as in variables in the next shader.
out vec4 frag_color;

void main()
{
	//compute a color and pass it to the fragment shader.
	frag_color = vec4(valence, valence, valence, 1) / maxvalence;
	if(valence > 6)
	{
		frag_color = vec4(1, 0, 0, 1);
	}
	else if(valence == 6)
	{
		frag_color = vec4(0, 1, 0, 1);
	}
	else
	{
		frag_color = vec4(0, 0, 1, 1);
	}
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position, this variable has to be computed
	// either in the vertex shader or in the geometry shader, if present.
	gl_Position = projection * modelview * position;
}
