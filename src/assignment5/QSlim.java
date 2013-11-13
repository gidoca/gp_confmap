package assignment5;

import helper.Iter;

import java.util.HashMap;
import java.util.PriorityQueue;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector4f;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.objects.Transformation;


/** 
 * Implement the QSlim algorithm here
 * 
 * @author Alf
 *
 */
public class QSlim {
	
	private HalfEdgeStructure hs;
	private HalfEdgeCollapse collaptor;
	
	private HashMap<Vertex, Matrix4f> errorQuadrics;
	private HashMap<HalfEdge, PotentialCollapse> potentialCollapses;
	
	private PriorityQueue<PotentialCollapse> collapseQueue;
	
	/********************************************
	 * Use or discard the skeleton, as you like.
	 ********************************************/
	
	public QSlim(HalfEdgeStructure hs) {
		this.hs = hs;
		this.collaptor = new HalfEdgeCollapse(hs);
		this.potentialCollapses = new HashMap<HalfEdge, QSlim.PotentialCollapse>();
		this.init();
	}
	
	
	/**
	 * Compute per vertex matrices
	 * Compute edge collapse costs,
	 * Fill up the Priority queue/heap or similar
	 */
	private void init(){
		this.errorQuadrics = new HashMap<Vertex, Matrix4f>();
		
		for(Vertex v: hs.getVertices())
		{
			Matrix4f out = new Matrix4f();
			for(Face f: Iter.ate(v.iteratorVF()))
			{
				float[] planeArray = new float[4];
				f.plane().get(planeArray);
				Matrix4f errorQuadric = new Matrix4f();
				for(int i = 0; i < 4; i++)
				{
					for(int j = 0; j < 4; j++)
					{
						errorQuadric.setElement(i, j, planeArray[i] * planeArray[j]);
					}
				}
				out.add(errorQuadric);
			}
			this.errorQuadrics.put(v, out);
		}
		
		this.collapseQueue = new PriorityQueue<>();
		
		for(HalfEdge e: hs.getHalfEdges())
		{
			this.collapseQueue.add(new PotentialCollapse(e));
		}
	}
	
	
	/**
	 * The actual QSlim algorithm, collapse edges until
	 * the target number of vertices is reached.
	 * @param target
	 */
	public void simplify(int target){
		while(hs.getVertices().size() - collaptor.deadVertices.size() > target)
		{
			PotentialCollapse next = collapseQueue.poll();
			if(next == null) break;
			if(next.isDeleted) continue;
			assert(!collaptor.isEdgeDead(next.edge));
			if(!HalfEdgeCollapse.isEdgeCollapsable(next.edge) )//|| collaptor.isCollapseMeshInv(next.edge, next.newpos))
			{
				next.cost = (next.cost + 0.1f) * 10;
				next.updateQueuePos();
				continue;
			}
			next.collapse();
		}
		collaptor.finish();
	}
	
	
	/**
	 * Collapse the next cheapest eligible edge. ; this method can be called
	 * until some target number of vertices is reached.
	 */
	public void collapsEdge(){
		
	}
	
	public Matrix4f getErrorQuadric(Vertex v)
	{
		return errorQuadrics.get(v);
	}
	
	/**
	 * helper method that might be useful..
	 * @param p
	 * @param ppT
	 */
	private void compute_ppT(Vector4f p, Transformation ppT) {
		assert(p.x*0==0);
		assert(p.y*0==0);
		assert(p.z*0==0);
		assert(p.w*0==0);
		ppT.m00 = p.x*p.x; ppT.m01 = p.x*p.y; ppT.m02 = p.x*p.z; ppT.m03 = p.x*p.w;
		ppT.m10 = p.y*p.x; ppT.m11 = p.y*p.y; ppT.m12 = p.y*p.z; ppT.m13 = p.y*p.w;
		ppT.m20 = p.z*p.x; ppT.m21 = p.z*p.y; ppT.m22 = p.z*p.z; ppT.m23 = p.z*p.w;
		ppT.m30 = p.w*p.x; ppT.m31 = p.w*p.y; ppT.m32 = p.w*p.z; ppT.m33 = p.w*p.w;
			
		
	}
	
	
	
	
	
	/**
	 * Represent a potential collapse
	 * @author Alf
	 *
	 */
	protected class PotentialCollapse implements Comparable<PotentialCollapse>{
		
		float cost;
		HalfEdge edge;
		boolean isDeleted;
		Point3f newpos;
		private Matrix4f newErrorQuadric;
		
		public PotentialCollapse(HalfEdge edge) {
			isDeleted = false;
			this.edge = edge;
			updateNewPos();
			QSlim.this.potentialCollapses.put(edge, this);
		}
		
		public void collapse()
		{
			isDeleted = true;
			Point3f pos = edge.end().getPos();
			QSlim.this.collaptor.collapseEdge(edge);
			pos.set(newpos);
			errorQuadrics.put(edge.end(), newErrorQuadric);
			for(HalfEdge e: Iter.ate(edge.end().iteratorVE()))
			{
				PotentialCollapse potentialCollapse = potentialCollapses.get(e);
				potentialCollapse.updateNewPos();
				potentialCollapse.updateQueuePos();
			}
		}
		
		public void updateQueuePos()
		{
			PotentialCollapse updated = new PotentialCollapse(this);
			this.isDeleted = true;
			QSlim.this.collapseQueue.add(updated);
		}
		
		private void updateNewPos()
		{
			newpos = new Point3f(edge.start().getPos());
			newpos.add(edge.end().getPos());
			newpos.scale(1/2.f);
			Vector4f newPos4 = new Vector4f(newpos);
			newPos4.w = 1;
			newErrorQuadric = new Matrix4f(errorQuadrics.get(edge.start()));
			
			Vertex vertex = edge.end();
			Matrix4f endQuadric = errorQuadrics.get(vertex );
			assert(errorQuadrics.size() == hs.getVertices().size());
			newErrorQuadric.add(endQuadric );
			Vector4f tNewPos = new Vector4f();
			newErrorQuadric.transform(newPos4, tNewPos);
			this.cost = newPos4.dot(tNewPos);			
		}
		
		public PotentialCollapse(PotentialCollapse other)
		{
			this.cost = other.cost;
			this.edge = other.edge;
			this.isDeleted = other.isDeleted;
			this.newpos = other.newpos;
		}

		@Override
		public int compareTo(PotentialCollapse arg1) {
			return new Float(-cost).compareTo(new Float(-arg1.cost));
		}
	}

}
