package assignment7;

import java.util.HashMap;

import javax.vecmath.Point2f;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLUpdatableHEStructure;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import openGL.MyPickingDisplay;
import openGL.gl.GLDisplayable.Semantic;
import algorithms.ConformalMapper;
import assignment4.generatedMeshes.Bock;
import assignment6.DeformationPickingProcessor;

public class ConformalMapDemo {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		WireframeMesh wf = ObjReader.read("./objs/head.obj", true);
		WireframeMesh wf = ObjReader.read("./objs/testData/peter.obj", true);
//		WireframeMesh wf = new Bock(1, 1, 1).result;
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		
		LabelReader l = new LabelReader("labels/1nh.lbl", "labels/1nh.txc");
		HashMap<Integer, Point2f> labels = l.read();
		
		

		ConformalMapper mapper = new ConformalMapper(hs, labels);
		mapper.compute();
		System.out.println("Bla");
		
		GLHalfEdgeStructure glhs = new GLHalfEdgeStructure(hs);
		glhs.addElement2D(mapper.get(), Semantic.POSITION, "pos");
		glhs.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		
		MyDisplay d = new MyDisplay();
		
		d.addToDisplay(glhs);
		
//		GLUpdatableHEStructure glHE = new GLUpdatableHEStructure(hs);
//		MyPickingDisplay disp = new MyPickingDisplay();
//		DeformationPickingProcessor pr = new DeformationPickingProcessor(hs, glHE);
//		disp.addAsPickable(glHE, pr);
		
	}

}
