package assignment5;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;
import helper.Iter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable.Semantic;

public class Assignment5RandomCollapse {

	public static final float collapseRatio = .5f;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		WireframeMesh wf = ObjReader.read("objs/bunny5k.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		

		System.out.println(hs.getVertices().size());
		
		WireframeMesh collapsedWF = new WireframeMesh();
		collapsedWF.vertices = new ArrayList<>();
		for(Vertex v: hs.getVertices())
		{
			collapsedWF.vertices.add(v.getPos());
		}
		collapsedWF.faces = new ArrayList<int[]>();
		WireframeMesh nonCollapsedWF = new WireframeMesh();
		nonCollapsedWF.vertices = collapsedWF.vertices;
		nonCollapsedWF.faces = new ArrayList<int[]>();
		for(Face f: hs.getFaces())
		{
			int[] indices = new int[3];
			int i = 0;
			for(Vertex v: Iter.ate(f.iteratorFV()))
			{
				indices[i++] = v.index;
			}
			nonCollapsedWF.faces.add(indices);
		}

		HalfEdgeCollapse collaptor = new HalfEdgeCollapse(hs);
		Random ran = new Random(1);
		final int numV = hs.getVertices().size();
		for(int i = 0; i < collapseRatio * numV; i++)
		{
			int index = ran.nextInt(hs.getHalfEdges().size());
			HalfEdge e = hs.getHalfEdges().get(index);
			if(collaptor.isEdgeDead(e)) continue;
			if(collaptor.isCollapseMeshInv(e, e.end().getPos()))
			{
				e = e.getOpposite();
			}
			if(!HalfEdgeCollapse.isEdgeCollapsable(e) || collaptor.isCollapseMeshInv(e, e.end().getPos()))
			{
				continue;
			}
			for(int j = 0; j < nonCollapsedWF.faces.size(); j++)
			{
				boolean containsStart = false, containsEnd = false;
				int[] vIndices = nonCollapsedWF.faces.get(j);
				for(int k = 0; k < vIndices.length; k++)
				{
					if(vIndices[k] == e.start().index) containsStart = true;
					if(vIndices[k] == e.end().index) containsEnd = true;
				}
				if(containsStart && containsEnd)
				{
					collapsedWF.faces.add(nonCollapsedWF.faces.remove(j));
				}
			}
			collaptor.collapseEdge(e);
		}
		collaptor.finish();
		
		System.out.println(hs.getVertices().size());
		
		
		
		GLWireframeMesh glNonCollapsed = new GLWireframeMesh(nonCollapsedWF);
		glNonCollapsed.addElement(new ArrayList<>(Collections.nCopies(nonCollapsedWF.vertices.size(), new Vector3f(0, 0, 1))), Semantic.USERSPECIFIED, "color");
		glNonCollapsed.configurePreferredShader("shaders/trimesh_flatColor3f.vert", 
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");
		
		GLWireframeMesh glCollapsed = new GLWireframeMesh(collapsedWF);
		glCollapsed.addElement(new ArrayList<>(Collections.nCopies(wf.vertices.size(), new Vector3f(1, 0, 0))), Semantic.USERSPECIFIED, "color");
		glCollapsed.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		GLHalfEdgeStructure glNew = new GLHalfEdgeStructure(hs);
		glNew.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		MyDisplay d = new MyDisplay();
		d.addToDisplay(glNonCollapsed);
		d.addToDisplay(glCollapsed);
		d.addToDisplay(glNew);
		
		
	}

}
