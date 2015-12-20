package techniques.baseline.QProberSVM.model;


import java.util.Comparator;
import java.util.List;

public interface IterateValidator<T> {

	public boolean validate(T candidate);
	
	public Comparator<T> getComparator(List<T> list);

	public void clean();
	
}
