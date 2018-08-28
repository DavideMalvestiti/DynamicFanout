package controls;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import managers.BLE;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.graph.Graph;
import peersim.reports.GraphObserver;
import rgg.RGGCoordinates;


public class BLEStamp extends GraphObserver {
    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------

    /**
     * The filename base to print out the topology relations.
     * 
     * @config
     */
    private static final String PAR_FILENAME_BASE = "file_base";

    /**
     * The coordinate protocol to look at.
     * 
     * @config
     */
    private static final String PAR_COORDINATES_PROT = "coord_protocol";
    
    /**
	 * The protocol to look at.
     * 
     * @config
     */
	private static final String PAR_PROT = "pid";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /**
     * Topology filename. Obtained from config property
     * {@link #PAR_FILENAME_BASE}.
     */
    private final String graph_filename;

    /**
    * The number of filenames already returned.
    */
    private long counter = 0;

    /**
     * Coordinate protocol identifier. Obtained from config property
     * {@link #PAR_COORDINATES_PROT}.
     */
    private final int coordPid;
    
    /**
     * Value obtained from config property
     * {@link #PAR_PROT}.
     */
    private final int pid;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Standard constructor that reads the configuration parameters. Invoked by
     * the simulation engine.
     * 
     * @param prefix
     *            the configuration prefix for this class.
     */
    public BLEStamp(String prefix) {
        super(prefix);
        coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        graph_filename = Configuration.getString(prefix + "."
                + PAR_FILENAME_BASE, "bleStates");
    }

    // Control interface method.
    public boolean execute() {
        try {
            updateGraph();

            System.out.print(name + ": ");

            // initialize output streams
            String fname1 = graph_filename + counter + ".dat";
            String fname2 = "receivers" + counter + ".dat";
            counter++;
            
            FileOutputStream fos = new FileOutputStream(fname1);
            System.out.println("Writing to file " + fname1);
            PrintStream pstr = new PrintStream(fos);

            // dump topology:
            statesToFile(g, pstr, coordPid, pid);

            fos.close();
            
            
            FileOutputStream flos = new FileOutputStream(fname2);
            System.out.println("Writing to file " + fname2);
            PrintStream ps = new PrintStream(flos);

            // dump topology:
            graphToFile(g, ps, coordPid, pid);

            flos.close();
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
    
    private static void statesToFile(Graph g, PrintStream ps, int coordPid, int pid) {
        for (int i = 0; i < g.size(); i++) {
        	
            Node current = (Node) g.getNode(i);
            
            double x_to = ((RGGCoordinates) current
                    .getProtocol(coordPid)).getX();
            double y_to = ((RGGCoordinates) current
                    .getProtocol(coordPid)).getY();
            
            int bleState = ((BLE) current
            		.getProtocol(pid)).getbleState();
            
            ps.println(x_to + " " + y_to + " " + bleState);
            ps.println();
        }
    }
    
    private static void graphToFile(Graph g, PrintStream ps, int coordPid, int pid) {
        for (int i = 0; i < g.size(); i++) {
        	
            Node current = (Node) g.getNode(i);
            
            double x_to = ((RGGCoordinates) current
                    .getProtocol(coordPid)).getX();
            double y_to = ((RGGCoordinates) current
                    .getProtocol(coordPid)).getY();
            
            Node receiver = ((BLE) current
            		.getProtocol(pid)).getReceiver();
            
            if (receiver != null){
            	
            	double x_from = ((RGGCoordinates) receiver
                        .getProtocol(coordPid)).getX();
                double y_from = ((RGGCoordinates) receiver
                        .getProtocol(coordPid)).getY();
            	
                ps.println(x_from + " " + y_from);
                ps.println(x_to + " " + y_to);
                ps.println();
            }
        }
    }

}