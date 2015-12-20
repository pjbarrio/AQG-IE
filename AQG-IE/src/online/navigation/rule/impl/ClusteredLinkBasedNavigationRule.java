package online.navigation.rule.impl;

import java.util.List;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.nodes.TagNode;

import online.navigation.rule.NavigationRule;
import online.navigation.textTransformer.TextTransformer;
import online.navigation.utils.NavigationUtils;
import searcher.interaction.Interaction;
import searcher.interaction.factory.InteractionFactory;
import utils.execution.TextRetriever;

public class ClusteredLinkBasedNavigationRule implements NavigationRule {

	private Map<String, List<String>> textMap;
	private Map<String, Map<String, Integer>> hypFreq;
	private Map<String, Map<String, Integer>> textFreq;
	private Map<String, List<String>> hypMap;
	private TextTransformer tt;
	private boolean containsEquals;
	private int total;
	public boolean isAllNumbers;
	private String lctext;
	private String text;
	public Integer number;

	public ClusteredLinkBasedNavigationRule(Map<String, List<String>> textMap,
			Map<String, List<String>> hypMap, boolean containsEquals, TextTransformer tt, int total) {
		
		textFreq = NavigationUtils.generateTextFreqStructure(textMap);

		hypFreq = NavigationUtils.generateTextFreqStructure(hypMap);

		this.textMap = textMap;
		
		this.hypMap = hypMap;
		
		this.tt = tt;
		
		this.containsEquals = containsEquals;
		
		this.total = total;
	}

	@Override
	public boolean isNavigable(TagNode node, String qur, String encoding) {
		
		String dqur = InteractionFactory.decode(qur, encoding);
		
		isAllNumbers = false;
		
		text = TextRetriever.recoverText(node,true,tt.personalize(qur));

		lctext = text.toLowerCase();
		
		String href = NavigationUtils.getAttribute(node ,NavigationUtils.HREF_ATTRIBUTE);

		String onclick = NavigationUtils.getAttribute(node ,NavigationUtils.ONCLICK_ATTRIBUTE);

		if (onclick == null){
			
			String lhref = href.toLowerCase();
			
			List<String> toCompare = NavigationUtils.generateWords(href.toLowerCase());
//			if (!href.startsWith("javascript:") && ((!toCompare.contains(qur) || !href.toLowerCase().contains(dqur)) || 
//					(containsEquals && (!href.toLowerCase().contains("="+qur) && !href.toLowerCase().contains("="+dqur)))))
//				return false;
//			
			if (!lhref.startsWith(NavigationUtils.JAVASCRIPT_PREFIX)){
				
				String pr = "";
				
				if (containsEquals){
					
					pr = "=";
					
				}
				
				boolean contained = false;
				
				for (int i = 0; i < toCompare.size() && !contained; i++) {
					
					if (!toCompare.get(i).contains(pr+qur) && !toCompare.get(i).contains(pr+dqur))
						contained = true;
					
				}

				if (!contained)
					return false;
				
			}
			
		}		
		
		String combined = NavigationUtils.combine(href, onclick);

		boolean javascript = NavigationUtils.isJavaScript(node);

		if (javascript){

			String te = TextRetriever.recoverText(node).trim();
			
			isAllNumbers = NavigationUtils.isAllNumbers(te.toCharArray());
			
			if (isAllNumbers){
				number = Integer.valueOf(te);
			}
			
			boolean samefreqInAll = NavigationUtils.sameFrequencyInAll(textFreq.get(text)); 

			boolean isbelow = NavigationUtils.isAlwaysBelowFrequency(hypFreq.get(combined),4);

			boolean istextbelow = NavigationUtils.isAlwaysBelowFrequency(textFreq.get(text), 4);

			boolean next = text.toLowerCase().contains("next");

			boolean page = false;

			if (isbelow && istextbelow && samefreqInAll && (next || page || isAllNumbers)){

				return true;

			}

		} else {

			int freqHyp = NavigationUtils.getFrequency(hypMap.get(combined));

			if (freqHyp <= 1){

				String te = TextRetriever.recoverText(node).trim();
								
				isAllNumbers = NavigationUtils.isAllNumbers(te.toCharArray());
				
				if (isAllNumbers){
					number = Integer.valueOf(te);
				}
				
				int freqText = NavigationUtils.getFrequency(textMap.get(text));

				if (freqText > 1){

					boolean samefreqTextInDoc = NavigationUtils.isAlwaysBelowFrequency(textFreq.get(text),4);

					boolean inAll = NavigationUtils.isInAll(textFreq.get(text), total);

					if (samefreqTextInDoc && !inAll){

						return true;

					}
				}
			}
		}
		
		return false;
		
	}

	public boolean contains(String string) {
		return lctext.contains(string);
	}

	public String getText(){
		return text;
	}
	
	public int getFrequency(String text){
		return textFreq.get(text).size();
	}
	
}
