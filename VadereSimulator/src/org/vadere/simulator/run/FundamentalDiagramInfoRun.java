package org.vadere.simulator.run;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Precision;
import org.vadere.simulator.control.simulation.ScenarioRun;
import org.vadere.simulator.control.simulation.Simulation;
import org.vadere.simulator.projects.Scenario;
import org.vadere.simulator.utils.cache.ScenarioCache;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VRectangle;
import org.vadere.util.geometry.shapes.Vector2D;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Class to run an experience and have the fundamental diagram
 * which is the speed in terms of the density. The area of calculation
 * must be specified in the constructor
 */
public class FundamentalDiagramInfoRun {
    Logger logger = Logger.getLogger(FundamentalDiagramInfoRun.class);

    public FundamentalDiagramInfoRun(String projectPath, ArrayList<Scenario> scenarios, String fileName, VRectangle zone) throws IOException {
        Path path = Paths.get(projectPath, IOUtils.SCENARIO_DIR);

        for (Scenario scenario : scenarios) {
            String scenarioName = scenario.getName();

            String jsonFile = "resultsSimulations/FundamentalDiagram/" + fileName + "_" + scenarioName + ".json";

            logger.info("Running scenario " + scenarioName);

            final ScenarioRun scenarioRun = new ScenarioRun(scenario, null, path.resolve(IOUtils.SCENARIO_DIR).resolve(scenario.getName() + IOUtils.SCENARIO_FILE_EXTENSION), ScenarioCache.load(scenario, path.resolve(IOUtils.SCENARIO_DIR)), true);
            scenarioRun.run();
            Simulation simulation = scenarioRun.getSimulation();
            HashMap<Integer, ArrayList<Pair<Double, VPoint>>> trajectories = simulation.getTrajectories();
            HashMap<Integer, ArrayList<Pair<Double, Vector2D>>> velocities = simulation.getVelocities();


            double simTimeStepLength = simulation.getSimTimeStepLength();
            double epsilon = 0.001d;
            double simulationTime = simulation.getCurrentTime();

            ArrayList<Pair<Double, HashMap<Integer, VPoint>>>  restructuredTrajectories = restructureTrajectories(trajectories, simTimeStepLength, epsilon, simulationTime);
            ArrayList<Pair<Double, HashMap<Integer, Vector2D>>>  restructuredVelocities = restructureVelocities(velocities, simTimeStepLength, epsilon, simulationTime);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();

            root.put("area", zone.getArea());

            ObjectNode timeseries = mapper.createObjectNode();

            for (int i = 0; i < restructuredTrajectories.size(); i++) {
                Pair<Double, HashMap<Integer, VPoint>> trajectoryPair = restructuredTrajectories.get(i);
                Pair<Double, HashMap<Integer, Vector2D>> velocitiesPair = restructuredVelocities.get(i);
                timeseries.set(trajectoryPair.getLeft().toString(), getPedestriansInZone(mapper, trajectoryPair.getRight(), velocitiesPair.getRight(), zone));
            }
            root.set("speeds", timeseries);

            File fichier = new File(jsonFile);
            mapper.writerWithDefaultPrettyPrinter().writeValue(fichier, root);

        }
    }

    public void generateGraphs(ArrayList<Scenario> scenarios, String fileName) throws IOException {
        for (Scenario scenario : scenarios) {
            try {
                // Commande pour exécuter le script Python
                ProcessBuilder pb = new ProcessBuilder("python", "../../../../../Generate Graphs/fundamentalDiagram.py", fileName + "_" + scenario.getName());

                // Démarrer le processus
                Process process = pb.start();

                // Attendre la fin du processus
                int exitCode = process.waitFor();
                logger.info("Processus terminé avec le code de sortie : " + exitCode);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public ObjectNode getPedestriansInZone(ObjectMapper mapper, HashMap<Integer, VPoint> trajectories, HashMap<Integer, Vector2D> velocities, VRectangle zone) {
        ObjectNode pedestriansInZone = mapper.createObjectNode();
        for (Integer pedId : trajectories.keySet()) {
            if(zone.contains(trajectories.get(pedId))){
                pedestriansInZone.put(Integer.toString(pedId), velocities.get(pedId).getLength());
            }
        }
        return pedestriansInZone;
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
