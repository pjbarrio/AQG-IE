package techniques.baseline.QProberSVM.model;

import utils.SVM.Rules.Candidateable;

public class BitIntSet implements Candidateable{

	private static final int SIZE = 63;
	private static final String SEPARATOR = "-";
	private long[] array;
	private int lastSet;
	private int cardinality;
	private String[] spl;

	public BitIntSet(Integer integer) {
		
		array = new long[integer.intValue()/SIZE + 1];
		
		for (int i = 0; i < array.length; i++) {
			array[i] = 0;
		}
		
		cardinality = 0;
		
		lastSet = -1;
		
	}

	public BitIntSet(long[] arr) {
		
		array = arr;
		int i = arr.length-1;
			
		lastSet = i;
		
		cardinality = 0;
		
		for (i = 0; i < array.length; i++) {
			
			cardinality += getBitSet(array[i]);
			
		}
		
	}

	public BitIntSet(long[] array2, int lastSet2, int cardinality2) {
		
		this.array = array2;
		
		this.lastSet = lastSet2;
		
		this.cardinality = cardinality2;
		
	}

	public BitIntSet(long[] arr, int cardinality) {
		
		this.array = arr;
		
		int i = arr.length;
		
		while(i-->=0 && arr[i]==0);
		
		lastSet = i;
		
		this.cardinality = cardinality;
	}

	public BitIntSet() {
		;
	}

	private int getBitSet(long l) {
		
		int count = 0;
		long val2;
		for (int i = 0; i < SIZE && l!=0; i++) {
			
			val2 = l;
			
			l = l>>1;
			l = l<<1;
			
			if (val2!=l){
				count++;
			}
			
			l = l>>1;
			
		}
		
		return count;
		
	}

	public void set(Integer instance) {
		
		int inti = instance.intValue();
		
		int pos = inti/SIZE;
		
		if (pos>lastSet){
			lastSet = pos;
		}
		
		setValue(pos,inti%SIZE);
		
	}

	private void setValue(int pos, int i) {
		
		long val = array[pos];
		
		array[pos] = array[pos] | (long)Math.pow(2.0, (double)i);
		
		if (val!=array[pos])
			cardinality++;
	
	}

	public BitIntSet and(BitIntSet set) {
		
		int si1 = this.lastSet;
		int si2 = set.lastSet;
		
		int val = Math.min(si1,si2)+1;
		
		long[] arr = new long[val];
		
		int i;
		
		for (i = 0; i < val; i++) {
			arr[i] = array[i] & set.array[i];
		}
		
		return new BitIntSet(arr);
	}


	public int cardinality() {
		return cardinality;
	}

	public void clear() {
		
		array = new long[0];
		lastSet = -1;
		cardinality = 0;
		
	}

	public void clear(Integer index) {
		
		int inti = index.intValue();
		
		int pos = inti/SIZE;
		
		if (pos>=array.length){
			return;
		}
		
		clearValue(pos,inti%SIZE);
		
		if (pos == lastSet){
			
			if (array[pos]==0){
				
				int i;
				
				for (i = pos-1; i >= 0 && array[i]==0 ; i--);
				
				lastSet = i;
			}
			
		}
		
	}

	private void clearValue(int pos, int i) {
		
		long valbef = array[pos];
		
		array[pos] = array[pos] & ~((long)Math.pow(2.0, (double)i));
		
		if (valbef != array[pos])
			cardinality--;
		
	}



	public Object clone(){
		
		return new BitIntSet(array,lastSet,cardinality);
		
	}

	public BitIntSet or(BitIntSet set) {
		
		int si1 = this.lastSet;
		
		int si2 = set.lastSet;
		
		long[] arr = new long[Math.max(si1,si2)+1];
		
		int i;
		
		if (si1<si2){
						
			for (i = 0; i <= si1; i++) {
				arr[i] = array[i] | set.array[i];
			}
			for (;i<=si2;i++){
				
				arr[i] = set.array[i];
								
			}
		} else if (si2<si1){
			
			for (i = 0; i <= si2; i++) {
				arr[i] = array[i] | set.array[i];
			}
			for (;i<=si1;i++){
				
				arr[i] = array[i];
								
			}
			
		} else{
						
			for (i = 0; i <=si1; i++) {
				arr[i] = array[i] | set.array[i];
			}
						
		}
		
		return new BitIntSet(arr);

	}

	public BitIntSet getMID() {
		
		long[] arr = new long[lastSet+1];
		
		for (int i = 0; i < arr.length; i++) {
			arr[i] = array[i];
		}
		
		generateMID(arr,lastSet);
		
		return new BitIntSet(arr,cardinality-1);
		
	}

	private void generateMID(long[] arr, int lastSet2) {
		
		long val = arr[lastSet2];
		
		int i;
		
		long valbefore = val;
		
		for (i = 0; i < SIZE; i++) {
			
			val = val<<1;
			val = val>>1;
			
			if (valbefore!=val)
				break;
			
			val = val<<1;
			
			valbefore = val;
			
		}
		
		arr[lastSet2] = arr[lastSet2] & ~((long)Math.pow(2.0, (double)SIZE-i-1));
	}

	public boolean equals(Object o){
		
		BitIntSet bis = (BitIntSet)o;

		if (lastSet!=bis.lastSet)
			return false;
		
		long[] arr = ((BitIntSet)o).array;
		
		for (int i = 0; i < array.length; i++) {
			
			if (array[i]!=arr[i])
				return false;
			
		}
		
		return true;
		
	}

	public String toString(){
		String ret  = "";
		
		for (int i = 0; i <= lastSet; i++) {
			ret += array[i] + SEPARATOR;
		}
		
		return ret.substring(0, ret.length()-1);
	}

	public int nextSetBit(int i) {
		
		int len = array.length;
		
		int pos = i/SIZE;
		
		if (pos>lastSet)
			return -1;
		
		long val = (long)Math.pow(2, i%SIZE);
		
		if (array[pos]>=val){
			return pos*SIZE + nextBitAfter(pos,i%SIZE); //NO!
		}else{
			pos++;
			while (pos < len && array[pos]==0){pos++;}
			
			if (pos==len)
				return -1;
			
			return pos*SIZE + nextBitAfter(pos,0); //NO!
		}
		
	}

	private int nextBitAfter(int pos, int i) {
		
		int ret = i;
		
		long valbefore,val = array[pos] >> i;
		
		while (true){
		
			valbefore = val;
			
			val = val>>1;
			val = val<<1;
			
			if (valbefore!=val)
				break;
			
			val = val>>1;
			ret++;
		}
			
		return ret;
		
	}

	@Override
	public Candidateable fromDisk(String t) {
		
		spl = t.split(SEPARATOR);
		
		cardinality = Integer.valueOf(spl[0]);
		
		lastSet = spl.length - 2;
		
		array = new long[spl.length-1];
		
		for (int i = 0; i < array.length; i++) {
			array[i] = Long.valueOf(spl[i+1]);
		}
		
		return this;
		
	}

	@Override
	public String toDisk() {
		return cardinality + SEPARATOR + toString();
	}

	public String toBit() {
		
		String ret = "";
		
		for (int i = 0; i < array.length; i++) {
			ret += Long.toBinaryString(array[i]) + "-" + array[i] + "-";
		}
		
		return ret;
	}
	
}
