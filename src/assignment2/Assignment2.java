package assignment2;

import glWrapper.GLHashtree;
import glWrapper.GLHashtree_Vertices;

import java.io.IOException;

import meshes.PointCloud;
import meshes.reader.PlyReader;
import openGL.MyDisplay;


public class Assignment2 {
	
	public static void main(String[] args) throws IOException{
		
		mortonCodesDemo();
		
		//these demos will run once all methods in the MortonCodes class are
		//implemented.
//		hashTreeDemo(ObjReader.readAsPointCloud("./objs/dragon.obj", true));
		hashTreeDemo(PlyReader.readPointCloud("./objs/octreeTest.ply", true));
		
	}
	

	
	public static void mortonCodesDemo(){
		
		//example of a level 4 (cell) morton code
		long hash = 		0b1000101000100;
		
		//the hashes of its parent and neighbors
		long parent = 		0b1000101000;
		long nbr_plus_x = 	0b1000101100000;
		long nbr_plus_y =   0b1000101000110;
		long nbr_plus_z =   0b1000101000101;
		
		long nbr_minus_x = 	0b1000101000000;
		long nbr_minus_y =  -1; //invalid: the vertex lies on the boundary and an underflow should occur
		long nbr_minus_z =  0b1000100001101;
		
		
		//example of a a vertex morton code in a multigrid of
		//depth 4. It lies on the level 3 and 4 grids
		long vertexHash = 0b1000110100000;
				
		//you can test your MortonCode methods by checking these results, e.g. as a Junit test
		//Further test at least one case where -z underflow should occur
		//and a case where overflow occurs.
		assert(MortonCodes.parentCode(hash) == parent);
		assert(MortonCodes.nbrCode(hash, 4, 0b100) == nbr_plus_x);
		assert(MortonCodes.nbrCode(hash, 4, 0b010) == nbr_plus_y);
		assert(MortonCodes.nbrCode(hash, 4, 0b001) == nbr_plus_z);
		assert(MortonCodes.nbrCode(0b1111111, 2, 0b100) == -1);
		assert(MortonCodes.nbrCodeMinus(hash, 4, 0b100) == nbr_minus_x);
		assert(MortonCodes.nbrCodeMinus(hash, 4, 0b010) == nbr_minus_y);
		assert(MortonCodes.nbrCodeMinus(hash, 4, 0b001) == nbr_minus_z);
		assert(MortonCodes.isCellOnLevelXGrid(hash, 4));
		assert(!MortonCodes.isCellOnLevelXGrid(hash, 3));
		assert(MortonCodes.isVertexOnLevelXGrid(vertexHash, 3, 4));
		assert(MortonCodes.isVertexOnLevelXGrid(vertexHash, 4, 4));
		assert(!MortonCodes.isVertexOnLevelXGrid(vertexHash, 2, 4));
	}
	
	public static void hashTreeDemo(PointCloud p)
	{
		HashOctree ot = new HashOctree(p, 4, 1, 1);
		
		GLHashtree glot = new GLHashtree(ot);
		glot.configurePreferredShader("shaders/octree.vert", "shaders/octree.frag", "shaders/octree.geom");
		GLHashtree glotP = new GLHashtree(ot);
		glotP.configurePreferredShader("shaders/octree_parent.vert", "shaders/octree.frag", "shaders/octree_parent.geom");
		glotP.addElement(3, "parent_pos", new HashOctreeCellAttribute() {
			@Override
			public float[] getAttribute(HashOctreeCell v, HashOctree t) {
				HashOctreeCell p = t.getParent(v);
				if(p == null)
				{
					return new float[]{0, 0, 0};
				}
				else
				{
					return new float[]{p.center.x, p.center.y, p.center.z};
				}
			}
		});
		GLHashtree glotN = new GLHashtree(ot);
		glotN.configurePreferredShader("shaders/octree_nbr.vert", "shaders/octree.frag", "shaders/octree_nbr.geom");
		for(int i = 0b001; i != 0b1000; i <<= 1)
		{
			final int Obxyz = i;
			String coord = i == 0b100 ? "x" :
				           i == 0b010 ? "y" :
				                        "z";
			glotN.addElement(3, "nbr_pos_" + coord, new HashOctreeCellAttribute() {
				@Override
				public float[] getAttribute(HashOctreeCell v, HashOctree t) {
					HashOctreeCell p = t.getNbr_c2c(v, Obxyz);
					if(p == null)
					{
						return new float[]{0, 0, 0};
					}
					else
					{
						return new float[]{p.center.x, p.center.y, p.center.z};
					}
				}
			});
		}
		GLHashtree_Vertices glotNV = new GLHashtree_Vertices(ot);
		glotNV.configurePreferredShader("shaders/octree_nbr.vert", "shaders/octree.frag", "shaders/octree_vnbr.geom");
		for(int i = 0b001; i != 0b1000; i <<= 1)
		{
			final int Obxyz = i;
			String coord = i == 0b100 ? "x" :
				           i == 0b010 ? "y" :
				                        "z";
			glotNV.addElement(3, "nbr_pos_" + coord, new HashOctreeVertexAttribute() {
				@Override
				public float[] getAttribute(HashOctreeVertex v, HashOctree t) {
					HashOctreeVertex p = t.getNbr_v2v(v, Obxyz);
					if(p == null)
					{
						return new float[]{v.position.x, v.position.y, v.position.z};
					}
					else
					{
						return new float[]{p.position.x, p.position.y, p.position.z};
					}
				}
			});
		}
		GLHashtree_Vertices glotNVM = new GLHashtree_Vertices(ot);
		glotNVM.configurePreferredShader("shaders/octree_nbr.vert", "shaders/octree.frag", "shaders/octree_vnbr.geom");
		for(int i = 0b001; i != 0b1000; i <<= 1)
		{
			final int Obxyz = i;
			String coord = i == 0b100 ? "x" :
				           i == 0b010 ? "y" :
				                        "z";
			glotNVM.addElement(3, "nbr_pos_" + coord, new HashOctreeVertexAttribute() {
				@Override
				public float[] getAttribute(HashOctreeVertex v, HashOctree t) {
					HashOctreeVertex p = t.getNbr_v2vMinus(v, Obxyz);
					if(p == null)
					{
						return new float[]{v.position.x, v.position.y, v.position.z};
					}
					else
					{
						return new float[]{p.position.x, p.position.y, p.position.z};
					}
				}
			});
		}

		
		MyDisplay disp = new MyDisplay();
		disp.addToDisplay(glot);
		disp.addToDisplay(glotP);
		disp.addToDisplay(glotN);
		disp.addToDisplay(glotNV);
		disp.addToDisplay(glotNVM);
	}
}
