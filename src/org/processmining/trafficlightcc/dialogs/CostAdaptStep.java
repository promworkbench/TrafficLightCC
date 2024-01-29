package org.processmining.trafficlightcc.dialogs;

import java.awt.Color;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

/**
 * Configure if the specified alignment cost functions should be automatically
 * adapted to resolve practical non-determinism problems.
 * @author brockhoff
 *
 */
public class CostAdaptStep extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2584310212364639573L;

	final JRadioButton rBtnCostYes;
	final JRadioButton rBtnCostNo;

	public CostAdaptStep() {
		// init instance
		SlickerFactory slickerFactory = SlickerFactory.instance();
		
		double mainPanelSize[][] = { { TableLayoutConstants.FILL }, { 80, 40, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(mainPanelSize));

		// Background color
		this.setBackground(new Color(200, 200, 200));
		
		this.add(slickerFactory.createLabel(
				"<html><h1>Configure Costs</h1>"
				+ "<p>Do you want to automatically adapt alignment costs?"),
			"0, 0, l, t");
		
		rBtnCostYes = slickerFactory.createRadioButton("Yes");
		rBtnCostNo = slickerFactory.createRadioButton("No");
		rBtnCostYes.setSelected(true);
		rBtnCostNo.setSelected(false);
		//DefActListener defaultAction = new DefActListener(context, fitnessRB, fitnessCompleteRB, net, log, mapping);

		ButtonGroup rGroupCost = new ButtonGroup();
		//fitnessRB.addActionListener(defaultAction);
		//behavAppRB.addActionListener(defaultAction);
		rGroupCost.add(rBtnCostYes);
		rGroupCost.add(rBtnCostNo);
		this.add(rBtnCostYes, "0, 1, l, t");
		this.add(rBtnCostNo, "0, 2, l, t");
	}
	
	public CostAdaptation getCostAdaptation() {
		if (rBtnCostYes.isSelected()) {
			return CostAdaptation.YES;
		}
		else {
			return CostAdaptation.NO;
		}
	}
}
