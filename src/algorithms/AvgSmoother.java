package algorithms;

import java.util.Iterator;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;


public class AvgSmoother extends Smoother {
	private HalfEdgeStructure mesh;
	
	public AvgSmoother(HalfEdgeStructure mesh) {
		super(mesh);
	}
	
	protected void computeNew()
	{
		for(Vertex v: mesh.getVertices())
		{
			newVertices.set(v.index, new Vector3f(neighbourhoodAvg(v)));
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
