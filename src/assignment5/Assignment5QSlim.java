package assignment5;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class Assignment5QSlim {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		WireframeMesh wf = ObjReader.read("objs/dragon.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		
		GLWireframeMesh glOriginal = new GLWireframeMesh(wf);
		glOriginal.setName("Original");
		glOriginal.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		System.out.println(hs.getVertices().size());
		
		QSlim qslim = new QSlim(hs);
		qslim.simplify(1500);

		System.out.println(hs.getVertices().size());
		
		GLHalfEdgeStructure glDecimated = new GLHalfEdgeStructure(hs);
		glDecimated.setName("Decimated");
		glDecimated.configurePreferredShader("shaders/trimesh_flatColor3f.vert", "shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom");
		
		
		
		MyDisplay d = new MyDisplay();
		d.addToDisplay(glOriginal);
		d.addToDisplay(glDecimated);
	}

}
