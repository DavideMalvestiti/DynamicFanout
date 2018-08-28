package rgg;

import peersim.core.Network;
import peersim.core.Node;


public class RGGCityNodeMoving extends RGGInitializer {
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
    public RGGCityNodeMoving(String prefix) {
        super(prefix);
        
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
        double x = 0;
        double y = 0;

        for (int i = 0; i < Network.size(); i++) {
            n = Network.get(i);
            prot = (RGGCoordinates) n.getProtocol(pid);
            
            x = prot.getX() - R;
            y = prot.getY() - R;
            
            r = Math.sqrt( (x*x) + (y*y) );
            t = Math.atan2(y, x);
            
            if (r < R) {
            	r = r + 2;
            }
            
            prot.setX( r * Math.cos(t) + R );
            prot.setY( r * Math.sin(t) + R );
        }
        return false;
    }

}