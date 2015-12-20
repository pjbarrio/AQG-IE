package execution.alternatives.visual.alternatives;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class StaticExecutionPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3378385431427035351L;
	private JLabel jLabel = null;

	/**
	 * This method initializes 
	 * 
	 */
	public StaticExecutionPane() {
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

}
