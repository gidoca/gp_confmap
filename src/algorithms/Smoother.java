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
	
	public void apply()
	{
		apply(0);
	}

	/**
	 * 
	 * @param unsharpMaskFactor positive for smoothing, negative for unsharp masking, zero for original mesh
	 */
	public void apply(float unsharpMaskFactor) {
		this.initalVolume = mesh.getVolume();
		computeNew();
		setNew(unsharpMaskFactor);
		rescale();
	}
	
	abstract void computeNew();

	protected void setNew(float unsharpMaskFactor) {
		for(Vertex v: mesh.getVertices())
		{
			Vector3f diff = new Vector3f(v.getPos());
			diff.sub(newVertices.get(v.index));
			diff.scale(unsharpMaskFactor);
			diff.add(newVertices.get(v.index));
			v.getPos().set(diff);
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