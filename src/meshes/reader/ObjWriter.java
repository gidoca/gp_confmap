package meshes.reader;

import glWrapper.GLWireframeMesh;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import openGL.MyDisplay;

public class ObjWriter {
	FileWriter fw;
	
	ObjWriter(String file) throws IOException
	{
		fw = new FileWriter("./out/"+file);
	}

	public void write(WireframeMesh m) throws IOException{
		
		for(Point3f v: m.vertices){
			fw.write("v " + v.x + " " + v.y + " " + v.z + "\n");
		}
		
		for(int[] f : m.faces){
			fw.write("f " + (f[0] + 1) + " " + (f[1]+1) + " " + (f[2]+1) + "\n");
		}
	}
	
	public void write(HalfEdgeStructure hs) throws IOException {
		hs.enumerateVertices();
		for(Vertex v: hs.getVertices()){
			fw.write("v " + v.getPos().x + " " 
						+ v.getPos().y + " " 
						+ v.getPos().z + "\n");
		}
		
		for(Face f : hs.getFaces()){
		
			Iterator<Vertex> it = f.iteratorFV();
			int[] fc = {it.next().index, it.next().index, it.next().index};
			fw.write("f " + (fc[0] + 1) + " " + (fc[1]+1) + " " + (fc[2]+1) + "\n");
		}
	}
	
	public void writeTexcoord(ArrayList<Point2f> texcoords) throws IOException {
		for(Point2f tc: texcoords)
		{
			fw.write("vt " + tc.x + " " + tc.y);
		}
	}
	
	public void close() throws IOException
	{
		fw.close();
	}
	
	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{
		WireframeMesh m = ObjReader.read("objs/bunny.obj", true);
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(m);
		
		ObjWriter writer = new ObjWriter("bla.obj");
		writer.write(hs);
		
		m = ObjReader.read("out/bla.obj", true);
		
		GLWireframeMesh glwf = new GLWireframeMesh(m);
		MyDisplay d = new MyDisplay();
		d.addToDisplay(glwf);
	}


}
