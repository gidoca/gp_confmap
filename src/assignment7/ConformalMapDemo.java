package assignment7;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLPointCloud;
import glWrapper.GLWireframeMesh;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import meshes.HalfEdgeStructure;
import meshes.PointCloud;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import meshes.reader.ObjWriter;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable.Semantic;
import algorithms.ConformalMapper;
import algorithms.DelaunayTriangulation;
import algorithms.DelaunayTriangulation.Triangle;

public class ConformalMapDemo {

	private static HalfEdgeStructure hs;
	private static LinkedHashMap<Integer, Point2f> labels;
	private static LinkedHashMap<Integer, Point2f> allLabels;
	private static ArrayList<Point2f> unmorphedTexCoords;
	private static WireframeMesh delaunayWf;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		String refName = "aaron";
		String[] names = {"cedric", "gian", "michele", "stefan", "tiziano"};
		System.out.println("Reading obj...");
		
		
		ArrayList<Point2f> refTexcoords = compute(refName);
		unmorphedTexCoords = new ArrayList<Point2f>(refTexcoords);
		ArrayList<Point2f> refPos = getLabelCoords(refTexcoords);
		display(refTexcoords);
		
		ArrayList<Point2f> texcoords = compute(names[2]);
		unmorphedTexCoords = new ArrayList<Point2f>(texcoords);
		delaunayWf = morph(texcoords, refPos, getLabelCoords(texcoords));
		
		display(texcoords);
		
	}
	
	public static ArrayList<Point2f> getLabelCoords(ArrayList<Point2f> texcoords)
	{
		ArrayList<Point2f> pos = new ArrayList<>();
		pos.ensureCapacity(allLabels.size());
		for(int i: allLabels.keySet())
		{
			pos.add(texcoords.get(i));
		}
		return pos;
	}
	
	public static WireframeMesh morph(ArrayList<Point2f> texcoords, ArrayList<Point2f> refpos, ArrayList<Point2f> featurepos)
	{
		refpos.add(new Point2f(0, 0));
		refpos.add(new Point2f(0, 1));
		refpos.add(new Point2f(1, 0));
		refpos.add(new Point2f(1, 1));
		System.out.println("[");
		for(Point2f p: featurepos)
		{
			System.out.println("" + p.x + "," + p.y + ";");
			
		}
		System.out.println("]");

		featurepos.add(new Point2f(0, 0));
		featurepos.add(new Point2f(0, 1));
		featurepos.add(new Point2f(1, 0));
		featurepos.add(new Point2f(1, 1));
		
		Triangle[] delaunay = DelaunayTriangulation.triangulate(featurepos);
		WireframeMesh wf = new WireframeMesh();
		for(Triangle t: delaunay)
		{
			wf.faces.add(new int[]{t.p1, t.p2, t.p3});
		}
		for(Point2f p: featurepos)
		{
			wf.vertices.add(new Point3f(p.x, p.y, 0));
		}
		GLWireframeMesh glwf = new GLWireframeMesh(wf);
		glwf.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		PointCloud pc = new PointCloud();
		pc.points = new ArrayList<Point3f>(wf.vertices);
		GLPointCloud glpc = new GLPointCloud(pc);
		MyDisplay d = new MyDisplay();
		d.addToDisplay(glwf);
		d.addToDisplay(glpc);
		
		int i = 0;
		for(Point2f p: texcoords)
		{
			Triangle t = DelaunayTriangulation.getTriangle(p, delaunay, featurepos);
			assert(t != null);
			Point2f barycentricCoordinates = t.getBarycentricCoordinates(p, featurepos);
			Point2f newPos1 = refpos.get(t.p1);
			Point2f newPos2 = refpos.get(t.p2);
			Point2f newPos3 = refpos.get(t.p3);
			newPos1.scale(1 - barycentricCoordinates.x - barycentricCoordinates.y);
			newPos2.scale(barycentricCoordinates.y);
			newPos3.scale(barycentricCoordinates.x);
			Point2f newPos = new Point2f();
			newPos.add(newPos1);
			newPos.add(newPos2);
			newPos.add(newPos3);
			texcoords.set(i++, newPos);
		}
		
		return wf;
	}
	
	public static ArrayList<Point2f> compute(String name) throws Exception
	{
		WireframeMesh wf = ObjReader.read("./objs/faces/" + name + "_disk_remeshed.obj", true);
		hs = new HalfEdgeStructure();
		hs.init(wf);
		System.out.println("Reading labels");
		LabelReader l = new LabelReader("labels/faces/" + name + "_disk_remeshed.lab", "labels/faces/faces.txc");
		labels = l.read();
		LabelReader lAll = new LabelReader("labels/faces/" + name + "_disk_remeshed.lab", "labels/faces/faces_all_constraints.txc");
		allLabels = lAll.read();
		
//		LabelReader boundaryLabels = new LabelReader("out/" + name + "_boundary.lbl", "out/" + name + "_boundary.txc");
//		labels.putAll(boundaryLabels.read());
		
		ConformalMapper mapper = new ConformalMapper(hs, labels);
		mapper.compute();
		System.out.println("Done, writing OBJ");	
		
		FileWriter autoConstrWriter = new FileWriter("out/" + name + "_auto_constr.txc");
		for(String label: l.lbl.keySet())
		{
			Point2f labelCoord = mapper.get().get(l.lbl.get(label));
//			System.out.println("Output labels:");
			autoConstrWriter.write("" + labelCoord.x + " " + labelCoord.y + " " + label + "\n");
		}
		autoConstrWriter.close();
		
		FileWriter boundaryConstraint = new FileWriter("out/" + name + "_boundary.txc");
		FileWriter boundaryConstraintLabel = new FileWriter("out/" + name + "_boundary.lbl");
		for(Vertex v: hs.getVertices())
		{
			if(v.isOnBoundary())
			{
				Point2f texcoord = mapper.get().get(v.index);
				boundaryConstraint.write(texcoord.x + " " + texcoord.y + " l" + v.index + "\n");
				boundaryConstraintLabel.write(v.index + " l" + v.index + "\n");
			}
		}
		boundaryConstraint.close();
		boundaryConstraintLabel.close();
		
		ObjWriter writer = new ObjWriter(name + "_tex.obj");
		writer.writeTexcoord(mapper.get());
		writer.write(hs);
		writer.close();

		return mapper.get();
	}
	
	public static void display(ArrayList<Point2f> texcoords)
	{
		MyDisplay d = new MyDisplay();

		GLConstraints glc = new GLConstraints(hs, getLabelCoords(unmorphedTexCoords));
//		glc.addElement2D(texcoords, Semantic.POSITION, "pos");
		
		System.out.println("Creating conformal-ish map");
		
		
		GLHalfEdgeStructure glhs = new GLHalfEdgeStructure(hs);
		glhs.addElement2D(texcoords, Semantic.POSITION, "pos");
		glhs.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		
		GLHalfEdgeStructure glhs2 = new GLHalfEdgeStructure(hs);
		glhs2.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		
		if(delaunayWf != null)
		{
			GLWireframeMesh glwf2 = new GLWireframeMesh(delaunayWf);
			glwf2.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
			d.addToDisplay(glwf2);
		}
		
		
		
		d.addToDisplay(glhs);
		d.addToDisplay(glhs2);
		d.addToDisplay(glc);
		
	}

}
