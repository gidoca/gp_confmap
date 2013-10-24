package assignment3;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLHashtree;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLPointCloud;
import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.PointCloud;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import meshes.reader.PlyReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;
import sparse.LinearSystem;
import sparse.SCIPY;
import algorithms.AvgSmoother;
import assignment2.HashOctree;
import assignment2.HashOctreeVertex;

public class Assignment3 {
	
	public static void main(String[] args) throws IOException{
		
		PointCloud pc = nonUniformPointCloud(15);
		HashOctree tree = new HashOctree( 
				pc,
				6,7,1.2f);
		ArrayList<Float> x = sphericalFunction(tree);
//		marchingCubesDemo(pc, x, tree, true);
		
		PointCloud pc2 = ObjReader.readAsPointCloud("./objs/dragon_withNormals.obj", true);
//		PointCloud pc2 = spherePointCloud(20);
		HashOctree tree2 = new HashOctree(pc2, 7, 1, 1.1f);
		tree2.refineTree(2);
		ArrayList<Float> x2 = new ArrayList<>();
		LinearSystem s = SSDMatrices.ssdSystem(tree2, pc2, 1f, 0.01f, 11f);
		SCIPY.solve(s, "dragon", x2);
		marchingCubesDemo(pc2, x2, tree2);
		
		energyTest();
			
	}
	
