package sample.generation.relation.database.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JTextPane;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.UIManager;
import java.awt.Color;

import org.apache.commons.io.FileUtils;
import org.eclipse.wb.swing.FocusTraversalOnArray;

import com.google.gdata.util.common.base.Pair;

import execution.workload.tuple.Tuple;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.rdf.RDFPESExtractor;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import net.miginfocom.swing.MigLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;

public class TupleGenerator {

	private static persistentWriter pW;
	private static String relation;
	private static String collection;
	private static int selectednumber;
	private JFrame frmRelation;
	private JButton takekey;
	private JLabel tuple;
	private JLabel prefix;
	private JLabel exact;
	private JLabel suffix;
	private List<String> usefulFiles;
	private int lastDocument;
	private int actualTuple;
	private List<Pair<Tuple,String[]>> cached;
	private int currentIndex; //global
	private Map<Integer, Integer> docs;
	private Map<Integer, Integer> tups;
	private Map<Integer, Integer> value;
	private JPanel panel_1;
	private JLabel lblGoodTuples;
	private JLabel gt;
	private JLabel lblUncategorized;
	private JLabel ut;
	private JLabel lblBadTuples;
	private JLabel bt;
	private int currentValue;
	
	
	private int goodt = 0;
	private int unkt = 0;
	private int badt = 0;
	private int coref = 0;
	private int both = 0;
	private JPanel panel_2;
	private JButton btnSaveInDisk;
	private String origin;
	private File toSaveFile;
	private List<String> attributes;
	private File toSaveAllFile;
	private JPanel panel_3;
	private JLabel lblDoc;
	private JTextField lbldoc;
	private JPanel panel_4;
	private JLabel lblValue;
	private JLabel lblCurrentValue;
	private JButton btnCoref;
	private JButton btnFixcr;
	private JLabel lblCoref;
	private JLabel cr;
	private JLabel lblFixcr;
	private JLabel fcr;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

//		relation = "Quotation"; //Too much and co-reference really needed.
//		relation = "ProductIssues";
		
		relation = "PersonCareer";
//		relation = "PollsResult";
//		relation = "VotingResult";
//		relation = "NaturalDisaster";
//		relation = "ManMadeDisaster";
//		relation = "PersonTravel";
//		relation = "Indictment";
//		relation = "Trial";
//		relation = "Arrest";		

		
		collection = "TREC";
		
		selectednumber = 650;
		
