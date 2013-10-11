package assignment3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.vecmath.Point3f;

import meshes.Point2i;
import meshes.WireframeMesh;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;


/**
 * Implwmwnr your Marching cubes algorithms here.
 * @author bertholet
 *
 */
public class MarchingCubes {
	
	//the reconstructed surface
	public WireframeMesh result;
	

	//the tree to march
	private HashOctree tree;
	//per marchable cube values
	private ArrayList<Float> val;
	
	private HashMap<Point2i, Integer> existingEdgeIndices;
	
	
	
		
	/**
	 * Implementation of the marching cube algorithm. pass the tree
	 * and either the primary values associated to the trees edges
	 * @param tree
	 * @param byLeaf
	 */
	public MarchingCubes(HashOctree tree){
		this.tree = tree;
		
		
	}

	/**
	 * Perform primary Marching cubes on the tree.
	 */
	public void primaryMC(ArrayList<Float> byVertex) {
		this.val = byVertex;
		this.result = new WireframeMesh();
		this.existingEdgeIndices = new HashMap<Point2i, Integer>();
		
		for(HashOctreeCell c: tree.getLeaves())
		{
			pushCube(c);
		}
	}
	
	/**
	 * Perform dual marchingCubes on the tree
	 */
	public void dualMC(ArrayList<Float> byVertex) {
		this.result = new WireframeMesh();
		this.existingEdgeIndices = new HashMap<Point2i, Integer>();
		
		this.val = new ArrayList<>(Collections.nCopies(tree.numberofVertices(), 0.f));
		for(HashOctreeCell c: tree.getLeaves())
		{
			float avgVal = 0;
			for(int i = 0b000; i <= 0b111; i++)
			{
				MarchableCube v = c.getCornerElement(i, tree);
				avgVal += byVertex.get(v.getIndex()) / 8;
			}
			this.val.set(c.getIndex(), avgVal);
		}
		for(HashOctreeVertex v: tree.getVertices())
		{
			if(v.isOnBoundary()) continue;
			
			pushCube(v);
		}
	}
	
	/**
	 * March a single cube: compute the triangles and add them to the wireframe model
	 * @param n
	 */
	private void pushCube(MarchableCube n){
		float[] vVal = new float[8];
		Point2i[] triangles = new Point2i[15];
		for(int i = 0b000; i <= 0b111; i++)
		{
			vVal[i] = val.get(n.getCornerElement(i, tree).getIndex());
		}
		for(int i = 0; i < 15; i++)
		{
			triangles[i] = new Point2i();
		}
		MCTable.resolve(vVal, triangles);
		int i = 0;
		for(int j = 0; j < 5; j++)
		{
			int[] indices = new int[3];
			if(triangles[i].x == -1 || triangles[i].y == -1) continue;
			for(int k = 0; k < 3; k++)
			{
				Point2i p = triangles[i++];
				indices[k] = pushEdge(p, n);
			}
			if(indices[0] != indices[1] && indices[0] != indices[2] && indices[1] != indices[2])
			{
				result.faces.add(indices);
			}
		}
	}
	
	private int pushEdge(Point2i edge, MarchableCube n)
	{
		Point2i key = key(n, edge);
		if(!existingEdgeIndices.containsKey(key))
		{
			existingEdgeIndices.put(key, existingEdgeIndices.size());
			MarchableCube v1 = n.getCornerElement(edge.x, tree), v2 = n.getCornerElement(edge.y, tree);
			float split = val.get(v1.getIndex()) / (val.get(v1.getIndex()) - val.get(v2.getIndex()));
			Point3f p1 = v1.getPosition(), p2 = v2.getPosition();
			p1.scale(1 - split);
			p2.scale(split);
			p1.add(p2);
			result.vertices.add(p1);
		}
		return existingEdgeIndices.get(key);
//		return result.vertices.size() - 1;
	}

	
	/**
	 * Get a nicely marched wireframe mesh...
	 * @return
	 */
	public WireframeMesh getResult() {
		return this.result;
	}


	/**
	 * compute a key from the edge description e, that can be used to
	 * uniquely identify the edge e of the cube n. See Assignment 3 Exercise 1-5
	 * @param n
	 * @param e
	 * @return
	 */
	private Point2i key(MarchableCube n, Point2i e) {
		Point2i p = new Point2i(n.getCornerElement(e.x, tree).getIndex(),
				n.getCornerElement(e.y, tree).getIndex());
		if(p.x > p.y) {
			int temp = p.x;
			p.x= p.y; p.y = temp;
		}
		return p;
	}
	

}
