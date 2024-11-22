package org.vadere.gui.topographycreator.control;

import org.vadere.gui.topographycreator.model.IDrawPanelModel;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VPolygon;
import org.vadere.util.logging.Logger;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

public class ActionDeletePedestrianZone extends TopographyAction{

    private IDrawPanelModel panelModel;
    private static final Logger logger = Logger.getLogger(ActionDeletePedestrianZone.class);

    private VPolygon polygon;

    private UndoableEditSupport undoSupport;

    public ActionDeletePedestrianZone(String name, IDrawPanelModel<?> panelModel, UndoableEditSupport undoSupport) {
        super(name, panelModel);
        this.panelModel = panelModel;
        this.undoSupport = undoSupport;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Topography topography = panelModel.getTopography();
        Collection<Pedestrian> pedestrians = topography.getPedestrianDynamicElements().getInitialElements();
        if (!pedestrians.isEmpty()) {
            for (Pedestrian pedestrian : pedestrians) {
                VPoint position = pedestrian.getPosition();
                if (this.polygon.contains(position)) {
                    topography.removeElement(pedestrian);
                }
            }
            getScenarioPanelModel().notifyObservers();
        }
    }

    public void setPolygon(VPolygon polygon) {
        this.polygon = polygon;
    }
}
