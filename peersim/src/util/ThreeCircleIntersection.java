package util;

import peersim.core.Node;
import rgg.RGGCoordinates;


public class ThreeCircleIntersection {
	
	
	private ThreeCircleIntersection() {
	}
	
	
	public static String  calculateThreeCircleIntersection( 
			RGGCoordinates p0, double r0,
			RGGCoordinates p1, double r1,
			RGGCoordinates p2, double r2) {
		
		
		double a, dx, dy, d, h, rx, ry, point2_x, point2_y;
		double x0 = p0.getX(); double y0 = p0.getY();
		double x1 = p1.getX(); double y1 = p1.getY();
		double x2 = p2.getX(); double y2 = p2.getY();
		
		/* dx and dy are the vertical and horizontal distances between
		* the circle centers.
		*/
		
		dx = x1 - x0;
		dy = y1 - y0;
		
		/* Determine the straight-line distance between the centers. */
		d = Math.sqrt((dy*dy) + (dx*dx));
		
		/* Check for solvability. */
		if (d > (r0 + r1)) {
			/* no solution. circles do not intersect. */
			return null;
		} else if (d < Math.abs(r0 - r1)) {
			/* no solution. one circle is contained in the other */
			return null;
		}
		
		/* point 2 is the point where the line through the circle
		* intersection points crosses the line between the circle
		* centers.
		*/
		
		/* Determine the distance from point 0 to point 2. */
		a = ((r0*r0) - (r1*r1) + (d*d)) / (2.0 * d) ;
		
		/* Determine the coordinates of point 2. */
		point2_x = x0 + (dx * a/d);
		point2_y = y0 + (dy * a/d);
		
		/* Determine the distance from point 2 to either of the
		 * intersection points.
		 */
		h = Math.sqrt((r0*r0) - (a*a));
		
		/* Now determine the offsets of the intersection points from
		 * point 2.
		 */
		rx = -dy * (h/d);
		ry = dx * (h/d);
		
		/* Determine the absolute intersection points. */
		
		double intersectionPoint1_x = point2_x + rx;
		double intersectionPoint1_y = point2_y + ry;
		
		double intersectionPoint2_x = point2_x - rx;
		double intersectionPoint2_y = point2_y - ry;
		
		/* Lets determine if circle 3 intersects at either of the above intersection points. */
		dx = intersectionPoint1_x - x2;
		dy = intersectionPoint1_y - y2;
		double d1 = Math.sqrt((dy*dy) + (dx*dx));
		
		dx = intersectionPoint2_x - x2;
		dy = intersectionPoint2_y - y2;
		double d2 = Math.sqrt((dy*dy) + (dx*dx));
		
		
		if (Math.abs(d1 - r2) < 0.000001) {
			
			return ( intersectionPoint1_x )+"-"+( intersectionPoint1_y );
			
		} else if (Math.abs(d2 - r2) < 0.000001) {
			
			return ( intersectionPoint2_x )+"-"+( intersectionPoint2_y );
			
		} else {
			return null;
		}
	
	}
	
	
	/**
     * Utility function: returns the Euclidean distance based on the x,y
     * coordinates of a node. A {@link RuntimeException} is raised if a not
     * initialized coordinate is found.
     * 
     * @param new_node
     *            the node to insert in the topology.
     * @param old_node
     *            a node already part of the topology.
     * @param coordPid
     *            identifier index.
     * @return the distance value.
     */
    public static double distance(Node new_node, Node old_node, int coordPid) {
        double x1 = ((RGGCoordinates) new_node.getProtocol(coordPid))
                .getX();
        double x2 = ((RGGCoordinates) old_node.getProtocol(coordPid))
                .getX();
        double y1 = ((RGGCoordinates) new_node.getProtocol(coordPid))
                .getY();
        double y2 = ((RGGCoordinates) old_node.getProtocol(coordPid))
                .getY();
        if (x1 == -1 || x2 == -1 || y1 == -1 || y2 == -1)
            throw new RuntimeException(
                    "Found un-initialized coordinate. Use e.g., InetInitializer class in the config file.");
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

}