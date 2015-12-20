package execution.alternatives.visual.alternatives;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;

import execution.alternatives.visual.alternatives.when.AfterNPane;
import execution.alternatives.visual.alternatives.when.GoodSamplePane;
import execution.alternatives.visual.alternatives.when.LowPerformancePane;

public class WhenPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1433435017180742891L;
	private JPanel optionPane = null;
	private JPanel parametersPane = null;
	private LowPerformancePane lowPerformancePane = null;
	private AfterNPane afterNPane = null;
	private GoodSamplePane goodSamplePane = null;
	private JRadioButton lowPerformanceButton = null;
	private JRadioButton goodSampleButton = null;
	private JRadioButton afterNButton = null;
	private ButtonGroup buttonGroup;

	/**
	 * This method initializes 
	 * 
	 */
	public WhenPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setSize(new Dimension(755, 248));
        this.add(getOptionPane(), null);
        this.add(getParametersPane(), null);
        buttonGroup = new ButtonGroup();
		buttonGroup.add(getLowPerformanceButton());
		buttonGroup.add(getGoodSampleButton());
		buttonGroup.add(getAfterNButton());
		buttonGroup.setSelected(getLowPerformanceButton().getModel(), true);
        this.setBorder(new LineBorder(Color.BLACK));

	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOptionPane() {
		if (optionPane == null) {
			optionPane = new JPanel();
			optionPane.setLayout(new BoxLayout(getOptionPane(), BoxLayout.Y_AXIS));
			optionPane.add(getLowPerformanceButton(), null);
			optionPane.add(getGoodSampleButton(), null);
			optionPane.add(getAfterNButton(), null);
		}
		return optionPane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getParametersPane() {
		if (parametersPane == null) {
			parametersPane = new JPanel();
			parametersPane.setLayout(new CardLayout());
			parametersPane.add(getLowPerformancePane(), getLowPerformancePane().getName());
			parametersPane.add(getGoodSample(), getGoodSample().getName());
			parametersPane.add(getAfterN(), getAfterN().getName());
	        parametersPane.setBorder(new LineBorder(Color.BLACK));

		}
		return parametersPane;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getLowPerformancePane() {
		if (lowPerformancePane == null) {
			lowPerformancePane = new LowPerformancePane();
			lowPerformancePane.setName("lowPerformancePane");
		}
		return lowPerformancePane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getAfterN() {
		if (afterNPane == null) {
			afterNPane = new AfterNPane();
			afterNPane.setName("afterNPane");
		}
		return afterNPane;
	}

	/**
	 * This method initializes jContentPane2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getGoodSample() {
		if (goodSamplePane == null) {
			goodSamplePane = new GoodSamplePane();
			goodSamplePane.setName("goodSamplePane");
		}
		return goodSamplePane;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getLowPerformanceButton() {
		if (lowPerformanceButton == null) {
			lowPerformanceButton = new JRadioButton();
			lowPerformanceButton.setText("Low Performance");
			lowPerformanceButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					((CardLayout)getParametersPane().getLayout()).show(getParametersPane(), getLowPerformancePane().getName());
				}
			});
		}
		return lowPerformanceButton;
	}

	/**
	 * This method initializes jRadioButton1	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getGoodSampleButton() {
		if (goodSampleButton == null) {
			goodSampleButton = new JRadioButton();
			goodSampleButton.setText("Good Gathered Sample");
			goodSampleButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					((CardLayout)getParametersPane().getLayout()).show(getParametersPane(), getGoodSample().getName());
				}
			});
		}
		return goodSampleButton;
	}

	/**
	 * This method initializes jRadioButton2	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getAfterNButton() {
		if (afterNButton == null) {
			afterNButton = new JRadioButton();
			afterNButton.setText("After N Tokens");
			afterNButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					((CardLayout)getParametersPane().getLayout()).show(getParametersPane(), getAfterN().getName());
				}
			});
		}
		return afterNButton;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
