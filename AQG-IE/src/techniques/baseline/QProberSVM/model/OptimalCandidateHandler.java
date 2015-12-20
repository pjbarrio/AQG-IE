package techniques.baseline.QProberSVM.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import utils.SVM.Rules.Candidateable;
import utils.arff.myArffHandler;
import weka.core.Instances;

public class OptimalCandidateHandler<T extends Candidateable> implements Iterable<T> {

	private static final int GARBAGE_COLLECTION_ITEMS = 25000;
	private static final int MAX_SEQ_IN_MEM = 100000;
	private ArrayList<ArrayList<Integer>> f1Itemsets;
	private int K;
	private BitTable dataBasebitTable;
	private BitTable cKbitTable;
	private int min_supp;
	private FromIntToT converter;
	private ArrayList<T> ret;
	private Instances data;
	private int size;
	private IterateValidator<T> iterateValidator;
	private String CandidatesFile;
	private int sizeOfIterator;
	private String fileCK;
	private String fileDatabase;
	private int fileSize;
	private String fileNameForIterator;
	private Transformer<Candidateable> transformerforIterator;
	private ArrayList<Integer> vec;

	public OptimalCandidateHandler(int size,int min_supp,FromIntToT converter,Instances data, IterateValidator<T> validator, String CandidatesFile, int fileSize, Transformer<Candidateable> transIterator){
		this.fileSize = fileSize;
		f1Itemsets = new ArrayList<ArrayList<Integer>>(size);
		K = 0;
		this.min_supp = min_supp;
		this.converter = converter;
		cKbitTable = null;
		this.data = data;
		this.size = size;
		this.iterateValidator = validator;
		this.CandidatesFile = CandidatesFile;
		this.fileCK = CandidatesFile + "-CK";
		this.fileDatabase = CandidatesFile + "-DB";
		this.fileNameForIterator = CandidatesFile + "-IT";
		this.transformerforIterator = transIterator;
	}
	
	@Override
	public Iterator<T> iterator() {
		
		T aux;
		
		ret = new ArrayList<T>();
		
		
		
		if (cKbitTable == null){

			long size = f1Itemsets.size();
			
			for (int i = 0; i < size; i++) {
				
				System.out.println(i + " out OF " + size);
				
				aux = (T)converter.createObject(f1Itemsets.get(i));
				
				if (iterateValidator.validate(aux))
					ret.add(aux);
				
			}
					
		}else{
		
			long size = cKbitTable.size();
			
			int i = 0;
				
			for (BitIntSet bitintSet : cKbitTable){
				
				System.out.println(i++ + " out Of: " + size);
				
				vec = generateValues(bitintSet);
				
				aux = (T)converter.createObject(vec);
				
				vec.clear();				
				
				if (iterateValidator.validate(aux))
					ret.add(aux);
				
				if (i%GARBAGE_COLLECTION_ITEMS==0)
					Runtime.getRuntime().gc();
				
			}
		
		}
		
		sizeOfIterator = ret.size();
		
		Comparator<T> c = iterateValidator.getComparator(ret);
		
		if (c !=null)
			Collections.sort(ret, c);
				
		StorableArray<T> sto = new StorableArray<T>(fileNameForIterator, transformerforIterator, MAX_SEQ_IN_MEM);
		
		while (ret.size()>0){
			
			sto.add(ret.remove(0));
			
		}
		
		iterateValidator.clean();
		
		return sto.iterator();
	}
	
