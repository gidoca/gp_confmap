package glWrapper;

import java.util.Iterator;

import javax.media.opengl.GL;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

public class GLHalfEdgeStructure extends GLDisplayable {

	public GLHalfEdgeStructure(HalfEdgeStructure mesh) {
		super(mesh.getVertices().size());
		
		float[] vertices = new float[getNumberOfVertices() * 3];
		int[] indices = new int[mesh.getFaces().size() * 3];
		
		int i = 0;
		for(Vertex v: mesh.getVertices())
		{
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
		this.addElement(vertices, Semantic.USERSPECIFIED , 3, "color");
		this.addIndices(indices);
	}

	@Override
	public int glRenderFlag() {
		return GL.GL_TRIANGLES;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
	}

}
