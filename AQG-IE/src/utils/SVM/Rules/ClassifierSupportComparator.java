package utils.SVM.Rules;

import java.util.Comparator;

public class ClassifierSupportComparator implements Comparator<SVMRule> {

	@Override
	public int compare(SVMRule o1, SVMRule o2) {
		
		return Double.compare(o2.getSupport(), o1.getSupport());
	}

}
