package meshes;

import helper.Iter;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Implementation of a vertex for the {@link HalfEdgeStructure}
 */
public class Vertex extends HEElement{
	
	/**position*/
	Point3f pos;
	/**adjacent edge: this vertex is startVertex of anEdge*/
	HalfEdge anEdge;
	
	/**The index of the vertex, mainly used for toString()*/
	public int index;

	public Vertex(Point3f v) {
		pos = v;
		anEdge = null;
	}
	
	
	public Point3f getPos() {
		return pos;
	}

	public void setHalfEdge(HalfEdge he) {
		anEdge = he;
	}
	
	public HalfEdge getHalfEdge() {
		return anEdge;
	}
	
	public int getValence() {
		int out = 0;
		for(@SuppressWarnings("unused") HalfEdge e: Iter.ate(iteratorVE()))
		{
			out++;
		}
		return out;
	}
	
	public Vector3f getNormal() {
		Vector3f normal = new Vector3f();
		for(HalfEdge current: Iter.ate(iteratorVE()))
		{
			if(current.incident_f == null) continue;
			Vector3f e1 = current.getVec();
			Vector3f e2 = current.getNextOnEnd().getVec();
			float angle = e1.angle(e2);
			Vector3f cp = new Vector3f();
			cp.cross(e1, e2);
			cp.normalize();
			cp.scale(angle);
			normal.add(cp);
			
		}
		normal.normalize();
		return normal;
	}
	
	/**
	 * Get an iterator which iterates over the 1-neighbouhood
	 * @return
	 */
	public Iterator<Vertex> iteratorVV(){
		return new IteratorVV(anEdge);
	}
	
	/**
	 * Iterate over the incident edges
	 * @return
	 */
	public Iterator<HalfEdge> iteratorVE(){
		return new IteratorVE(anEdge);
	}
	
	/**
	 * Iterate over the neighboring faces
	 * @return
	 */
	public Iterator<Face> iteratorVF(){
		return new IteratorVF(anEdge);
	}
	
	
	public String toString(){
		return "" + index;
	}
	
	public float getCurvature()
	{
		Vector3f out = new Vector3f();
		for(HalfEdge e: Iter.ate(iteratorVE()))
		{
			float a1 = e.getNext().getIncidentAngle();
			float cotA1 = (float) (1.f / Math.tan(a1));
			float a2 = e.getOpposite().getNext().getIncidentAngle();
			float cotA2 = (float) (1.f / Math.tan(a2));
			Vector3f v = e.getVec();
			v.scale(cotA1 + cotA2);
			out.add(v);
		}
		return out.length() / (4.f * mixedArea());
	}
	
	private float mixedArea()
	{
		float out = 0;
		for(HalfEdge e: Iter.ate(iteratorVE()))
		{
			if(e.hasFace()) out += area(e);
		}
		return out;
	}
	
	private float area(HalfEdge e)
	{
		if(e.getFace().isObtuse())
		{
			Vector3f e1 = e.getVec();
			Vector3f e2 = e.getNext().opposite.getVec();
			Vector3f e3 = e.getPrev().opposite.getVec();
			float pqSq = e1.lengthSquared();
			float prSq = e2.lengthSquared();
			float cotQ = (float) (1 / Math.tan(e1.angle(e3)));
			e3.negate();
			float cotR = (float) (1 / Math.tan(e2.angle(e3)));
			return (prSq * cotQ + pqSq * cotR) / 8.f;
		}
		else if(e.isObtuse())
		{
			return e.getFace().area() / 2;
		}
		else
		{
			return e.getFace().area() / 4;
		}
	}

	public boolean isAdjacent(Vertex w) {
		Vertex v = null;
		Iterator<Vertex> it = iteratorVV();
		for( v = it.next() ; it.hasNext(); v = it.next()){
			if(v == w) return true;
		}
		return false;
	}
	
	public class IteratorV {
		protected HalfEdge initial, current;
		
		IteratorV(HalfEdge initial) {
			this.initial = initial;
			this.current = null;
		}
		
		protected HalfEdge getNextEdge()
		{
			return current == null ? initial : current.getNextOnStart();
		}
		
		protected HalfEdge updateEdge()
		{
			if(!hasNext())
			{
				throw new NoSuchElementException();
			}
			current = getNextEdge();
			return current;			
		}
		
		public boolean hasNext()
		{
			return current == null || (current.prev != null && getNextEdge() != initial);
		}

		public void remove() {
			//we don't support removing through the iterator.
			throw new UnsupportedOperationException();
		}
	}
	
	public class IteratorVE extends IteratorV implements Iterator<HalfEdge>
	{
		IteratorVE(HalfEdge initial) {
			super(initial);
		}

		@Override
		public HalfEdge next() {
			return updateEdge().opposite;
		}
	}
	
	public class IteratorVV extends IteratorV implements Iterator<Vertex>
	{
		IteratorVV(HalfEdge initial) {
			super(initial);
		}
		
		public Vertex next() {
			return updateEdge().incident_v;
		}
	}
	
	public class IteratorVF extends IteratorV implements Iterator<Face>
	{

		IteratorVF(HalfEdge initial) {
			super(initial);
		}
		
		public Face next() {
			Face out = updateEdge().incident_f;
			while(hasNext() && getNextEdge().incident_f == null) updateEdge();
			return out;
		}
	}

}
