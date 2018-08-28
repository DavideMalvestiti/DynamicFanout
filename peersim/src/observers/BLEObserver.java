package observers;

import managers.BLE;
import peersim.config.*;
import peersim.core.*;


public class BLEObserver implements Control {
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
	public BLEObserver(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	}
	
	
    // Control interface method.
	public boolean execute() {
		
		
		System.out.print(CommonState.getTime() + ":");	
		
		for (int j = 0; j < Network.size(); j++){
			
			BLE blem = (BLE) Network.get(j).getProtocol(pid);
			
			
			System.out.print( "  node" + Network.get(j).getID() + "->" + blem.getbleState() );
		}
		
		System.out.println(" ");

		return false;
    }

}