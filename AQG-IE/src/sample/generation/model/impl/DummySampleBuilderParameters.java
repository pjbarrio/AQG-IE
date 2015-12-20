package sample.generation.model.impl;

import sample.generation.model.SampleBuilderParameters;
import utils.word.extraction.WordExtractorAbs;

public class DummySampleBuilderParameters extends SampleBuilderParameters {

	public DummySampleBuilderParameters(int sampleBuilderId, int noF,
			double minF, double maxF, boolean unique, boolean lowercase,
			boolean stemmed, boolean tasw,
			WordExtractorAbs generalWordExtractor,
			WordExtractorAbs usefulWordExtractor, int parameterId, int usefulDocuments, int uselessDocuments) {
		super(sampleBuilderId, noF, minF, maxF, unique, lowercase, stemmed, tasw,
				generalWordExtractor, usefulWordExtractor, parameterId, usefulDocuments, uselessDocuments);
	}

	public DummySampleBuilderParameters(int sampleBuilderId) {
		
		super(sampleBuilderId,0,0,0,false,false,false,false,null,null,0,0,0);
		
	}

}
