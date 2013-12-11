package assignment7;

import java.util.ArrayList;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.vecmath.Point2f;

import meshes.HalfEdgeStructure;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

public class GLConstraints extends GLDisplayable {

	public GLConstraints(HalfEdgeStructure hs, HashMap<Integer, Point2f> labels) {
		super(labels.size());
		
		
		int[] ind = new int[labels.size()];
		float[] pos = new float[labels.size()*3];
		
		int idx=0;
		for(Integer i : labels.keySet()){
			pos[idx++] = hs.getVertices().get(i).getPos().x;
			pos[idx++] = hs.getVertices().get(i).getPos().y;
			pos[idx++] = hs.getVertices().get(i).getPos().z;
		}
		
		for(int i = 0; i < ind.length; i++){
			ind[i] = i;
		}
		
		this.addIndices(ind);
		this.addElement(pos, Semantic.POSITION, 3);
		this.addElement2D(new ArrayList<>(labels.values()), Semantic.USERSPECIFIED, "texcoords");
	}

	@Override
	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// TODO Auto-generated method stub

	}

}
