package org.vadere.gui.topographycreator.control;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.GeometryCombiner;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;
import org.vadere.gui.projectview.view.ProjectView;
import org.vadere.gui.topographycreator.model.IDrawPanelModel;
import org.vadere.state.scenario.Obstacle;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.VLine;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.voronoi.VoronoiDiagram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActionTopographyInformation extends TopographyAction {

    private final IDrawPanelModel drawPanelModel;
    private Topography topography;
    private GeometryFactory geometryFactory;

    public ActionTopographyInformation(String name, String iconPath, IDrawPanelModel<?> panelModel) {
        super(name, iconPath, panelModel);
        drawPanelModel = panelModel;
        topography = panelModel.getTopography();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        {
            topography = drawPanelModel.getTopography();
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("- Nombre total de piétons : " + getNumberOfPeds()));
            panel.add(new JLabel("- Nombre de sorties de secours : " + getNumberOfTargets()));
            DecimalFormat numberFormat = new DecimalFormat("#.00");
            panel.add(new JLabel("- Longueur totale des sorties de secours : " + numberFormat.format(getTotalLengthOfTargets()) + " mètres"));

            if (!getTargets().isEmpty()) {
                DistanceResult distance = getFarthestPoint();
                Coordinate farthest = distance.getCoordinate();
                double dist = distance.getDistance();
                panel.add(new JLabel("- Point le plus éloigné de toute sortie : (" + numberFormat.format(farthest.x) + ", " + numberFormat.format(farthest.y) + ")"));
                panel.add(new JLabel("- Distance entre ce point et la sortie la plus proche : " + numberFormat.format(dist) + " mètres"));
            }

            // Afficher un popup avec le panneau
            JOptionPane.showMessageDialog(ProjectView.getMainWindow(), panel, "Informations sur la topographie", JOptionPane.INFORMATION_MESSAGE);

        }
    }

    private int getNumberOfTargets(){
        return getTargets().size();
    }

    private List<Target> getTargets(){
        return topography.getTargets();
    }

    private List<Obstacle> getObstacles(){
        return topography.getObstacles();
    }

    private int getNumberOfPeds(){ return topography.getPedestrianDynamicElements().getInitialElements().size(); }

    /**
     * Computes the total length of the targets by taking the longest side of each target
     */
    private double getTotalLengthOfTargets(){
        double length = 0;
        if (!getTargets().isEmpty()){
            for(Target target : getTargets()){
                double greatestSide = 0;
                for (VLine line : target.getShape().lines()){
                    if (greatestSide < line.length()){
                        greatestSide = line.length();
                    }
                }
                length += greatestSide;
            }
            return length;
        } else {
            return 0;
        }
    }

    /**
     * Function to create a Polygon representing the topography
     * @param geometryFactory
     * @return a Polygon of the size of the topography
     */
    private Polygon getTopographyBounds(GeometryFactory geometryFactory){
        double x1 = topography.getBounds().getMinX();
        double y1 = topography.getBounds().getMinY();
        double x2 = topography.getBounds().getMaxX();
        double y2 = topography.getBounds().getMaxY();
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(x1, y1);
        coordinates[1] = new Coordinate(x2, y1);
        coordinates[2] = new Coordinate(x2, y2);
        coordinates[3] = new Coordinate(x1, y2);
        coordinates[4] = new Coordinate(x1, y1);
        return geometryFactory.createPolygon(coordinates);
    }

    /**
     * Creates a hashmap to store each target with its bound in a list of coordinates
     * @return Hashmap mapping the target with the list of its coordinates
     */
    private HashMap<Target, List<Coordinate>> getCoordinatesTargets(){
        HashMap<Target, List<Coordinate>> result = new HashMap<Target, List<Coordinate>>();
        for (Target target : getTargets()){
            List<Coordinate> coordinates = new ArrayList<>();
            List<VPoint> path = target.getShape().getPath();
            for (VPoint point : path){
                coordinates.add(new Coordinate(point.x, point.y));
            }
            result.put(target, coordinates);
        }
        return result;
    }

    /**
     *
     * @return the list of all coordinates of all targets
     */
    private List<Coordinate> getSites(){
        List<Coordinate> coordinates = new ArrayList<>();
        for (List<Coordinate> coordinateList : getCoordinatesTargets().values()){
            coordinates.addAll(coordinateList);
        }
        return coordinates;
    }

    /**
     * Creates a union on all the obstacles
     */
    private List<Geometry> allObstacles(GeometryFactory geometryFactory) {
        if (!getObstacles().isEmpty()){
            List<Geometry> obstacles = new ArrayList<>();
            for (int i=0; i<getObstacles().size(); i++){
                Obstacle ob = getObstacles().get(i);
                List<VPoint> p = ob.getShape().getPath();
                Coordinate[] coordinates2 = new Coordinate[p.size()+1];
                for (int j = 0; j < p.size(); j++){
                    coordinates2[j] = new Coordinate(p.get(j).getX(), p.get(j).getY());
                }
                coordinates2[coordinates2.length-1] = new Coordinate(p.get(0).getX(), p.get(0).getY());
                obstacles.add(geometryFactory.createPolygon(coordinates2));
            }
            return obstacles;
        }
        return new ArrayList<>();
    }

    final class DistanceResult {
        private final Coordinate coordinate;
        private final double distance;

        public DistanceResult(Coordinate coordinate, double distance) {
            this.coordinate = coordinate;
            this.distance = distance;
        }

        public Coordinate getCoordinate() {
            return coordinate;
        }

        public double getDistance() {
            return distance;
        }
    }

    private DistanceResult getFarthestPoint(){
        GeometryFactory geometryFactory = new GeometryFactory();

        VoronoiDiagramBuilder voronoiBuilder = new VoronoiDiagramBuilder();
        voronoiBuilder.setSites(getSites());
        voronoiBuilder.setClipEnvelope(getTopographyBounds(geometryFactory).getEnvelopeInternal());

        Geometry voronoiDiagram = voronoiBuilder.getDiagram(geometryFactory);

        Geometry validVoronoi = voronoiDiagram.intersection(getTopographyBounds(geometryFactory));
        if(!getObstacles().isEmpty()){
            //Exclure les obstacles
            for (Geometry obstacle : allObstacles(geometryFactory)) {
                // Exclure chaque obstacle après l'intersection avec les limites
                Geometry boundedVoronoi = validVoronoi.intersection(getTopographyBounds(geometryFactory));

                // Gérer le résultat potentiel de GeometryCollection
                validVoronoi = handleDifference(boundedVoronoi, obstacle);
            }

        }

        //Trouver le point le plus éloigné
        Coordinate farthestPoint = null;
        double maxDistance = -1;

        for (int i = 0; i < validVoronoi.getNumGeometries(); i++){
            //Parcourir les cellules du diagramme de Voronoi
            Geometry cell = validVoronoi.getGeometryN(i);

            //Parcourir les sommets de la cellule
            for (Coordinate coord : cell.getCoordinates()){
                double minDistance = Double.MAX_VALUE;

                //Calculer la distance minimale entre ce sommet et les points donnés
                for (Coordinate p : getSites()){
                    double distance = coord.distance(p);
                    minDistance = Math.min(minDistance, distance);
                }

                //Mettre à jour si on trouve un sommet avec une distance minimale plus grande
                if(minDistance > maxDistance){
                    farthestPoint = coord;
                    maxDistance = minDistance;
                }
            }
        }
        return new DistanceResult(farthestPoint, maxDistance);
    }

    private Geometry handleDifference(Geometry source, Geometry obstacle) {
        if (source instanceof GeometryCollection) {
            List<Geometry> processedGeometries = new ArrayList<>();

            GeometryCollection collection = (GeometryCollection) source;
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                Geometry geom = collection.getGeometryN(i);
                processedGeometries.add(geom.difference(obstacle));
            }

            return GeometryCombiner.combine(processedGeometries);
        } else {
            // Si source n'est pas GeometryCollection, appliquer directement
            return source.difference(obstacle);
        }
    }

}
