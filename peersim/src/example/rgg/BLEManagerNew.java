package example.rgg;

import java.util.ArrayList;
import java.util.Comparator;

import peersim.config.*;
import peersim.core.*;

import peersim.edsim.EDSimulator;


public class BLEManagerNew extends BLEManager {
	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
	
	private ArrayList<Node> priority;
	
	
	//--------------------------------------------------------------------------
	// Initialization
	//--------------------------------------------------------------------------
	
	/**
	 * @param prefix string prefix for config properties
	 */
	public BLEManagerNew(String prefix) {
		super(prefix);
		
	}
	
	
	/**
	 *	Clone method of the class. Returns a deep copy of the BLEManager class. Used
	 *	by the simulation to initialize the {@link peersim.core.Network}
	 *	@return the deep copy of the BLEManager class.
	 */
	public Object clone(){
		Object blem = null;
		blem = (BLEManagerNew)super.clone();;
		
		((BLEManagerNew)blem).priority = new ArrayList();
		
		return blem;
	}
	
	
	//--------------------------------------------------------------------------
	// methods
	//--------------------------------------------------------------------------
	
	
	protected void doAdvertising( String idmsg, Node node, int pid, Cookie cookie ) {
		
		// battery --
		this.battery = this.battery - 0.05;
		
		this.updatepriority( node, pid );
		
		
		for (int i = 0; i < priority.size(); i++) {
			
			Node peern = priority.get(i);
			
			EDSimulator.add(
					3,
					new AdvertisingMessage( idmsg, 
							node,
							new Cookie( cookie.dynamicFanout, cookie.advertiseLimit, cookie.tx, cookie.ae ) ),
					peern,
					pid);
			
			
		}
	
	}
	
	
	protected void updatepriority( Node node, int pid ) {
		Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
		
		priority.clear();
		for (int i = 0; i < linkable.degree(); i++) {
			
			Node peern = linkable.getNeighbor(i);
			priority.add(peern);
		}
		
		priority.sort( new Comparator<Node>() {

			public int compare(Node n1, Node n2) {
				
				int df1 = ((BLEManagerNew)n1.getProtocol(pid)).dynamicFanout;
				int df2 = ((BLEManagerNew)n2.getProtocol(pid)).dynamicFanout;
				
				
				if ( df1 > df2 ) {
					return -1;
				} else if ( df1 < df2 ) {
					return 1;
				}
				return 0;
			}
		});
	
	}

}