package algorithms;

import java.util.Iterator;

import javax.vecmath.Point3f;

import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;


public class AvgSmoother {
	private HEData3d newVertices;
	private HalfEdgeStructure mesh;
	
	public AvgSmoother(HalfEdgeStructure mesh) {
		this.mesh = mesh;
		this.newVertices = new HEData3d(mesh);
	}
	
	public void apply()
	{
		computeNew();
		setNew();
	}
	
	private void setNew()
	{
		for(Vertex v: mesh.getVertices())
		{
			v.getPos().set(newVertices.get(v));
		}
	}
	
	private void computeNew()
	{
		for(Vertex v: mesh.getVertices())
		{
			newVertices.put(v, neighbourhoodAvg(v));
		}
	}
	
	private Point3f neighbourhoodAvg(Vertex v)
	{
		Point3f sum = new Point3f();
		Iterator<Vertex> neighbours = v.iteratorVV();
		while(neighbours.hasNext())
		{
			Vertex current = neighbours.next();
			sum.add(current.getPos());
		}
		sum.scale(1.f / v.getValence());
		return sum;
	}
}
