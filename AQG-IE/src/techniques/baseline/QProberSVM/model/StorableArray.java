package techniques.baseline.QProberSVM.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import utils.SVM.Rules.Candidateable;

public class StorableArray<T extends Candidateable> implements Iterable<T>{

//	public static final int MAX_ALLOWED_TO_KEEP_IN_MEMORY = 10000;
	private int size;
	private ArrayList<Candidateable> data;
	private int round;
	private String fileName;
	private Transformer<Candidateable> transformer;
	private HashSet<Candidateable> remove;
	private BufferedReader br;
	private String[] opened;
	private int indexopened;
	private boolean justStored;
	private boolean stored;
	private int MAX_ALLOWED_TO_KEEP_IN_MEMORY;
		
	public StorableArray(String fileName, Transformer<Candidateable> transformer, int size) {
	
		MAX_ALLOWED_TO_KEEP_IN_MEMORY = size;
		
		stored = false;
		
		indexopened = -1;
		
		opened = new String[MAX_ALLOWED_TO_KEEP_IN_MEMORY];
		
		this.transformer = transformer;
		
		round = 0;
		
		size = 0;
		
		data = new ArrayList<Candidateable>(MAX_ALLOWED_TO_KEEP_IN_MEMORY);
		
		this.fileName = fileName;
		
		new File(generateName(fileName,round)).delete();
		
		justStored = false;
	}

	public static String generateName(String fileName, int value) {
		
		return fileName + "-" + value;
		
	}

	public void add(Candidateable element) {
		
		if (justStored){
			round++;
			new File(generateName(fileName,round)).delete();
			justStored = false;
		}
		
		size++;
		
		data.add(element);
		
		if (data.size() == MAX_ALLOWED_TO_KEEP_IN_MEMORY){
			try {
				storeInDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		
	}

	private void storeInDisk() throws IOException {
		
		if ((round == 0 && data.size()<MAX_ALLOWED_TO_KEEP_IN_MEMORY) || data.size() == 0)
			return;
		
		stored = true;
		
		File f = new File(generateName(fileName,round));
		
		f.deleteOnExit();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(f/*, true*/));
		
		for (Candidateable element : data) {
			
			bw.write(element.toDisk() + "\n");
			
		}
		
		bw.close();
		
		data.clear();
			
		justStored = true;

	}

	@Override
	public Iterator<T> iterator() {
		
		if (!stored){
			return (Iterator<T>)data.iterator();
		}
		
		try {
			storeInDisk();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (remove==null)
			return new IterateFromFileIterator<T>(fileName,round,transformer);
		return new IterateFromFileIterator<T>(fileName,round, transformer,remove);
	
	}

	public int size() {
		
		return size;
	
	}

	public void cleanCurrentRound() {
		
		if (stored)
			new File(generateName(fileName,round)).delete();
		
	}

	public void remove(Candidateable chosenSet) {
		
		size--;
		
		getRemove().add(chosenSet);
			
	}

	private HashSet<Candidateable> getRemove() {
		
		if (remove==null)
			remove = new HashSet<Candidateable>();
		return remove;
	}

	public Candidateable get(int integer) {
		
		if (!stored){
			
			Candidateable aux = data.get(integer);
			
			if (remove!=null){
				
				if (remove.contains(aux))
					aux = null;
			}
			
			return aux;
		}
		
		int rr = integer / MAX_ALLOWED_TO_KEEP_IN_MEMORY;
		
		if (rr==indexopened){
			
			Candidateable aux = transformer.generateObject(opened[integer % MAX_ALLOWED_TO_KEEP_IN_MEMORY]);
			
			if (remove!=null){
				
				if (remove.contains(aux))
					aux = null;
			}
			return aux;
		}
		try {
			
//			System.out.println("Loading...");
			
			if (rr == round  && !justStored){
				
				System.out.println("GUAT!?");
				
				Candidateable aux = data.get(integer % MAX_ALLOWED_TO_KEEP_IN_MEMORY);
				
				if (remove!=null){
					
					if (remove.contains(aux))
						aux = null;
				}
				
				return aux;
				
			}
			
			indexopened = rr;
			
			br = new BufferedReader(new FileReader(generateName(fileName,rr)));
		
			String line = br.readLine();
			
			for (int i = 0; i < MAX_ALLOWED_TO_KEEP_IN_MEMORY && line!=null; i++) {
				opened[i] = line;
				line=br.readLine();
			}
		
			br.close();
			
			Candidateable aux = transformer.generateObject(opened[integer % MAX_ALLOWED_TO_KEEP_IN_MEMORY]);
			
			if (remove!=null){
			
				if (remove.contains(aux))
					aux = null;

			}
			
			return aux;
						
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	public void clear() {
		
		if (stored){
			for (int i = 0; i <= round; i++) {
				new File(generateName(fileName,i)).delete();
			}
		}
		
		justStored = false;
		round = 0;
		indexopened = -1;
		opened = new String[MAX_ALLOWED_TO_KEEP_IN_MEMORY];
		data.clear();
		size = 0;
		getRemove().clear();
	}

	public void finish() {
		try {
			storeInDisk();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void cleanRemoved() {
		getRemove().clear();
		
	}

}
