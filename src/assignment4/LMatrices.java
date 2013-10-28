package assignment4;

import helper.Iter;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;

/**
 * Methods to create different flavours of the cotangent and uniform laplacian.
 * @author Alf
 *
 */
public class LMatrices {
	
	/**
	 * The uniform Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix uniformLaplacian(HalfEdgeStructure hs){
		CSRMatrix out = new CSRMatrix(0, hs.getVertices().size());
		for(Vertex v: hs.getVertices())
		{
			out.addRow();
			out.setLastRow(v.index, 1);
			float invValence = 1.f / v.getValence();
			for(Vertex n: Iter.ate(v.iteratorVV()))
			{
				out.setLastRow(n.index, -invValence);
			}
		}
		return out;
	}
	
	/**
	 * The cotangent Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs){
		CSRMatrix out = new CSRMatrix(0, hs.getVertices().size());
		for(Vertex v: hs.getVertices())
		{
			out.addRow();
			float sumWeights = 0;
			for(HalfEdge e: Iter.ate(v.iteratorVE()))
			{
				Vertex n = e.start();
				float a1 = e.getNext().getIncidentAngle();
				float cotA1 = (float) (1.f / Math.tan(a1));
				float a2 = e.getOpposite().getNext().getIncidentAngle();
				float cotA2 = (float) (1.f / Math.tan(a2));
				float weight = (cotA1 + cotA2) / (2 * v.mixedArea());
//				weight = (float) Math.max(weight, 1e-2);
				sumWeights += weight;
				out.setLastRow(n.index, -weight);
			}
			out.setLastRow(v.index, sumWeights);
		}
		return out;
	}
	
	/**
	 * A symmetric cotangent Laplacian, cf Assignment 4, exercise 4.
	 * @param hs
	 * @return
	 */
	public static CSRMatrix symmetricCotanLaplacian(HalfEdgeStructure hs){
		return null;
	}
	
	
	/**
	 * helper method to multiply x,y and z coordinates of the halfedge structure at once
	 * @param m
	 * @param s
	 * @param res
	 */
	public static void mult(CSRMatrix m, HalfEdgeStructure s, ArrayList<Vector3f> res){
		ArrayList<Float> x = new ArrayList<>(), b = new ArrayList<>(s.getVertices().size());
		x.ensureCapacity(s.getVertices().size());
		
		res.clear();
		res.ensureCapacity(s.getVertices().size());
		for(Vertex v : s.getVertices()){
			x.add(0.f);
			res.add(new Vector3f());
		}
		
		for(int i = 0; i < 3; i++){
			
			//setup x
			for(Vertex v : s.getVertices()){
				switch (i) {
				case 0:
					x.set(v.index, v.getPos().x);	
					break;
				case 1:
					x.set(v.index, v.getPos().y);	
					break;
				case 2:
					x.set(v.index, v.getPos().z);	
					break;
				}
				
			}
			
			m.mult(x, b);
			
			for(Vertex v : s.getVertices()){
				switch (i) {
				case 0:
					res.get(v.index).x = b.get(v.index);	
					break;
				case 1:
					res.get(v.index).y = b.get(v.index);	
					break;
				case 2:
					res.get(v.index).z = b.get(v.index);	
					break;
				}
				
			}
		}
	}
}
