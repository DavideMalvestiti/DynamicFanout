package example.rgg;

import peersim.config.*;
import peersim.core.*;


public class BLEPull implements Control {
	// ------------------------------------------------------------------------
	// Parameters
	// ------------------------------------------------------------------------

	/**
	 * The protocol to look at.
     * 
     * @config
     */
	private static final String PAR_PROT = "protocol";
	
	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

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
	public BLEPull(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		
	}
	
	
    // Control interface method.
	public boolean execute() {
		
		
		for (int j = 0; j < Network.size(); j++){
			
			((BLEManagerVer4)Network.get(j).getProtocol(pid)).pull( Network.get(j), pid );
			
		}

		return false;
    }

}