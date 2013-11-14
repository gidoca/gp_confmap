package assignment5;

import helper.Iter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector4f;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;


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
		this.errorQuadrics = new HashMap<>();
		
		for(Vertex v: hs.getVertices())
		{
			Matrix4f out = new Matrix4f();
			for(Face f: Iter.ate(v.iteratorVF()))
			{
				out.add(calculateQuadric(f));
			}
			assert(v.index == this.errorQuadrics.size());
			this.errorQuadrics.put(v, out);
		}
		
		this.collapseQueue = new PriorityQueue<>();
		
		for(HalfEdge e: hs.getHalfEdges())
		{
			this.collapseQueue.add(new PotentialCollapse(e));
		}
	}
	
	private Matrix4f calculateQuadric(Face f)
	{
		float[] planeArray = new float[4];
		f.plane().get(planeArray);
		for(int i = 0; i < 4; i++)
		{
			if(Float.isNaN(planeArray[i]) || Float.isInfinite(planeArray[i]))
			{
				return new Matrix4f();
			}
		}
		Matrix4f errorQuadric = new Matrix4f();
		for(int i = 0; i < 4; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				errorQuadric.setElement(i, j, planeArray[i] * planeArray[j]);
			}
		}
		return errorQuadric;
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
			if(next.isDeleted || collaptor.isEdgeDead(next.edge)) continue;
			assert(!collaptor.isEdgeDead(next.edge));
			if(!HalfEdgeCollapse.isEdgeCollapsable(next.edge) || collaptor.isCollapseMeshInv(next.edge, next.newpos))
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
		return new Matrix4f(errorQuadrics.get(v));
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
			assert(!isDeleted);
			isDeleted = true;
			Point3f pos = edge.end().getPos();
			QSlim.this.collaptor.collapseEdge(edge);
			pos.set(newpos);
			assert(newErrorQuadric != null);
			errorQuadrics.put(edge.end(), newErrorQuadric);
			for(HalfEdge e: Iter.ate(edge.end().iteratorVE()))
			{
				potentialCollapses.get(e).updateCollapse();
				potentialCollapses.get(e.getOpposite()).updateCollapse();
			}
		}
		
		private void updateCollapse()
		{
			this.updateNewPos();
			this.updateQueuePos();
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
		
		private PotentialCollapse(PotentialCollapse other)
		{
			this.cost = other.cost;
			this.edge = other.edge;
			this.isDeleted = false;
			this.newpos = other.newpos;
			this.newErrorQuadric = other.newErrorQuadric;
		}

		@Override
		public int compareTo(PotentialCollapse arg1) {
			return new Float(cost).compareTo(new Float(arg1.cost));
		}
	}

}
