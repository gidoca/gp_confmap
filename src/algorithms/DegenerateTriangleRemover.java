package algorithms;

import java.util.ArrayList;

import assignment5.HalfEdgeCollapse;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;

public class DegenerateTriangleRemover
{
	private HalfEdgeStructure mesh;
	private HalfEdgeCollapse collaptor;
	private float epsilon;
	
	public DegenerateTriangleRemover(HalfEdgeStructure mesh, float epsilon)
	{
		this.mesh = mesh;
		this.epsilon = epsilon;
		this.collaptor = new HalfEdgeCollapse(mesh);
	}
	
	public void apply()
	{
		boolean changed = true;
		
		while(changed)
		{
			changed = false;
			ArrayList<HalfEdge> halfEdges = new ArrayList<>(mesh.getHalfEdges());
			for(HalfEdge e: halfEdges)
			{
				if(
						!collaptor.isEdgeDead(e) && 
						e.getVec().length() <= epsilon && 
						HalfEdgeCollapse.isEdgeCollapsable(e) && 
						!collaptor.isCollapseMeshInv(e, e.end().getPos()))
				{
					collaptor.collapseEdge(e);
					changed = true;
				}
			}
		}
		
		collaptor.finish();
	}
}
