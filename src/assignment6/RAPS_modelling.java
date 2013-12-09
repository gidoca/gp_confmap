package assignment6;

import helper.Iter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import assignment4.LMatrices;



/**
 * As rigid as possible deformations.
 * @author Alf
 *
 */
public class RAPS_modelling {
	
	private static final float userWeight = 100; 

	//ArrayList containing all optimized rotations,
	//keyed by vertex.index
	ArrayList<Matrix3f> rotations;
	
	//A copy of the original half-edge structure. This is needed  to compute the correct
	//rotation matrices.
	private HalfEdgeStructure hs_originl;
	//The halfedge structure being deformed
	private HalfEdgeStructure hs_deformed;
	
	//The unnormalized cotan weight matrix, with zero rows for
	//boundary vertices.
	//It can be computed once at setup time and then be reused
	//to compute the matrix needed for position optimization
	CSRMatrix L_cotan;
	CSRMatrix L_transposed;
	//The matrix used when solving for optimal positions
	CSRMatrix L_deform;
	
	//allocate righthand sides and x only once.
	ArrayList<Vector3f> b;
	ArrayList<Float> x;

	//sets of vertex indices that are constrained.
	private HashSet<Integer> keepFixed;
	private HashSet<Integer> deform;

	private Cholesky cotanCholesky;
	
	
	
	/**
	 * The mesh to be deformed
	 * @param hs
	 */
	public RAPS_modelling(HalfEdgeStructure hs){
		this.hs_originl = new HalfEdgeStructure(hs); //deep copy of the original mesh
		this.hs_deformed = hs;
		
		this.keepFixed = new HashSet<>();
		this.deform = new HashSet<>();
		
		
		init_b_x(hs);
		
	}
	
	/**
	 * Set which vertices should be kept fixed. 
	 * @param verts_idx
	 */
	public void keep(Collection<Integer> verts_idx) {
		this.keepFixed.clear();
		this.keepFixed.addAll(verts_idx);
		System.out.println(verts_idx);

	}
	
	/**
	 * constrain these vertices to the new target position
	 */
	public void target(Collection<Integer> vert_idx){
		this.deform.clear();
		this.deform.addAll(vert_idx);
	}
	
	
	/**
	 * update the linear system used to find optimal positions
	 * for the currently constrained vertices.
	 * Good place to do the cholesky decompositoin
	 */
	public void updateL() {
		rotations = new ArrayList<Matrix3f>();
		rotations.ensureCapacity(hs_originl.getVertices().size());
		for(Vertex v: hs_originl.getVertices())
		{
			if(v.isOnBoundary())
			{
				keepFixed.add(v.index);
			}
			Matrix3f m = new Matrix3f();
			m.setIdentity();
			rotations.add(m);
		}
		L_cotan = LMatrices.unweightedCotanLaplacian(hs_originl);
		//L_cotan.scale(-1);
		L_transposed = L_cotan.transposed();
		CSRMatrix L_nouser = new CSRMatrix(L_cotan.nRows, L_cotan.nCols);
		L_transposed.multParallel(L_cotan, L_nouser);
		CSRMatrix I_constr = new CSRMatrix(L_cotan.nRows, L_cotan.nCols);
		for(int i: keepFixed)
		{
			I_constr.set(i, i, userWeight * userWeight);
		}
		for(int i: deform)
		{
			I_constr.set(i, i, userWeight * userWeight);
		}
		L_deform = new CSRMatrix(0, 0);
		L_deform.add(I_constr, L_nouser);
		assert(L_deform.isSymmetric(0.01f));
		cotanCholesky = new Cholesky(L_deform);
	}
	
	/**
	 * The RAPS modelling algorithm.
	 * @param t
	 * @param nRefinements
	 */
	public void deform(Matrix4f t, int nRefinements){
		this.transformTarget(t);
		
		for(int i = 0; i < nRefinements; i++)
		{
			optimalPositions();
			optimalRotations();
		}
	}
	

	/**
	 * Method to transform the target positions and do nothing else.
	 * @param t
	 */
	public void transformTarget(Matrix4f t) {
		for(Vertex v : hs_deformed.getVertices()){
			if(deform.contains(v.index)){
				t.transform(v.getPos());
			}
		}
	}
	
	
	/**
	 * ArrayList keyed with the vertex indices.
	 * @return
	 */
	public ArrayList<Matrix3f> getRotations() {
		return rotations;
	}

