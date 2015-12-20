package plot.data;

import java.util.Hashtable;

public class Pair {

	private double xValue;
	private double yValue;

	private Pair(double xValue, double yValue) {
					
		this.xValue = xValue;
		this.yValue = yValue;
		
	}

	public static Pair getPair(double xValue, double yValue){
		
		return new Pair(xValue,yValue);
			
	}
		
	public double getxValue() {
		return xValue;
	}

	public double getyValue() {
		return yValue;
	}

	public void normalize(double normalizedXValue, double normalizedYValue) {
		xValue = xValue / normalizedXValue;
		yValue = yValue / normalizedYValue;
	}

	
}
