package assignment4;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.VertexAttribute;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable.Semantic;
import sparse.CSRMatrix;

public class Assignment4_1_Demo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Load a wireframe mesh
		//WireframeMesh m = ObjReader.read("/home/gidoca/files/uni/ma/code/renderer/qtcreator-build/src/objfiles/cow.obj", false);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			WireframeMesh m = ObjReader.read("objs/dragon.obj", true);
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException | IOException e) {
			e.printStackTrace();
			return;
		}
		
		

		//... do something with it, display it ....
		GLHalfEdgeStructure glMeshDiffuse = new GLHalfEdgeStructure(hs);
		glMeshDiffuse.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		
		GLHalfEdgeStructure glMeshCurvature = new GLHalfEdgeStructure(hs);
		glMeshCurvature.addElement(1, "curvature", new VertexAttribute() {
			
			@Override
			public float[] getAttribute(Vertex v) {
				return new float[]{v.getCurvature()};
			}
		});
		glMeshCurvature.configurePreferredShader("shaders/curvature.vert", 
				"shaders/default.frag");
		
		
		
		ArrayList<Vector3f> meanCurvatureNormals = new ArrayList<>();
		float[] curvature = new float[hs.getVertices().size()];
		CSRMatrix mat = LMatrices.mixedCotanLaplacian(hs);
		mat.scale(-1);
		LMatrices.mult(mat, hs, meanCurvatureNormals);
		for(int i = 0; i < curvature.length; i++)
		{
			curvature[i] = meanCurvatureNormals.get(i).length() / 2.f;
		}
		
		
		GLHalfEdgeStructure glMeshCurvatureNormal = new GLHalfEdgeStructure(hs);
		glMeshCurvatureNormal.addElement(meanCurvatureNormals, Semantic.USERSPECIFIED, "normal");
		glMeshCurvatureNormal.configurePreferredShader("shaders/normal_vec.vert", 
				"shaders/normal_vec.frag", "shaders/normal_vec.geom");
		
		
		GLHalfEdgeStructure glMeshCurvatureL = new GLHalfEdgeStructure(hs);
		glMeshCurvatureL.addElement(curvature, Semantic.USERSPECIFIED, 1, "curvature");
		glMeshCurvatureL.configurePreferredShader("shaders/curvature.vert", 
				"shaders/default.frag");
		
		
		
		MyDisplay d = new MyDisplay();
		d.addToDisplay(glMeshDiffuse);
		d.addToDisplay(glMeshCurvature);
		d.addToDisplay(glMeshCurvatureL);
		d.addToDisplay(glMeshCurvatureNormal);
	}

}
