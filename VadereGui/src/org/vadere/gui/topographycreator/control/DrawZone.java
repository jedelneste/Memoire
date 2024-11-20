package org.vadere.gui.topographycreator.control;

import org.vadere.gui.components.control.DefaultSelectionMode;
import org.vadere.gui.components.utils.Messages;
import org.vadere.gui.projectview.view.ProjectView;
import org.vadere.gui.topographycreator.model.IDrawPanelModel;
import org.vadere.gui.topographycreator.view.ActionPedestrianDensityZoneDialog;
import org.vadere.gui.topographycreator.view.TopographyWindow;
import org.vadere.util.geometry.GrahamScan;
import org.vadere.util.geometry.shapes.VLine;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VPolygon;

import javax.swing.*;
import javax.swing.undo.UndoableEditSupport;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class DrawZone extends DrawConvexHullMode {


    private VPolygon polygon;
    private DrawPathState state = DrawPathState.START;
    private final UndoableEditSupport undoSupport;
    private Path2D.Double path;
    private Line2D.Double line;
    private int lineCount = 0;
    private List<VPoint> points;


    private IDrawPanelModel panelModel;

    public DrawZone(IDrawPanelModel panelModel, final UndoableEditSupport undoSupport) {
        super(panelModel, undoSupport);
        this.panelModel = panelModel;
        this.undoSupport = undoSupport;
        this.polygon = null;
        this.points = new ArrayList<>();

    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event) && state == DrawPathState.START) {
            super.mouseDragged(event);
        } else {
            mouseMoved(event);
        }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event) && state == DrawPathState.START) {
            super.mousePressed(event);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event) && state == DrawPathState.START) {
            super.mouseReleased(event);
        }
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        if (!SwingUtilities.isRightMouseButton(event)){
            if (SwingUtilities.isLeftMouseButton(event)){
                if (event.getClickCount() <= 1 && state == DrawPathState.START){
                    panelModel.setMousePosition(event.getPoint());
                    panelModel.setStartSelectionPoint(event.getPoint());
                    path = new Path2D.Double(Path2D.WIND_NON_ZERO);
                    path.moveTo(panelModel.getMousePosition().x, panelModel.getMousePosition().y);
                    path.lineTo(panelModel.getMousePosition().x, panelModel.getMousePosition().y);
                    line = new VLine(panelModel.getMousePosition().x, panelModel.getMousePosition().y,
                            panelModel.getMousePosition().x, panelModel.getMousePosition().y);

                    panelModel.setSelectionShape(new VPolygon(path));
                    points.add(panelModel.getMousePosition());

                    state = DrawPathState.ADD;
                    panelModel.showSelection();
                } else if (event.getClickCount() <= 1 && state == DrawPathState.ADD){
                    path.lineTo(line.x2, line.y2);
                    points.add(new VPoint(line.x2, line.y2));

                    if (lineCount <= 1) {
                        // dirty trick to see the first line!
                        VPolygon poly = new VPolygon(path);
                        poly.moveTo(line.x2, line.y2 + 0.0001 * panelModel.getScaleFactor());
                        panelModel.setSelectionShape(poly);
                    } else {
                        GrahamScan scan = new GrahamScan(points);
                        panelModel.setSelectionShape(scan.getPolytope());
                    }
                    line = new Line2D.Double(panelModel.getMousePosition().x, panelModel.getMousePosition().y,
                            panelModel.getMousePosition().x, panelModel.getMousePosition().y);
                    panelModel.showSelection();
                    lineCount++;
                } else {
                    panelModel.hideSelection();
                    if (lineCount <= 1) {
                        // panelModel.deleteLastShape();
                    } else {
                        path.closePath();
                        GrahamScan scan = new GrahamScan(points);
                        panelModel.setSelectionShape(scan.getPolytope());

                        if (scan.isPolytope()) {
                            this.polygon = scan.getPolytope();
                            ActionPlacePedestrianDensityZone actionPed = new ActionPlacePedestrianDensityZone(Messages.getString("TopographyCreator.PlacePedestrianDensityZone.label"), panelModel);
                            actionPed.setPolygon(polygon);
                            actionPed.actionPerformed(null);

                        }
                        panelModel.notifyObservers();
                    }

                    state = DrawPathState.START;
                    lineCount = 0;
                    points.clear();
                }

            }
        } else {
            if (state == DrawPathState.ADD) {
                panelModel.hideSelection();
                state = DrawPathState.START;
                lineCount = 0;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);

        if (state == DrawPathState.ADD) {
            VPoint cursorPosition = panelModel.getMousePosition();
            line.x2 = cursorPosition.x;
            line.y2 = cursorPosition.y;

            if (points.size() >= 2) {
                List<VPoint> cloneList = new ArrayList<>(points);
                cloneList.add(new VPoint(line.x2, line.y2));
                GrahamScan scan = new GrahamScan(cloneList);
                panelModel.setSelectionShape(scan.getPolytope());
            } else {
                VPolygon poly = new VPolygon(path);
                poly.append(line, false);
                // poly.lineTo(line.x2, line.y2);
                panelModel.setSelectionShape(poly);
            }
        }
    }


    public List<VPoint> getPoints(){
        return points;
    }

    public boolean isZoneValid(){
        return (this.polygon != null);
    }

    public VPolygon getPolygon(){
        if (isZoneValid()) {
            return this.polygon;
        } else {
            return null;
        }
    }
}
