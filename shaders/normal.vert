#version 150
// Default vertex shader

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes passed from the main program to shader 
// The position variable corresponds to data passed using
// glDisplayable.addElement(float[], Semantic.POSITION, 3);
in vec4 position;

in vec3 normal;
out vec3 normal_g;

void main()
{
	//compute a color and pass it to the fragment shader.
	normal_g = normal;
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position, this variable has to be computed
	// either in the vertex shader or in the geometry shader, if present.
	gl_Position = projection * modelview * vec4(normal, 1);
}
