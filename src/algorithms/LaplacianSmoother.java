package algorithms;

import meshes.HalfEdgeStructure;
import sparse.CSRMatrix;
import sparse.solver.JMTSolver;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;
import assignment3.SSDMatrices;
import assignment4.LMatrices;

public class LaplacianSmoother extends Smoother {
	private float lambda;
	
	public LaplacianSmoother(HalfEdgeStructure mesh, float lambda) {
		super(mesh);
		this.lambda = lambda;
	}
	
	void computeNew()
	{
		CSRMatrix laplacian = /*LMatrices.uniformLaplacian(mesh);//*/LMatrices.mixedCotanLaplacian(mesh);
		laplacian.scale(-lambda);
		CSRMatrix mat = new CSRMatrix(0, laplacian.nCols);
		mat.add(SSDMatrices.eye(laplacian.nRows, laplacian.nCols), laplacian);
		Solver solver = new SciPySolver("lap");
		newVertices = LMatrices.solve(mat, mesh, solver);
	}
}
