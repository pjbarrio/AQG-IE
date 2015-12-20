package execution.alternatives.visual;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class AlternativesGenerator {

	private JFrame jFrame = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private JPanel jContentPane = null;
	private JMenuBar jJMenuBar = null;
	private JMenu fileMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JPanel jSourcePane = null;
	private JPanel jVersionPane = null;
	private JPanel jGenerationPane = null;
	private JPanel jExecutionPane = null;
	private JPanel contraintPane = null;
	private JPanel configurationPane = null;
	private JPanel configPane = null;
	private JPanel workload = null;
	private JList jList = null;
	private JScrollPane jScrollPane = null;
	private JPanel ExperimentTypePane = null;
	private JRadioButton evaluationButton = null;
	private JPanel ExecutionPane = null;
	private JRadioButton executionButton = null;
	private JTextField paralellExecutionField = null;
	private ButtonGroup experimentGroup;
	/**
	 * This method initializes jFrame
	 * 
	 * @return javax.swing.JFrame
	 */
	private JFrame getJFrame() {
		if (jFrame == null) {
			jFrame = new JFrame();
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setJMenuBar(getJJMenuBar());
			jFrame.setSize(1200, 660);
			jFrame.setContentPane(getJContentPane());
			jFrame.setTitle("Application");
		}
		return jFrame;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BoxLayout(getJContentPane(), BoxLayout.Y_AXIS));
			jContentPane.add(getJContentPane14(), null);
			jContentPane.add(getJContentPane1(), null);
			jContentPane.add(getJContentPane12(), null);
			jContentPane.add(getJContentPane13(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getFileMenu());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("File");
			fileMenu.add(getSaveMenuItem());
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			exitMenuItem.setText("Exit");
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
		}
		return exitMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem();
			saveMenuItem.setText("Save");
			saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
					Event.CTRL_MASK, true));
		}
		return saveMenuItem;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSourcePane() {
		if (jSourcePane == null) {
			jSourcePane = new JSourcePane();
		}
		return jSourcePane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJVersionPane() {
		if (jVersionPane == null) {
			jVersionPane = new JVersionPane();
		}
		return jVersionPane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane1() {
		if (jGenerationPane == null) {
			jGenerationPane = new JGenerationPane();
		}
		return jGenerationPane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane12() {
		if (jExecutionPane == null) {
			jExecutionPane = new JExecutionPane();
		}
		return jExecutionPane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane13() {
		if (contraintPane == null) {
			contraintPane = new ConstraintPane();
		}
		return contraintPane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane14() {
		if (configurationPane == null) {
			configurationPane = new JPanel();
			configurationPane.setLayout(new BoxLayout(getJContentPane14(), BoxLayout.X_AXIS));
			configurationPane.add(getJContentPane15(), null);
			configurationPane.add(getJContentPane16(), null);
			configurationPane.add(getExperimentTypePane(), null);
		}
		return configurationPane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane15() {
		if (configPane == null) {
			configPane = new JPanel();
			configPane.setLayout(new BoxLayout(getJContentPane15(), BoxLayout.Y_AXIS));
			configPane.add(getSourcePane(), null);
			configPane.add(getJVersionPane(), null);
		}
		return configPane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane16() {
		if (workload == null) {
			workload = new JPanel();
			workload.setLayout(new BoxLayout(getJContentPane16(), BoxLayout.X_AXIS));
			workload.add(getJScrollPane(), null);
		}
		return workload;
	}

	/**
	 * This method initializes jList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJList() {
		if (jList == null) {

			jList = new JList(loadListOfTuples());
			jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jList.setLayoutOrientation(JList.VERTICAL);
			jList.setVisibleRowCount(3);
			
		}
		return jList;
	}

	private ListModel loadListOfTuples() {
		
		DefaultListModel lm = new DefaultListModel();
		
		lm.addElement("Natural Disasters");
		lm.addElement("Car Accidents");
		lm.addElement("Join Ventures");
		lm.addElement("Stock changes");
		
		return lm;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJList());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes ExperimentTypePane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getExperimentTypePane() {
		if (ExperimentTypePane == null) {
			ExperimentTypePane = new JPanel();
			ExperimentTypePane.setLayout(new BoxLayout(getExperimentTypePane(), BoxLayout.X_AXIS));
			ExperimentTypePane.add(getEvaluationButton(), null);
			ExperimentTypePane.add(getExecutionPane(), null);
			
			experimentGroup = new ButtonGroup();
			experimentGroup.add(getEvaluationButton());
			experimentGroup.add(getExecutionButton());
			
			
			
		}
		return ExperimentTypePane;
	}

	/**
	 * This method initializes evaluationButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getEvaluationButton() {
		if (evaluationButton == null) {
			evaluationButton = new JRadioButton();
			evaluationButton.setText("Evaluation");
		}
		return evaluationButton;
	}

	/**
	 * This method initializes ExecutionPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getExecutionPane() {
		if (ExecutionPane == null) {
			ExecutionPane = new JPanel();
			ExecutionPane.setLayout(new BoxLayout(getExecutionPane(), BoxLayout.Y_AXIS));
			ExecutionPane.add(getExecutionButton(), null);
			ExecutionPane.add(getParalellExecutionField(), null);
		}
		return ExecutionPane;
	}

	/**
	 * This method initializes executionButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getExecutionButton() {
		if (executionButton == null) {
			executionButton = new JRadioButton();
			executionButton.setText("Execution");
		}
		return executionButton;
	}

	/**
	 * This method initializes paralellExecutionField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getParalellExecutionField() {
		if (paralellExecutionField == null) {
			paralellExecutionField = new JTextField();
		}
		return paralellExecutionField;
	}

	/**
	 * Launches this application
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AlternativesGenerator application = new AlternativesGenerator();
				application.getJFrame().setVisible(true);
			}
		});
	}

}
