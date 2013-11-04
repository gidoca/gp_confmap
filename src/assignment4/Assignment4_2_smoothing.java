package assignment4;

import java.io.IOException;

import openGL.MyDisplay;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import glWrapper.GLHalfEdgeStructure;
import algorithms.LaplacianSmoother;


/**
 * Smoothing
 * @author Alf
 *
 */
public class Assignment4_2_smoothing {

	//implement the implicit smoothing scheme
	public static void main(String[] args)
	{
		//Load a wireframe mesh
		//WireframeMesh m = ObjReader.read("/home/gidoca/files/uni/ma/code/renderer/qtcreator-build/src/objfiles/cow.obj", false);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			WireframeMesh m = ObjReader.read("objs/bunny5k.obj", true);
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException | IOException e) {
			e.printStackTrace();
			return;
		}

		GLHalfEdgeStructure glMeshDiffuse = new GLHalfEdgeStructure(hs);
		glMeshDiffuse.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");

		
		LaplacianSmoother smoother = new LaplacianSmoother(hs, .01f);
		smoother.apply(-1);
		GLHalfEdgeStructure glMeshSmoothed = new GLHalfEdgeStructure(hs);
		glMeshSmoothed.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		
		MyDisplay d = new MyDisplay();
		d.addToDisplay(glMeshDiffuse);
		d.addToDisplay(glMeshSmoothed);
	}

}
