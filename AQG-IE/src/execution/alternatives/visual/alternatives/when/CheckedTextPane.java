package execution.alternatives.visual.alternatives.when;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CheckedTextPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9069483660541694493L;
	private JCheckBox jCheckBox = null;
	private JTextField jTextField = null;
	private String text;

	public CheckedTextPane(String string) {
		
		this.text = string;
		initialize();
	}
		

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(getJCheckBox(), null);
		this.add(getJTextField(), null);
	}
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setText(text);
		}
		return jCheckBox;
	}
	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
		}
		return jTextField;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
