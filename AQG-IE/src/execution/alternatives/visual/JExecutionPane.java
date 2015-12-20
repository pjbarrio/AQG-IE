package execution.alternatives.visual;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import execution.alternatives.visual.alternatives.AlternativePanel;
import execution.alternatives.visual.alternatives.when.LowPerformancePane;

public class JExecutionPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7218784859120420731L;
	private LowPerformancePane finishStrategyPane = null;
	private AlternativePanel alternativePane = null;
	/**
	 * This method initializes 
	 * 
	 */
	public JExecutionPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setSize(new Dimension(580, 167));
        this.add(getFinishStrategyPane(), null);
        this.add(AlternativePane(), null);
			
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getFinishStrategyPane() {
		if (finishStrategyPane == null) {
			finishStrategyPane = new LowPerformancePane();
		}
		return finishStrategyPane;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel AlternativePane() {
		if (alternativePane == null) {
			alternativePane = new AlternativePanel();
		}
		return alternativePane;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
