package assignment7;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLUpdatableHEStructure;

import java.util.HashMap;

import javax.vecmath.Point2f;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import openGL.MyPickingDisplay;
import openGL.gl.GLDisplayable.Semantic;
import algorithms.ConformalMapper;
import assignment6.DeformationPickingProcessor;

public class ConformalMapDemo {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		WireframeMesh wf = ObjReader.read("./objs/head.obj", true);
		System.out.println("Reading obj...");
		WireframeMesh wf = ObjReader.read("./objs/faces/stefan_disk_remeshed.obj", true);
//		WireframeMesh wf = new Bock(1, 1, 1).result;
		System.out.println("Createing half edge structure");
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		
		System.out.println("Reading labels");
		LabelReader l = new LabelReader("labels/example.lbl", "labels/example.txc");
		HashMap<Integer, Point2f> labels = l.read();
		
		GLConstraints glc = new GLConstraints(hs, labels);
		System.out.println("Creating conformal-ish map");
		
		ConformalMapper mapper = new ConformalMapper(hs, labels);
		mapper.compute();
		System.out.println("Bla");
		
		GLHalfEdgeStructure glhs = new GLHalfEdgeStructure(hs);
		glhs.addElement2D(mapper.get(), Semantic.POSITION, "pos");
		glhs.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		
		GLHalfEdgeStructure glhs2 = new GLHalfEdgeStructure(hs);
		glhs2.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		
		MyDisplay d = new MyDisplay();
		
		d.addToDisplay(glhs);
		d.addToDisplay(glhs2);
		d.addToDisplay(glc);
		
//		GLUpdatableHEStructure glHE = new GLUpdatableHEStructure(hs);
//		MyPickingDisplay disp = new MyPickingDisplay();
//		DeformationPickingProcessor pr = new DeformationPickingProcessor(hs, glHE);
//		disp.addAsPickable(glHE, pr);
		
	}

}
