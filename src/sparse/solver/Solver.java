package sparse.solver;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Vector3f;

import sparse.CSRMatrix;
import sparse.LinearSystem;

public abstract class Solver {
	
	/**
	 * x will be used as an initial guess, the result will be stored in x
	 * @param mat
	 * @param b
	 * @param x
	 */
	public abstract void solve(CSRMatrix mat, ArrayList<Float> b,ArrayList<Float> x);
	
	
	public void solve(LinearSystem l, ArrayList<Float> x){
		if(l.mat.nCols == l.mat.nRows){
			solve(l.mat, l.b, x);
		}
		else{
			throw new UnsupportedOperationException("can solve only square mats");
		}
	}
	
	
	public ArrayList<Vector3f> solve(CSRMatrix mat, ArrayList<Vector3f> b)
	{
		assert(b.size() == mat.nRows);
		ArrayList<float[]> resA = new ArrayList<float[]>();
		resA.ensureCapacity(mat.nCols);
		ArrayList<Vector3f> out = new ArrayList<Vector3f>();
		out.ensureCapacity(mat.nCols);
		for(int i = 0; i < mat.nCols; i++)
		{
			out.add(new Vector3f());
			resA.add(new float[3]);
		}
		
		for(int i = 0; i < 3; i++)
		{
			ArrayList<Float> bs = new ArrayList<Float>();
			bs.ensureCapacity(mat.nRows);
			for(int j = 0; j < mat.nRows; j++)
			{
				float[] values = new float[3];
				b.get(j).get(values);
				bs.add(values[i]);
			}
			ArrayList<Float> res = new ArrayList<Float>(Collections.nCopies(bs.size(), 0.f));
			solve(mat, bs, res);
			for(int j = 0; j < mat.nCols; j++)
			{
				resA.get(j)[i] = res.get(j);
			}
			System.out.println("Fuck you!!!");
		}
		
		for(int i = 0; i < mat.nCols; i++)
		{
			out.get(i).set(resA.get(i));
		}
		return out;
	}

}
