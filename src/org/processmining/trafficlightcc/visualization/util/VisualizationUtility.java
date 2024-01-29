package org.processmining.trafficlightcc.visualization.util;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.framework.util.ui.scalableview.interaction.ZoomInteractionPanel;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;

import com.fluxicon.slickerbox.ui.SlickerScrollBarUI;

public class VisualizationUtility {

	public static JScrollPane wrapInProMStyleScrollPane(JComponent comp) {
		JScrollPane vscrollPane = null;
		if (comp instanceof ProMJGraph) {

			//TODO this ProMJGraphPanel is really urgely, find another way to fit and scale
			ProMJGraphPanel panel = new ProMJGraphPanel((ProMJGraph) comp);
			//		panel.setSize(300, 300);
			panel.addViewInteractionPanel(new ZoomInteractionPanel(panel, ScalableViewPanel.MAX_ZOOM),
					SwingConstants.WEST);
			panel.scaleToFit();
			vscrollPane = new JScrollPane(panel);
		} else {
			vscrollPane = new JScrollPane(comp);
		}
		vscrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		vscrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		vscrollPane.setOpaque(true);
		vscrollPane.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		vscrollPane.getViewport().setOpaque(true);
		vscrollPane.getViewport().setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		vscrollPane.setBorder(BorderFactory.createEmptyBorder());

		JScrollBar vBar = vscrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
				WidgetColors.COLOR_NON_FOCUS, 4, 12));
		vBar.setOpaque(true);
		vBar.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);

		JScrollBar hBar = vscrollPane.getHorizontalScrollBar();
		hBar.setUI(new SlickerScrollBarUI(hBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
				WidgetColors.COLOR_NON_FOCUS, 4, 12));
		hBar.setOpaque(true);
		hBar.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		return vscrollPane;
	}
}
