package execution.alternatives.visual;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import execution.alternatives.visual.alternatives.SchedulerPane;

public class ConstraintPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5336572398042036623L;
	private JTextField DocsPerSecond = null;
	private JTextField extractionTime = null;
	private JTextField databasesContacted = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JPanel informationExtractionPane = null;
	private JPanel databasePane = null;
	private JLabel jLabel4 = null;
	private JTextField retrievalTime = null;
	private JLabel jLabel5 = null;
	private JTextField queriesPerDatabase = null;
	private JLabel jLabel6 = null;
	private JTextField issuingTime = null;
	private JLabel jLabel7 = null;
	private JTextField documentsPerDatabase = null;
	private JLabel jLabel8 = null;
	private JTextField documentsPerQuery = null;
	private JLabel jLabel9 = null;
	private JTextField queriesperSecond = null;
	private SchedulerPane schedulerPane = null;
	private JPanel ExtractionsASecondPane = null;
	private JPanel QueriesASecond = null;
	private JPanel optionsPane = null;
	private JPanel parametersPane = null;
	private JRadioButton Parallel = null;
	private JRadioButton Sequential = null;
	private ButtonGroup IEoptionsGroup;
	private JPanel optionsQueryPane = null;
	private JPanel parametersQueryPane = null;
	private JRadioButton parallelQuery = null;
	private JRadioButton sequentialQuery = null;
	private ButtonGroup optionsQueryButtons;
	private JLabel jLabel3 = null;
	private JTextField numberOfExtractors = null;

	/**
	 * This method initializes 
	 * 
	 */
	public ConstraintPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        jLabel2 = new JLabel();
        jLabel2.setText("Databases");
        jLabel1 = new JLabel();
        jLabel1.setText("ExtractionTime");
        jLabel = new JLabel();
        
        jLabel.setText("Documents/Second (IE)");
        this.add(getInformationExtractionPane(), null);
        this.add(getDatabasePane(), null);
        this.add(getSchedulerPane(), null);
			
	}

	/**
	 * This method initializes DocsPerSecond	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getDocsPerSecond() {
		if (DocsPerSecond == null) {
			DocsPerSecond = new JTextField();
			DocsPerSecond.setEnabled(false);
		}
		return DocsPerSecond;
	}

	/**
	 * This method initializes extractionTime	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getExtractionTime() {
		if (extractionTime == null) {
			extractionTime = new JTextField();
		}
		return extractionTime;
	}

	/**
	 * This method initializes databasesContacted	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getDatabasesContacted() {
		if (databasesContacted == null) {
			databasesContacted = new JTextField();
		}
		return databasesContacted;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getInformationExtractionPane() {
		if (informationExtractionPane == null) {
			jLabel3 = new JLabel();
			jLabel3.setText("Number of Extractors");
			jLabel8 = new JLabel();
			jLabel8.setText("Document/query");
			jLabel7 = new JLabel();
			jLabel7.setText("Documents/database");
			informationExtractionPane = new JPanel();
			informationExtractionPane.setLayout(new BoxLayout(getInformationExtractionPane(), BoxLayout.X_AXIS));
			informationExtractionPane.add(getExtractionsASecondPane(), null);
			informationExtractionPane.add(jLabel1, null);
			informationExtractionPane.add(getExtractionTime(), null);
			informationExtractionPane.add(jLabel7, null);
			informationExtractionPane.add(getDocumentsPerDatabase(), null);
			informationExtractionPane.add(jLabel8, null);
			informationExtractionPane.add(getDocumentsPerQuery(), null);
			informationExtractionPane.add(getJLabel4(), null);
			informationExtractionPane.add(getRetrievalTime(), null);
			informationExtractionPane.add(jLabel3, null);
			informationExtractionPane.add(getNumberOfExtractors(), null);
		}
		return informationExtractionPane;
	}

	/**
	 * This method initializes databasePane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDatabasePane() {
		if (databasePane == null) {
			jLabel9 = new JLabel();
			jLabel9.setText("Queries/second");
			jLabel6 = new JLabel();
			jLabel6.setText("Issuing time");
			jLabel5 = new JLabel();
			jLabel5.setText("Queries/database");
			databasePane = new JPanel();
			databasePane.setLayout(new BoxLayout(getDatabasePane(), BoxLayout.X_AXIS));
			databasePane.add(jLabel2, null);
			databasePane.add(getDatabasesContacted(), null);
			databasePane.add(jLabel5, null);
			databasePane.add(getQueriesPerDatabase(), null);
			databasePane.add(jLabel6, null);
			databasePane.add(getIssuingTime(), null);
			databasePane.add(getQueriesASecond(), null);
		}
		return databasePane;
	}

	/**
	 * This method initializes jLabel4	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getJLabel4() {
		if (jLabel4 == null) {
			jLabel4 = new JLabel();
			jLabel4.setText("Retrieval Time");
		}
		return jLabel4;
	}

	/**
	 * This method initializes retrievalTime	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getRetrievalTime() {
		if (retrievalTime == null) {
			retrievalTime = new JTextField();
		}
		return retrievalTime;
	}

	/**
	 * This method initializes queriesPerDatabase	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getQueriesPerDatabase() {
		if (queriesPerDatabase == null) {
			queriesPerDatabase = new JTextField();
		}
		return queriesPerDatabase;
	}

	/**
	 * This method initializes issuingTime	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getIssuingTime() {
		if (issuingTime == null) {
			issuingTime = new JTextField();
		}
		return issuingTime;
	}

	/**
	 * This method initializes documentsPerDatabase	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getDocumentsPerDatabase() {
		if (documentsPerDatabase == null) {
			documentsPerDatabase = new JTextField();
		}
		return documentsPerDatabase;
	}

	/**
	 * This method initializes documentsPerQuery	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getDocumentsPerQuery() {
		if (documentsPerQuery == null) {
			documentsPerQuery = new JTextField();
		}
		return documentsPerQuery;
	}

	/**
	 * This method initializes queriesperSecond	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getQueriesperSecond() {
		if (queriesperSecond == null) {
			queriesperSecond = new JTextField();
			queriesperSecond.setEnabled(false);
		}
		return queriesperSecond;
	}

	/**
	 * This method initializes schedulerPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSchedulerPane() {
		if (schedulerPane == null) {
			schedulerPane = new SchedulerPane();
		}
		return schedulerPane;
	}

	/**
	 * This method initializes ExtractionsASecondPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getExtractionsASecondPane() {
		if (ExtractionsASecondPane == null) {
			ExtractionsASecondPane = new JPanel();
			ExtractionsASecondPane.setLayout(new BoxLayout(getExtractionsASecondPane(), BoxLayout.Y_AXIS));
			ExtractionsASecondPane.add(getOptionsPane(), null);
			ExtractionsASecondPane.add(getParametersPane(), null);
		}
		return ExtractionsASecondPane;
	}

	/**
	 * This method initializes QueriesASecond	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getQueriesASecond() {
		if (QueriesASecond == null) {
			QueriesASecond = new JPanel();
			QueriesASecond.setLayout(new BoxLayout(getQueriesASecond(), BoxLayout.Y_AXIS));
			QueriesASecond.add(getOptionsQueryPane(), null);
			QueriesASecond.add(getParametersQueryPane(), null);
		}
		return QueriesASecond;
	}

	/**
	 * This method initializes optionsPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOptionsPane() {
		if (optionsPane == null) {
			optionsPane = new JPanel();
			optionsPane.setLayout(new BoxLayout(getOptionsPane(), BoxLayout.X_AXIS));
			optionsPane.add(getParallel(), null);
			optionsPane.add(getSequential(), null);
			IEoptionsGroup = new ButtonGroup();
			IEoptionsGroup.add(getParallel());
			IEoptionsGroup.add(getSequential());
			IEoptionsGroup.setSelected(getSequential().getModel(), true);
		}
		return optionsPane;
	}

	/**
	 * This method initializes parametersPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getParametersPane() {
		if (parametersPane == null) {
			parametersPane = new JPanel();
			parametersPane.setLayout(new BoxLayout(getParametersPane(), BoxLayout.X_AXIS));
			parametersPane.add(jLabel, null);
			parametersPane.add(getDocsPerSecond(), null);
		}
		return parametersPane;
	}

	/**
	 * This method initializes Parallel	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getParallel() {
		if (Parallel == null) {
			Parallel = new JRadioButton();
			Parallel.setText("Parallel");
			Parallel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getDocsPerSecond().setEnabled(true);
				}
			});
		}
		return Parallel;
	}

	/**
	 * This method initializes Sequential	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getSequential() {
		if (Sequential == null) {
			Sequential = new JRadioButton();
			Sequential.setText("Sequential");
			Sequential.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getDocsPerSecond().setEnabled(false);
				}
			});
		}
		return Sequential;
	}

	/**
	 * This method initializes optionsQueryPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOptionsQueryPane() {
		if (optionsQueryPane == null) {
			optionsQueryPane = new JPanel();
			optionsQueryPane.setLayout(new BoxLayout(getOptionsQueryPane(), BoxLayout.X_AXIS));
			optionsQueryPane.add(getParallelQuery(), null);
			optionsQueryPane.add(getSequentialQuery(), null);
			optionsQueryButtons = new ButtonGroup();
			optionsQueryButtons.add(getParallelQuery());
			optionsQueryButtons.add(getSequentialQuery());
			optionsQueryButtons.setSelected(getSequentialQuery().getModel(), true);
		}
		return optionsQueryPane;
	}

	/**
	 * This method initializes parametersQueryPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getParametersQueryPane() {
		if (parametersQueryPane == null) {
			parametersQueryPane = new JPanel();
			parametersQueryPane.setLayout(new BoxLayout(getParametersQueryPane(), BoxLayout.X_AXIS));
			parametersQueryPane.add(jLabel9, null);
			parametersQueryPane.add(getQueriesperSecond(), null);
		}
		return parametersQueryPane;
	}

	/**
	 * This method initializes parallelQuery	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getParallelQuery() {
		if (parallelQuery == null) {
			parallelQuery = new JRadioButton();
			parallelQuery.setText("Parallel");
			parallelQuery.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getQueriesperSecond().setEnabled(true);
				}
			});
		}
		return parallelQuery;
	}

	/**
	 * This method initializes sequentialQuery	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getSequentialQuery() {
		if (sequentialQuery == null) {
			sequentialQuery = new JRadioButton();
			sequentialQuery.setText("Sequential");
			sequentialQuery.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getQueriesperSecond().setEnabled(false);
				}
			});
		}
		return sequentialQuery;
	}

	/**
	 * This method initializes numberOfExtractors	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getNumberOfExtractors() {
		if (numberOfExtractors == null) {
			numberOfExtractors = new JTextField();
		}
		return numberOfExtractors;
	}

}
