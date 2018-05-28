package example.rgg;

import peersim.config.*;
import peersim.core.*;


public class GPStarter implements Control {
	// ------------------------------------------------------------------------
	// Parameters
	// ------------------------------------------------------------------------

	/**
	 * The protocol to look at.
     * 
     * @config
     */
	private static final String PAR_PROT = "protocol";

	/**
	 * The number of nodes lost.
     * 
     * @config
     */
	private static final String PAR_NLOST = "nlost";

	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

	/**
     * Value obtained from config property
     * {@link #PAR_PROT}.
     */
    private final int pid;

    /**
     * Value obtained from config property
     * {@link #PAR_NLOST}.
     */
    private final double nlost;

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
	public GPStarter(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		nlost = Configuration.getDouble(prefix + "." + PAR_NLOST, 0);
	}
	
	
    // Control interface method.
	public boolean execute() {
		
		
		int num;
		if (nlost > 1 || nlost < 0) {
			return false;
		} else {
			num = (int) (Network.size() * nlost);
		}
		
		for (int i = 0; i < num; i++) {
			
			Node n = Network.get(i);
            ((GPSManager) n.getProtocol(pid)).isLost();
		}

		return false;
    }

}