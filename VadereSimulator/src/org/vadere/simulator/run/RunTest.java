package org.vadere.simulator.run;

import org.vadere.simulator.control.simulation.Simulation;
import org.vadere.simulator.projects.Scenario;
import org.vadere.simulator.projects.VadereProject;
import org.vadere.simulator.projects.io.IOVadere;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RunTest {

    private static Logger logger = Logger.getLogger(RunTest.class);

    public static String projectPath = "/home/acer/Documents/Master INFO/Mémoire/vadere/Scenarios/TrajectoryTest/";

    public static Path path = Paths.get(projectPath, IOUtils.SCENARIO_DIR);

    private static VadereProject project;

    public static void main(String[] args) throws IOException {
        project = IOVadere.readProject(projectPath);

        ArrayList<Scenario> scenarios = new ArrayList<>(project.getScenarios());

        TrajectoriesInfoRun info = new TrajectoriesInfoRun(projectPath, scenarios, "test");
        logger.info("Simulation finished");




    }
}
