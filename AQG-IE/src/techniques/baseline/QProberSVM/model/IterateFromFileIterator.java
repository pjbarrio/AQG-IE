package techniques.baseline.QProberSVM.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import utils.SVM.Rules.Candidateable;


public class IterateFromFileIterator<T extends Candidateable> implements Iterator<T> {

	private BufferedReader br;

	ArrayList<T> objects;

	private int actual;

	private Transformer<Candidateable> transformer;

	private HashSet<Candidateable> remove;

	private int finalRound;

	private int round;

	private String fileName;

	private Candidateable generateObject;
	
	public IterateFromFileIterator(String fileName, int round, Transformer<Candidateable> transformer2) {
		
		this(fileName, round, transformer2, new HashSet<Candidateable>());
		
	}

	public IterateFromFileIterator(String fileName,
			int round, Transformer<Candidateable> transformer2,
			HashSet<Candidateable> remove) {
		try {
			
			this.fileName = fileName;
			
			this.transformer = transformer2;
			
			objects = new ArrayList<T>();
			
			this.remove = remove;
			
			finalRound = round;
			
			this.round = 0;
			
			loadLines();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}

	private void loadLines() throws IOException {
		
		br = new BufferedReader(new FileReader(new File(StorableArray.generateName(fileName, round))));
		
		actual = 0;
		
		objects.clear();
		
		String line = br.readLine();
		
		while (line != null){
			
			generateObject = transformer.generateObject(line);
			
			if (!remove.contains(generateObject)){
				objects.add((T) generateObject);
			}
			
			line = br.readLine();
		
		}
		
		br.close();
		
		round++;
	}

	@Override
	public boolean hasNext() {
		
		if (actual < objects.size())
			return true;
		
		if (round <= finalRound)
			return true;
			
		objects.clear();
		
		return false;
		
	}

	@Override
	public T next() {
		
		T act = objects.get(actual);
		
		actual++;
		if (actual==objects.size() && round <= finalRound)
			try {
				loadLines();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return act;
			
	}

	@Override
	public void remove() {
		
		//TODO see if it's worth it
		
		;
		
	}

}
