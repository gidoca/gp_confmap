package glWrapper;

import java.util.Iterator;

import javax.media.opengl.GL;
import javax.vecmath.Tuple3f;

import meshes.Face;
import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

public class GLHalfEdgeStructure extends GLDisplayable {

	public GLHalfEdgeStructure(HalfEdgeStructure mesh) {
		super(mesh.getVertices().size());
		this.mesh = mesh;
		
		float[] vertices = new float[getNumberOfVertices() * 3];
		int[] indices = new int[mesh.getFaces().size() * 3];
		
		int i = 0;
		for(Vertex v: mesh.getVertices())
		{
			int currentvalence = v.getValence();
			if(currentvalence > maxValence) maxValence = currentvalence;
			
			vertices[i] = v.getPos().x;
			i++;
			vertices[i] = v.getPos().y;
			i++;
			vertices[i] = v.getPos().z;
			i++;
		}
		
		i = 0;
		for(Face f: mesh.getFaces())
		{
			Iterator<Vertex> faceVertices = f.iteratorFV();
			while(faceVertices.hasNext())
			{
				indices[i] = mesh.getVertices().indexOf(faceVertices.next());
				i++;
			}
		}
		
		this.addElement(vertices, Semantic.POSITION , 3);
		this.addIndices(indices);
	}

	@Override
	public int glRenderFlag() {
		return GL.GL_TRIANGLES;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		glRenderContext.setUniform("maxvalence", maxValence);
	}
	
	public void addElement(int n, String name, VertexAttribute attr)
	{
		float[] f = new float[n * getNumberOfVertices()];
		int i = 0;
		for(Vertex v: mesh.getVertices())
		{
			float[] current_f = attr.getAttribute(v);
			assert(current_f.length == n);
			for(int j = 0; j < n; j++)
			{
				f[i] = current_f[j];
				i++;
			}
		}
		addElement(f, Semantic.USERSPECIFIED, n, name);
	}
	
	public void addElement(String name, HEData3d data)
	{
		float[] f = new float[getNumberOfVertices() * 3];
		int i = 0;
		for(Vertex v: mesh.getVertices())
		{
			Tuple3f current = data.get(v);
			f[i] = current.x;
			i++;
			f[i] = current.y;
			i++;
			f[i] = current.z;
			i++;
		}
	}
	
	public void addElement(String name, HEData1d data)
	{
		float[] f = new float[getNumberOfVertices()];
		int i = 0;
		for(Vertex v: mesh.getVertices())
		{
			Number current = data.get(v);
			f[i] = current.floatValue();
			i++;
		}
	}
	
	private int maxValence = 0;
	
	private HalfEdgeStructure mesh;

}
