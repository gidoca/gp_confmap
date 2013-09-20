package assignment1;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;

import openGL.MyDisplay;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

/**
 * 
 * @author Alf
 *
 */
public class Assignment1 {

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/teapot.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
		
		//... do something with it, display it ....
		GLHalfEdgeStructure glMesh = new GLHalfEdgeStructure(hs);
		glMesh.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		MyDisplay disp = new MyDisplay();
		disp.addToDisplay(glMesh);
	}
	

}
