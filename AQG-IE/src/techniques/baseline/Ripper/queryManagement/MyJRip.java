package techniques.baseline.Ripper.queryManagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.google.gdata.util.common.base.Pair;

import searcher.interaction.formHandler.TextQuery;
import utils.persistence.persistentWriter;
import weka.classifiers.rules.JRip;
import weka.core.Attribute;

public class MyJRip extends JRip {

	/**
	 * 
	 */
	private static final long serialVersionUID = 336224775054312439L;

	private static RipperRuleQueryGenerator rrqg = new RipperRuleQueryGenerator();
	
	public void writeQueries(int combinationId, persistentWriter pW, Attribute att, long measuredTime) throws IOException {
		
		for(Enumeration<JRip.RipperRule> e = getRuleset().elements();e.hasMoreElements();){
			
			JRip.RipperRule rule = e.nextElement();

			System.out.println(rule.toString(att));
			
//			if (rule.hasAntds())
//				pW.writeQuery(combinationId, rrqg.generateQuery(rule.toString(att)),measuredTime);
			
		}
		
	}

	public List<Pair<TextQuery,Long>> getQueries(persistentWriter pW, Attribute att, long measuredTime) throws IOException {
		
		List<Pair<TextQuery,Long>> ret = new ArrayList<Pair<TextQuery,Long>>();
		
		for(Enumeration<JRip.RipperRule> e = getRuleset().elements();e.hasMoreElements();){
			
			JRip.RipperRule rule = e.nextElement();

			System.out.println(rule.toString(att));
			
			if (rule.hasAntds())
				ret.add(new Pair<TextQuery,Long>(rrqg.generateQuery(rule.toString(att)),measuredTime));
			
		}
		
		return ret;
	
	}
	
}
