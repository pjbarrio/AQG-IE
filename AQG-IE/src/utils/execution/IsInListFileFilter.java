package utils.execution;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class IsInListFileFilter implements FileFilter {

	private List<String> toProcess;

	public IsInListFileFilter(List<String> toProcess) {
		this.toProcess = toProcess;
	}

	@Override
	public boolean accept(File arg0) {
		return toProcess.contains(arg0.getName());
	}

}