	public static void marchingCubesDemo(PointCloud pc, ArrayList<Float> x, HashOctree tree)
	{
		marchingCubesDemo(pc, x, tree, false);
	}
	
	
	public static void marchingCubesDemo(PointCloud pc, ArrayList<Float> x, HashOctree tree, boolean testWatertight){
		
		//Test Data: create an octree
		
		
		//And show off...
		
		//visualization of the per vertex values (blue = negative, 
		//red = positive, green = 0);
		MyDisplay d = new MyDisplay();
		GLHashtree_Vertices gl_v = new GLHashtree_Vertices(tree);
		gl_v.addFunctionValues(x);
		gl_v.configurePreferredShader("shaders/func.vert", 
				"shaders/func.frag", null);
		d.addToDisplay(gl_v);

		//visualization of the per vertex values (blue = negative, 
		//red = positive, green = 0);
		GLPointCloud gl_pc = new GLPointCloud(pc);
		d.addToDisplay(gl_pc);
		
		//discrete approximation of the zero level set: render all
		//tree cubes that have negative values.
		GLHashtree gltree = new GLHashtree(tree);
		gltree.addFunctionValues(x);
		gltree.configurePreferredShader("shaders/octree_zro.vert", 
				"shaders/octree_zro.frag", "shaders/octree_zro.geom");
		d.addToDisplay(gltree);
		
		MarchingCubes mc = new MarchingCubes(tree);
		
		mc.primaryMC(x);
		GLWireframeMesh glwm = new GLWireframeMesh(mc.getResult());
		glwm.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		d.addToDisplay(glwm);
		
		
		mc.dualMC(x);
		GLWireframeMesh glwmd = new GLWireframeMesh(mc.getResult());
		glwmd.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		d.addToDisplay(glwmd);
		
		HalfEdgeStructure mesh = new HalfEdgeStructure();
		try {
			mesh.init(mc.getResult());
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		// Test if watertight
		for(HalfEdge e: mesh.getHalfEdges())
		{
			if(testWatertight) assert(!e.isOnBorder());
		}
		/*AvgSmoother smoother = new AvgSmoother(mesh);
		smoother.apply();
		GLHalfEdgeStructure glsm = new GLHalfEdgeStructure(mesh);
		glsm.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		d.addToDisplay(glsm);*/
	}
	
	public static void energyTest()
	{
		//Test Data: create an octree
		PointCloud cloud = nonUniformPointCloud(15);
		HashOctree tree = new HashOctree( 
				cloud,
				6,7,1.2f);
		ArrayList<Float> xcoords = new ArrayList<Float>();
		for(HashOctreeVertex v: tree.getVertices())
		{
			xcoords.add(v.getPosition().x);
		}
		ArrayList<Float> out = new ArrayList<>();
		CSRMatrix d0 = SSDMatrices.D0Term(tree, cloud);
		d0.mult(xcoords, out);
		float sqrdiff = 0;
		for(int i = 0; i < out.size(); i++)
		{
			float diff = cloud.points.get(i).x - out.get(i);
			sqrdiff += diff * diff / out.size();
		}
		System.out.println(Math.sqrt(sqrdiff));
		
		ArrayList<Float> linearF = new ArrayList<>();
		for(HashOctreeVertex v: tree.getVertices())
		{
			linearF.add(v.getPosition().x + v.getPosition().y + v.getPosition().z);
		}
		out = new ArrayList<>();
		CSRMatrix d1 = SSDMatrices.D1Term(tree, cloud, new ArrayList<Float>());
		d1.mult(linearF, out);
		sqrdiff = 0;
		for(int i = 0; i < out.size(); i++)
		{
			float expected = 1;
			float diff = expected - out.get(i);
			sqrdiff += diff * diff / out.size();
		}
		System.out.println(Math.sqrt(sqrdiff));

		out = new ArrayList<>();
		CSRMatrix r = SSDMatrices.RTerm(tree);
		r.mult(linearF, out);
		sqrdiff = 0;
		for(int i = 0; i < out.size(); i++)
		{
			float expected = 0;
			float diff = expected - out.get(i);
			sqrdiff += diff * diff / out.size();
		}
		System.out.println(Math.sqrt(sqrdiff));
}
	
	
	/**
	 * Samples the implicit function of a sphere at the tree's vertex positions.
	 * @param tree
	 * @return
	 */
	private static ArrayList<Float> sphericalFunction(HashOctree tree){
		
		//initialize the array
		ArrayList<Float> primaryValues = new ArrayList<>(tree.numberofVertices());
		for(int i = 0; i <tree.numberofVertices(); i++){
			primaryValues.add(new Float(0));
		}
		
		//compute the implicit function
		Point3f c = new Point3f(0.f,0.f,0.f);
		for(HashOctreeVertex v : tree.getVertices()){
			primaryValues.set(v.index, (float)
					v.position.distance(c) - 1f); 
		}
		return primaryValues;
	}
	
	/**
	 * generating a pointcloud
	 * @param max
	 * @return
	 */
	private static PointCloud nonUniformPointCloud(int max){
		PointCloud pc = new PointCloud();
		float delta = 1.f/max;
		for(int i = -max; i < max; i++){
			for(int j = -max; j < max; j++){
				for(int k = -max; k < max; k++){
					if(k>0){
						k+=3;
						if(j%3 !=0 || i%3 !=0){
							continue;
						}
					}
					pc.points.add(new Point3f(
							delta*i,
							delta*j,
							delta*k));
					pc.normals.add(new Vector3f(1,0,0));
				}
			}

		}
		
		return pc;
	}

	/**
	 * generating a pointcloud
	 * @param max
	 * @return
	 */
	private static PointCloud spherePointCloud(int max){
		PointCloud pc = new PointCloud();
		float delta = 1.f/max;
		for(float phi = 0; phi < 2* Math.PI; phi+= delta){
			for(float psi = (float) -Math.PI/2; psi < Math.PI/2; psi+= delta){
					
					pc.points.add(new Point3f((float)(Math.cos(phi) * Math.cos(psi)),
							(float)(Math.sin(phi) * Math.cos(psi)),
							(float)(Math.sin(psi))));
					
					pc.normals.add(new Vector3f((float)(Math.cos(phi) * Math.cos(psi)),
							(float)(Math.sin(phi) * Math.cos(psi)),
							(float)(Math.sin(psi))));
			}

		}
		
		return pc;
	}
}