		pW = PersistenceImplementation.getWriter();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TupleGenerator window = new TupleGenerator(collection,relation,pW,selectednumber);
					window.frmRelation.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @param pW 
	 * @throws IOException 
	 */
	public TupleGenerator(String collection,String relation, persistentWriter pW, int selectedNumber) throws IOException {
		initialize(relation);
		
		origin = pW.getSelectedUsefulDocumentsForCollection(collection, relation, selectedNumber);
		
		attributes = FileUtils.readLines(pW.getAttributesToKeep(collection,relation));
		
		System.out.println(attributes.toString());
		
		usefulFiles = FileUtils.readLines(new File(origin));
	
		toSaveFile = new File(pW.getSavedOutputForRelationExtractionTraining(collection,relation, selectedNumber));
		
		toSaveAllFile = new File(pW.getSavedOutputForRelationExtractionTrainingAll(collection,relation, selectedNumber));
		
		lastDocument = -1;
		
		cached = new ArrayList<Pair<Tuple,String[]>>();
		
		docs = new HashMap<Integer,Integer>();
		
		tups = new HashMap<Integer, Integer>();
		
		value = new HashMap<Integer, Integer>();
		
		currentIndex=-1;
		
		if (toSaveAllFile.exists()){ //already stored values
			
			goForward();
			
			List<String> values = FileUtils.readLines(toSaveAllFile);
			
			for (int i = 0; i < values.size(); i++) {
				
				setCurrentAs(Integer.valueOf(values.get(i)));
				
				goForward();
				
			}
			
		}else{
			
			goForward();
		}
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String relation) {
		frmRelation = new JFrame();
		frmRelation.setTitle("Relation: " + relation);

		frmRelation.setAlwaysOnTop(true);

		frmRelation.setBounds(100, 100, 907, 547);
		frmRelation.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRelation.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		tuple = new JLabel("tuple");
		tuple.setBackground(UIManager.getColor("Button.focus"));
		frmRelation.getContentPane().add(tuple);
		
		prefix = new JLabel("prefix");
		prefix.setBackground(new Color(0, 255, 0));
		frmRelation.getContentPane().add(prefix);
		
		exact = new JLabel("exact");
		exact.setBackground(new Color(244, 164, 96));
		frmRelation.getContentPane().add(exact);
		
		suffix = new JLabel("suffix");
		suffix.setBackground(new Color(240, 230, 140));
		frmRelation.getContentPane().add(suffix);
		
		JPanel panel = new JPanel();


		frmRelation.getContentPane().add(panel);
		
		JButton btnBack = new JButton("Back");
		btnBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				goBack();
				
			}
		});
		
		panel.add(btnBack);
		
		JButton btnGood = new JButton("Good(1)");

		btnGood.setMnemonic('1');
		btnGood.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setCurrentAs(1);
				goForward();
			}
		});
		panel.add(btnGood);
		
		JButton btnDontKnow = new JButton("Fix(2)");
		btnDontKnow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnDontKnow.setMnemonic('2');
		btnDontKnow.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			
				setCurrentAs(2);
				goForward();
			
			}
		});
		panel.add(btnDontKnow);
		
		JButton btnBad = new JButton("Bad(3)");
		btnBad.setMnemonic('3');
		btnBad.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				setCurrentAs(3);
				goForward();
				
			}
		});
		
		panel.add(btnBad);
		
		takekey = new JButton("Skip");
		takekey.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
		});
		takekey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				setCurrentAs(currentValue);
				goForward();
				
			}
		});
		takekey.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				
				int key = e.getKeyChar() - 48;
				
				if (key >=0 && key <=4){
				
					setCurrentAs(key);
					goForward();
				
				}

			}
		});
		
		btnCoref = new JButton("Co-Ref(4)");
		btnCoref.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				setCurrentAs(4);
				goForward();
				
			}
		});
		btnCoref.setMnemonic('4');
		panel.add(btnCoref);
		
		btnFixcr = new JButton("Fix&CR(0)");
		btnFixcr.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				setCurrentAs(0);
				goForward();
				
			}
		});
		btnFixcr.setMnemonic('0');
		panel.add(btnFixcr);
		panel.add(takekey);
		
		panel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{takekey, btnBack, btnGood, btnDontKnow, btnBad}));
		
		panel_1 = new JPanel();
		frmRelation.getContentPane().add(panel_1);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		lblGoodTuples = new JLabel("Good Tuples:");
		panel_1.add(lblGoodTuples);
		
		gt = new JLabel("gt");
		panel_1.add(gt);
		
		lblUncategorized = new JLabel("Fix:");
		panel_1.add(lblUncategorized);
		
		ut = new JLabel("ut");
		panel_1.add(ut);
		
		lblBadTuples = new JLabel("Bad Tuples:");
		panel_1.add(lblBadTuples);
		
		bt = new JLabel("bt");
		panel_1.add(bt);
		
		lblCoref = new JLabel("Co-Ref: ");
		panel_1.add(lblCoref);
		
		cr = new JLabel("cr");
		panel_1.add(cr);
		
		lblFixcr = new JLabel("Fix&CR:");
		panel_1.add(lblFixcr);
		
		fcr = new JLabel("fcr");
		panel_1.add(fcr);
		
		panel_2 = new JPanel();
		frmRelation.getContentPane().add(panel_2);
		
		btnSaveInDisk = new JButton("Save In Disk");
		btnSaveInDisk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				saveCurrentState();
				
			}
		});
		panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel_2.add(btnSaveInDisk);
		
		panel_4 = new JPanel();
		frmRelation.getContentPane().add(panel_4);
		
		lblValue = new JLabel("Value: ");
		panel_4.add(lblValue);
		
		lblCurrentValue = new JLabel("New label");
		panel_4.add(lblCurrentValue);
		
		panel_3 = new JPanel();
		frmRelation.getContentPane().add(panel_3);
		
		lblDoc = new JLabel("Doc: ");
		panel_3.add(lblDoc);
		
		lbldoc = new JTextField("-");
		panel_3.add(lbldoc);
		
		takekey.requestFocusInWindow();
		
	}

	protected void saveCurrentState() {
		
		try {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(toSaveFile));
			
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(toSaveAllFile));
			
			bw.write("Current Index: " + currentIndex);
			
			for (int i = 0; i < currentIndex; i++) {
				
				if (i < currentIndex-1){
					bw2.write(value.get(i).toString());
					bw2.newLine();
				}
				else
					bw2.write(value.get(i).toString());
				
				if (value.get(i) == 1){
				
					bw.write("\n1," + usefulFiles.get(docs.get(i)) + "," + tups.get(i));
				
				}
				
				if (value.get(i) == 4){
					
					bw.write("\n4," + usefulFiles.get(docs.get(i)) + "," + tups.get(i));
				
				}
				
			}
			
			bw2.close();
			
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected void goForward() {

		currentIndex++;
		
		while (currentIndex == cached.size()){//we run out of tuples
		
			lastDocument++; //go for the next document
			
			try {
				
				cached.addAll(process(usefulFiles.get(lastDocument)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			actualTuple = -1;

		}

		actualTuple++;
		
		if (!value.containsKey(currentIndex))
			setCurrentAs(-1);
		
		update(currentIndex);
		
	}

	private void set(int index, int tuple,
			int val) {

		if (!docs.containsKey(index))
			docs.put(index, lastDocument);
		
		tups.put(index, tuple);
		
		value.put(index, val);
		
	}

	private void update(int currentIndex) {
		
		Pair<Tuple,String[]> vals = cached.get(currentIndex);
		
		String ttext = "<html>";
		
		for (int i = 0; i < vals.getFirst().getFieldNames().length; i++) {
			
			if (attributes.contains(vals.getFirst().getFieldNames()[i]))
				ttext += vals.getFirst().getFieldNames()[i] + ":" + vals.getFirst().getFieldValue(vals.getFirst().getFieldNames()[i]) + "<p>";
			
		}
		
		ttext += "</html>";
		
		tuple.setText(ttext);
		
		prefix.setText("<html>Prefix:<p><p>" + vals.getSecond()[0] + "</html>");
		
		exact.setText("<html>Exact:<p><p>" + vals.getSecond()[1] + "</html>");
		
		suffix.setText("<html>Suffix:<p><p>" + vals.getSecond()[2] + "</html>");
		
		gt.setText(Integer.toString(goodt));
		
		ut.setText(Integer.toString(unkt));

		bt.setText(Integer.toString(badt));
		
		cr.setText(Integer.toString(coref));
		
		fcr.setText(Integer.toString(both));
		
		currentValue = value.get(currentIndex);
		
		lblCurrentValue.setText(Integer.toString(currentValue));
		
		lbldoc.setText(usefulFiles.get(docs.get(currentIndex)));
		
		frmRelation.setTitle("Relation: " + relation + " - (" + (docs.get(currentIndex)+1) + "/" + selectednumber + ")");
		
		takekey.requestFocusInWindow();
		
	}

	private List<Pair<Tuple,String[]>> process(String usefulFile) throws IOException {
		return RDFPESExtractor.extract(usefulFile, relation);
	}

	protected void setCurrentAs(int value) {
		
		if (value==1){
			goodt++;
		}else if (value==2){
			unkt++;
		}else if (value==3){
			badt++;
		}else if (value==4){
			coref++;
		}else if (value==0){
			both++;
		}
		
		System.out.println(value);
		
		set(currentIndex,actualTuple,value);
		
	}

	protected void goBack() {
		
		currentIndex--;
		
		actualTuple = tups.get(currentIndex);
		
		int torem = value.get(currentIndex);
		
		if (torem==1){
			goodt--;
		}else if (torem==2){
			unkt--;
		}else if (torem==3){
			badt--;
		}else if (torem==4){
			coref--;
		}else if (torem==0){
			both--;
		}
		
		update(currentIndex);
		
	}

}
