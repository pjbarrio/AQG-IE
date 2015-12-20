package execution.alternatives.visual;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class JVersionPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5798238221131973998L;
	private JRadioButton independent = null;
	private JRadioButton dependent = null;
	private ButtonGroup buttonGroup;  //  @jve:decl-index=0:

	/**
	 * This method initializes 
	 * 
	 */
	public JVersionPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setSize(new Dimension(793, 239));
        buttonGroup = new ButtonGroup();
        buttonGroup.add(getIndependent());
        buttonGroup.add(getDependent());
        buttonGroup.setSelected(getDependent().getModel(), true);
        this.add(getIndependent(), null);
        this.add(getDependent(), null);
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getIndependent() {
		if (independent == null) {
			independent = new JRadioButton();
			independent.setText("Workload-Independent");
		}
		return independent;
	}

	/**
	 * This method initializes jRadioButton1	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getDependent() {
		if (dependent == null) {
			dependent = new JRadioButton();
			dependent.setText("Workload-Dependent");
		}
		return dependent;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
