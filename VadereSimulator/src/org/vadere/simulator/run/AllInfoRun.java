package org.vadere.simulator.run;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Precision;
import org.vadere.simulator.control.simulation.ScenarioRun;
import org.vadere.simulator.control.simulation.Simulation;
import org.vadere.simulator.projects.Scenario;
import org.vadere.simulator.projects.SimulationResult;
import org.vadere.simulator.utils.cache.ScenarioCache;
import org.vadere.state.scenario.Obstacle;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VRectangle;
import org.vadere.util.geometry.shapes.Vector2D;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class AllInfoRun {
    Logger logger = Logger.getLogger(AllInfoRun.class);

    private Scenario scenario;
    private Path path;
    private String jsonFileName;
    private Optional<VRectangle> zone;

    public AllInfoRun(String projectPath, Scenario scenario, String fileName, Optional<VRectangle> zone){
        this.scenario = scenario;
        this.jsonFileName = "resultsSimulations/" + fileName + ".json";
        this.path = Paths.get(projectPath, IOUtils.SCENARIO_DIR);
        this.zone = zone;

    }


    public void run() throws IOException {

        //Launch the simulation
        final ScenarioRun scenarioRun = new ScenarioRun(this.scenario, null, this.path.resolve(IOUtils.SCENARIO_DIR).resolve(this.scenario.getName() + IOUtils.SCENARIO_FILE_EXTENSION), ScenarioCache.load(this.scenario, this.path.resolve(IOUtils.SCENARIO_DIR)), true);
        scenarioRun.run();
        Simulation simulation = scenarioRun.getSimulation();
        SimulationResult result = scenarioRun.getSimulationResult();
        HashMap<Integer, ArrayList<Pair<Double, VPoint>>> trajectories = simulation.getTrajectories();
        HashMap<Integer, ArrayList<Pair<Double, Vector2D>>> velocities = simulation.getVelocities();


        //Creating the ObjectMApper to construct the json file
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        //Get the time infos
        root.put("Simulation time", simulation.getCurrentTime());
        root.put("Execution time", result.getRunTime().toMillis()/1000.0);
        root.put("Number of iterations", simulation.getNumIterations());

        //Get informations about the topography
        root.set("topography", getTopographyInfo(mapper, simulation.getTopographyController().getTopography()));

        //Get informations about the trajectories and velocities
        int firstPedId = simulation.getTopographyController().getTopography().getInitialElements(Pedestrian.class).get(0).getId();
        int lastPedId = simulation.getTopographyController().getTopography().getInitialElements(Pedestrian.class).get(simulation.getTopographyController().getTopography().getInitialElements(Pedestrian.class).size()-1).getId();

        root.put("first pedestrian id", firstPedId);
        root.put("last pedestrian id", lastPedId);

        double simTimeStepLength = simulation.getSimTimeStepLength();
        double epsilon = 0.001d;
        double simulationTime = simulation.getCurrentTime();

        ArrayList<Pair<Double, HashMap<Integer, VPoint>>>  restructuredTrajectories = restructureTrajectories(trajectories, simTimeStepLength, epsilon, simulationTime);
        ArrayList<Pair<Double, HashMap<Integer, Vector2D>>>  restructuredVelocities = restructureVelocities(velocities, simTimeStepLength, epsilon, simulationTime);

        root.set("trajectories", getAllTrajectoriesInfo(mapper, restructuredTrajectories));
        root.set("velocities", getAllVelocitiesInfo(mapper, restructuredVelocities));

        if (this.zone.isEmpty()){
            root.put("density zone", (JsonNode) null);
        } else {
            root.put("density zone", getRectangleInfo(mapper, zone.get()));
        }


        File fichier = new File(this.jsonFileName);
        mapper.writerWithDefaultPrettyPrinter().writeValue(fichier, root);

        logger.info("All information have been collected");

    }


    public ObjectNode getAllTrajectoriesInfo(ObjectMapper mapper, ArrayList<Pair<Double, HashMap<Integer, VPoint>>>  trajectories) {
        ObjectNode traj = mapper.createObjectNode();
        for (int i = 0; i < trajectories.size(); i++) {
            traj.set(trajectories.get(i).getLeft().toString(), getTimeTrajectoryInfo(mapper, trajectories.get(i).getRight()));
        }
        return traj;
    }

    public ObjectNode getTimeTrajectoryInfo(ObjectMapper mapper, HashMap<Integer, VPoint>  timeTrajectory) {
        ObjectNode time = mapper.createObjectNode();
        for (Integer id : timeTrajectory.keySet()) {
            time.put(Integer.toString(id), String.valueOf(timeTrajectory.get(id)));
        }
        return time;
    }

    public ObjectNode getAllVelocitiesInfo(ObjectMapper mapper, ArrayList<Pair<Double, HashMap<Integer, Vector2D>>>  velocities) {
        ObjectNode vel = mapper.createObjectNode();
        for (int i = 0; i < velocities.size(); i++) {
            vel.set(velocities.get(i).getLeft().toString(), getTimeVelocityInfo(mapper, velocities.get(i).getRight()));
        }
        return vel;
    }

    public ObjectNode getTimeVelocityInfo(ObjectMapper mapper, HashMap<Integer, Vector2D>  timeVelocity) {
        ObjectNode time = mapper.createObjectNode();
        for (Integer id : timeVelocity.keySet()) {
            time.put(Integer.toString(id), timeVelocity.get(id).getLength());
        }
        return time;
    }



    public ObjectNode getTopographyInfo(ObjectMapper mapper, Topography topography){
        ObjectNode topo = mapper.createObjectNode();
        topo.set("bounds", getBoundInfo(mapper, topography));
        topo.set("targets", getTargetInfo(mapper, topography));
        topo.set("obstacles", getObstacleInfo(mapper, topography));
        return topo;
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

    public ArrayList<Pair<Double, HashMap<Integer, VPoint>>> restructureTrajectories(HashMap<Integer, ArrayList<Pair<Double, VPoint>>> trajectories, double simTimeStepLength, double epsilon, double simulationTime) {
        ArrayList<Pair<Double, HashMap<Integer, VPoint>>> restructuredTrajectories = new ArrayList<>();
        for (double t = 0; t <= simulationTime; t += simTimeStepLength) {
            HashMap<Integer, VPoint> positions = new HashMap<>();
            for (Integer pedId : trajectories.keySet()) {
                for (Pair<Double, VPoint> pair : trajectories.get(pedId)) {
                    if (Precision.equals(t, pair.getLeft(), epsilon)) {
                        positions.put(pedId, pair.getRight());
                    }
                }
            }
            restructuredTrajectories.add(new ImmutablePair<>(t, positions));
        }
        return restructuredTrajectories;
    }

    public ArrayList<Pair<Double, HashMap<Integer, Vector2D>>> restructureVelocities(HashMap<Integer, ArrayList<Pair<Double, Vector2D>>> velocities, double simTimeStepLength, double epsilon, double simulationTime) {
        ArrayList<Pair<Double, HashMap<Integer, Vector2D>>> restructuredVelocities = new ArrayList<>();
        for (double t = 0; t <= simulationTime; t += simTimeStepLength) {
            HashMap<Integer, Vector2D> speeds = new HashMap<>();
            for (Integer pedId : velocities.keySet()) {
                for (Pair<Double, Vector2D> pair : velocities.get(pedId)) {
                    if (Precision.equals(t, pair.getLeft(), epsilon)) {
                        speeds.put(pedId, pair.getRight());
                    }
                }
            }
            restructuredVelocities.add(new ImmutablePair<>(t, speeds));
        }
        return restructuredVelocities;
    }
}
