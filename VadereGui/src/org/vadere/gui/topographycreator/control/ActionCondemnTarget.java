package org.vadere.gui.topographycreator.control;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import org.vadere.gui.components.control.IMode;
import org.vadere.gui.projectview.view.ProjectView;
import org.vadere.gui.topographycreator.model.IDrawPanelModel;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;

import javax.swing.*;
import javax.swing.undo.UndoableEditSupport;

/**
 *
 * 
 *
 */
public class ActionCondemnTarget extends TopographyAction {

	private static final long serialVersionUID = -5926543529036212640L;
    IDrawPanelModel panelModel;

    private final UndoableEditSupport undoSupport;

	public ActionCondemnTarget(final String name, final String iconPath, final IDrawPanelModel<?> panelModel, UndoableEditSupport undoSupport) {
		super(name, iconPath, panelModel);
        this.panelModel = panelModel;
        this.undoSupport = undoSupport;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        IMode mode = new SelectElementMode(panelModel, undoSupport);
        getScenarioPanelModel().setMouseSelectionMode(mode);
        Topography topography = panelModel.getTopography();
        List<Target> targets = topography.getTargets();
        ScenarioElement element = getScenarioPanelModel().getSelectedElement();
        if (element.getClass() == Target.class){
            Target target = (Target) element;
            if (target.isAvailable()){
                target.setAvailable(false);
                for (Pedestrian pedestrian : topography.getElements(Pedestrian.class)) {
                    pedestrian.setNearestTarget(targets);
                }
            }
            getScenarioPanelModel().notifyObservers();
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Sortie de secours condamnée ! "));
            JOptionPane.showMessageDialog(ProjectView.getMainWindow(), panel, "Information", JOptionPane.INFORMATION_MESSAGE);

        }
	}


}
