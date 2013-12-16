package assignment7;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.FileWriter;
import java.util.HashMap;

import javax.vecmath.Point2f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import meshes.reader.ObjWriter;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable.Semantic;
import algorithms.ConformalMapper;

public class ConformalMapDemo {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		WireframeMesh wf = ObjReader.read("./objs/head.obj", true);
		String name = "stefan";
		System.out.println("Reading obj...");
		WireframeMesh wf = ObjReader.read("./objs/faces/" + name + "_disk_remeshed.obj", true);
//		WireframeMesh wf2 = ObjReader.read("./objs/faces/stefan_disk_remeshed.obj", true);
//		WireframeMesh wf = new Bock(1, 1, 1).result;
		System.out.println("Creating half edge structure");
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		
		System.out.println("Reading labels");
		LabelReader l = new LabelReader("labels/faces/" + name + "_disk_remeshed.lab", "labels/faces/faces.txc");
		HashMap<Integer, Point2f> labels = l.read();
		
//		LabelReader boundaryLabels = new LabelReader("out/" + name + "_boundary.lbl", "out/" + name + "_boundary.txc");
//		labels.putAll(boundaryLabels.read());
		
		ConformalMapper mapper = new ConformalMapper(hs, labels);
		mapper.compute();
		System.out.println("Done, writing OBJ");
		
		GLConstraints glc = new GLConstraints(hs, labels);
		glc.addElement2D(mapper.get(), Semantic.POSITION, "pos");
		
		System.out.println("Creating conformal-ish map");
		
		for(String label: l.lbl.keySet())
		{
			Point2f labelCoord = mapper.get().get(l.lbl.get(label));
//			System.out.println("Output labels:");
			System.out.println("" + labelCoord.x + " " + labelCoord.y + " " + label);
		}
		
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
		
		GLHalfEdgeStructure glhs = new GLHalfEdgeStructure(hs);
		glhs.addElement2D(mapper.get(), Semantic.POSITION, "pos");
		glhs.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		
		GLHalfEdgeStructure glhs2 = new GLHalfEdgeStructure(hs);
		glhs2.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		
//		GLWireframeMesh glwf2 = new GLWireframeMesh(wf2);
//		glwf2.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		
		
		MyDisplay d = new MyDisplay();
		
		d.addToDisplay(glhs);
		d.addToDisplay(glhs2);
//		d.addToDisplay(glwf2);
		d.addToDisplay(glc);
		
		
//		GLUpdatableHEStructure glHE = new GLUpdatableHEStructure(hs);
//		MyPickingDisplay disp = new MyPickingDisplay();
//		DeformationPickingProcessor pr = new DeformationPickingProcessor(hs, glHE);
//		disp.addAsPickable(glHE, pr);
		
	}

}
