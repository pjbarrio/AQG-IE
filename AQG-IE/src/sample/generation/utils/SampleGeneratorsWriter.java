package sample.generation.utils;

import utils.persistence.databaseWriter;

public class SampleGeneratorsWriter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		databaseWriter dW = new databaseWriter("");
		
		int i = 2;
		
		int sc = 452;
		
		for (int s = 92; s <= 451; s++) {
			
			String s1 = "insert into `AutomaticQueryGeneration`.`SampleGenerator` (`idSampleGenerator`, `idParameter`, `idSampleConfiguration`) values ("+ i +",353782," + s + ");";
			
			dW.performStatement(s1);
			
			s1 = "INSERT INTO `AutomaticQueryGeneration`.`SampleConfiguration` (`IdSampleConfiguration`, `idParameter`, `idVersion`, `idWorkload`, " +
					"`idRelationConfiguration`, `idExtractionSystem`, `idQueryPoolExecutor`, `idSampleGenerator`, `composite`, `active`," +
					" `resultsPerQuery`, `usefulNumber`, `uselessNumber`, `maxQueries`, `maxDocuments`, `CountsAll`, `baseCollection`, " +
					"`docsInTraining`) VALUES ( "+ sc + ", -1, -1, -1, -1, <{idExtractionSystem: }>, <{idQueryPoolExecutor: }>, <{idSampleGenerator: }>, <{composite: 0}>, <{active: }>, <{resultsPerQuery: }>, <{usefulNumber: }>, <{uselessNumber: }>, <{maxQueries: }>, <{maxDocuments: }>, <{CountsAll: 0}>, <{baseCollection: TREC}>, <{docsInTraining: 5000}> );";
			
			dW.performStatement(s1);
			
			sc++;
			i++;
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleGenerator` (`idSampleGenerator`, `idParameter`, `idSampleConfiguration`) values ("+ i +",353783," + s + ");";
			
			dW.performStatement(s1);
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleConfiguration` (`IdSampleConfiguration`, `idParameter`, `idVersion`, `idWorkload`, `idExtractionSystem`, `idQueryPoolExecutor`, " +
					"`idSampleGenerator`, `active`, `resultsPerQuery`, `usefulNumber`, `uselessNumber`, `maxQueries`, `maxDocuments`, `numberOfFeatures`, `MinFrequency`, `MaxFrequency`, `CountsAll`) " +
					"values ("+sc+", "+ getParameter(s) +", '1', '6', '1'," + getQueryPoolExecutor(s) + ", "+i+", 0, "+getLimit(s)+", '400', '0', '400', '4000', '700', '0.003', '0.9', "+getCountAll(s)+");";
			
			dW.performStatement(s1);
			
			sc++;
			i++;
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleGenerator` (`idSampleGenerator`, `idParameter`, `idSampleConfiguration`) values ("+ i +",353784," + s + ");";
			
			dW.performStatement(s1);
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleConfiguration` (`IdSampleConfiguration`, `idParameter`, `idVersion`, `idWorkload`, `idExtractionSystem`, `idQueryPoolExecutor`, " +
					"`idSampleGenerator`, `active`, `resultsPerQuery`, `usefulNumber`, `uselessNumber`, `maxQueries`, `maxDocuments`, `numberOfFeatures`, `MinFrequency`, `MaxFrequency`, `CountsAll`) " +
					"values ("+sc+", "+ getParameter(s) +", '1', '6', '1'," + getQueryPoolExecutor(s) + ", "+i+", 0, "+getLimit(s)+", '400', '0', '400', '4000', '700', '0.003', '0.9', "+getCountAll(s)+");";
			
			dW.performStatement(s1);
			
			sc++;
			i++;
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleGenerator` (`idSampleGenerator`, `idParameter`, `idSampleConfiguration`) values ("+ i +",353785," + s + ");";
			
			dW.performStatement(s1);
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleConfiguration` (`IdSampleConfiguration`, `idParameter`, `idVersion`, `idWorkload`, `idExtractionSystem`, `idQueryPoolExecutor`, " +
					"`idSampleGenerator`, `active`, `resultsPerQuery`, `usefulNumber`, `uselessNumber`, `maxQueries`, `maxDocuments`, `numberOfFeatures`, `MinFrequency`, `MaxFrequency`, `CountsAll`) " +
					"values ("+sc+", "+ getParameter(s) +", '1', '6', '1', " + getQueryPoolExecutor(s) + ", "+i+", 0, "+getLimit(s)+", '400', '0', '400', '4000', '700', '0.003', '0.9', "+getCountAll(s)+");";
			
			dW.performStatement(s1);
			
			sc++;
			i++;
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleGenerator` (`idSampleGenerator`, `idParameter`, `idSampleConfiguration`) values ("+ i +",353786," + s + ");";
			
			dW.performStatement(s1);
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleConfiguration` (`IdSampleConfiguration`, `idParameter`, `idVersion`, `idWorkload`, `idExtractionSystem`, `idQueryPoolExecutor`, " +
					"`idSampleGenerator`, `active`, `resultsPerQuery`, `usefulNumber`, `uselessNumber`, `maxQueries`, `maxDocuments`, `numberOfFeatures`, `MinFrequency`, `MaxFrequency`, `CountsAll`) " +
					"values ("+sc+", "+ getParameter(s) +", '1', '6', '1', " + getQueryPoolExecutor(s) + ", "+i+", "+getActive(s)+", "+getLimit(s)+", '400', '0', '400', '4000', '700', '0.003', '0.9', "+getCountAll(s)+");";
			
			dW.performStatement(s1);
			
			sc++;
			i++;
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleGenerator` (`idSampleGenerator`, `idParameter`, `idSampleConfiguration`) values ("+ i +",353787," + s + ");";
			
			dW.performStatement(s1);
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleConfiguration` (`IdSampleConfiguration`, `idParameter`, `idVersion`, `idWorkload`, `idExtractionSystem`, `idQueryPoolExecutor`, " +
					"`idSampleGenerator`, `active`, `resultsPerQuery`, `usefulNumber`, `uselessNumber`, `maxQueries`, `maxDocuments`, `numberOfFeatures`, `MinFrequency`, `MaxFrequency`, `CountsAll`) " +
					"values ("+sc+", "+ getParameter(s) +", '1', '6', '1', " + getQueryPoolExecutor(s) + ", "+i+", "+getActive(s)+", "+getLimit(s)+", '400', '0', '400', '4000', '700', '0.003', '0.9', "+getCountAll(s)+");";
			
			dW.performStatement(s1);
			
			sc++;
			i++;
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleGenerator` (`idSampleGenerator`, `idParameter`, `idSampleConfiguration`) values ("+ i +",353788," + s + ");";
			
			dW.performStatement(s1);
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleConfiguration` (`IdSampleConfiguration`, `idParameter`, `idVersion`, `idWorkload`, `idExtractionSystem`, `idQueryPoolExecutor`, " +
					"`idSampleGenerator`, `active`, `resultsPerQuery`, `usefulNumber`, `uselessNumber`, `maxQueries`, `maxDocuments`, `numberOfFeatures`, `MinFrequency`, `MaxFrequency`, `CountsAll`) " +
					"values ("+sc+", "+ getParameter(s) +", '1', '6', '1', " + getQueryPoolExecutor(s) + ", "+i+", "+getActive(s)+", "+getLimit(s)+", '400', '0', '400', '4000', '700', '0.003', '0.9', "+getCountAll(s)+");";
			
			dW.performStatement(s1);
			
			sc++;
			i++;
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleGenerator` (`idSampleGenerator`, `idParameter`, `idSampleConfiguration`) values ("+ i +",353789," + s + ");";

			dW.performStatement(s1);
			
			s1 = "insert into `AutomaticQueryGeneration`.`SampleConfiguration` (`IdSampleConfiguration`, `idParameter`, `idVersion`, `idWorkload`, `idExtractionSystem`, `idQueryPoolExecutor`, " +
					"`idSampleGenerator`, `active`, `resultsPerQuery`, `usefulNumber`, `uselessNumber`, `maxQueries`, `maxDocuments`, `numberOfFeatures`, `MinFrequency`, `MaxFrequency`, `CountsAll`) " +
					"values ("+sc+", "+ getParameter(s) +", '1', '6', '1', " + getQueryPoolExecutor(s) + ", "+i+", "+getActive(s)+", "+getLimit(s)+", '400', '0', '400', '4000', '700', '0.003', '0.9', "+getCountAll(s)+");";
		
			dW.performStatement(s1);
			
			sc++;
			i++;
			
		}

	}

	private static int getQueryPoolExecutor(int s) {
		
		int val =  (((s-92) / 30) + 1) % 6;
		
		if (val == 0)
			return 6;
		
		return val;
	}

	private static int getActive(int s) {
		
		if (((s - 92) % 10) == 4){
			return 1;
		}
		
		return 0;
	
	}

	private static int getParameter(int s) {
		
		return ((s - 92) % 10) + 353756;
				
	}

	private static int getLimit(int s) {
		
		int val = ((s - 92) / 10);
		
		switch (val) {
		case 0:
			
			return 10;

		case 1:
			return 20;
			
		default:
			return 40;
		}
		
	}

	private static int getCountAll(int s) {
		
		if (s >= 272)
			return 1;
		return 0;
	}

}
