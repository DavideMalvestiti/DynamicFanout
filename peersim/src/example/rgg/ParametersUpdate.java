package example.rgg;

public class ParametersUpdate {
	
	
	
	private ParametersUpdate() {
	}
	
	
	public static int updatedynamicFanout( int numNeighbors, double battery ) {
		
		double batFactor;
		double result;
		int resultint = 0;
		
		batFactor = Math.sqrt( (0.2*battery) - 1.9 ) / 10;
		
		result = 1 + (batFactor * numNeighbors) - ( 0.0000004 * Math.pow(numNeighbors,4) );
		
		if ( (numNeighbors > 30) && ( (20*batFactor) + 1 > result ) ){
			resultint = (int)Math.ceil( (20*batFactor) + 1 );
		} else if ( result <= 0 ) {
			resultint = 1;
		} else {
			resultint = (int)Math.ceil(result);
		}
		
		return resultint;
	}
	
	
	public static int updateadvertiseLimit( int numNeighbors, double battery ) {
		
		double result = Math.log( 2 * numNeighbors ) + 1;
		int resultint = (int)Math.ceil(result);
		
		if ( result <= 0 ) {
			resultint = 1;
		}
		
		return resultint;
	}

}