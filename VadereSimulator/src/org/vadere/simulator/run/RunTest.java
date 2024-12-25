package org.vadere.simulator.run;

import org.vadere.simulator.projects.Scenario;
import org.vadere.simulator.projects.VadereProject;
import org.vadere.simulator.projects.io.IOVadere;
import org.vadere.util.geometry.shapes.VRectangle;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


public class RunTest {

    private static Logger logger = Logger.getLogger(RunTest.class);

    public static String projectPath = "/home/acer/Documents/Master INFO/Mémoire/vadere/Scenarios/FundamentalDiagram/";

    public static Path path = Paths.get(projectPath, IOUtils.SCENARIO_DIR);

    private static VadereProject project;

    public static void main(String[] args) throws IOException {
        project = IOVadere.readProject(projectPath);

        Scenario scenario = project.getScenarioByName("OSM");

        VRectangle zone = new VRectangle(8, 11, 4, 1);
        Optional<VRectangle> zoneOption = Optional.of(zone);
        AllInfoRun info = new AllInfoRun(projectPath, scenario, "test_fundamentalDiagram_OSM", zoneOption);
        info.run();


    }
}
