package plot.generator;

import java.util.List;

import plot.data.ChartData;
import plot.selector.SampleGenerationSelector;
import plot.selector.Selector;

public class ChartGenerator<T extends Selector> {

	public ChartData generateChart(String outputName,SeriesGenerator<T> seriesGenerator, List<T> selectors, String selection){
		
		ChartData ret = new ChartData(outputName, seriesGenerator.getNormalizedYAttribute() != SeriesGenerator.NO_NORMALIZATION);
		
		ret.setTitle(seriesGenerator.getExtendedTitle(selection.replaceAll("_", " ")));
		
		ret.setAxisXTitle(seriesGenerator.getAxisXTitle());
		
		ret.setAxisYTitle(seriesGenerator.getAxisYTitle());
		
		for (T selector : selectors) {
			
			ret.addSeries(seriesGenerator.generateSeries(selector));
			
		}
		
		return ret;
	
	}

//	public ChartData generateChart(String outputName,
//			SeriesGenerator<T> series,
//			T selector, String selection) {
//		
//		ChartData ret = new ChartData(outputName, series.getNormalizedYAttribute() != SeriesGenerator.NO_NORMALIZATION);
//		
//		ret.setTitle(series.getExtendedTitle(selection.replaceAll("_", " ")));
//		
//		ret.setAxisXTitle(series.getAxisXTitle());
//		
//		ret.setAxisYTitle(series.getAxisYTitle());
//				
//		for (SeriesGenerator<T> seriesGenerator : series.generateByParameterSeries()) {
//			
//			ret.addSeries(seriesGenerator.generateSeries(selector));
//			
//		}
//		
//		return ret;
//		
//	}

}
