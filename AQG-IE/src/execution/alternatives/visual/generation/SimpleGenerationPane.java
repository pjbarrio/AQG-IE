package execution.alternatives.visual.generation;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class SimpleGenerationPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8775939294418087720L;
	private JLabel jLabel = null;

	/**
	 * This method initializes 
	 * 
	 */
	public SimpleGenerationPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        jLabel = new JLabel();
        jLabel.setText("No More parameters are required");
        this.add(jLabel, null);
        this.setBorder(new LineBorder(Color.BLACK));
			
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
