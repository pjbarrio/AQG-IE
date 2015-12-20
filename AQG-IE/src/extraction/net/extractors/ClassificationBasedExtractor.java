package extraction.net.extractors;



import etxt2db.api.ClassificationExecutor;

import etxt2db.api.ClassificationModel;
import etxt2db.api.ClassificationExecutor.ClassifiedSpan;

import java.io.IOException;

import java.text.ParseException;

import java.util.*;
import java.util.Map.Entry;



// Referenced classes of package etxt2db.extractors:

// EntityExtractor



public class ClassificationBasedExtractor extends EntityExtractor

{

	private ClassificationExecutor exec;

	private ClassificationModel model;

	public ClassificationBasedExtractor(ClassificationExecutor exec, ClassificationModel model, int informationExtractionId, Map<String,Integer> tagsTable, List<String> tags)

	{

		super(informationExtractionId, tagsTable, tags);

		this.exec = exec;

		this.model = model;

	}



	public synchronized Map<String, List<ClassifiedSpan>> getClassifiedSpans(String content)

	{

		Map<String, List<ClassifiedSpan>> ret;

		ret = new HashMap<String, List<ClassifiedSpan>>();

		String tag;

		for(Iterator<String> iterator = getTags().iterator(); iterator.hasNext(); ret.put(tag, new ArrayList<ClassificationExecutor.ClassifiedSpan>()))

			tag = (String)iterator.next();



		Map<String, List<ClassifiedSpan>> outp;
		try {
			outp = exec.getClassifiedSpans(content, model, getTags());
			
			for(Iterator<Entry<String, List<ClassifiedSpan>>> iterator1 = outp.entrySet().iterator(); iterator1.hasNext();)

			{

				Entry<String, List<ClassifiedSpan>> entry = iterator1.next();

				for(int i = 0; i < ((List<ClassifiedSpan>)entry.getValue()).size(); i++)

					((List<ClassifiedSpan>)ret.get(((etxt2db.api.ClassificationExecutor.ClassifiedSpan)((List<ClassifiedSpan>)entry.getValue()).get(i)).getType())).add((etxt2db.api.ClassificationExecutor.ClassifiedSpan)((List<ClassifiedSpan>)entry.getValue()).get(i));



			}



			return ret;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return null;

	}


}
	