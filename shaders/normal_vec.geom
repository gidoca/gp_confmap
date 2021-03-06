#version 150
// input from the vertex shader:  the positions, laid out as triangles
// output to the fragment shader: a triangle and its normal

//the projection matrix set by the main program
uniform mat4 projection; 
uniform mat4 modelview;

//In a geometry shader you have to specify how the input and output have to be interpreted
//note that the maximum number of vertices passed to the fragment shader has
//to be fixed.
layout(triangles) in;
layout(line_strip, max_vertices = 2) out;

//the variable passed in from the vertex shader
//in geometry shaders the input is always organized as an array
in vec4 position_g[];

in vec3 normal_g[];

//the variables passed out to the fragment shader
out vec4 color_g;

void main()
{		
	//use a constant color
	color_g = vec4(0.2f,0.2f,0.8f,1.f);
	
	//and some ID which can be left untouched but has to be passed explicitely.
	gl_Position = projection * modelview * position_g[0];		
	EmitVertex();
	
	gl_Position = projection * modelview * (position_g[0] + vec4(.1 * normalize(normal_g[0]), 0));		
	EmitVertex();
			
	gl_PrimitiveID = gl_PrimitiveIDIn;
	
	EndPrimitive();
	
	
}
