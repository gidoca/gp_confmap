package assignment4;


import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

import sparse.CSRMatrix;

public class Assignment4_1_Tests {
	
	// A sphere of radius 2.
	private HalfEdgeStructure hs; 
	// An ugly sphere of radius 1, don't expect the Laplacians 
	//to perform accurately on this mesh.
	private HalfEdgeStructure hs2;
	
	
	private CSRMatrix uniformLaplacian, mixedCotanLaplacian;
	private CSRMatrix symmetricCotanLaplacian;
	
	@Before
	public void setUp(){
		try {
			WireframeMesh m = ObjReader.read("objs/sphere.obj", false);
			hs = new HalfEdgeStructure();
			hs.init(m);
			
			m = ObjReader.read("objs/uglySphere.obj", false);
			hs2 = new HalfEdgeStructure();
			hs2.init(m);
			
			uniformLaplacian = LMatrices.uniformLaplacian(hs);
			mixedCotanLaplacian = LMatrices.mixedCotanLaplacian(hs);
			symmetricCotanLaplacian = LMatrices.symmetricCotanLaplacian(hs);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
	
	private void testLaplacian(CSRMatrix l)
	{
		List<Float> ones = Collections.nCopies(l.nRows, 1.f);
		ArrayList<Float> result = new ArrayList<>();
		l.mult(ones, result);
		for(float v: result)
		{
			if(Math.abs(v) >= 1e-5)
			{
				System.out.println(v);
			}
			assertTrue(Math.abs(v) < 1e-5);
		}
		
		for(int i = 0; i < l.nRows; i++)
		{
			ArrayList<CSRMatrix.col_val> row = l.rows.get(i);
			for(CSRMatrix.col_val val: row)
			{
				assertTrue(l.get(val.col, i) != 0);
			}
		}
	}
	
	@Test
	public void uniformLaplaciaon()
	{
		testLaplacian(uniformLaplacian);
	}
	
	@Test
	public void cotanLaplacian()
	{
		testLaplacian(mixedCotanLaplacian);
	}
	
	@Test
	public void symmetricCotanLaplacian()
	{
		testLaplacian(symmetricCotanLaplacian);
		assertTrue(symmetricCotanLaplacian.isSymmetric(1e-7f));
	}
	
	@Test
	public void curvatureNormalTest()
	{
		ArrayList<Vector3f> meanCurvatureNormals = new ArrayList<>();
		LMatrices.mult(mixedCotanLaplacian, hs, meanCurvatureNormals);
		for(Vertex v: hs.getVertices())
		{
			Vector3f result = meanCurvatureNormals.get(v.index);
			result.scale(1/2.f);
			assertEquals(result.length(), v.getCurvature(), 1e-5);
		}
	}
	
	@Test
	public void volume()
	{
		assertEquals(hs.getVolume(), 2 * 2 * 2 * 4 / 3.f * Math.PI, 1);
	}
}
