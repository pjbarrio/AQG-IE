package domain.caching.candidatesentence.tool;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gdata.util.common.base.Pair;

import edu.columbia.cs.ref.algorithm.CandidatesGenerator;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.Document;
import edu.columbia.cs.ref.model.relationship.RelationshipType;
import edu.columbia.cs.ref.tool.document.splitter.impl.OpenNLPMESplitter;
import edu.columbia.cs.ref.tool.loader.document.impl.RawDocumentLoader;
import edu.columbia.cs.ref.tool.preprocessor.Preprocessor;
import edu.columbia.cs.ref.tool.preprocessor.impl.DoNothingPreprocessor;
import edu.columbia.cs.ref.tool.segmentator.DocumentSegmentator;
import edu.columbia.cs.ref.tool.segmentator.impl.SimpleSegmentDocumentSegmentator;
import edu.columbia.cs.ref.tool.tagger.entity.impl.MapBasedEntityTagger;

import utils.persistence.persistentWriter;

public class CandidateSentenceGenerator {

	private persistentWriter pW;
	private int relationConfigurationId;
	private CandidatesGenerator generator;
	private Set<RelationshipType> relationshipTypes;
	private Preprocessor preprocessor;
	private DocumentSegmentator segmentator;
	private Set<String> tags;

	public CandidateSentenceGenerator(persistentWriter pW, int relationConfigurationId, Set<RelationshipType> relationshipTypes, Set<String> tags) throws FileNotFoundException {
		this.pW = pW;
		this.relationConfigurationId = relationConfigurationId;
		this.relationshipTypes = relationshipTypes;
		
		OpenNLPMESplitter splitter = new OpenNLPMESplitter("en-sent.bin");
		
		generator = new CandidatesGenerator(splitter);
		
		preprocessor = new DoNothingPreprocessor();
		
		segmentator = new SimpleSegmentDocumentSegmentator();
		
		this.tags = tags;
		
	}

	public int getRelationConfigurationId() {
		return relationConfigurationId;
	}

	public Set<CandidateSentence> generateCandidateSentences(String content,
			Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>> entitiesMap, Map<Integer,String> entityTable) {
		
		if (entitiesMap.size() <= 1){
			return new HashSet<CandidateSentence>(0);
		}
		
		Document doc = new RawDocumentLoader(relationshipTypes, preprocessor, segmentator, new MapBasedEntityTagger(tags,entitiesMap,entityTable)).load(content);
		
		return generator.generateCandidates(doc, relationshipTypes);
		
	}

}
