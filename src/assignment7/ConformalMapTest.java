package assignment7;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point2f;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import meshes.reader.ObjWriter;

import org.junit.Test;

import sparse.CSRMatrix;
import algorithms.ConformalMapper;

public class ConformalMapTest {

	@Test
	public void test1NB() throws Exception {
		WireframeMesh wf = ObjReader.read("objs/oneNeighborhood.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		
		ConformalMapper cm = new ConformalMapper(hs, new HashMap<Integer, Point2f>());
		
		CSRMatrix m = new CSRMatrix(0, 2 * hs.getVertices().size());
		for(Face f: hs.getFaces())
		{
			m.append(cm.getMatrix(f), 1.f / f.area());
		}
		
		ArrayList<Float> x = new ArrayList<>();
//		for(Vector3f)
	}
	
	@Test
	public void testObj() throws Exception
	{
		WireframeMesh wf = ObjReader.read("objs/oneNeighborhood.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);

		ArrayList<Point2f> texcoords = new ArrayList<Point2f>();
		for(int i = 0; i < 5; i++)
		{
			texcoords.add(new Point2f(hs.getVertices().get(i).getPos().x, hs.getVertices().get(i).getPos().y));
		}
		
		ObjWriter w = new ObjWriter("1nhtest.obj");
		w.writeTexcoord(texcoords);
		w.write(hs);
		w.close();
		
		for (int i = 0 ; i < texcoords.size(); i++){
			assertTrue(ObjReader.readRawData("out/1nhtest.obj", true).texCoords.get(i)[0] - texcoords.get(i).x < 1e-6);
			assertTrue(ObjReader.readRawData("out/1nhtest.obj", true).texCoords.get(i)[1] - texcoords.get(i).y < 1e-6);
		}
	}

}
