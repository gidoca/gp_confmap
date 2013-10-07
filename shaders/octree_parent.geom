#version 150
// input: an octree center and a side length
// output: linesegments outlining the octree cube

uniform mat4 projection; 
uniform mat4 modelview;

layout(points) in;
layout(line_strip, max_vertices = 18) out;

in vec4 position_g[];
in float side_g[];
in vec4 parent_pos_g[];
out vec4 color_g;

void main()
{		
	gl_PrimitiveID = gl_PrimitiveIDIn;
	color_g = vec4(0, 0, 0, 1);	
	
	gl_Position = projection*modelview*(position_g[0]);
	EmitVertex();
	gl_Position = projection*modelview*(parent_pos_g[0]);
	EmitVertex();
	
}
