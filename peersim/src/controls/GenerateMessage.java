package controls;

import managers.BLE;
import peersim.config.*;
import peersim.core.*;


public class GenerateMessage implements Control {
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
	 * The node generate the msg.
     * 
     * @config
     */
	private static final String PAR_NODE = "node";

	/**
	 * The size of the msg.
     * 
     * @config
     */
	private static final String PAR_SIZE = "size";
	
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
     * {@link #PAR_NODE}.
     */
    private final int node;

    /**
     * Value obtained from config property
     * {@link #PAR_SIZE}.
     */
    private final int sizex;

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
	public GenerateMessage(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		node = Configuration.getInt(prefix + "." + PAR_NODE, 0);
		sizex = Configuration.getInt(prefix + "." + PAR_SIZE, 2);  // 10^x Kbyte
	}
	
	
    // Control interface method.
	public boolean execute() {
		
		
		String newid = String.valueOf( node ) + System.currentTimeMillis();
		String newmsg = String.valueOf( sizex ) + Long.toHexString(Double.doubleToLongBits(Math.random()));
		
		BLE blem = (BLE) Network.get(node).getProtocol(pid);
		
		blem.myNewmsg( newid, newmsg, Network.get(node), pid );

		return false;
    }

}
