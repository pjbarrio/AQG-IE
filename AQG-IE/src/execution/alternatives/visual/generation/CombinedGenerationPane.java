package execution.alternatives.visual.generation;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import execution.alternatives.visual.JSourcePane;
import exploration.model.enumerations.RecommenderEnum;
import exploration.model.enumerations.UserNeighborhoodEnum;
import exploration.model.enumerations.UserSimilarityEnum;


public class CombinedGenerationPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8537005522650570425L;
	private JPanel configurationPane = null;
	private JCheckBox jCheckBox = null;
	private JCheckBox jCheckBox1 = null;
	private JSourcePane sourcePane = null;
	private JPanel origin = null;
	private JPanel parameters = null;
	private JCheckBox preserveOrder = null;
	private JPanel neighbors = null;
	private JLabel jLabel = null;
	private JTextField neighborsText = null;
	private JComboBox userSimilarity = null;
	private JComboBox userNeighborhood = null;
	private JComboBox recommender = null;

	/**
	 * This method initializes 
	 * 
	 */
	public CombinedGenerationPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setSize(new Dimension(653, 113));
        this.add(getOrigin(), null);
        this.add(getParameters(), null);
			
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getConfigurationPane() {
		if (configurationPane == null) {
			configurationPane = new JPanel();
			configurationPane.setLayout(new BoxLayout(getConfigurationPane(), BoxLayout.X_AXIS));
			configurationPane.add(getJCheckBox(), null);
			configurationPane.add(getJCheckBox1(), null);
		}
		return configurationPane;
	}

	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setText("Coverage");
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
			jCheckBox1.setText("Specificity");
		}
		return jCheckBox1;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSourcePane() {
		if (sourcePane == null) {
			sourcePane = new JSourcePane();
		}
		return sourcePane;
	}

	/**
	 * This method initializes origin	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOrigin() {
		if (origin == null) {
			origin = new JPanel();
			origin.setLayout(new BoxLayout(getOrigin(), BoxLayout.Y_AXIS));
			origin.add(getSourcePane(), null);
			origin.add(getConfigurationPane(), null);
		}
		return origin;
	}

	/**
	 * This method initializes parameters	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getParameters() {
		if (parameters == null) {
			parameters = new JPanel();
			parameters.setLayout(new BoxLayout(getParameters(), BoxLayout.Y_AXIS));
			parameters.add(getPreserveOrder(), null);
			parameters.add(getNeighbors(), null);
			parameters.add(getUserSimilarity(), null);
			parameters.add(getUserNeighborhood(), null);
			parameters.add(getRecommender(), null);
			parameters.setBorder(new LineBorder(Color.BLACK));
			
		}
		return parameters;
	}

	/**
	 * This method initializes preserveOrder	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getPreserveOrder() {
		if (preserveOrder == null) {
			preserveOrder = new JCheckBox();
			preserveOrder.setText("Preserve Order");
		}
		return preserveOrder;
	}

	/**
	 * This method initializes neighbors	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getNeighbors() {
		if (neighbors == null) {
			jLabel = new JLabel();
			jLabel.setText("Number of Neighbors");
			neighbors = new JPanel();
			neighbors.setLayout(new BoxLayout(getNeighbors(), BoxLayout.X_AXIS));
			neighbors.add(jLabel, null);
			neighbors.add(getNeighborsText(), null);
		}
		return neighbors;
	}

	/**
	 * This method initializes neighborsText	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getNeighborsText() {
		if (neighborsText == null) {
			neighborsText = new JTextField();
		}
		return neighborsText;
	}

	/**
	 * This method initializes userSimilarity	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getUserSimilarity() {
		if (userSimilarity == null) {
			userSimilarity = new JComboBox(UserSimilarityEnum.values());
		}
		return userSimilarity;
	}

	/**
	 * This method initializes userNeighborhood	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getUserNeighborhood() {
		if (userNeighborhood == null) {
			userNeighborhood = new JComboBox(UserNeighborhoodEnum.values());
		}
		return userNeighborhood;
	}

	/**
	 * This method initializes recommender	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getRecommender() {
		
		if (recommender == null) {
			
			recommender = new JComboBox(RecommenderEnum.values());
		
		}
		
		return recommender;
		
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