	private ArrayList<Integer> generateValues(BitIntSet bitSet) {
		
		ArrayList<Integer> a = new ArrayList<Integer>(bitSet.cardinality());
		
		for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i+1)) {
			
			a.add(i);
		
		}
		
		return a;
	
	}
	
	public void CleanIterator(){
		ret.clear();
	}

	public void addF1Itemset(Integer i){
		
		ArrayList<Integer> aux = new ArrayList<Integer>(1);
		
		aux.add(i);
		
		f1Itemsets.add(aux);
		
	}
	
	public void doneWithF1Itemset(){
		
		generateBitTable(data);
		
		K = 1;
		
	}
	
	private void generateBitTable(Instances data) {
		
		ArrayList<ArrayList<Integer>> entrybitTable= new ArrayList<ArrayList<Integer>>(f1Itemsets.size());
		
		for (ArrayList<Integer> feat : f1Itemsets) {
			
			ArrayList<Integer> aux = myArffHandler.getTPInstances(data, feat);
			
			entrybitTable.add(aux);
			
		}
		
		dataBasebitTable = new BitTable(entrybitTable,data.numInstances(),fileDatabase,f1Itemsets.size()+1);
		
	}

	public void generateNextFrequentItemset(){
		
		K++;
		
		if (K==2){
			generateF2Itemset();
			
		} else {
			generateFKItemset();
		}
		
	}

	private void generateFKItemset() {
		
		StorableArray<BitIntSet> auxBitTable = new StorableArray<BitIntSet>(CandidatesFile, new StringToBitIntSetTransformer(), MAX_SEQ_IN_MEM);
		
		BitIntSet MID,aux2,auxMID;
		
		long s = cKbitTable.size();
		
		int i = 0;
		
		int previ = 0;
		
		for (BitIntSet bitIntSet : cKbitTable){
		
			System.out.println(previ + " Generated K out Of " + s);
			
			MID = generateMID(bitIntSet);
			
			boolean morematching = true;
			
			i = ++previ;
			
			while (i<s && morematching) {
				
				aux2 = cKbitTable.get(i);
				
				if (aux2 == null){
					i++;
				} else {
				
					auxMID = aux2.and(MID);
	
					if (auxMID.equals(MID)){
	
						aux2 = aux2.or(bitIntSet);
						
						if (min_supp>0){
						
							if (dataBasebitTable.getSupport(aux2)>=min_supp){
								
								auxBitTable.add(aux2);
							
							}
										
						}
						i++;
						
					} else {
						
						morematching = false;
					
					}
				}
			}
			
			if (previ % GARBAGE_COLLECTION_ITEMS == 0){
				Runtime.getRuntime().gc();
			}
			
		}
		
		cKbitTable.clear();
		
		cKbitTable = new BitTable(auxBitTable,fileCK,fileSize);
		
		auxBitTable.clear();
		
	}

	private BitIntSet generateMID(BitIntSet bitSet) {
		
		return bitSet.getMID();
	
	}

	private void generateF2Itemset(){
		
		ArrayList<ArrayList<Integer>> c2set = new ArrayList<ArrayList<Integer>>((f1Itemsets.size()*(f1Itemsets.size()-1))/2);
		
		ArrayList<Integer> aux;
		
		int s = f1Itemsets.size();
		
		for (int i = 0; i < f1Itemsets.size()-1; i++) {
			
			System.out.println(i + " Generated 2 Out of " + s);
			
			for (int j = i+1; j < f1Itemsets.size(); j++) {
				
				aux = new ArrayList<Integer>(2);
				
				aux.add(f1Itemsets.get(i).get(0));
				aux.add(f1Itemsets.get(j).get(0));
				
				if (dataBasebitTable.getSupport(aux) >= min_supp){
					
					c2set.add(aux);
				
				}
			}
		}
		
		cKbitTable = new BitTable(c2set, f1Itemsets.size(),fileCK,fileSize);
		
		
	}

	public int size() {
		
		if (cKbitTable==null){
			return f1Itemsets.size();
		}
		return cKbitTable.size();
	}

	public void remove(ArrayList<Integer> features) {
		
		if (cKbitTable != null){	
			
			BitIntSet bset = new BitIntSet(features.get(features.size()-1));
			
			for (Integer integer : features) {
				bset.set(integer);
			}

			cKbitTable.remove(bset);
		
		
		} else {
			
			int i;
			
			for (i = 0; !features.toString().equals(f1Itemsets.get(i).toString()); i++);
			
			f1Itemsets.remove(i);
			
			size--;
			
		}
		
		
		
	}

	public void removeTransaction(Integer transaction) {
		
		dataBasebitTable.removeIndex(transaction);
		
	}

	public int sizeOfIterator() {

		return sizeOfIterator;
		
	}

	
}
