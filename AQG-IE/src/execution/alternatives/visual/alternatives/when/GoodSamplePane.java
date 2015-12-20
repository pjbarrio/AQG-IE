package execution.alternatives.visual.alternatives.when;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class GoodSamplePane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7441029671648793585L;
	private FittingFunctionPane fittingFunctionPane = null;
	private ButtonGroup buttonGroup;  //  @jve:decl-index=0:

	/**
	 * This method initializes 
	 * 
	 */
	public GoodSamplePane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setSize(new Dimension(473, 213));
        this.add(getFittingFunctionPane(), null);
        this.setBorder(new LineBorder(Color.BLACK));
		
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getFittingFunctionPane() {
		if (fittingFunctionPane == null) {
			fittingFunctionPane = new FittingFunctionPane();
		}
		return fittingFunctionPane;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
