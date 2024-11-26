package org.vadere.gui.topographycreator.control;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.jetbrains.annotations.NotNull;
import org.vadere.gui.components.utils.Messages;
import org.vadere.gui.projectview.view.ProjectView;
import org.vadere.gui.projectview.view.VDialogManager;
import org.vadere.gui.topographycreator.model.AgentWrapper;
import org.vadere.gui.topographycreator.model.IDrawPanelModel;
import org.vadere.gui.topographycreator.view.ActionPedestrianDensityZoneDialog;
import org.vadere.gui.topographycreator.view.ActionRandomPedestrianDialog;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.psychology.cognition.GroupMembership;
import org.vadere.state.scenario.Agent;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.GrahamScan;
import org.vadere.util.geometry.shapes.*;
import org.vadere.util.logging.Logger;
import org.vadere.util.random.SimpleReachablePointProvider;

import javax.swing.*;
import javax.swing.undo.UndoableEditSupport;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ActionPlacePedestrianDensityZone extends TopographyAction{

    private IDrawPanelModel panelModel;
    private static final Logger logger = Logger.getLogger(ActionPlacePedestrianDensityZone.class);
    private static final int BINOMIAL_DISTRIBUTION_SUCCESS_VALUE = 1;

    private final double agentRadius;

    private VPolygon polygon;
    private double density;


    public ActionPlacePedestrianDensityZone(String name, IDrawPanelModel<?> panelModel) {
        super(name, panelModel);
        this.panelModel = panelModel;
        this.agentRadius = new AttributesAgent().getRadius();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ActionPedestrianDensityZoneDialog dialog = new ActionPedestrianDensityZoneDialog(panelModel);
        if (dialog.showDialog() && dialog.isValid()){
            Topography topography = getScenarioPanelModel().getTopography();

            Random random = dialog.getRandom();
            Rectangle2D.Double legalBound = polygon.getEnclosingRectangle();
            SimpleReachablePointProvider pointProvider = SimpleReachablePointProvider.uniform(random, legalBound, topography.getObstacleDistanceFunction());

            double groupMembershipRatio = 0.5;
            BinomialDistribution binomialDistribution = new BinomialDistribution(BINOMIAL_DISTRIBUTION_SUCCESS_VALUE, groupMembershipRatio);

            this.density = dialog.getPedestrianDensity();

            int firstPedId = dialog.getFirstPedId();
            int numOfPeds = computeNumberOfPeds(dialog);
            int createdPeds = 0;
            int iter = 0;
            int maxIter = Math.min(1000*numOfPeds, 100000);

            while (createdPeds < numOfPeds && iter < maxIter) {
                IPoint point = pointProvider.stream(dist ->  dist > 0.25).findFirst().get();
                if(polygon.contains(point)){
                    VCircle newPosition = new VCircle(point.getX(), point.getY(), this.agentRadius);

                    if (!checkOverlap(newPosition)) {
                        int pedId = firstPedId + createdPeds;
                        Pedestrian pedestrian = createPedestrian(dialog, topography, random, binomialDistribution, point, pedId);
                        addPedestrianToTopography(pedestrian);
                        createdPeds++;
                    }
                }
                iter ++;
            }
            showInformation(createdPeds, numOfPeds);
        } else {
            logger.warn("Dialog canceled or input invalid!");
        }

        getScenarioPanelModel().notifyObservers();
    }

    public void setPolygon(VPolygon polygon) {
        this.polygon = polygon;
    }

    @NotNull
    private Pedestrian createPedestrian(ActionPedestrianDensityZoneDialog dialog, Topography topography, Random random, BinomialDistribution binomialDistribution, IPoint point, int id) {
        AttributesAgent attributesAgent = new AttributesAgent(
                topography.getAttributesPedestrian(),
                id);

        Pedestrian pedestrian = new Pedestrian(attributesAgent, random);
        pedestrian.setPosition(new VPoint(point));
        pedestrian.setTargets(getTargetList(dialog.getTargetOption(), pedestrian));

        if (binomialDistribution.sample() == BINOMIAL_DISTRIBUTION_SUCCESS_VALUE) {
            pedestrian.setGroupMembership(GroupMembership.IN_GROUP);
        } else {
            pedestrian.setGroupMembership(GroupMembership.OUT_GROUP);
        }
        return pedestrian;
    }

    private int computeNumberOfPeds(ActionPedestrianDensityZoneDialog dialog) {
        double area = polygon.getArea();
        return (int) (density*area);
    }

    private boolean checkOverlap(VShape newPedestrian){
        boolean pedOverlap = getScenarioPanelModel().getTopography().getInitialElements(Pedestrian.class)
                .stream()
                .map(Agent::getShape)
                .anyMatch(shape -> shape.intersects(newPedestrian));
        boolean targetOverlap = getScenarioPanelModel().getTopography().getTargets()
                .stream()
                .map(Target::getShape)
                .anyMatch(shape -> shape.intersects(newPedestrian));

        return pedOverlap  || targetOverlap;
    }

    private LinkedList<Integer> getTargetList(ActionPedestrianDensityZoneDialog.TARGET_OPTION_ZONE selectedOption, Pedestrian pedestrian) {
        LinkedList<Integer> targetList = new LinkedList<>();

        if (selectedOption == ActionPedestrianDensityZoneDialog.TARGET_OPTION_ZONE.RANDOM) {
            List<Target> targets = getScenarioPanelModel().getTopography().getTargets();
            Random random = new Random();
            Target randomTarget = targets.get(random.nextInt(targets.size()));

            targetList.add(randomTarget.getId());

        } else if (selectedOption == ActionPedestrianDensityZoneDialog.TARGET_OPTION_ZONE.NEAREST) {
            if (!getScenarioPanelModel().getTopography().getTargets().isEmpty()) {
                List<Target> targets = getScenarioPanelModel().getTopography().getTargets();
                int target = pedestrian.findNearestTarget(targets);
                targetList.add(target);
            }
        }

        return targetList;
    }

    private void addPedestrianToTopography(Pedestrian pedestrian) {
        AgentWrapper agentWrapper = new AgentWrapper(pedestrian);
        getScenarioPanelModel().addShape(agentWrapper);
        getScenarioPanelModel().setElementHasChanged(agentWrapper);
    }

    public void showInformation(int createdPeds, int numOfPeds) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        panel.add(new JLabel("- Aire de la zone sélectionnée : " + numberFormat.format(polygon.getArea()) + "m²"));
        panel.add(new JLabel("- Densité désirée : " + numberFormat.format(density) + " piétons par m²"));
        panel.add(new JLabel("- Réelle densité : "+ numberFormat.format(createdPeds/polygon.getArea()) + " piétons par m²"));
        panel.add(new JLabel("- " +createdPeds + " piétons ont été créés sur les " + numOfPeds + " désirés"));

        JOptionPane.showMessageDialog(ProjectView.getMainWindow(), panel, "Zone créée", JOptionPane.INFORMATION_MESSAGE);


    }

    private void showWarning(int numOfPeds, int createdPeds) {
        String message = String.format("%s: %d\n%s: %d",
                Messages.getString("TopographyCreator.PlaceRandomPedestrians.couldNotPlaceAllPeds.requested.text"), numOfPeds,
                Messages.getString("TopographyCreator.PlaceRandomPedestrians.couldNotPlaceAllPeds.placed.text"), createdPeds);

        VDialogManager.showWarning(
                Messages.getString("TopographyCreator.PlaceRandomPedestrians.couldNotPlaceAllPeds.title"),
                message
        );
    }

}
