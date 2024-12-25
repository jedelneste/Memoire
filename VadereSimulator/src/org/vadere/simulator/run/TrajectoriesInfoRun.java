package org.vadere.simulator.run;

import au.com.bytecode.opencsv.CSVWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import org.vadere.simulator.control.simulation.ScenarioRun;
import org.vadere.simulator.control.simulation.Simulation;
import org.vadere.simulator.projects.Scenario;
import org.vadere.simulator.projects.SimulationResult;
import org.vadere.simulator.projects.VadereProject;
import org.vadere.simulator.projects.io.IOVadere;
import org.vadere.simulator.utils.cache.ScenarioCache;
import org.vadere.state.scenario.Obstacle;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;
import org.vadere.state.simulation.FootStep;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class TrajectoriesInfoRun {

    private static Logger logger = Logger.getLogger(TrajectoriesInfoRun.class);


    public TrajectoriesInfoRun(String projectPath, ArrayList<Scenario> scenarios, String fileName) throws IOException {
        Path path = Paths.get(projectPath, IOUtils.SCENARIO_DIR);

        for (Scenario scenario : scenarios) {

            String scenarioName = scenario.getName();

            String jsonFile = "resultsSimulations/trajectories/" + fileName + "_" + scenarioName + ".json";

            logger.info("Running scenario " + scenarioName);

            final ScenarioRun scenarioRun = new ScenarioRun(scenario, null, path.resolve(IOUtils.SCENARIO_DIR).resolve(scenario.getName() + IOUtils.SCENARIO_FILE_EXTENSION), ScenarioCache.load(scenario, path.resolve(IOUtils.SCENARIO_DIR)), true);
            scenarioRun.run();
            Simulation simulation = scenarioRun.getSimulation();
            HashMap<Integer, ArrayList<Pair<Double, VPoint>>> trajectories = simulation.getTrajectories();
            Topography topography = simulation.getTopographyController().getTopography();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();

            root.set("bounds", getBoundInfo(mapper, topography));
            root.set("targets", getTargetInfo(mapper, topography));
            root.set("obstacles", getObstacleInfo(mapper, topography));
            root.set("trajectories", getTrajectoriesInfo(mapper, trajectories));

            File fichier = new File(jsonFile);
            mapper.writerWithDefaultPrettyPrinter().writeValue(fichier, root);

        }

    }

    public ObjectNode getBoundInfo(ObjectMapper mapper, Topography topography) {
        ObjectNode boundInfo = mapper.createObjectNode();
        Rectangle2D bounds = topography.getBounds();
        boundInfo.put("x", bounds.getX());
        boundInfo.put("y", bounds.getY());
        boundInfo.put("width", bounds.getWidth());
        boundInfo.put("height", bounds.getHeight());
        return boundInfo;
    }

    public ObjectNode getTargetInfo(ObjectMapper mapper, Topography topography) {
        ObjectNode targetInfo = mapper.createObjectNode();
        for (Target target : topography.getTargets()) {
            if (target.getShape() instanceof Rectangle2D){
                targetInfo.set(Integer.toString(target.getId()), getRectangleInfo(mapper, (Rectangle2D) target.getShape()));
            }
        }
        return targetInfo;
    }

    public ObjectNode getTrajectoriesInfo(ObjectMapper mapper, HashMap<Integer, ArrayList<Pair<Double, VPoint>>> trajectories) {
        ObjectNode trajectoriesInfo = mapper.createObjectNode();
        for (Integer id : trajectories.keySet()) {
            trajectoriesInfo.put(Integer.toString(id), String.valueOf(valeurs(trajectories.get(id))));
        }
        return trajectoriesInfo;
    }

    public ArrayList<VPoint> valeurs(ArrayList<Pair<Double, VPoint>> trajectories) {
        ArrayList<VPoint> valeurs = new ArrayList<>();
        for (Pair<Double, VPoint> pair : trajectories) {
            valeurs.add(pair.getRight());
        }
        return valeurs;
    }

    public ObjectNode getRectangleInfo(ObjectMapper mapper, Rectangle2D shape) {
        ObjectNode rectangleInfo = mapper.createObjectNode();
        rectangleInfo.put("type", "RECTANGLE");
        rectangleInfo.put("x", shape.getX());
        rectangleInfo.put("y", shape.getY());
        rectangleInfo.put("width", shape.getWidth());
        rectangleInfo.put("height", shape.getHeight());
        return rectangleInfo;
    }

    public ObjectNode getObstacleInfo(ObjectMapper mapper, Topography topography) {
        ObjectNode obstacleInfo = mapper.createObjectNode();
        for (Obstacle obstacle : topography.getObstacles()) {
            if (obstacle.getShape() instanceof Rectangle2D){
                obstacleInfo.set(Integer.toString(obstacle.getId()), getRectangleInfo(mapper, (Rectangle2D) obstacle.getShape()));
            }
        }
        return obstacleInfo;
    }
}
