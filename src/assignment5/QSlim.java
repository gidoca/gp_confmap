package assignment5;

import helper.Iter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.SingularMatrixException;
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
			//System.out.println(next.cost);
			
			if(next == null) break;
			if(next.isDeleted || collaptor.isEdgeDead(next.edge)) continue;
			
			if(!HalfEdgeCollapse.isEdgeCollapsable(next.edge) || collaptor.isCollapseMeshInv(next.edge, next.newpos))
			{
				PotentialCollapse updated = new PotentialCollapse(next);
				updated.cost = Math.max((next.cost + 0.1f) * 10, 0);//collapseQueue.peek().cost + 0.1f);
				if(updated.cost < -1e-2) System.out.println(updated.cost);
				next.updateQueuePos(updated);
				continue;
			}
			next.collapse();
			System.out.println(hs.getVertices().size() - collaptor.deadVertices.size() - target);
		}
		collaptor.finish();
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
			/*System.out.println(this.cost);
			System.out.println(this.edge);*/
			isDeleted = true;
			Vertex start = edge.start(), end = edge.end();
			Point3f pos = end.getPos();
			QSlim.this.collaptor.collapseEdge(edge);
			assert(collaptor.isVertexDead(start));
			assert(!collaptor.isVertexDead(end));
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
			PotentialCollapse pc = new PotentialCollapse(this);
			pc.updateNewPos();
			this.updateQueuePos(pc);
		}
		
		public void updateQueuePos(PotentialCollapse updated)
		{
			this.isDeleted = true;
			QSlim.this.potentialCollapses.put(edge, updated);
			QSlim.this.collapseQueue.add(updated);
		}
		
		private void updateNewPos()
		{
			assert(errorQuadrics.size() == hs.getVertices().size());
			newErrorQuadric = new Matrix4f(errorQuadrics.get(edge.start()));
			newErrorQuadric.add(errorQuadrics.get(edge.end()));
			newpos = optimizedPos();
			Vector4f tNewPos = new Vector4f();
			Vector4f newPos4 = new Vector4f(newpos);
			newPos4.w = 1;
			newErrorQuadric.transform(newPos4, tNewPos);
			this.cost = newPos4.dot(tNewPos);
			if(cost < -1e-2)
			{
				System.out.println(cost);
				//cost = 0;
			}
		}
		
		private Point3f optimizedPos()
		{
			Matrix4f a = new Matrix4f(newErrorQuadric);
			a.setRow(3, 0, 0, 0, 1);
			Vector4f b = new Vector4f(0, 0, 0, 1);
			if(Math.abs(a.determinant()) < 1e-3)
			{
				Point3f pos = new Point3f(edge.end().getPos());
				pos.add(edge.start().getPos());
				pos.scale(1/2.f);
				return pos;
			}
			a.invert();
			Vector4f out = new Vector4f();
			a.transform(b, out);
			out.scale(1.f / out.w);
			return new Point3f(out.x, out.y, out.z);
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
		
		public String toString()
		{
			return "Cost " + cost + " Edge " + edge;
		}
	}

}