	/**
	 * Getter for undeformed version of the mesh
	 * @return
	 */
	public HalfEdgeStructure getOriginalCopy() {
		return hs_originl;
	}
	
	

	/**
	 * initialize b and x
	 * @param hs
	 */
	private void init_b_x(HalfEdgeStructure hs) {
		b = new ArrayList<Vector3f>();
		x = new ArrayList<>(hs.getVertices().size());
		for(int j = 0; j < hs.getVertices().size(); j++){
			x.add(0.f);
			b.add(new Vector3f());
		}
	}
	
	
	
	/**
	 * Compute optimal positions for the current rotations.
	 */
	public void optimalPositions()
	{
		compute_b();
		
		ArrayList<Vector3f> rhs = L_transposed.mult(b);
		for(int i = 0; i < rhs.size(); i++)
		{
			if(keepFixed.contains(i) || deform.contains(i))
			{
				Vector3f x = rhs.get(i);
				Vector3f pos = new Vector3f(hs_deformed.getVertices().get(i).getPos());
				pos.scale(userWeight * userWeight);
				x.add(pos);
			}
		}
		
		ArrayList<Vector3f> res = cotanCholesky.solve(L_deform, rhs);
		
		for(int i = 0; i < res.size(); i++)
		{
			hs_deformed.getVertices().get(i).getPos().set(res.get(i));
		}
	}
	

	/**
	 * compute the righthand side for the position optimization
	 */
	private void compute_b() {
		for(Vertex v: hs_originl.getVertices())
		{
			Vector3f b = new Vector3f();
			
			for(Vertex n: Iter.ate(v.iteratorVV()))
			{
				float w = L_cotan.get(v.index, n.index);
				//float w = 0;
				if(w == 0) continue;
				Matrix3f r = new Matrix3f(rotations.get(v.index));
				r.add(rotations.get(n.index));
				Vector3f p = new Vector3f(v.getPos());
				p.sub(n.getPos());
				r.transform(p);
				p.scale(-.5f * w);
				b.add(p);
			}
			
			this.b.set(v.index, b);
		}
	}



	/**
	 * Compute the optimal rotations for 1-neighborhoods, given
	 * the original and deformed positions.
	 */
	public void optimalRotations() {
		//for the svd.
		Linalg3x3 l = new Linalg3x3(3);// argument controls number of iterations for ed/svd decompositions 
										//3 = very low precision but high speed. 3 seems to be good enough
			
		//Note: slightly better results are achieved when the absolute of cotangent
		//weights w_ij are used instead of plain cotangent weights.		
			
		//do your stuff..
		for(Vertex v: hs_deformed.getVertices())
		{
			Matrix3f transform = new Matrix3f();
			Point3f vPos = hs_originl.getVertices().get(v.index).getPos();
			Point3f vPrimePos = v.getPos();
			for(Vertex n: Iter.ate(v.iteratorVV()))
			{
				float w = Math.abs(L_cotan.get(v.index, n.index));
				Vector3f e = new Vector3f(vPos);
				e.sub(hs_originl.getVertices().get(n.index).getPos());
				Vector3f ePrime = new Vector3f(vPrimePos);
				ePrime.sub(n.getPos());
				Matrix3f eeT = new Matrix3f();
				compute_ppT(e, ePrime, eeT);
				eeT.mul(w);
				transform.add(eeT);
			}
			
			
			Matrix3f u1 = new Matrix3f();
			Matrix3f sigma = new Matrix3f();
			Matrix3f u2 = new Matrix3f();
			l.svd(transform, u1, sigma, u2);
			
			Matrix3f rot = u2;
			u1.transpose();
			if(u1.determinant() < 0)
			{
				Vector3f lastcol = new Vector3f();
				u1.getColumn(2, lastcol);
				lastcol.scale(-1);
				u1.setColumn(2, lastcol);
			}
			rot.mul(u1);
			
			rotations.set(v.index, rot);
		}
	}

	


	
	

	private void compute_ppT(Vector3f p, Vector3f p2, Matrix3f pp2T) {
		assert(p.x*0==0);
		assert(p.y*0==0);
		assert(p.z*0==0);

		pp2T.m00 = p.x*p2.x; pp2T.m01 = p.x*p2.y; pp2T.m02 = p.x*p2.z; 
		pp2T.m10 = p.y*p2.x; pp2T.m11 = p.y*p2.y; pp2T.m12 = p.y*p2.z; 
		pp2T.m20 = p.z*p2.x; pp2T.m21 = p.z*p2.y; pp2T.m22 = p.z*p2.z; 

	}


	
	




}
