package algorithms;

import java.io.IOException;
import java.util.ArrayList;

import assignment4.LMatrices;
import sparse.CSRMatrix;
import sparse.SCIPYEVD;
import meshes.HalfEdgeStructure;

public class SpectralSmoother extends Smoother {
	
	private int nEV;
	
	private FloatFunction kernel;
	
	public SpectralSmoother(HalfEdgeStructure mesh, int nEV, FloatFunction kernel)
	{
		super(mesh);
		this.nEV = nEV;
		this.kernel = kernel;
	}

	public SpectralSmoother(HalfEdgeStructure mesh, int nEV) {
		super(mesh);
		this.nEV = nEV;
		this.kernel = new FloatFunction() {
			
			@Override
			public float f(float x) {
				return 1;
			}
		};
	}

	@Override
	void computeNew() {
		CSRMatrix laplacian = LMatrices.symmetricCotanLaplacian(mesh);
		ArrayList< ArrayList<Float> > evs = new ArrayList<>();
		ArrayList<Float> evals = new ArrayList<Float>();
		try {
			//SCIPYEVD.loadResults("spectralsmoothing", nEV, evals, evs);
			SCIPYEVD.doSVD(laplacian, "spectralsmoothing", nEV, evals, evs);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		CSRMatrix evMatrix = new CSRMatrix(laplacian.nRows, nEV);
		for(int i = 0; i < nEV; i++)
		{
			evMatrix.setVec(i, evs.get(i));
		}
		CSRMatrix evMatrixT = evMatrix.transposed();
		CSRMatrix kernel = new CSRMatrix(nEV, nEV);
		for(int i = 0; i < nEV; i++)
		{
			kernel.set(i, i, this.kernel.f(evals.get(i)));
		}
		CSRMatrix temp = new CSRMatrix(0, nEV);
		evMatrix.multParallel(kernel, temp);
		CSRMatrix transform = new CSRMatrix(0, laplacian.nCols);
		temp.multParallel(evMatrixT, transform);
		LMatrices.mult(transform, mesh, newVertices);
	}

}
