package assignment4;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;

import algorithms.LaplacianSmoother;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class Assignment4_2_uMasking {

	public static void main(String[] arg) throws IOException{
		headDemo();
					
	}

	private static void headDemo() throws IOException {
		WireframeMesh m = ObjReader.read("./objs/head.obj", true);//*/
		
		MyDisplay disp = new MyDisplay();
		GLWireframeMesh glwf = new GLWireframeMesh(m);
		glwf.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf);
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
			try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
		GLHalfEdgeStructure glMeshDiffuse = new GLHalfEdgeStructure(hs);
		glMeshDiffuse.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");

			
		LaplacianSmoother smoother = new LaplacianSmoother(hs, .01f);
		smoother.apply(1);
		GLHalfEdgeStructure glMeshSmoothed = new GLHalfEdgeStructure(hs);
		glMeshSmoothed.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
			
			
		MyDisplay d = new MyDisplay();
		d.addToDisplay(glMeshDiffuse);
		d.addToDisplay(glMeshSmoothed);
	}

}
