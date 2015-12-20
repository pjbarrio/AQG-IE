package execution.alternatives.visual.alternatives.when;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class AfterNPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6855008200688884365L;
	private JRadioButton documentsButton = null;
	private JRadioButton queriesButton = null;
	private JPanel optionPane = null;
	private JPanel valuePane = null;
	private JTextField valueField = null;
	private ButtonGroup buttonGroup;

	/**
	 * This method initializes
	 * 
	 */
	public AfterNPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setSize(new Dimension(362, 56));
		this.setBorder(new LineBorder(Color.BLACK));
		this.add(getOptionPane(), null);
		this.add(getValuePane(), null);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(getDocumentsButton());
		buttonGroup.add(getQueriesButton());
		buttonGroup.setSelected(getDocumentsButton().getModel(), true);
	}

	/**
	 * This method initializes jRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getDocumentsButton() {
		if (documentsButton == null) {
			documentsButton = new JRadioButton();
			documentsButton.setText("Documents");
		}
		return documentsButton;
	}

	/**
	 * This method initializes jRadioButton1
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getQueriesButton() {
		if (queriesButton == null) {
			queriesButton = new JRadioButton();
			queriesButton.setText("Queries");
		}
		return queriesButton;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getOptionPane() {
		if (optionPane == null) {
			optionPane = new JPanel();
			optionPane.setLayout(new BoxLayout(getOptionPane(),
					BoxLayout.Y_AXIS));
			optionPane.setBorder(new LineBorder(Color.BLACK));
			optionPane.add(getDocumentsButton(), null);
			optionPane.add(getQueriesButton(), null);
		}
		return optionPane;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getValuePane() {
		if (valuePane == null) {
			valuePane = new JPanel();
			valuePane.setLayout(new BoxLayout(getValuePane(), BoxLayout.X_AXIS));
			valuePane.add(getValueField(), null);
		}
		return valuePane;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getValueField() {
		if (valueField == null) {
			valueField = new JTextField();
		}
		return valueField;
	}

} // @jve:decl-index=0:visual-constraint="10,10"
