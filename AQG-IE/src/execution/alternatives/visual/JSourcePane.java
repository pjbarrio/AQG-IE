package execution.alternatives.visual;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.SimilarityFunctionEnum;

public class JSourcePane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -203522684368648850L;
	private JRadioButton similarButton = null;
	private JRadioButton localButton = null;
	private JRadioButton clusterButton = null;
	private JRadioButton globalButton = null;
	private ButtonGroup buttonGroup;
	private JPanel similarPane = null;
	private JPanel clusterPane = null;
	private JComboBox similarFunction = null;
	private JComboBox clusterFunction = null;

	/**
	 * This method initializes 
	 * 
	 */
	public JSourcePane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setSize(new Dimension(543, 224));
		this.add(getLocalButton(), null);
		this.add(getSimilarPane(), null);
		this.add(getClusterPane(), null);
		this.add(getGlobalButton(), null);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(getLocalButton());
		buttonGroup.add(getGlobalButton());
		buttonGroup.add(getClusterButton());
		buttonGroup.add(getSimilarButton());
		buttonGroup.setSelected(getLocalButton().getModel(), true);
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getSimilarButton() {
		if (similarButton == null) {
			similarButton = new JRadioButton();
			similarButton.setText("Similar");
		}
		return similarButton;
	}

	/**
	 * This method initializes jRadioButton1	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getLocalButton() {
		if (localButton == null) {
			localButton = new JRadioButton();
			localButton.setText("Local");
		}
		return localButton;
	}

	/**
	 * This method initializes jRadioButton2	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getClusterButton() {
		if (clusterButton == null) {
			clusterButton = new JRadioButton();
			clusterButton.setText("Cluster");
		}
		return clusterButton;
	}

	/**
	 * This method initializes jRadioButton3	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getGlobalButton() {
		if (globalButton == null) {
			globalButton = new JRadioButton();
			globalButton.setText("Global");
		}
		return globalButton;
	}

	/**
	 * This method initializes similarPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSimilarPane() {
		if (similarPane == null) {
			similarPane = new JPanel();
			similarPane.setLayout(new BoxLayout(getSimilarPane(), BoxLayout.Y_AXIS));
			similarPane.add(getSimilarButton(), null);
			similarPane.add(getSimilarFunction(), null);
		}
		return similarPane;
	}

	/**
	 * This method initializes clusterPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getClusterPane() {
		if (clusterPane == null) {
			clusterPane = new JPanel();
			clusterPane.setLayout(new BoxLayout(getClusterPane(), BoxLayout.Y_AXIS));
			clusterPane.add(getClusterButton(), null);
			clusterPane.add(getClusterFunction(), null);
		}
		return clusterPane;
	}

	/**
	 * This method initializes similarFunction	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getSimilarFunction() {
		if (similarFunction == null) {
			similarFunction = new JComboBox(SimilarityFunctionEnum.values());
		}
		return similarFunction;
	}

	/**
	 * This method initializes clusterFunction	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getClusterFunction() {
		if (clusterFunction == null) {
			clusterFunction = new JComboBox(ClusterFunctionEnum.values());
		}
		return clusterFunction;
	}

}  //  @jve:decl-index=0:visual-constraint="10,2"
