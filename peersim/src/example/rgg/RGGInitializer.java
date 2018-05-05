package example.rgg;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class RGGInitializer implements Control {
    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------
    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";
    
    protected static final String PAR_DENSITY = "density";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
    private final int pid;
    
    protected double density;  // float ?

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
    public RGGInitializer(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        density = (Configuration.getDouble(prefix + "." + PAR_DENSITY));
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    public boolean execute() {
        Node n;
        RGGCoordinates prot;

        // Set coordinates x,y
        for (int i = 0; i < Network.size(); i++) {
            n = Network.get(i);
            prot = (RGGCoordinates) n.getProtocol(pid);
            prot.setX( CommonState.r.nextDouble() * this.getSide(density) );
            prot.setY( CommonState.r.nextDouble() * this.getSide(density) );
        }
        return false;
    }

    public double getSide(double d) {
		return Math.sqrt( (Network.size() / d) );
    }

}
