package utils.execution;

import java.io.File;
import java.util.Comparator;

public class AbsoluteNumberNameComparator<T> implements Comparator<File> {

	@Override
	public int compare(File o1, File o2) {
		return Double.compare(Double.valueOf(o1.getName()), Double.valueOf(o2.getName()));
	}

}
