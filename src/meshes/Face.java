package meshes;

import helper.Iter;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * Implementation of a face for the {@link HalfEdgeStructure}
 *
 */
public class Face extends HEElement {

	//an adjacent edge, which is positively oriented with respect to the face.
	private HalfEdge anEdge;
	
	public Face(){
		anEdge = null;
	}

	public void setHalfEdge(HalfEdge he) {
		this.anEdge = he;
	}

	public HalfEdge getHalfEdge() {
		return anEdge;
	}
	
	/**
	 * Test if this triangle is obtuse
	 * @return true iff triangle is obtuse
	 */
	public boolean isObtuse() {
		for(HalfEdge e: Iter.ate(iteratorFE()))
		{
			if(e.isObtuse()) return true;
		}
		return false;
	}
	
	public float area() {
		Vector3f e1 = anEdge.getOpposite().getVec();
		Vector3f e2 = anEdge.getNext().getVec();
		Vector3f cross = new Vector3f();
		cross.cross(e1, e2);
		return cross.length() / 2;
	}
	
	
	/**
	 * Iterate over the vertices on the face.
	 * @return
	 */
	public Iterator<Vertex> iteratorFV(){
		return new IteratorFV(anEdge);
	}
	
	/**
	 * Iterate over the adjacent edges
	 * @return
	 */
	public Iterator<HalfEdge> iteratorFE(){
		return new IteratorFE(anEdge);
	}
	
	public String toString(){
		if(anEdge == null){
			return "f: not initialized";
		}
		String s = "f: [";
		Iterator<Vertex> it = this.iteratorFV();
		while(it.hasNext()){
			s += it.next().toString() + " , ";
		}
		s+= "]";
		return s;
		
	}
	
	public class IteratorE {
		IteratorE(HalfEdge anEdge)
		{
			first = anEdge;
			current = null;
		}
		protected HalfEdge first, current;

		public boolean hasNext() {
			return first != null && (current == null || current.next != first);
		}

		public void remove() {
			//we don't support removing through the iterator.
			throw new UnsupportedOperationException();
		}
		
		protected HalfEdge nextEdge()
		{
			//make sure eternal iteration is impossible
			if(!hasNext()){
				throw new NoSuchElementException();
			}

			//update what edge was returned last
			current = (current == null?
						first:
						current.next);
			return current;
		}
		
	}
	
	

	/**
	 * Iterator to iterate over the vertices on a face
	 * @author Alf
	 *
	 */
	public final class IteratorFV extends IteratorE implements Iterator<Vertex> {
		
		
		public IteratorFV(HalfEdge anEdge) {
			super(anEdge);
		}

		@Override
		public Vertex next() {
			return nextEdge().incident_v;
		}

		
		/**
		 * return the face this iterator iterates around
		 * @return
		 */
		public Face face() {
			return first.incident_f;
		}
	}

	public final class IteratorFE extends IteratorE implements Iterator<HalfEdge>
	{
		
		public IteratorFE(HalfEdge initial) {
			super(initial);
		}

		@Override
		public HalfEdge next() {
			return nextEdge();
		}

	}

	public Vector3f normal() {
		Vector3f out = new Vector3f();
		HalfEdge edge1 = this.anEdge;
		HalfEdge edge2 = this.anEdge.getNext();
		out.cross(edge1.getVec(), edge2.getVec());
		out.normalize();
		return out;
	}
	
	public Vector4f plane() {
		Vector3f normal = normal();
		Vector4f out = new Vector4f(normal);
		out.w = -normal.dot(new Vector3f(anEdge.start().getPos()));
		return out;
	}
}
