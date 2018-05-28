package example.rgg;

import peersim.config.*;
import peersim.core.*;


public class GPSObserver implements Control {
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
	public GPSObserver(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	}
	
	
    // Control interface method.
	public boolean execute() {
		
		
		int l = 0;
		int f = 0;
		for (int j = 0; j < Network.size(); j++){
			
			GPSManager gpsm = (GPSManager) Network.get(j).getProtocol(pid);
			
			if (gpsm.lost) {
				l++;
			} else if (gpsm.found) {
				f++;
			}
			
		}
		
		System.out.print(CommonState.getTime() + ": nodes found "+f+" nodes lost "+l);	

		return false;
    }

}