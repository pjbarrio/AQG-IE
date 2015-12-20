package execution.alternatives.visual.alternatives;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;

public class AlternativePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1624130027729110611L;
	private JPanel executionChooserPane = null;
	private JPanel parametersPane = null;
	private JRadioButton staticButton = null;
	private JRadioButton adaptiveButton = null;
	private ButtonGroup buttonGroup;  //  @jve:decl-index=0:
	private StaticExecutionPane staticExecutionPane = null;
	private AdaptiveExecutionPane adaptiveExecutionPane = null;
	/**
	 * This method initializes 
	 * 
	 */
	public AlternativePanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setSize(new Dimension(741, 227));
        this.add(getExecutionChooserPane(), null);
        this.add(getParametersPane(), null);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(getStaticButton());
		buttonGroup.add(getadaptiveButton());
		buttonGroup.setSelected(getStaticButton().getModel(), true);
		this.setBorder(new LineBorder(Color.BLACK));

	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getExecutionChooserPane() {
		if (executionChooserPane == null) {
			executionChooserPane = new JPanel();
			executionChooserPane.setLayout(new BoxLayout(getExecutionChooserPane(), BoxLayout.Y_AXIS));
			executionChooserPane.add(getStaticButton(), null);
			executionChooserPane.add(getadaptiveButton(), null);
		}
		return executionChooserPane;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getParametersPane() {
		if (parametersPane == null) {
			parametersPane = new JPanel();
			parametersPane.setLayout(new CardLayout());
			parametersPane.add(getStaticExecutionPane(), getStaticExecutionPane().getName());
			parametersPane.add(getAdaptiveExecutionPane(), getAdaptiveExecutionPane().getName());
		}
		return parametersPane;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getStaticButton() {
		if (staticButton == null) {
			staticButton = new JRadioButton();
			staticButton.setText("Static");
			staticButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					((CardLayout)getParametersPane().getLayout()).show(getParametersPane(), getStaticExecutionPane().getName());
				}
			});
		}
		return staticButton;
	}

	/**
	 * This method initializes jRadioButton1	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getadaptiveButton() {
		if (adaptiveButton == null) {
			adaptiveButton = new JRadioButton();
			adaptiveButton.setText("Adaptive");
			adaptiveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					((CardLayout)getParametersPane().getLayout()).show(getParametersPane(), getAdaptiveExecutionPane().getName());
				}
			});
		}
		return adaptiveButton;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getStaticExecutionPane() {
		if (staticExecutionPane == null) {
			staticExecutionPane = new StaticExecutionPane();
			staticExecutionPane.setName("staticExecutionPane");
		}
		return staticExecutionPane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getAdaptiveExecutionPane() {
		if (adaptiveExecutionPane == null) {
			adaptiveExecutionPane = new AdaptiveExecutionPane();
			adaptiveExecutionPane.setName("adaptiveExecutionPane");
		}
		return adaptiveExecutionPane;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
