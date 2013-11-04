package meshes;

import java.util.ArrayList;

import javax.vecmath.Tuple3f;

/**
 * Simple data structure, which lets you associate 3d data to the vertices of a half-edge structure 
 * @author bertholet
 *
 */
public class HEData3d extends IterableHEData<Vertex, Tuple3f> {
	
	
	public HEData3d(HalfEdgeStructure hs) {
		super(hs.getVertices());
	}
	
	public HEData3d(HalfEdgeStructure hs, ArrayList<Tuple3f> val)
	{
		super(hs.getVertices());
		for(int i = 0; i < val.size(); i++)
		{
			put(hs.getVertices().get(i), val.get(i));
		}
	}
}
