package assignment5;

import java.io.IOException;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

public class Assignment5QSlim {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		WireframeMesh wf = ObjReader.read("objs/bunny5k.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		
		System.out.println(hs.getVertices().size());
		
		QSlim qslim = new QSlim(hs);
		qslim.simplify(2500);

		System.out.println(hs.getVertices().size());
		
	}

}
