package algorithms;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;

public abstract class Smoother {

	protected ArrayList<Vector3f> newVertices;
	protected HalfEdgeStructure mesh;
	private float initalVolume;
	boolean unsharpMask;

	public Smoother(HalfEdgeStructure mesh) {
		super();
		this.newVertices = new ArrayList<Vector3f>(Collections.nCopies(mesh.getVertices().size(), new Vector3f()));
		this.mesh = mesh;
	}

	public void apply() {
		this.initalVolume = mesh.getVolume();
		computeNew();
		setNew();
		rescale();
	}
	
	abstract void computeNew();

	protected void setNew() {
		for(Vertex v: mesh.getVertices())
		{
			v.getPos().set(newVertices.get(v.index));
		}
	}
	
	private void rescale() {
		float newVolume = mesh.getVolume();
		for(Vertex v: mesh.getVertices())
		{
			v.getPos().scale((float) Math.pow(initalVolume / newVolume, 1/3.));
		}
	}

}