package glWrapper;

import java.util.ArrayList;

import javax.media.opengl.GL;

import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeCellAttribute;

/**
 * Simple GLWrapper for the {@link HashOctree}.
 * The octree is sent to the Gpu as set of cell-center points and
 * side lengths. 
 * @author Alf
 *
 */
public class GLHashtree extends GLDisplayable {

	private HashOctree myTree;
	public GLHashtree(HashOctree tree) {
		
		super(tree.numberOfLeaves());
		this.myTree = tree;
		//Add Vertices
		float[] verts = new float[myTree.numberOfLeaves()*3];
		float[] sides = new float[myTree.numberOfLeaves()];
		
		
		int idx = 0, idx2 = 0;
		for(HashOctreeCell n : tree.getLeaves()){
			verts[idx++] = n.center.x;
			verts[idx++] = n.center.y;
			verts[idx++] = n.center.z;
			sides[idx2++] = n.side;
		}
		
		int[] ind = new int[myTree.numberOfLeaves()];
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		this.addElement(verts, Semantic.POSITION , 3);
		this.addElement(sides, Semantic.USERSPECIFIED , 1, "side");
		
		this.addIndices(ind);
		
	}
	
	/**
	 * values are given by OctreeVertex
	 * @param values
	 */
	public void addFunctionValues(ArrayList<Float> values){
		float[] vals = new float[myTree.numberOfLeaves()];
		
		for(HashOctreeCell n: myTree.getLeaves()){
			for(int i = 0; i <=0b111; i++){
				vals[n.leafIndex] += values.get(myTree.getNbr_c2v(n, i).index);//*/Math.signum(values.get(myTree.getVertex(n, i).index));
			}
			vals[n.leafIndex] /=8;
			//vals[n.leafIndex] = Math.abs(vals[n.leafIndex]) < 5.99 ? -1: 1;
		}
		
		this.addElement(vals, Semantic.USERSPECIFIED , 1, "func");
	}
	
	public void addElement(int n, String name, HashOctreeCellAttribute attr)
	{
		float[] f = new float[n * getNumberOfVertices()];
		int i = 0;
		for(HashOctreeCell v: myTree.getLeaves())
		{
			float[] current_f = attr.getAttribute(v, myTree);
			assert(current_f.length == n);
			for(int j = 0; j < n; j++)
			{
				f[i++] = current_f[j];
			}
		}
		addElement(f, Semantic.USERSPECIFIED, n, name);
	}

	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		
	}
}
