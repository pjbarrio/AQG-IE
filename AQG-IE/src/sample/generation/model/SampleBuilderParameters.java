package sample.generation.model;

import utils.word.extraction.WordExtractorAbs;

public class SampleBuilderParameters {

	private int sampleBuilderId;
	private int noF;
	private double minF;
	private double maxF;
	private boolean unique;
	private boolean lowercase;
	private boolean stemmed;
	private boolean tasw;
	private WordExtractorAbs generalWordExtractor;
	private WordExtractorAbs usefulWordExtractor;
	private int parameterId;
	private int usefulDocuments;
	private int uselessDocuments;

	public SampleBuilderParameters(int sampleBuilderId, int noF, double minF,
			double maxF, boolean unique, boolean lowercase, boolean stemmed,
			boolean tasw, WordExtractorAbs generalWordExtractor,
			WordExtractorAbs usefulWordExtractor, int parameterId, int usefulDocuments, int uselessDocuments) {
		
		this.sampleBuilderId = sampleBuilderId;
		this.noF = noF;
		this.minF = minF;
		this.maxF = maxF;
		this.unique = unique;
		this.lowercase = lowercase;
		this.stemmed = stemmed;
		this.tasw = tasw;
		this.generalWordExtractor = generalWordExtractor;
		this.usefulWordExtractor = usefulWordExtractor;
		this.parameterId = parameterId;
		this.usefulDocuments = usefulDocuments;
		this.uselessDocuments = uselessDocuments;
	}

	public WordExtractorAbs getGeneralWordExtractor() {
		return generalWordExtractor;
	}

	public WordExtractorAbs getUsefulWordExtractor() {
		return usefulWordExtractor;
	}

	public boolean getStemmed() {
		return stemmed;
	}

	public boolean getUnique() {
		return unique;
	}

	public double getMaxFrequency() {
		return maxF;
	}

	public double getMinFrequency() {
		return minF;
	}

	public int getFeatures() {
		return noF;
	}

	public boolean getTuplesAsStopWords() {
		return tasw;
	}

	public int getId() {
		return sampleBuilderId;
	}

	public boolean getLowerCase() {
		return lowercase;
	}

	public int getParameter() {
		return parameterId;
	}

	public int getUsefulDocuments() {
		return usefulDocuments;
	}

	public int getUselessDocuments() {
		return uselessDocuments;
	}

}
