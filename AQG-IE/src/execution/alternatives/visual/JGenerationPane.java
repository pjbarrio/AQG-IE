package execution.alternatives.visual;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;

import execution.alternatives.visual.generation.CombinedGenerationPane;
import execution.alternatives.visual.generation.SimpleGenerationPane;

public class JGenerationPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1548788027727708349L;
	private JPanel jGenerationPane = null;
	private ButtonGroup buttonGroup;
	private JRadioButton combinedButton = null;
	private JRadioButton simpleButton = null;
	private JPanel individualizedPane = null;
	private SimpleGenerationPane SimpleGenerationPane;
	private CombinedGenerationPane CombinedGenerationPane;
	private JPanel algorithmPane = null;
	private JPanel parametersPane = null;
	private JCheckBox jCheckBox = null;
	private JCheckBox jCheckBox1 = null;
	private JCheckBox jCheckBox2 = null;
	private JCheckBox jCheckBox3 = null;
	private JCheckBox jCheckBox4 = null;
	private JCheckBox jCheckBox5 = null;
	private JCheckBox jCheckBox6 = null;
	private JCheckBox jCheckBox7 = null;

	/**
	 * This method initializes 
	 * 
	 */
	public JGenerationPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setSize(new Dimension(778, 90));
        this.setBorder(new LineBorder(Color.BLACK));
        this.add(getAlgorithmPane(), null);
        this.add(getParametersPane(), null);
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getGenerationtPane() {
		if (jGenerationPane == null) {
			jGenerationPane = new JPanel();
			jGenerationPane.setLayout(new BoxLayout(getGenerationtPane(), BoxLayout.Y_AXIS));
			jGenerationPane.add(getSimpleButton(), null);
			jGenerationPane.add(getCombinedButton(), null);
			
			buttonGroup = new ButtonGroup();
			buttonGroup.add(getSimpleButton());
			buttonGroup.add(getCombinedButton());
			buttonGroup.setSelected(getSimpleButton().getModel(), true);
		}
		return jGenerationPane;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getCombinedButton() {
		if (combinedButton == null) {
			combinedButton = new JRadioButton();
			combinedButton.setText("Combined");
			combinedButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					((CardLayout)(getIndividualizedPane().getLayout())).show(getIndividualizedPane(), getCombinedGenerationPane().getName());
				
				}
			});
		}
		return combinedButton;
	}

	protected JPanel getCombinedGenerationPane() {
		if (CombinedGenerationPane == null) {
			CombinedGenerationPane = new CombinedGenerationPane();
			CombinedGenerationPane.setName("CombinedGenerationPane");
		}
		return CombinedGenerationPane;
	}

	/**
	 * This method initializes jRadioButton1	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getSimpleButton() {
		if (simpleButton == null) {
			simpleButton = new JRadioButton();
			simpleButton.setText("Simple");
			simpleButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					((CardLayout)(getIndividualizedPane().getLayout())).show(getIndividualizedPane(), getSimpleGenerationPane().getName());
				}
			});
		}
		return simpleButton;
	}

	protected JPanel getSimpleGenerationPane() {
		if (SimpleGenerationPane == null) {
			SimpleGenerationPane = new SimpleGenerationPane();
			SimpleGenerationPane.setName("SimpleGenerationPane");
		}
		return SimpleGenerationPane;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getIndividualizedPane() {
		if (individualizedPane == null) {
			individualizedPane = new JPanel();
			individualizedPane.setLayout(new CardLayout());
			individualizedPane.setBorder(new LineBorder(Color.BLACK));
			individualizedPane.add(getSimpleGenerationPane(), getSimpleGenerationPane().getName());
			individualizedPane.add(getCombinedGenerationPane(), getCombinedGenerationPane().getName());
		}
		return individualizedPane;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getAlgorithmPane() {
		if (algorithmPane == null) {
			algorithmPane = new JPanel();
			algorithmPane.setLayout(new BoxLayout(getAlgorithmPane(), BoxLayout.X_AXIS));
			algorithmPane.add(getJCheckBox1(), null);
			algorithmPane.add(getJCheckBox(), null);
			algorithmPane.add(getJCheckBox2(), null);
			algorithmPane.add(getJCheckBox4(), null);
			algorithmPane.add(getJCheckBox3(), null);
			algorithmPane.add(getJCheckBox5(), null);
			algorithmPane.add(getJCheckBox6(), null);
			algorithmPane.add(getJCheckBox7(), null);
			
		}
		return algorithmPane;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getParametersPane() {
		if (parametersPane == null) {
			parametersPane = new JPanel();
			parametersPane.setLayout(new BoxLayout(getParametersPane(), BoxLayout.X_AXIS));
			parametersPane.setBorder(new LineBorder(Color.BLACK));

			parametersPane.add(getGenerationtPane(), null);
			parametersPane.add(getIndividualizedPane(), null);
			
		}
		return parametersPane;
	}

	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setText("MSC");
		}
		return jCheckBox;
	}

	/**
	 * This method initializes jCheckBox1	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox1() {
		if (jCheckBox1 == null) {
			jCheckBox1 = new JCheckBox();
			jCheckBox1.setText("Incremental");
		}
		return jCheckBox1;
	}

	/**
	 * This method initializes jCheckBox2	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox2() {
		if (jCheckBox2 == null) {
			jCheckBox2 = new JCheckBox();
			jCheckBox2.setText("Optimistic");
		}
		return jCheckBox2;
	}

	/**
	 * This method initializes jCheckBox3	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox3() {
		if (jCheckBox3 == null) {
			jCheckBox3 = new JCheckBox();
			jCheckBox3.setText("Ripper");
		}
		return jCheckBox3;
	}

	/**
	 * This method initializes jCheckBox4	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox4() {
		if (jCheckBox4 == null) {
			jCheckBox4 = new JCheckBox();
			jCheckBox4.setText("QProber");
		}
		return jCheckBox4;
	}

	/**
	 * This method initializes jCheckBox5	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox5() {
		if (jCheckBox5 == null) {
			jCheckBox5 = new JCheckBox();
			jCheckBox5.setText("Tuples");
		}
		return jCheckBox5;
	}

	/**
	 * This method initializes jCheckBox6	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox6() {
		if (jCheckBox6 == null) {
			jCheckBox6 = new JCheckBox();
			jCheckBox6.setText("Any");
		}
		return jCheckBox6;
	}

	/**
	 * This method initializes jCheckBox7	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox7() {
		if (jCheckBox7 == null) {
			jCheckBox7 = new JCheckBox();
			jCheckBox7.setText("All");
			jCheckBox7.setSelected(true);
		}
		return jCheckBox7;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
