package sample.generation.combination.impl;

import java.util.List;

import exploration.model.Database;
import exploration.model.enumerations.ClusterFunctionEnum;
import sample.generation.combination.CombinationGenerator;
import utils.persistence.databaseWriter;

public class GlobalCombination extends CombinationGenerator {

	private int split;

	public GlobalCombination(databaseWriter pW, int split) {
		super(pW);
		this.split = split;
	}

	@Override
	public List<Database> getCombination(int i) {
		
//		return new String[]{"Bloomberg","TheCelebrityCafe","TheEconomist","UsNews","Variety"};
		
//		return new String[]{"28","217","231","321","452","739","790","1174","1367","1387","1482","1769","2086","2098","2167","2175","2694","2746"};
	
//		return new String[]{"http://www.mauconline.net/","http://www.carmeuse.com","http://diversifiedproduct.com/","http://joehollywood.com/",
//				"http://travel.state.gov/","http://northeasteden.blogspot.com/","http://www.muffslap.com/","http://www.paljorpublications.com/",
//				"http://www.biostat.washington.edu/","http://www.brannan.co.uk/","http://www.improv.ca/","http://www.avclub.com/",
//				"http://www.shopcell.com/","http://keep-racing.de","http://www.worldenergy.org","http://www.infoaxon.com/","http://www.codecranker.com/",
//				"http://www.canf.org/","http://www.thecampussocialite.com/","http://micro.magnet.fsu.edu/","http://www.jamesandjames.com",
//				"http://www.pokkadots.com/","http://www.time.com/"};
		
		return pW.getSamplableDatabases(split);
		
	}

	@Override
	public int numberOfCombinations() {
		return 1;
	}

	@Override
	public String getDatabaseName(int i) {
		
		return "GlobalSample";
	
	}

	@Override
	public String getSampleType(int i) {
		
		return "Mixed";
	}

	@Override
	public int isGlobal() {
		return 1;
	}

	@Override
	public int isCluster() {
		return 0;
	}

	@Override
	public ClusterFunctionEnum getClusteredFunction() {
		return ClusterFunctionEnum.GLOBAL;
	}
}
