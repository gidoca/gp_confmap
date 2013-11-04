package assignment4;

import helper.Iter;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Vector3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.solver.Solver;

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
			if(v.isOnBoundary()) continue;
			out.setLastRow(v.index, -1);
			float invValence = 1.f / v.getValence();
			for(Vertex n: Iter.ate(v.iteratorVV()))
			{
				out.setLastRow(n.index, invValence);
			}
			
			Collections.sort(out.lastRow());
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
			if(v.isOnBoundary()) continue;
			float sumWeights = 0;
			for(HalfEdge e: Iter.ate(v.iteratorVE()))
			{
				Vertex n = e.start();
				float a1 = e.getNext().getIncidentAngle();
				float cotA1 = (float) (1.f / Math.tan(a1));
				final float CLAMP = 1e2f;
				if(Math.abs(cotA1) > CLAMP) cotA1 = (float) (CLAMP * Math.signum(cotA1));
				float a2 = e.getOpposite().getNext().getIncidentAngle();
				float cotA2 = (float) (1.f / Math.tan(a2));
				if(Math.abs(cotA2) > CLAMP) cotA2 = (float) (CLAMP * Math.signum(cotA2));
				float weight = (cotA1 + cotA2) / (2 * v.mixedArea());
				
				assert(weight*0 == 0);
//				weight = (float) Math.max(weight, 1e-2);
				sumWeights += weight;
				out.setLastRow(n.index, weight);
			}
			out.setLastRow(v.index, -sumWeights);
			
			Collections.sort(out.lastRow());
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
	
	public static ArrayList<Vector3f> solve(CSRMatrix m, HalfEdgeStructure mesh, Solver s)
	{
		ArrayList<Vector3f> x = new ArrayList<>();
		ArrayList<float[]> xArrays = new ArrayList<>();
		ArrayList< ArrayList<Float> > bArrays = new ArrayList<>();
		x.ensureCapacity(mesh.getVertices().size());
		xArrays.ensureCapacity(mesh.getVertices().size());
		bArrays.ensureCapacity(mesh.getVertices().size());
		
		for(int i = 0; i < 3; i++)
		{
			bArrays.add(new ArrayList<Float>());
		}
		
		for(int i = 0; i < mesh.getVertices().size(); i++)
		{
			xArrays.add(new float[3]);
			float[] pos = new float[3];
			mesh.getVertices().get(i).getPos().get(pos);
			for(int j = 0; j < 3; j++)
			{
				bArrays.get(j).add(pos[j]);
			}
		}

		for(int i = 0; i < 3; i++)
		{
			ArrayList<Float> currentX = new ArrayList<>();
			for(int j = 0; j < mesh.getVertices().size(); j++){
				currentX.add(0.f);
			}
			s.solve(m, bArrays.get(i), currentX);
			for(int j = 0; j < mesh.getVertices().size(); j++)
			{
				xArrays.get(j)[i] = currentX.get(j);
			}
		}
		
		for(int i = 0; i < mesh.getVertices().size(); i++)
		{
			Vector3f v = new Vector3f();
			v.set(xArrays.get(i));
			x.add(v);
		}
		
		return x;
	}
}
