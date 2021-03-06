#version 150
// Shader to paint an Octree

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes; passed from main program to shader 
// via vertex buffer objects
in vec4 position;
in float side;
in vec4 parent_pos;

out vec4 position_g;
out float side_g;
out vec4 parent_pos_g;

//pass stuff through
void main()
{
	position_g = position;
	side_g = side;
	parent_pos_g = parent_pos;
}
