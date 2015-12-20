package domain.caching.tuple.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.axis.encoding.Base64;
import org.apache.tools.ant.filters.StringInputStream;

import execution.workload.tuple.Tuple;

public class TestSerialization {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
	
		Tuple t = new Tuple();
		
		t.setTupleField("att1", "valuvvvvvvvvvvvvvvvvvv1");
		t.setTupleField("att2", "valuccccccccccccccc2");

		Tuple t2 = new Tuple();
		
		t2.setTupleField("att1", "valu3");
		t2.setTupleField("att2", "valu4");
		
		List<Tuple> ts = new ArrayList<Tuple>();
		
		ts.add(t);
		ts.add(t2);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		ObjectOutput out = new ObjectOutputStream(os);
		
		out.writeObject(ts);
		
		out.close();
		
		byte[] sb = os.toByteArray();
		
		String ss = Base64.encode(sb);
		
		System.out.println(ss);
		
		System.out.println(Arrays.toString(sb));
		
		String s = os.toString();
		
		byte[] ssb = Base64.decode(ss);
		
		InputStream buffer = new ByteArrayInputStream(ssb);
		ObjectInput input = new ObjectInputStream ( buffer );
		List<Tuple> tuples = (List<Tuple>)input.readObject();
		buffer.close();
		input.close();
		
		System.out.println(tuples.toString());
		
		
	}
	
}
