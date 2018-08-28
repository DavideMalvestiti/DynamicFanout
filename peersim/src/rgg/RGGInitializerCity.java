package rgg;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;


public class RGGInitializerCity extends RGGInitializer {
    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------

	/**
	 * If set, the city has a center. Defaults to false.
	 * @config
	 */
	private static final String PAR_CENTER = "center";

	// --------------------------------------------------------------------------
	// Fields
	// --------------------------------------------------------------------------

	/**
     * Value obtained from config property
     * {@link #PAR_CENTER}.
     */
	protected final boolean center;

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
    public RGGInitializerCity(String prefix) {
        super(prefix);
        center = Configuration.contains(prefix + "." + PAR_CENTER);
        
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    public boolean execute() {
        Node n;
        RGGCoordinates prot;
        
        double side = this.getSide(density);
        double R = side / Math.sqrt( Math.PI );
        
        double r = 0;
        double t = 0;
        double Re = 0;
        double Ri = 0;
        int Ni;
        
        if (center) {
        	
        	/* 
        	 * center radius 2/3 of the city radius
        	 * center population 75% of the city population
        	 */
        	Ri = R * 2/3 ;
        	Re = R - Ri;
        	Ni = (int)Math.ceil( Network.size() * 0.75 );
        	
        } else {
        	
        	Ri = R;
        	Ni = Network.size();
        }

        // Set coordinates x,y
        for (int i = 0; i < Network.size(); i++) {
            n = Network.get(i);
            prot = (RGGCoordinates) n.getProtocol(pid);
            
            if (i < Ni) {
            	
            	r = Ri * CommonState.r.nextDouble();
            } else {
            	
            	r = Re * CommonState.r.nextDouble() + Ri;
            }
            t = 2 * Math.PI * CommonState.r.nextDouble();
            
            prot.setX( r * Math.cos(t) + R );
            prot.setY( r * Math.sin(t) + R );
        }
        return false;
    }

}