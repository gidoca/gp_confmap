package assignment7;

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point2f;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

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

}
