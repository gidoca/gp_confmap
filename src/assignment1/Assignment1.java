package assignment1;

import glWrapper.GLHalfEdgeStructure;

import java.io.IOException;
import java.util.Iterator;

import openGL.MyDisplay;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
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
		WireframeMesh m = ObjReader.read("./objs/dragon.obj", true);
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
		GLHalfEdgeStructure glMeshDiffuse = new GLHalfEdgeStructure(hs);
		glMeshDiffuse.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		
		//... do something with it, display it ....
		GLHalfEdgeStructure glMeshValence = new GLHalfEdgeStructure(hs);
		glMeshValence.configurePreferredShader("shaders/valence.vert", 
				"shaders/default.frag");
		
		
		MyDisplay disp = new MyDisplay();
		disp.addToDisplay(glMeshDiffuse);
		disp.addToDisplay(glMeshValence);
		
		
		
		
		
		/*WireframeMesh oneNeighbourhood = ObjReader.read("./objs/oneNeighborhood.obj", true);
		HalfEdgeStructure oneNeighbourhoodHS = new HalfEdgeStructure();
		
		try {
			oneNeighbourhoodHS.init(oneNeighbourhood);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		oneNeighbourhoodHS.enumerateVertices();
		
		Vertex center = oneNeighbourhoodHS.getVertices().get(0);
		Iterator<HalfEdge> iteratorVE = center.iteratorVE();
		for(HalfEdge he = null; iteratorVE.hasNext(); )
		{
			he = iteratorVE.next();
			System.out.println(he);
		}
		Iterator<Vertex> iteratorVV = center.iteratorVV();
		for(Vertex he = null; iteratorVV.hasNext(); )
		{
			he = iteratorVV.next();
			System.out.println(he);
		}
		Iterator<Face> iteratorVF = center.iteratorVF();
		for(Face he = null; iteratorVF.hasNext(); )
		{
			he = iteratorVF.next();
			System.out.println(he);
		}*/
	}
	

}
