#version 150
// Shader to paint an Octree

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes; passed from main program to shader 
// via vertex buffer objects
in vec4 position;
in float side;
in vec4 nbr_pos_z, nbr_pos_y, nbr_pos_x;

out vec4 position_g;
out float side_g;
out vec4 nbr_pos_z_g, nbr_pos_y_g, nbr_pos_x_g;

//pass stuff through
void main()
{
	position_g = position;
	side_g = side;
	nbr_pos_z_g = nbr_pos_z;
	nbr_pos_y_g = nbr_pos_y;
	nbr_pos_x_g = nbr_pos_x;
}
