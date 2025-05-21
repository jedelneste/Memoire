package org.vadere.simulator.run;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.vadere.simulator.control.simulation.ScenarioRun;
import org.vadere.simulator.control.simulation.Simulation;
import org.vadere.simulator.projects.Scenario;
import org.vadere.simulator.projects.SimulationResult;
import org.vadere.simulator.utils.cache.ScenarioCache;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.psychology.cognition.GroupMembership;
import org.vadere.state.scenario.Agent;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.IPoint;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;
import org.vadere.util.random.SimpleReachablePointProvider;

import java.awt.geom.Rectangle2D;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;

public class GeneralRun {

    /**
     * Class used to run scenarios and collect some data
     */

    Logger logger = Logger.getLogger(GeneralRun.class);
    private static final int BINOMIAL_DISTRIBUTION_SUCCESS_VALUE = 1;
    private String projectPath;
    private Scenario scenario;
    private String fileName;

    public GeneralRun(String projectPath, Scenario scenario, String fileName) {
        this.projectPath = projectPath;
        this.scenario = scenario;
        this.fileName = fileName;

    }

    public void run(){
        Path path = Paths.get(this.projectPath, IOUtils.SCENARIO_DIR);
        Scenario scenario_copy = this.scenario.clone();
        final ScenarioRun scenarioRun = new ScenarioRun(scenario_copy, null, path.resolve(IOUtils.SCENARIO_DIR).resolve(scenario_copy.getName() + IOUtils.SCENARIO_FILE_EXTENSION), ScenarioCache.load(scenario_copy, path.resolve(IOUtils.SCENARIO_DIR)), false);
        scenarioRun.run();
    }

    public void runStability(int numberOfRuns, int numPeds, Rectangle2D.Double zone){
        Path path = Paths.get(this.projectPath, IOUtils.SCENARIO_DIR);
        String csvFile = "Simulation results/StabilityStudy/" + this.fileName + ".csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {

            String[] header = {"Run", "evacuationTime", "createdPeds", "flow"};
            writer.writeNext(header);


            for (int i = 0; i < numberOfRuns; i++) {
                Scenario scenario_copy = this.scenario.clone();
                int createdPeds = placeRandomPedestrians(scenario_copy, numPeds, zone);
                final ScenarioRun scenarioRun = new ScenarioRun(scenario_copy, null, path.resolve(IOUtils.SCENARIO_DIR).resolve(scenario_copy.getName() + IOUtils.SCENARIO_FILE_EXTENSION), ScenarioCache.load(scenario_copy, path.resolve(IOUtils.SCENARIO_DIR)), false);
                scenarioRun.run();
                Simulation simulation = scenarioRun.getSimulation();


                double simulatedEvacuationTime = simulation.getCurrentTime();
                double flow = createdPeds/simulatedEvacuationTime;


                String[] line = new String[]{Integer.toString(i), Double.toString(simulatedEvacuationTime), Double.toString(createdPeds), Double.toString(flow)};
                writer.writeNext(line);
            }
        } catch (IOException e){
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void runScalability(ArrayList<Integer> numsOfPeds, Rectangle2D.Double zone){
        Path path = Paths.get(this.projectPath, IOUtils.SCENARIO_DIR);
        String csvFile = "Simulation results/ExecutionTime/" + this.fileName + ".csv";
        Runtime runtime = Runtime.getRuntime();

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {

            String[] header = {"numOfPeds desired", "numOfPeds actual", "executionTime"};
            writer.writeNext(header);


            for (Integer numOfPeds : numsOfPeds) {
                logger.info(numOfPeds.toString());
                Scenario scenario_copy = this.scenario.clone();
                int createdPeds = placeRandomPedestrians(scenario_copy, numOfPeds, zone);
                final ScenarioRun scenarioRun = new ScenarioRun(scenario_copy, null, path.resolve(IOUtils.SCENARIO_DIR).resolve(scenario_copy.getName() + IOUtils.SCENARIO_FILE_EXTENSION), ScenarioCache.load(scenario_copy, path.resolve(IOUtils.SCENARIO_DIR)), false);
                scenarioRun.run();
                SimulationResult result = scenarioRun.getSimulationResult();


                Duration time = result.getRunTime();
                double simulationExecutionTime = time.toMillis() / 1000.0;


                String[] line = new String[]{Integer.toString(numOfPeds), Integer.toString(createdPeds), Double.toString(simulationExecutionTime)};
                writer.writeNext(line);
            }
        } catch (IOException e){
            logger.error(e);
            e.printStackTrace();
        }
    }




    public double mean(ArrayList<Double> flows) {
        double sum = 0.0;
        for (Double num : flows) {
            sum += num;
        }
        return (sum / flows.size());
    }

    public double standardDeviation(ArrayList<Double> flows) {
        double sumOfSquares = 0.0;
        for (Double num : flows) {
            sumOfSquares += Math.pow(num - mean(flows), 2);
        }
        return Math.sqrt(sumOfSquares / flows.size());
    }

    public int placeRandomPedestrians(Scenario scenario, int numOfPeds, Rectangle2D.Double zone) {
        Topography topography = scenario.getTopography();

        Random random = new Random();
        BinomialDistribution binomialDistribution = new BinomialDistribution(BINOMIAL_DISTRIBUTION_SUCCESS_VALUE, 0.5);
        SimpleReachablePointProvider pointProvider = SimpleReachablePointProvider.uniform(random, zone, topography.getObstacleDistanceFunction());
        double agentRadius = new AttributesAgent().getRadius();

        int firstPedId = topography.getNextDynamicElementId();
        int createdPeds = 0;
        int iter = 0;
        int maxIter = 50000;

        while (createdPeds < numOfPeds && iter < maxIter) {
            IPoint point = pointProvider.stream(dist ->  dist > 0.25).findFirst().get();
            VCircle newPosition = new VCircle(point.getX(), point.getY(), agentRadius);

            if (!checkOverlap(newPosition, topography)) {
                int pedId = firstPedId + createdPeds;
                Pedestrian pedestrian = createPedestrian(topography, random, binomialDistribution, point, pedId);
                topography.addInitialElement(pedestrian);
                createdPeds++;
            }
            iter ++;
        }

        return createdPeds;

    }

    private boolean checkOverlap(VShape newPedestrian, Topography topography) {
        boolean pedOverlap = topography.getInitialElements(Pedestrian.class)
                .stream()
                .map(Agent::getShape)
                .anyMatch(shape -> shape.intersects(newPedestrian));
        boolean targetOverlap = topography.getTargets()
                .stream()
                .map(Target::getShape)
                .anyMatch(shape -> shape.intersects(newPedestrian));

        return pedOverlap  || targetOverlap;
    }

    private Pedestrian createPedestrian(Topography topography, Random random, BinomialDistribution binomialDistribution, IPoint point, int id) {
        AttributesAgent attributesAgent = new AttributesAgent(
                topography.getAttributesPedestrian(),
                id);

        Pedestrian pedestrian = new Pedestrian(attributesAgent, random);

        pedestrian.setPosition(new VPoint(point));
        pedestrian.setNearestTarget(topography.getTargets());

        if (binomialDistribution.sample() == BINOMIAL_DISTRIBUTION_SUCCESS_VALUE) {
            pedestrian.setGroupMembership(GroupMembership.IN_GROUP);
        } else {
            pedestrian.setGroupMembership(GroupMembership.OUT_GROUP);
        }
        return pedestrian;
    }
}
