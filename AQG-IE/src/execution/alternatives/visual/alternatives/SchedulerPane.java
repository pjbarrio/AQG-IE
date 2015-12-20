package execution.alternatives.visual.alternatives;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class SchedulerPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JRadioButton fcfsButton = null;
	private JPanel roundRobinPane = null;
	private JRadioButton rrbutton = null;
	private JPanel quantumPane = null;
	private JLabel jLabel = null;
	private JTextField quantumField = null;
	private ButtonGroup buttonGroup;

	/**
	 * This method initializes 
	 * 
	 */
	public SchedulerPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(getFcfsButton(), null);
        this.add(getRoundRobinPane(), null);
			
        buttonGroup = new ButtonGroup();
        buttonGroup.add(getRrbutton());
        buttonGroup.add(getFcfsButton());
        buttonGroup.setSelected(getRrbutton().getModel(), true);

	}

	/**
	 * This method initializes fcfsButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getFcfsButton() {
		if (fcfsButton == null) {
			fcfsButton = new JRadioButton();
			fcfsButton.setText("First Come First Serve");
		}
		return fcfsButton;
	}

	/**
	 * This method initializes roundRobinPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getRoundRobinPane() {
		if (roundRobinPane == null) {
			roundRobinPane = new JPanel();
			roundRobinPane.setLayout(new BoxLayout(getRoundRobinPane(), BoxLayout.Y_AXIS));
			roundRobinPane.add(getRrbutton(), null);
			roundRobinPane.add(getQuantumPane(), null);
		}
		return roundRobinPane;
	}

	/**
	 * This method initializes rrbutton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRrbutton() {
		if (rrbutton == null) {
			rrbutton = new JRadioButton();
			rrbutton.setText("Round Robin");
		}
		return rrbutton;
	}

	/**
	 * This method initializes quantumPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getQuantumPane() {
		if (quantumPane == null) {
			jLabel = new JLabel();
			jLabel.setText("Quantum");
			quantumPane = new JPanel();
			quantumPane.setLayout(new BoxLayout(getQuantumPane(), BoxLayout.X_AXIS));
			quantumPane.add(jLabel, null);
			quantumPane.add(getQuantumField(), null);
		}
		return quantumPane;
	}

	/**
	 * This method initializes quantumField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getQuantumField() {
		if (quantumField == null) {
			quantumField = new JTextField();
		}
		return quantumField;
	}

}
