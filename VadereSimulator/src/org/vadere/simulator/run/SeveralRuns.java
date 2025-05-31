package org.vadere.simulator.run;

import au.com.bytecode.opencsv.CSVWriter;
import org.jetbrains.annotations.NotNull;
import org.vadere.simulator.control.simulation.ScenarioRun;
import org.vadere.simulator.control.simulation.Simulation;
import org.vadere.simulator.projects.Scenario;
import org.vadere.simulator.projects.SimulationResult;
import org.vadere.simulator.utils.cache.ScenarioCache;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;

/**
 * Class to run several scenarios.
 */

public class SeveralRuns {

    Logger logger = Logger.getLogger(SeveralRuns.class);
    private String projectPath;
    private ArrayList<Scenario> scenarios;

    public SeveralRuns(String projectPath, ArrayList<Scenario> scenarios) {
        this.projectPath = projectPath;
        this.scenarios = scenarios;
    }

    /**
     * Simple run
     */
    public void run(){
        Path path = Paths.get(this.projectPath, IOUtils.SCENARIO_DIR);

        for (Scenario scenario : this.scenarios) {

            String scenarioName = scenario.getName();

            logger.info("Running scenario " + scenarioName);

            final ScenarioRun scenarioRun = new ScenarioRun(scenario, null, path.resolve(IOUtils.SCENARIO_DIR).resolve(scenario.getName() + IOUtils.SCENARIO_FILE_EXTENSION), ScenarioCache.load(scenario, path.resolve(IOUtils.SCENARIO_DIR)), false);
            scenarioRun.run();
            Simulation simulation = scenarioRun.getSimulation();
            SimulationResult result = scenarioRun.getSimulationResult();
        }

    }

    /**
     * Runs the scenarios and retrieve the simulated evacuation time, the number
     * of iterations, and the simulation execution time in a csv file
     */
    public void runTimeInfo(String fileName){
        Path path = Paths.get(this.projectPath, IOUtils.SCENARIO_DIR);
        String csvFile = "Simulation results/" + fileName + ".csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {

            String[] header = {"Scenario", "simulation time", "iterations", "execution time"};
            writer.writeNext(header);

            for (Scenario scenario : this.scenarios) {

                String scenarioName = scenario.getName();

                logger.info("Running scenario " + scenarioName);

                final ScenarioRun scenarioRun = new ScenarioRun(scenario, null, path.resolve(IOUtils.SCENARIO_DIR).resolve(scenario.getName() + IOUtils.SCENARIO_FILE_EXTENSION), ScenarioCache.load(scenario, path.resolve(IOUtils.SCENARIO_DIR)), false);
                scenarioRun.run();
                Simulation simulation = scenarioRun.getSimulation();
                SimulationResult result = scenarioRun.getSimulationResult();
                String[] line = getLineTime(simulation, result, scenarioName);
                writer.writeNext(line);
            }

        } catch (IOException e){
            logger.error(e);
            e.printStackTrace();
        }
    }


    private static String [] getLineTime(Simulation simulation, SimulationResult result, String scenarioName) {


        double simulatedEvacuationTime = simulation.getCurrentTime();

        Duration time = result.getRunTime();

        double simulationExecutionTime = time.toMillis() / 1000.0;

        int iter = simulation.getNumIterations();

        DecimalFormat numberFormat = new DecimalFormat("#.00");

        return new String[]{scenarioName, numberFormat.format(simulatedEvacuationTime), String.valueOf(iter), String.valueOf(simulationExecutionTime)};
    }

}
