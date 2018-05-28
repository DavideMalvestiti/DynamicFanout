package example.rgg;

import java.util.ArrayList;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.*;

import peersim.edsim.EDSimulator;


public class GPSManager extends BLEManager {
	// ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------
	
    /**
     * The coordinate protocol to look at.
     * 
     * @config
     */
    private static final String PAR_COORDINATES_PROT = "coord_protocol";

	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
	
    /** Coordinate protocol pid. */
    private final int coordPid;
	
    
    private ArrayList<Node> nodes;
	private ArrayList<RGGCoordinates> pos;
	private ArrayList<Double> distances;
	
	protected boolean lost = false;
	protected boolean found = false;
	private Cookie cookie;

	//--------------------------------------------------------------------------
	// Initialization
	//--------------------------------------------------------------------------
	
	/**
	 * @param prefix string prefix for config properties
	 */
	public GPSManager(String prefix) {
		super(prefix);
		coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
		cookie = new Cookie(0,0,0,0);
		
	}
	
	
	/**
	 *	Clone method of the class. Returns a deep copy of the BLEManager class. Used
	 *	by the simulation to initialize the {@link peersim.core.Network}
	 *	@return the deep copy of the BLEManager class.
	 */
	public Object clone(){
		Object blem = null;
		blem = (GPSManager)super.clone();;
		
		((GPSManager)blem).nodes = new ArrayList();
		((GPSManager)blem).pos = new ArrayList();
		((GPSManager)blem).distances = new ArrayList();
		
		return blem;
	}
	
	
	//--------------------------------------------------------------------------
	// methods
	//--------------------------------------------------------------------------
	
	
	/**
	 * This is the standard method the define periodic activity.
	 * The frequency of execution of this method is defined by a
	 * {@link peersim.edsim.CDScheduler} component in the configuration.
	 */
	public void nextCycle( Node node, int pid ) {
		
		
		if ( lost 
				&& !busy 
				&& (pos.size() < 3) ) {
			
			this.doAdvertising(PAR_COORDINATES_PROT, node, pid, cookie);
		}
	
	}
	
	
	/**
	 * This is the standard method to define to process incoming messages.
	 */
	public void processEvent( Node node, int pid, Object event ) {
		
		
		if (event.getClass() == AdvertisingMessage.class) {
			/* AdvertisingMessage adv = (AdvertisingMessage)event;
			 * 
        	 * Neighbor Discovery
        	 */
			
		} else if (event.getClass() == ConnectionRequest.class) {
			ConnectionRequest rq = (ConnectionRequest)event;
			
			if ( !busy 
					&& !((GPSManager)rq.sender.getProtocol(pid)).busy  
					&& !lost 
					&& !nodes.contains( rq.sender ) ){
				
				
				nodes.add( rq.sender );
				
				// lock nodes
				this.busy = true;
				((GPSManager)rq.sender.getProtocol(pid)).busy = true;
				
				receiver = rq.sender.getIndex();
				
				// delay calculation
				int sizemsg = 100;  // 100 Kbyte
				long delay = ( sizemsg / 125 ) * 1000 + 3;  // 125 Kbyte/s
				
				RGGCoordinates mypos = ((RGGCoordinates)node.getProtocol(coordPid));
				
				
				EDSimulator.add(
						delay,
						new ConnectionMessage( rq.idmsg, node, mypos.getX()+"-"+mypos.getY() ),
						rq.sender,
						pid);
				
				EDSimulator.add(
						delay,
						new Unlock( rq.idmsg, cookie ),
						node,
						pid);
				
				
			}
			
		} else if (event.getClass() == ConnectionMessage.class) {
			ConnectionMessage con = (ConnectionMessage)event;
			
			if ( busy && lost ){
				
				
				String[] parts = con.msg.split("-");
				RGGCoordinates newpos = new RGGCoordinates(" ");
				
				newpos.setX( Double.parseDouble( parts[0] ) );
				newpos.setY( Double.parseDouble( parts[1] ) );
				
				pos.add(newpos);
				distances.add( this.clockSynchronization(node, con.sender) );
				
				battery = battery - 1;
				if (battery < 0) battery = 0;
				
				// unlock node
				this.busy = false;
				
				
				if (pos.size() > 2) {
					
					String p = ThreeCircleIntersection.calculateThreeCircleIntersection( 
							pos.get(0), distances.get(0), 
							pos.get(1), distances.get(1), 
							pos.get(2), distances.get(2));
					
					
					String[] pts = p.split("-");
					RGGCoordinates myp = new RGGCoordinates(" ");
					
					myp.setX( Double.parseDouble( pts[0] ) );
					myp.setY( Double.parseDouble( pts[1] ) );
					
					
					System.out.println( "node " + node.getIndex() + " found" );
					
					RGGCoordinates mypos = ((RGGCoordinates)node.getProtocol(coordPid));
					System.out.println( "E = "+(mypos.getX()-myp.getX())+"    "+(mypos.getY()-myp.getY()) );
					
					System.out.println(" ");
					
					
					found = true;
					lost = false;
				}
			}
			
		} else if (event.getClass() == Unlock.class) {
			
			battery = battery - 1;
			if (battery < 0) battery = 0;
			
			// unlock node
			this.busy = false;
			
			receiver = -1;
		}
	
	}
	
	
	protected void doAdvertising( String idmsg, Node node, int pid, Cookie cookie ) {
		Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
		
		for (int i = 0; i < linkable.degree(); i++) {
			
			Node peern = linkable.getNeighbor(i);
			
			EDSimulator.add(
					3,
					new ConnectionRequest(idmsg, node, cookie),
					peern,
					pid);
			
			
		}
	
	}
	
	
	protected double clockSynchronization( Node node, Node sender ) {
		
		/*
		temp.setX( ((RGGCoordinates) sender.getProtocol(coordPid)).getX() );
		temp.setY( ((RGGCoordinates) sender.getProtocol(coordPid)).getY() );
		pos.add(temp);
		*/
		
		return ThreeCircleIntersection.distance(node, sender, coordPid);
	}
	
	
	public void isLost(  ) {
		
		nodes.clear();
		pos.clear();
		distances.clear();
		lost = true;
		found = false;
	}

}