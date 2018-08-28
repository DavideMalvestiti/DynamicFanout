package rgg;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

public class WireRGGTopology extends WireGraph {
    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------
    /**
     * The alpha parameter. It affects the distance relevance in the wiring
     * process. Default value: 0.5.
     * 
     * @config
     */
    private static final String PAR_RADIUS = "radius";

    /**
     * The coordinate protocol to look at.
     * 
     * @config
     */
    private static final String PAR_COORDINATES_PROT = "coord_protocol";

    // --------------------------------------------------------------------------
    // Fields
    // --------------------------------------------------------------------------
    /* A parameter that affects the distance importance. */
    private final double radius;

    /** Coordinate protocol pid. */
    private final int coordPid;

    // --------------------------------------------------------------------------
    // Initialization
    // --------------------------------------------------------------------------

    /**
     * Standard constructor that reads the configuration parameters. Normally
     * invoked by the simulation engine.
     * 
     * @param prefix
     *            the configuration prefix for this class
     */
    public WireRGGTopology(String prefix) {
        super(prefix);
        coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
        
        radius = Configuration.getDouble(prefix + "." + PAR_RADIUS);
    }

    /**
     * Performs the actual wiring.
     * @param g a {@link peersim.graph.Graph} interface object to work on.
     */
    public void wire(Graph g) {
        Node n,m;
        for (int i = 0; i < Network.size() - 1; i++) {
        	n = (Node) g.getNode(i);
        	for (int j = i+1 ; j < Network.size(); j++) {
        		m = (Node) g.getNode(j);
        		
        		if ( distance(n, m, coordPid) <= radius ) 
        			g.setEdge(i, j);
        		
        	}
        }
    
    }


    /**
     * Utility function: returns the Euclidean distance based on the x,y
     * coordinates of a node. A {@link RuntimeException} is raised if a not
     * initialized coordinate is found.
     * 
     * @param new_node
     *            the node to insert in the topology.
     * @param old_node
     *            a node already part of the topology.
     * @param coordPid
     *            identifier index.
     * @return the distance value.
     */
    private static double distance(Node new_node, Node old_node, int coordPid) {
        double x1 = ((RGGCoordinates) new_node.getProtocol(coordPid))
                .getX();
        double x2 = ((RGGCoordinates) old_node.getProtocol(coordPid))
                .getX();
        double y1 = ((RGGCoordinates) new_node.getProtocol(coordPid))
                .getY();
        double y2 = ((RGGCoordinates) old_node.getProtocol(coordPid))
                .getY();
        if (x1 == -1 || x2 == -1 || y1 == -1 || y2 == -1)
            throw new RuntimeException(
                    "Found un-initialized coordinate. Use e.g., InetInitializer class in the config file.");
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

}
