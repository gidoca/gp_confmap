package assignment5;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable.Semantic;
import algorithms.DegenerateTriangleRemover;

public class Assignment5RemoveDenegerate {

	public static void main(String[] args) throws Exception{
		WireframeMesh wf = ObjReader.read("objs/buddha.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		

		System.out.println(hs.getVertices().size());
		
		WireframeMesh collapsedWF = new WireframeMesh();
		collapsedWF.vertices = wf.vertices;
		collapsedWF.faces = new ArrayList<int[]>();
		WireframeMesh nonCollapsedWF = new WireframeMesh();
		nonCollapsedWF.vertices = wf.vertices;
		nonCollapsedWF.faces = new ArrayList<int[]>(wf.faces);

		DegenerateTriangleRemover dtr = new DegenerateTriangleRemover(hs, .0001f);
		dtr.apply();
		System.out.println(hs.getVertices().size());
		
		
		
		GLWireframeMesh glNonCollapsed = new GLWireframeMesh(nonCollapsedWF);
		glNonCollapsed.addElement(new ArrayList<>(Collections.nCopies(wf.vertices.size(), new Vector3f(0, 0, 1))), Semantic.USERSPECIFIED, "color");
		glNonCollapsed.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		/*GLWireframeMesh glCollapsed = new GLWireframeMesh(collapsedWF);
		glCollapsed.addElement(new ArrayList<>(Collections.nCopies(wf.vertices.size(), new Vector3f(1, 0, 0))), Semantic.USERSPECIFIED, "color");
		glCollapsed.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");*/
		
		GLHalfEdgeStructure glNew = new GLHalfEdgeStructure(hs);
		glNew.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		MyDisplay d = new MyDisplay();
		d.addToDisplay(glNonCollapsed);
		//d.addToDisplay(glCollapsed);
		d.addToDisplay(glNew);
	}
}
