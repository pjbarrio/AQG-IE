package extraction.net.extractors;


import edu.stanford.nlp.ie.crf.CRFClassifier;

import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Triple;

import etxt2db.api.ClassificationExecutor;
import etxt2db.api.ClassificationExecutor.ClassifiedSpan;

import java.util.*;



// Referenced classes of package etxt2db.extractors:

// EntityExtractor



public class StanfordNLPBasedExtractor extends EntityExtractor

{

	private CRFClassifier<CoreMap> classifier;

	private ClassificationExecutor exec;


	public StanfordNLPBasedExtractor(int id, Map<String,Integer> tagsId, List<String> tags, CRFClassifier<CoreMap> crfClassifier)

	{

		super(id, tagsId, tags);

		classifier = crfClassifier;

		exec = new ClassificationExecutor();

	}



	public synchronized Map<String,List<ClassifiedSpan>> getClassifiedSpans(String content)

	{

		System.out.println(content.length());
		
		Map<String,List<ClassifiedSpan>> map = new HashMap<String, List<ClassifiedSpan>>();

		String tag;

		for(Iterator<String> iterator = getTags().iterator(); iterator.hasNext(); map.put(tag, new ArrayList<ClassificationExecutor.ClassifiedSpan>()))

			tag = (String)iterator.next();

		List<Triple<String, Integer, Integer>>list = classifier.classifyToCharacterOffsets(content);

		for(int i = 0; i < list.size(); i++)

			if(getTags().contains(list.get(i).first))

				map.get(list.get(i).first).add(exec.new ClassifiedSpan(list.get(i).second, list.get(i).third, list.get(i).first));



		return map;

	}




}
