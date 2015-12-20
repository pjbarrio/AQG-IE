package execution.alternatives.visual.alternatives.when;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import exploration.model.enumerations.ProbabilisticDistributionCheckerEnum;

public class WordsDistributionPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3721564110451305475L;
	private JCheckBox jCheckBox = null;
	private JComboBox jComboBox = null;

	/**
	 * This method initializes 
	 * 
	 */
	public WordsDistributionPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(getJCheckBox(), null);
        this.add(getJComboBox(), null);
			
	}

	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setText("Distribution Of words");
		}
		return jCheckBox;
	}

	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox(ProbabilisticDistributionCheckerEnum.values());
		}
		return jComboBox;
	}

}
