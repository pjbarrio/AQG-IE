package execution.model.factory;

import execution.model.collector.StatisticsForSampleSelector;
import execution.model.collector.selector.GlobalStatisticsForSample;
import execution.model.collector.selector.LocalStatisticsForSample;
import execution.model.collector.selector.SmartStatisticsForSample;
import exploration.model.enumerations.StatisticsForSampleEnum;

public class StatisticsForSampleSelectorFactory {

	public static StatisticsForSampleSelector generateInstance(String string) {
		
		switch (StatisticsForSampleEnum.valueOf(string)) {
		
		case LOCAL:
			
			return new LocalStatisticsForSample();

		case GLOBAL:
			
			return new GlobalStatisticsForSample();
			
		case SMART:
		
			return new SmartStatisticsForSample();
			
		default:
			
			return null;
			
		}
		
	}
	
}
