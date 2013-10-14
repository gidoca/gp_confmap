package assignment3;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.PointCloud;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.LinearSystem;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;
import assignment2.MortonCodes;


public class SSDMatrices {
	
	
	/**
	 * Example Matrix creation:
	 * Create an identity matrix, clamped to the provided format.
	 */
	public static CSRMatrix eye(int nRows, int nCols){
		CSRMatrix eye = new CSRMatrix(0, nCols);
		
		//initialize the identiti matrix part
		for(int i = 0; i< Math.min(nRows, nCols); i++){
			eye.addRow();
			eye.lastRow().add(
						//column i, vlue 1
					new col_val(i,1));
		}
		//fill up the matrix with empt rows.
		for(int i = Math.min(nRows, nCols); i < nRows; i++){
			eye.addRow();
		}
		
		return eye;
	}
	
	
	/**
	 * Example matrix creation:
	 * Identity matrix restricted to boundary per vertex values.
	 */
	public static CSRMatrix Eye_octree_boundary(HashOctree tree){
		
		CSRMatrix result = new CSRMatrix(0, tree.numberofVertices());
				
		for(HashOctreeVertex v : tree.getVertices()){
			if(MortonCodes.isVertexOnBoundary(v.code, tree.getDepth())){
				result.addRow();
				result.lastRow().add(new col_val(v.index,1));
			}
		}
		
		return result;
	}
	
	/**
	 * One line per point, One column per vertex,
	 * enforcing that the interpolation of the Octree vertex values
	 * is zero at the point position.
	 *
	 */
	public static CSRMatrix D0Term(HashOctree tree, PointCloud cloud){
		CSRMatrix out = new CSRMatrix(cloud.points.size(), tree.numberofVertices());
		for(int i = 0; i < out.nRows; i++)
		{
			Point3f p = cloud.points.get(i);
			HashOctreeCell cell = tree.getCell(p);
			for(int j = 0b000; j <= 0b111; j++)
			{
				HashOctreeVertex v = cell.getCornerElement(j, tree);
				Vector3f weights = new Vector3f(p);
				weights.sub(v.getPosition());
				weights.absolute();
				float weight = (1 - weights.x / cell.side) * (1 - weights.y / cell.side) * (1 - weights.z / cell.side);
				out.set(i, v.index, weight);
			}
		}
		return out;
	}

	/**
	 * matrix with three rows per point and 1 column per octree vertex.
	 * rows with i%3 = 0 cover x gradients, =1 y-gradients, =2 z gradients;
	 * The row i, i+1, i+2 corresponds to the point/normal i/3.
	 * Three consecutive rows belong to the same gradient, the gradient in the cell
	 * of pointcloud.point[row/3]; 
	 */
	public static CSRMatrix D1Term(HashOctree tree, PointCloud cloud, ArrayList<Float> rhs) {
		CSRMatrix out = new CSRMatrix(3 * cloud.points.size(), tree.numberofVertices());
		for(int i = 0; i < cloud.points.size(); i++)
		{
			Point3f p = cloud.points.get(i);
			HashOctreeCell cell = tree.getCell(p);
			float[] normal = new float[3];
			cloud.normals.get(i).get(normal);
			for(int j = 0; j < 3; j++)
			{
				int axis = 0b1 << j;
				for(long k = 0b000; k <= 0b111; k = ((k | axis) + 1) & ~axis)
				{
					HashOctreeVertex v1 = cell.getCornerElement((int)k, tree);
					out.set(3 * i + j, v1.getIndex(), -1.f / (4 * cell.side));
					HashOctreeVertex v2 = cell.getCornerElement((int)k | axis, tree);
					out.set(3 * i + j, v2.getIndex(), 1.f / (4 * cell.side));
				}
				rhs.add(normal[j]);
			}
		}
		return out;
	}
	
	
	
	public static CSRMatrix RTerm(HashOctree tree){
		//TODO implement
		return new CSRMatrix(0, tree.numberofVertices());
	}

	
	


	/**
	 * Set up the linear system for ssd: append the three matrices, 
	 * appropriately scaled. And set up the appropriate right hand side, i.e. the
	 * b in Ax = b
	 * @param tree
	 * @param pc
	 * @param lambda0
	 * @param lambda1
	 * @param lambda2
	 * @return
	 */
	public static LinearSystem ssdSystem(HashOctree tree, PointCloud pc, 
			float lambda0,
			float lambda1,
			float lambda2){
		
				
		LinearSystem system = new LinearSystem();
		system.mat = new CSRMatrix(0, tree.numberofVertices());
		system.b = new ArrayList<Float>();
		
		CSRMatrix d0 = D0Term(tree, pc);
		system.mat.append(d0, lambda0);
		system.b.addAll(Collections.nCopies(d0.nRows, 0.f));
		
		ArrayList<Float> d1Rhs = new ArrayList<Float>();
		CSRMatrix d1 = D1Term(tree, pc, d1Rhs);
		system.mat.append(d1, lambda1);
		system.b.addAll(d1Rhs);
		
		CSRMatrix r = RTerm(tree);
		system.mat.append(r, lambda2);
		system.b.addAll(Collections.nCopies(r.nRows, 0.f));
		return system;
	}

}
