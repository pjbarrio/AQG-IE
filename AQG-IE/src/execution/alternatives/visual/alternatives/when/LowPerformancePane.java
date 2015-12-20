package execution.alternatives.visual.alternatives.when;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import exploration.model.enumerations.UselessQueryConditionEnum;

public class LowPerformancePane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -927537564708797791L;
	private CheckedTextPane usefulDocumentsPane = null;
	private CheckedTextPane nBadDocumentsPane = null;
	private JPanel nBadQueriesPane = null;
	private JComboBox uselessQueryCondition = null;
	private JTextField uselessQueryConditionThreshold = null;
	private JPanel BadQueriesPane = null;
	private JPanel askAfterPane = null;
	private JLabel jLabel = null;
	private JTextField askAfterField = null;
	private JLabel jLabel1 = null;

	/**
	 * This method initializes 
	 * 
	 */
	public LowPerformancePane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setSize(new Dimension(487, 237));
        this.add(getAskAfterPane(), null);
        this.add(getusefulDocumentsPane(), null);
        this.add(getNBadDocumentsPane(), null);
        this.add(getBadQueriesPane(), null);
			
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getusefulDocumentsPane() {
		if (usefulDocumentsPane == null) {
			usefulDocumentsPane = new CheckedTextPane("Low Precision");
		}
		return usefulDocumentsPane;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getNBadDocumentsPane() {
		if (nBadDocumentsPane == null) {
			nBadDocumentsPane = new CheckedTextPane("Bad Documents Processed");
		}
		return nBadDocumentsPane;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getNBadQueriesPane() {
		if (nBadQueriesPane == null) {
			nBadQueriesPane = new CheckedTextPane("Bad Queries Issued");
			nBadQueriesPane.setLayout(new BoxLayout(getNBadQueriesPane(), BoxLayout.X_AXIS));
		}
		return nBadQueriesPane;
	}

	/**
	 * This method initializes uselessQueryCondition	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getUselessQueryCondition() {
		if (uselessQueryCondition == null) {
			uselessQueryCondition = new JComboBox(UselessQueryConditionEnum.values());
		}
		return uselessQueryCondition;
	}

	/**
	 * This method initializes uselessQueryConditionThreshold	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getUselessQueryConditionThreshold() {
		if (uselessQueryConditionThreshold == null) {
			uselessQueryConditionThreshold = new JTextField();
		}
		return uselessQueryConditionThreshold;
	}

	/**
	 * This method initializes BadQueriesPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getBadQueriesPane() {
		if (BadQueriesPane == null) {
			BadQueriesPane = new JPanel();
			BadQueriesPane.setLayout(new BoxLayout(getBadQueriesPane(), BoxLayout.X_AXIS));
			BadQueriesPane.add(getNBadQueriesPane(), null);
			BadQueriesPane.add(getUselessQueryCondition(), null);
			BadQueriesPane.add(getUselessQueryConditionThreshold(), null);
		}
		return BadQueriesPane;
	}

	/**
	 * This method initializes askAfterPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getAskAfterPane() {
		if (askAfterPane == null) {
			jLabel1 = new JLabel();
			jLabel1.setText(" documents");
			jLabel = new JLabel();
			jLabel.setText("Ask for conditions after processing ");
			askAfterPane = new JPanel();
			askAfterPane.setLayout(new BoxLayout(getAskAfterPane(), BoxLayout.X_AXIS));
			askAfterPane.add(jLabel, null);
			askAfterPane.add(getAskAfterField(), null);
			askAfterPane.add(jLabel1, null);
		}
		return askAfterPane;
	}

	/**
	 * This method initializes askAfterField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getAskAfterField() {
		if (askAfterField == null) {
			askAfterField = new JTextField();
		}
		return askAfterField;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
