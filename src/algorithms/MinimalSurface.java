package algorithms;

import java.util.ArrayList;
import java.util.Collections;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.solver.JMTSolver;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;
import assignment4.LMatrices;

public class MinimalSurface {
	
	private HalfEdgeStructure mesh;
	private float maxError;
	
	public MinimalSurface(HalfEdgeStructure mesh, float maxError) {
		this.mesh = mesh;
		this.maxError = maxError;
	}
	
	public void apply()
	{
		float oldSurfaceArea;
		float newSurfaceArea = mesh.getSurfaceArea();
		int i = 0;
		do {
			applyOnce();
			oldSurfaceArea = newSurfaceArea;
			newSurfaceArea = mesh.getSurfaceArea();
			System.out.print("Surface Area Ratio: ");
			System.out.println(newSurfaceArea / oldSurfaceArea);
			i++;
		} while(newSurfaceArea / oldSurfaceArea < 1 - maxError && i < 100);
	}

	private void applyOnce()
	{
		CSRMatrix a = LMatrices.mixedCotanLaplacian(mesh);
		@SuppressWarnings("unchecked")
		ArrayList<Float>[] b = new ArrayList[3];
		for(int i = 0; i < b.length; i++)
		{
			b[i] = new ArrayList<Float>();
			for(@SuppressWarnings("unused") Vertex v: mesh.getVertices())
			{
				b[i].add(0.f);
			}
		}
		for(Vertex v: mesh.getVertices())
		{
			if(!v.isOnBoundary()) continue;
			float[] posArray = new float[3];
			v.getPos().get(posArray);
			a.set(v.index, v.index, 1);
			for(int i = 0; i < 3; i++)
			{
				b[i].set(v.index, posArray[i]);
			}
		}
		@SuppressWarnings("unchecked")
		ArrayList<Float>[] x = new ArrayList[3];
		
		for(int i = 0; i < 3; i++)
		{
			x[i] = new ArrayList<>(Collections.nCopies(mesh.getVertices().size(), 0.f));
			Solver s = new JMTSolver();
			s.solve(a, b[i], x[i]);
		}
		
		for(Vertex v: mesh.getVertices()) {
			float[] newPos = new float[3];
			for(int i = 0; i < 3; i++)
			{
				newPos[i] = x[i].get(v.index);
			}
			v.getPos().set(newPos);
		}
	}

}
