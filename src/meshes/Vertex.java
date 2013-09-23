package meshes;

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
		Iterator<HalfEdge> edges = iteratorVE();
		while(edges.hasNext())
		{
			edges.next();
			out++;
		}
		return out;
	}
	
	public Vector3f getNormal() {
		Vector3f normal = new Vector3f();
		Iterator<HalfEdge> neighbourhood = this.iteratorVE();
		while(neighbourhood.hasNext())
		{
			HalfEdge current = neighbourhood.next();
			Vertex v1 = current.end();
			Vertex v2 = current.getOpposite().getNext().end();
			Vector3f e1 = new Vector3f(v1.getPos());
			e1.sub(this.getPos());
			Vector3f e2 = new Vector3f(v2.getPos());
			e2.sub(this.getPos());
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
	
	

	public boolean isAdjascent(Vertex w) {
		boolean isAdj = false;
		Vertex v = null;
		Iterator<Vertex> it = iteratorVV();
		for( v = it.next() ; it.hasNext(); v = it.next()){
			if( v==w){
				isAdj=true;
			}
		}
		return isAdj;
	}
	
	public class IteratorV {
		protected HalfEdge initial, current;
		
		IteratorV(HalfEdge initial) {
			this.initial = initial;
			this.current = null;
		}
		
		private HalfEdge getNextEdge()
		{
			return current == null ? initial : current.prev.opposite;
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
			return updateEdge();
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
			return updateEdge().incident_f;
		}
	}

}
