package assignment4;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLHalfedgeStructure;
import glWrapper.VertexAttribute;

import java.util.Iterator;

import javax.vecmath.Vector3f;

import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import openGL.MyDisplay;
import algorithms.MinimalSurface;
import assignment4.generatedMeshes.Bock;

public class Assignment4_3_minimalSurfaces {
	
	
	public static void main(String[] args) throws Exception{
		
		//generate example meshes
		WireframeMesh m = new Bock(1.3f,1.f,1.f).result;
		//WireframeMesh m = new Cylinder(1.f,1.5f).result;
		
		
		//generate he struture
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(m);
	
		//collect and display the boundary
		HEData1d boundary = collectBoundary(hs, 1);
		display(hs, boundary);
		
		MinimalSurface min = new MinimalSurface(hs, 0.01f);
		min.apply();
		
		display(hs, boundary);
	}
	

	/**
	 * Display the halfedge structure and highlight 
	 * the set of vertices described by boundary
	 * @param hs
	 * @param boundary
	 */
	public static void display(HalfEdgeStructure hs, HEData1d boundary) {
		MyDisplay disp = new MyDisplay();
		HEData3d colors = binaryColorMap(boundary, hs);
		
		GLHalfedgeStructure glHE = new GLHalfedgeStructure(hs);
		glHE.add(colors, "color");
		glHE.configurePreferredShader("shaders/trimesh_flatColor3f.vert", 
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");
		disp.addToDisplay(glHE);

		GLHalfEdgeStructure glMeshCurvature = new GLHalfEdgeStructure(hs);
		glMeshCurvature.addElement(1, "curvature", new VertexAttribute() {
			
			@Override
			public float[] getAttribute(Vertex v) {
				return new float[]{v.getCurvature()};
			}
		});
		glMeshCurvature.configurePreferredShader("shaders/curvature.vert", 
				"shaders/default.frag");
		disp.addToDisplay(glMeshCurvature);
}



	/**
	 * Collect the boundary: this method returns a HEData1d object containing a
	 * 1 for each vertex that is maximally dist number of vertices away from the boundary
	 * @param hs
	 * @param dist
	 * @return
	 */
	public static HEData1d collectBoundary(HalfEdgeStructure hs, int dist) {
		
		HEData1d has_jm1_dist = new HEData1d(hs);
		for(Vertex v : hs.getVertices()){
			if(isOnBoundary(v)){
				has_jm1_dist.put(v, new Integer(1));
			}
		}
		
		Vertex temp;
		HEData1d has_j_dist = new HEData1d(hs);
		for(int j = 0; j <dist; j++){
			for(Vertex v : hs.getVertices()){
				Iterator<Vertex> it = v.iteratorVV();
				while(it.hasNext()){
					temp = it.next();
					if(has_jm1_dist.get(temp) != null){
						has_j_dist.put(v, new Integer(1));
					}
				}
			}
			
			HEData1d tmp = has_jm1_dist;
			has_jm1_dist = has_j_dist;
			has_j_dist = tmp;
			
		}
		
		return has_jm1_dist;
	}

	private static boolean isOnBoundary(Vertex v) {
		Iterator<HalfEdge> it = v.iteratorVE();
		while(it.hasNext()){
			if(it.next().isOnBorder()){
				return true;
			}
		}
		return false;
	}
	
	public static HEData3d binaryColorMap(HEData1d boundary, HalfEdgeStructure hs) {
		HEData3d result = new HEData3d(hs);
		for(Vertex v: hs.getVertices()){
			if(boundary.get(v) != null){
				result.put(v, new Vector3f(0.9f,0.2f,0.2f));
			}
			else{
				result.put(v, new Vector3f(0.4f,0.4f,0.9f));
			}
		}
		
		return result;
	}
}
