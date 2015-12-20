package utils.SVM.Rules;

import java.util.Comparator;

public class SVMRuleComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		
		Double supp1 = Double.parseDouble(o1.split(" ")[1]);
		Double supp2 = Double.parseDouble(o2.split(" ")[1]);
		
		return supp2.compareTo(supp1);
	}

}


