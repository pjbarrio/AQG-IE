package execution.alternatives.visual.alternatives;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;

public class AdaptiveExecutionPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3914881410836963623L;
	private WhenPane whenPane = null;
	private JPanel whatPane = null;
	private JPanel howPane = null;
	private JRadioButton newAlgorithmButton = null;
	private JRadioButton retrainButton = null;
	private JRadioButton reScheduleButton = null;
	private JRadioButton anyButton = null;
	private JRadioButton newQuerySetButton = null;
	private JRadioButton aggregatedQuerySetButton = null;
	private ButtonGroup whatButtonGroup;
	private ButtonGroup howButtonGroup;
	private JPanel collectingStrategy = null;
	private JRadioButton newSampleButton = null;
	private JRadioButton aggregatedSampleButton = null;
	private ButtonGroup buttonGroup;
	private JPanel updateConfiguration = null;
	private JCheckBox fixedEvaluatedDatabase = null;
	private JRadioButton searchDatabaseChange = null;
	private JPanel usableStatisticsPane = null;
	private JRadioButton localUsable = null;
	private JRadioButton globalUsable = null;
	private JRadioButton smartUsable = null;
	private ButtonGroup selectorButton;
	private SchedulerPane schedulerPane = null;
	private ButtonGroup usSt;
	private JPanel evaluationSchedulerPane = null;
	private JCheckBox stopWhileUpdating = null;
	/**
	 * This method initializes 
	 * 
	 */
	public AdaptiveExecutionPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setSize(new Dimension(549, 213));
        this.setBorder(new LineBorder(Color.BLACK));

        this.add(getUpdateConfiguration(), null);
        this.add(getCollectingStrategy(), null);
        this.add(getWhenPane(), null);
        this.add(getWhatPane(), null);
        this.add(getHowPane(), null);
        this.add(getUsableStatisticsPane(), null);
        this.add(getEvaluationSchedulerPane(), null);
		whatButtonGroup = new ButtonGroup();
		whatButtonGroup.add(getRetrainButton());
		whatButtonGroup.add(getNewAlgorithmButton());
		whatButtonGroup.add(getReScheduleButton());
		whatButtonGroup.add(getSearchDatabaseChange());
		whatButtonGroup.add(getanyButton());
		whatButtonGroup.setSelected(getRetrainButton().getModel(), true);
		
		howButtonGroup = new ButtonGroup();
		howButtonGroup.add(getNewQuerySetButton());
		howButtonGroup.add(getAggregatedQuerySetButton());
		howButtonGroup.setSelected(getNewQuerySetButton().getModel(), true);
	
		buttonGroup = new ButtonGroup();
		buttonGroup.add(getNewSampleButton());
		buttonGroup.add(getAggregatedSampleButton());
		buttonGroup.setSelected(getNewSampleButton().getModel(), true);
		
		selectorButton = new ButtonGroup();
		selectorButton.add(getLocalUsable());
		selectorButton.add(getGlobalUsable());
		selectorButton.add(getSmartUsable());
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getWhenPane() {
		if (whenPane == null) {
			whenPane = new WhenPane();
		}
		return whenPane;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getWhatPane() {
		if (whatPane == null) {
			whatPane = new JPanel();
			whatPane.setLayout(new BoxLayout(getWhatPane(), BoxLayout.X_AXIS));
			whatPane.add(getRetrainButton(), null);
			whatPane.add(getNewAlgorithmButton(), null);
			whatPane.add(getReScheduleButton(), null);
			whatPane.add(getSearchDatabaseChange(), null);
			whatPane.add(getanyButton(), null);
		}
		return whatPane;
	}

	/**
	 * This method initializes jContentPane2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getHowPane() {
		if (howPane == null) {
			howPane = new JPanel();
			howPane.setLayout(new BoxLayout(getHowPane(), BoxLayout.X_AXIS));
			howPane.setBorder(new LineBorder(Color.BLACK));
			howPane.add(getNewQuerySetButton(), null);
			howPane.add(getAggregatedQuerySetButton(), null);
		}
		return howPane;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getNewAlgorithmButton() {
		if (newAlgorithmButton == null) {
			newAlgorithmButton = new JRadioButton();
			newAlgorithmButton.setText("New Algorithm");
		}
		return newAlgorithmButton;
	}

	/**
	 * This method initializes jRadioButton1	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRetrainButton() {
		if (retrainButton == null) {
			retrainButton = new JRadioButton();
			retrainButton.setText("Retrain");
		}
		return retrainButton;
	}

	/**
	 * This method initializes jRadioButton2	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getReScheduleButton() {
		if (reScheduleButton == null) {
			reScheduleButton = new JRadioButton();
			reScheduleButton.setText("Query re-schedule");
		}
		return reScheduleButton;
	}

	/**
	 * This method initializes jRadioButton3	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getanyButton() {
		if (anyButton == null) {
			anyButton = new JRadioButton();
			anyButton.setText("Any");
		}
		return anyButton;
	}

	/**
	 * This method initializes jRadioButton4	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getNewQuerySetButton() {
		
		if (newQuerySetButton == null) {
			newQuerySetButton = new JRadioButton();
			newQuerySetButton.setText("New Query Set");
		}
		return newQuerySetButton;
	}

	/**
	 * This method initializes jRadioButton5	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getAggregatedQuerySetButton() {
		if (aggregatedQuerySetButton == null) {
			aggregatedQuerySetButton = new JRadioButton();
			aggregatedQuerySetButton.setText("Aggregated Query Set");
		}
		return aggregatedQuerySetButton;
	}

	/**
	 * This method initializes collectingStrategy	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCollectingStrategy() {
		if (collectingStrategy == null) {
			collectingStrategy = new JPanel();
			collectingStrategy.setLayout(new BoxLayout(getCollectingStrategy(), BoxLayout.X_AXIS));
			collectingStrategy.add(getNewSampleButton(), null);
			collectingStrategy.add(getAggregatedSampleButton(), null);
		}
		return collectingStrategy;
	}

	/**
	 * This method initializes newSampleButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getNewSampleButton() {
		if (newSampleButton == null) {
			newSampleButton = new JRadioButton();
			newSampleButton.setText("New Sample");
		}
		return newSampleButton;
	}

	/**
	 * This method initializes aggregatedSampleButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getAggregatedSampleButton() {
		if (aggregatedSampleButton == null) {
			aggregatedSampleButton = new JRadioButton();
			aggregatedSampleButton.setText("Aggregated Sample");
		}
		return aggregatedSampleButton;
	}

	/**
	 * This method initializes updateConfiguration	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getUpdateConfiguration() {
		if (updateConfiguration == null) {
			updateConfiguration = new JPanel();
			updateConfiguration.setLayout(new BoxLayout(getUpdateConfiguration(), BoxLayout.X_AXIS));
			updateConfiguration.add(getFixedEvaluatedDatabase(), null);
		}
		return updateConfiguration;
	}

	/**
	 * This method initializes fixedEvaluatedDatabase	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getFixedEvaluatedDatabase() {
		if (fixedEvaluatedDatabase == null) {
			fixedEvaluatedDatabase = new JCheckBox();
			fixedEvaluatedDatabase.setText("Fix Evaluable Database");
		}
		return fixedEvaluatedDatabase;
	}

	/**
	 * This method initializes searchDatabaseChange	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getSearchDatabaseChange() {
		if (searchDatabaseChange == null) {
			searchDatabaseChange = new JRadioButton();
			searchDatabaseChange.setText("Search Database Change");
		}
		return searchDatabaseChange;
	}

	/**
	 * This method initializes usableStatisticsPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getUsableStatisticsPane() {
		if (usableStatisticsPane == null) {
			usableStatisticsPane = new JPanel();
			usableStatisticsPane.setLayout(new BoxLayout(getUsableStatisticsPane(), BoxLayout.X_AXIS));
			usableStatisticsPane.add(getLocalUsable(), null);
			usableStatisticsPane.add(getGlobalUsable(), null);
			usableStatisticsPane.add(getSmartUsable(), null);
			
			usSt = new ButtonGroup();
			usSt.add(getLocalUsable());
			usSt.add(getGlobalUsable());
			usSt.add(getSmartUsable());
			
			usSt.setSelected(getLocalUsable().getModel(), true);
		}
		return usableStatisticsPane;
	}

	/**
	 * This method initializes localUsable	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getLocalUsable() {
		if (localUsable == null) {
			localUsable = new JRadioButton();
			localUsable.setText("Local");
		}
		return localUsable;
	}

	/**
	 * This method initializes globalUsable	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getGlobalUsable() {
		if (globalUsable == null) {
			globalUsable = new JRadioButton();
			globalUsable.setText("Global");
		}
		return globalUsable;
	}

	/**
	 * This method initializes smartUsable	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getSmartUsable() {
		if (smartUsable == null) {
			smartUsable = new JRadioButton();
			smartUsable.setText("Smart");
		}
		return smartUsable;
	}

	/**
	 * This method initializes schedulerPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSchedulerPane() {
		if (schedulerPane == null) {
			schedulerPane = new SchedulerPane();
		}
		return schedulerPane;
	}

	/**
	 * This method initializes evaluationSchedulerPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getEvaluationSchedulerPane() {
		if (evaluationSchedulerPane == null) {
			evaluationSchedulerPane = new JPanel();
			evaluationSchedulerPane.setLayout(new BoxLayout(getEvaluationSchedulerPane(), BoxLayout.X_AXIS));
			evaluationSchedulerPane.add(getStopWhileUpdating(), null);
			evaluationSchedulerPane.add(getSchedulerPane(), null);
		}
		return evaluationSchedulerPane;
	}

	/**
	 * This method initializes stopWhileUpdating	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getStopWhileUpdating() {
		if (stopWhileUpdating == null) {
			stopWhileUpdating = new JCheckBox();
			stopWhileUpdating.setText("Stop While Updating");
		}
		return stopWhileUpdating;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
