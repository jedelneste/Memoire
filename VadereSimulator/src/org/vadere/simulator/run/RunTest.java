package org.vadere.simulator.run;

import au.com.bytecode.opencsv.CSVWriter;
import org.lwjgl.system.CallbackI;
import org.vadere.simulator.control.simulation.ScenarioRun;
import org.vadere.simulator.projects.Scenario;
import org.vadere.simulator.projects.SimulationResult;
import org.vadere.simulator.projects.VadereProject;
import org.vadere.simulator.projects.io.IOVadere;
import org.vadere.simulator.utils.cache.ScenarioCache;
import org.vadere.util.geometry.shapes.VRectangle;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;

import java.awt.geom.Rectangle2D;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;


/**
 * Class to run all our simulations tests and get the necessary data
 */

public class RunTest {

    private static Logger logger = Logger.getLogger(RunTest.class);

    public static String projectPath = "/home/acer/Documents/Master INFO/Mémoire/vadere/Scenarios/";

    private static VadereProject project;

    /**
     * Runs 100 times the same scenario but with different configuration of random placed
     * pedestrians to test the stability of the execution time
     * @param scenarioName : the name of the existing scenario, in the "StabilityStudy" file
     * @throws IOException
     */
    public static void runStabilityOneScenario(String scenarioName) throws IOException {
        String scenarioPath = projectPath + "StabilityStudy/";
        project = IOVadere.readProject(scenarioPath);
        Scenario scenario = project.getScenarioByName(scenarioName);

        Rectangle2D.Double zone = new Rectangle2D.Double(7, 5, 6, 5);

        GeneralRun stabilityRun = new GeneralRun(scenarioPath, scenario, scenarioName);
        stabilityRun.runStability(100, 100, zone);

    }

    /**
     * Runs all the stability study for all the models
     * @throws IOException
     */
    public static void runAllStabilityScenarios() throws IOException {
        runStabilityOneScenario("SFM");
        runStabilityOneScenario("OSM");
        runStabilityOneScenario("GNM");
        runStabilityOneScenario("CA");
    }

    /**
     * Runs a scenario with a growing number of pedestrians and record the execution time
     * to test the scalability of our models
     * @param scenarioName : the name of the existing scenario
     * @throws IOException
     */
    public static void runScalabilityOneScenario(String scenarioName, Rectangle2D.Double zone, ArrayList<Integer> numsOfPeds) throws IOException {
        String scenarioPath = projectPath + "ExecutionTime/";
        project = IOVadere.readProject(scenarioPath);
        Scenario scenario = project.getScenarioByName(scenarioName);


        GeneralRun executionTimeRun = new GeneralRun(scenarioPath, scenario, scenarioName);

        executionTimeRun.runScalability(numsOfPeds, zone);
    }

    /**
     * Runs the scalability study for all the models with the simple scenario (without obstacles)
     * @throws IOException
     */
    public static void runAllScalabilitySimpleScenarios() throws IOException {
        Rectangle2D.Double zone = new Rectangle2D.Double(1, 1, 15, 15);
        ArrayList<Integer> numsOfPeds = new ArrayList<Integer>(Arrays.asList(10, 50, 100, 250, 500, 750));
        logger.info("Running OSM");
        runScalabilityOneScenario("OSM", zone, numsOfPeds);
        logger.info("Running GN");
        runScalabilityOneScenario("GNM", zone, numsOfPeds);
        logger.info("Running SFM");
        runScalabilityOneScenario("SFM", zone, numsOfPeds);
        logger.info("Running CA");
        runScalabilityOneScenario("CA", zone, numsOfPeds);
    }

    /**
     * Runs the scalability study for all the models with the complex scenario (with obstacles)
     * @throws IOException
     */
    public static void runAllScalabilityComplexScenarios() throws IOException {
        Rectangle2D.Double zone = new Rectangle2D.Double(1, 1, 15, 15);
        ArrayList<Integer> numsOfPeds = new ArrayList<Integer>(Arrays.asList(10, 50, 100, 250, 500, 750));
        ArrayList<Integer> numsOfPedsSFM = new ArrayList<Integer>(Arrays.asList(10, 50, 100, 250));
        logger.info("Running OSM");
        runScalabilityOneScenario("OSM_Obs", zone, numsOfPeds);
        logger.info("Running GN");
        runScalabilityOneScenario("GNM_Obs", zone, numsOfPeds);
        logger.info("Running SFM");
        runScalabilityOneScenario("SFM_Obs", zone, numsOfPedsSFM);
        logger.info("Running CA");
        runScalabilityOneScenario("CA_Obs", zone, numsOfPeds);
    }

    /**
     * Runs the scalability study for all the models with the huge scenario (>1000 pedestrians)
     * @throws IOException
     */
    public static void runAllScalabilityHugeScenarios() throws IOException {
        Rectangle2D.Double zone = new Rectangle2D.Double(1, 1, 43, 52);
        ArrayList<Integer> numsOfPeds = new ArrayList<Integer>(Arrays.asList(1000, 2000, 3000, 4000, 5000));
        logger.info("Running OSM");
        runScalabilityOneScenario("OSM_Huge", zone, numsOfPeds);
        logger.info("Running GN");
        runScalabilityOneScenario("GNM_Huge", zone, numsOfPeds);
        logger.info("Running CA");
        runScalabilityOneScenario("CA_Huge", zone, numsOfPeds);

    }

    /**
     * Runs scenarios for the variation parameter of the model OSM and collect the
     * execution time of the simulation, the evacuation time and the number of iterations
     * @throws IOException
     */
    public static void runVariationParameterSearchOSM() throws IOException {
        String scenarioPath = projectPath + "OSM_VP_Search/";
        project = IOVadere.readProject(scenarioPath);

        ArrayList<String> NumberOfCircles = new ArrayList<>(Arrays.asList("1", "2", "3"));
        ArrayList<String> NumberOfPoints = new ArrayList<>(Arrays.asList("04", "08", "16", "32"));

        ArrayList<Scenario> scenarios = new ArrayList<>();
        for (String number : NumberOfCircles) {
            for (String point : NumberOfPoints) {
                scenarios.add(project.getScenarioByName("OSM_" + number + "_" + point));
            }
        }

        TimeInfoRun run = new TimeInfoRun(scenarioPath, scenarios, "Variation Parameter Search");

    }

    public static void main(String[] args) throws IOException {
        runAllStabilityScenarios();
        runAllScalabilitySimpleScenarios();
        runAllScalabilityComplexScenarios();
        runAllScalabilityHugeScenarios();
        runVariationParameterSearchOSM();

    }
}
