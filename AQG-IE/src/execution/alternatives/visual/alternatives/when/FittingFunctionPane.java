package execution.alternatives.visual.alternatives.when;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class FittingFunctionPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1861017182153196506L;
	private JPanel numberOfDocumentsPane = null;
	private JPanel wordsDistributionPane = null;
	private JPanel words = null;

	/**
	 * This method initializes 
	 * 
	 */
	public FittingFunctionPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setSize(new Dimension(587, 189));
        this.setBorder(new LineBorder(Color.BLACK));
        this.add(getNumberOfDocumentsPane(), null);
        this.add(getWordsDistributionPane(), null);
        this.add(getWords(), null);
			
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getNumberOfDocumentsPane() {
		if (numberOfDocumentsPane == null) {
			numberOfDocumentsPane = new CheckedTextPane("Documents");
		}
		return numberOfDocumentsPane;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getWordsDistributionPane() {
		if (wordsDistributionPane == null) {
			wordsDistributionPane = new WordsDistributionPane();
		}
		return wordsDistributionPane;
	}

	/**
	 * This method initializes words	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getWords() {
		if (words == null) {
			words = new CheckedTextPane("Words");
		}
		return words;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
