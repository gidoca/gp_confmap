package meshes;

import javax.vecmath.Vector3f;

/**
 * Implementation of a half-edge for the {@link HalfEdgeStructure}
 * @author Alf
 *
 */
public class HalfEdge extends HEElement{
	
	/** The end vertex of this edge*/
	Vertex incident_v;
	
	/**The face this half edge belongs to, which is the face
	* this edge is positively oriented for. This can be null if
	* the half edge lies on a boundary*/
	Face incident_f;
	
	/**the opposite, next and previous edge*/
	HalfEdge opposite, next, prev;
	
	/**
	 * Initialize a half-edge with the Face it belongs to (the face it is
	 * positively oriented for) and the vertex it points to 
	 */
	public HalfEdge(Face f, Vertex v) {
		incident_v = v;
		incident_f = f;
		opposite = null;
	}
	
	/**
	 * Initialize a half-edge with the Face it belongs to (the face it is
	 * positively oriented for), the vertex it points to and its opposite
	 * half-edge.
	 * @param f
	 * @param v
	 * @param oppos
	 */
	public HalfEdge(Face f, Vertex v, HalfEdge oppos) {
		incident_v = v;
		incident_f = f;
		opposite = oppos;
	}

	
	/**
	 * If this is the edge (a->b) this returns the edge (b->a).
	 * @return
	 */
	public HalfEdge getOpposite() {
		return opposite;
	}
	
	public void setOpposite(HalfEdge opp) {
		this.opposite = opp;
	}

	/**
	 * will return the next edge on the face this half-edge belongs to. 
	 * (If this is the edge (a->b) on the triangle (a,b,c) this will be (b->c).
	 * @return
	 */
	public HalfEdge getNext() {
		return next;
	}
	
	public void setNext(HalfEdge he) {
		this.next = he;
	}
	
	/**
	 * Returns the face this half-edge belongs to. If this is the edge (b->a) lying
	 * on the faces (a,b,c) and (b,a,d) this will be the face (b,a,d). If the
	 * half-edge lies on a boundary this can be null.
	 * @return
	 */
	public Face getFace() {
		return incident_f;
	}

	/**
	 * will return the previous edge on the face this half-edge belongs to. 
	 * (If this is the edge (a->b) on the triangle (a,b,c) this will be (c->a).
	 * @return
	 */
	public HalfEdge getPrev() {
		return prev;
	}
	
	public void setPrev(HalfEdge he) {
		this.prev = he;
	}
	
	
	public void setEnd(Vertex v){
		this.incident_v = v;
	}
	
	public HalfEdge getNextOnStart()
	{
		return getOpposite().getNext();
	}

	public HalfEdge getNextOnEnd()
	{
		return getNext().getOpposite();
	}
	
	public HalfEdge getPrevOnStart()
	{
		return getPrev().getOpposite();
	}
	
	public HalfEdge getPrevOnEnd()
	{
		return getOpposite().getPrev();
	}
	
	public Vertex start(){
		return opposite.incident_v;
	}
	
	public Vertex end(){
		return incident_v;
	}
	
	
	public boolean hasFace(){
		return this.incident_f != null;
	}
	
	/**
	 * Returns true iff this half edge's face is obtuse in the incident vertex
	 */
	public boolean isObtuse()
	{
		return getCosIncidentAngle() < 0;
	}
	
	public float getCosIncidentAngle()
	{
		Vector3f e1 = getVec();
		e1.negate();
		e1.normalize();
		Vector3f e2 = getNext().getVec();
		e2.normalize();
		return e1.dot(e2);		
	}
	
	public float getIncidentAngle()
	{
		return (float) Math.acos(getCosIncidentAngle());
	}
	
	public Vector3f getVec()
	{
		Vector3f v = new Vector3f(end().getPos());
		v.sub(start().getPos());
		return v;
	}
	
	/**
	 * Returns true if this edge and its opposite have a face only on one side.
	 */
	public boolean isOnBorder(){
		return this.incident_f == null || opposite.incident_f == null;
	}
	
	public String toString(){
		return "( " + start().toString() + " --> " + end().toString() + ")";
	}

}
