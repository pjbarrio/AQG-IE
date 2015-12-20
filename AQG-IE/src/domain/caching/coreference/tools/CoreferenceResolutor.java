package domain.caching.coreference.tools;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.columbia.cs.ref.model.Document;
import edu.columbia.cs.ref.model.entity.CorefEntity;
import edu.columbia.cs.ref.model.relationship.RelationshipType;
import edu.columbia.cs.ref.tool.loader.document.impl.RawDocumentLoader;
import edu.columbia.cs.ref.tool.preprocessor.Preprocessor;
import edu.columbia.cs.ref.tool.preprocessor.impl.DoNothingPreprocessor;
import edu.columbia.cs.ref.tool.segmentator.DocumentSegmentator;
import edu.columbia.cs.ref.tool.segmentator.impl.SimpleSegmentDocumentSegmentator;
import edu.columbia.cs.ref.tool.tagger.entity.coref.CorefEntityTagger;
import edu.columbia.cs.ref.tool.tagger.entity.impl.MapBasedEntityTagger;
import edu.columbia.cs.ref.tool.tagger.entity.impl.StanfordNLPCoreference;
import edu.columbia.cs.ref.tool.tagger.span.impl.CorefEntitySpan;
import edu.columbia.cs.utils.Pair;

import utils.persistence.persistentWriter;

public class CoreferenceResolutor {

	private CorefEntityTagger cr;
	private persistentWriter pW;
	private Preprocessor preprocessor;
	private DocumentSegmentator segmentator;
	private Set<String> tags;

	public CoreferenceResolutor(Set<String> tags, persistentWriter pW, CorefEntityTagger cet) {
		this.cr = cet;
		
		this.tags = tags;
		
		this.pW = pW;
		
		preprocessor = new DoNothingPreprocessor();
		
		segmentator = new SimpleSegmentDocumentSegmentator();
		
	}

	public List<CorefEntitySpan> generateCoreferenceResolutions(String content,
			Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> entities,
			Map<Integer, String> entitiesTable) {
		
		Document doc = new RawDocumentLoader(new HashSet<RelationshipType>(0), preprocessor, segmentator, new MapBasedEntityTagger(tags,entities,entitiesTable)).load(content); //does not need relationshipType
		
		return cr.getCorefSpans(doc);
		
	}

}
