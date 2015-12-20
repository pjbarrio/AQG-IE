package techniques.baseline.QProberSVM.model;

import java.util.ArrayList;
import java.util.Iterator;

public class BitTable implements Iterable<BitIntSet>{

	private StorableArray<BitIntSet> bitTable;

	public BitTable(ArrayList<ArrayList<Integer>> entrybitTable,
			int numInstances, String fileName, int size) {
		
		bitTable = new StorableArray<BitIntSet>(fileName, new StringToBitIntSetTransformer(), size);
		
		fillBitTable(entrybitTable,numInstances);
		
	}

	public BitTable(Iterable<BitIntSet> auxBitTable,String fileName,int size){
		
		bitTable = new StorableArray<BitIntSet>(fileName, new StringToBitIntSetTransformer(),size);
		for (BitIntSet bitSet : auxBitTable) {
			bitTable.add(bitSet);
		}
		
		bitTable.finish();
		
	}

	private void fillBitTable(ArrayList<ArrayList<Integer>> entrybitTable,
			int numInstances) {
		
		for (ArrayList<Integer> arrayList : entrybitTable) {
			
			if (arrayList.size() > 0){
			
				BitIntSet aux = new BitIntSet(arrayList.get(arrayList.size()-1));
				
				for (Integer instance : arrayList) {
					
					aux.set(instance);
					
				}

				bitTable.add(aux);
				
			}else{
				BitIntSet aux = new BitIntSet(0);
				bitTable.add(aux);
			}
			
		}
		
		bitTable.finish();
		
	}

	public int getSupport(ArrayList<Integer> aux) {
		
		BitIntSet set = (BitIntSet)bitTable.get(aux.get(0));
		
		for (int i = 1; i < aux.size(); i++) {
			
			set = set.and((BitIntSet)bitTable.get(aux.get(i)));
		
		}
		
		int val = set.cardinality();
		
		set.clear();
		
		return val;
	
	}

	public int size() {
		return bitTable.size();
	}

	public BitIntSet get(int i) {
		return (BitIntSet)bitTable.get(i);
	}

	public void remove(BitIntSet bset) {
		
		bitTable.remove(bset);
		
	}

	public void removeIndex(Integer transaction) {
		
		for (BitIntSet bset : bitTable) {
			
			bset.clear(transaction);
			
		}
		
	}

	public int getSupport(BitIntSet bitSet){
		
		int ind = bitSet.nextSetBit(0);
		
		BitIntSet aux = (BitIntSet)bitTable.get(ind);
		
		for (int i = bitSet.nextSetBit(ind+1); i >= 0; i = bitSet.nextSetBit(i+1)) {
			
			aux = aux.and((BitIntSet)bitTable.get(i));
		
		}
		
		int val = aux.cardinality();
		
		aux.clear();
		
		return val;
		
	}

	@Override
	public Iterator<BitIntSet> iterator() {
		
		return bitTable.iterator();
		
	}

	public void clear() {
		
		bitTable.clear();
		
	}

	public void cleanRemoved() {
		
		bitTable.cleanRemoved();
		
	}

}
