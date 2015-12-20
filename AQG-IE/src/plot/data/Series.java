package plot.data;

import java.util.ArrayList;
import java.util.List;

public class Series {

	private ArrayList<Pair> pairs;
	private ArrayList<Series> series;
	private int maxSize;
	private String name;

	public Series(String name){
		this.name = name;
		pairs = new ArrayList<Pair>();
		series = new ArrayList<Series>();
		maxSize = -1;
	}
	
	public void addPair(Pair pair) {
		pairs.add(pair);
	}

	public Series normalize(double normalizedXValue, double normalizedYValue) {
		
//		Series ret = new Series(getName());
		
		for (Pair pair : pairs) {
			
			pair.normalize(normalizedXValue,normalizedYValue);
//			ret.addPair(Pair.getPair(pair.getxValue()/normalizedXValue,pair.getyValue()/normalizedYValue));
			
		}
		
		return this;
		
	}

	public void addSeries(Series series) {
		this.series.add(series);
	}

	public Series averageSeries() {
		
		Series ret = new Series(this.name.replace(" ", "_"));

		updatemaxSize();
		
		for (int i = 0; i < maxSize; i++) {
			
			double cant = 0;
			double sum = 0;
			double xAxisValue = 0;
			for (Series ser : series) {

				cant++;
				if (ser.size() > i){
					sum += ser.pairs.get(i).getyValue();
					xAxisValue = ser.pairs.get(i).getxValue();
				} else {
					if (ser.pairs.size() == 0){
						sum += 0;
						cant--;
					}else{
						sum += ser.pairs.get(ser.pairs.size()-1).getyValue();
					}
				}				
				
			}
			
			ret.addPair(Pair.getPair(xAxisValue, sum/cant));
			
		}
		
		for (Series ser : series){
			while (ser.pairs.size() > 0){
				ser.pairs.remove(0);
			}
			ser.pairs = new ArrayList<Pair>(0);
		}
		
		return ret;
		
	}

	private void updatemaxSize() {
		
		for (Series seri : this.series) {
			
			if (seri.pairs.size() > maxSize){
				maxSize = seri.pairs.size();
			}
			
		}
		
	}

	public int size() {
		return pairs.size();
	}

	public String getName() {
		return name;
	}

	public List<Pair> getPairs() {
		return pairs;
	}

	public Series segment(int intervalNumbers){

		if (pairs.size() <= intervalNumbers)
			return removeDuplicates(this);
		
		double maxValue = pairs.get(pairs.size()-1).getxValue();
		
		double incr = Math.ceil(maxValue / (double)intervalNumbers);
		
		return segmentate(incr);
		
	}

	public Series segmentate(double incremental) {
		
		if (pairs.size() == 0)
			return this;
		
		Series ser = new Series(name);
		
		double max = pairs.get(pairs.size()-1).getxValue();
		
		double lastValue = 0.0;
		
		int actualIndex = 0;
		
		double actualincr = incremental;
		
		while (actualincr < max){
			
			while (actualIndex < pairs.size() && pairs.get(actualIndex).getxValue() < actualincr)	{
			
				actualIndex++;
			
			}
			
			//Omit duplicates
			
			while (actualIndex < pairs.size()-1 && (pairs.get(actualIndex).getxValue() == pairs.get(actualIndex+1).getxValue())){
				actualIndex++;
			}
			
			if (actualIndex < pairs.size()){
				
				if (pairs.get(actualIndex).getxValue() >= actualincr && pairs.get(actualIndex).getxValue() < actualincr + incremental){
					
					lastValue = pairs.get(actualIndex).getyValue();
				
				}
				
				ser.addPair(Pair.getPair(actualincr,lastValue));
				
			}
			
			actualincr+=incremental;
		
		}
			
		ser.addPair(Pair.getPair(actualincr,pairs.get(pairs.size()-1).getyValue()));
		
		while(pairs.size()>0){
			pairs.remove(0);
		}
		pairs = new ArrayList<Pair>(0);
		
		return ser;
	
	}

	private Series removeDuplicates(Series series2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Series independentAverage() {
		
		Series ret = new Series(this.name.replace(" ", "_"));

		updatemaxSize();
		
		for (int i = 0; i < maxSize; i++) {
			
			double cant = 0;
			double sum = 0;
			double xAxisValue = 0;
			for (Series ser : series) {
				
				if (ser.size() > i){
					cant++;
					sum += ser.pairs.get(i).getyValue();
					xAxisValue = ser.pairs.get(i).getxValue();
				}				
				
			}
			
			ret.addPair(Pair.getPair(xAxisValue, sum/cant));
			
		}
		
		for (Series ser : series){
			while (ser.pairs.size() > 0){
				ser.pairs.remove(0);
			}
			ser.pairs = new ArrayList<Pair>(0);
		}
		
		return ret;
		
	}

	public Series numberOfSeries() {
		
		Series ret = new Series(this.name.replace(" ", "_"));

		updatemaxSize();
		
		for (int i = 0; i < maxSize; i++) {
			
			double cant = 0;

			double xAxisValue = 0;
			for (Series ser : series) {
				
				if (ser.size() > i){
					cant++;
					xAxisValue = ser.pairs.get(i).getxValue();
				}				
				
			}
			
			ret.addPair(Pair.getPair(xAxisValue, cant));
			
		}
		
		for (Series ser : series){
			while (ser.pairs.size() > 0){
				ser.pairs.remove(0);
			}
			ser.pairs = new ArrayList<Pair>(0);
		}
		
		return ret;
		
	}

}
