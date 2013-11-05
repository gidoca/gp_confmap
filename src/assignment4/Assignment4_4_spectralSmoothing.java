package assignment4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import algorithms.FloatFunction;
import algorithms.Smoother;
import algorithms.SpectralSmoother;

import openGL.MyDisplay;
import openGL.gl.GLDisplayable.Semantic;
import sparse.CSRMatrix;
import sparse.SCIPYEVD;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.VertexAttribute;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;



/**
 * You can implement the spectral smoothing application here....
 * @author Alf
 *
 */
public class Assignment4_4_spectralSmoothing {
	
	public static void main(String[] args)
	{
		sphericalHarmonicsDemo();
		//spectralSmoothingDemo();
	}

	private static void spectralSmoothingDemo(){
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
			try {
			WireframeMesh m = ObjReader.read("./objs/dragon_5000.obj", true);
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException | IOException e) {
			e.printStackTrace();
			return;
		}
		
		GLHalfEdgeStructure glMeshDiffuse = new GLHalfEdgeStructure(hs);
		glMeshDiffuse.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		MyDisplay d = new MyDisplay();
		d.addToDisplay(glMeshDiffuse);


		Smoother s = new SpectralSmoother(hs, 1000, new FloatFunction() {
			@Override
			public float f(float x) {
				return Math.abs(x) > 10 ? .5f : (Math.abs(x) < 2 ? 1 : .3f);
			}
		});
		s.apply();
		GLHalfEdgeStructure glMeshSmoothed = new GLHalfEdgeStructure(hs);
		glMeshSmoothed.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		glMeshSmoothed.setName("smoothed");
		
		d.addToDisplay(glMeshSmoothed);
	}
	
	private static void sphericalHarmonicsDemo()
	{
		HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			WireframeMesh m = ObjReader.read("./objs/sphere.obj", true);
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException | IOException e) {
			e.printStackTrace();
			return;
		}
		
		GLHalfEdgeStructure glMeshDiffuse = new GLHalfEdgeStructure(hs);
		glMeshDiffuse.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		MyDisplay d = new MyDisplay();
		d.addToDisplay(glMeshDiffuse);
	
		CSRMatrix laplacian = LMatrices.symmetricCotanLaplacian(hs);
		ArrayList<Float> eigenValues = new ArrayList<Float>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<ArrayList<Float>>();
		try {
			SCIPYEVD.doSVD(laplacian, "laplacianevd", 20, eigenValues, eigenVectors);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		for(int i = 0; i < eigenVectors.size(); i++)
		{
			final ArrayList<Float> eigenVector = eigenVectors.get(i);
			final float minEV = Collections.min(eigenVector);
			final float maxEV = Collections.max(eigenVector);
			
			GLHalfEdgeStructure glEV = new GLHalfEdgeStructure(hs);
			glEV.configurePreferredShader("shaders/trimesh_flatColor3f.vert", "shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom");
			glEV.setName("EV " + i);
			glEV.addElement(3, "color", new VertexAttribute() {
				@Override
				public float[] getAttribute(Vertex v) {
					float[] out = new float[3];
					float val = (eigenVector.get(v.index) - minEV) / (float) Math.max(maxEV - minEV, .0001f);
					out[0] = (float) Math.min(2 * Math.max(val, 0.1), 0.8);
					out[2] = (float) Math.min(2 * Math.max(1 - val, 0.1), 0.8);
					out[1] = (float) Math.min(out[0], out[2]);
					return out;
				}
			});
			d.addToDisplay(glEV);
		}

	}
}
