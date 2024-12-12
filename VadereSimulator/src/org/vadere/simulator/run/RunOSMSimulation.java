package org.vadere.simulator.run;

import org.vadere.simulator.control.simulation.ScenarioRun;
import org.vadere.simulator.control.simulation.Simulation;
import org.vadere.simulator.projects.Scenario;
import org.vadere.simulator.projects.SimulationResult;
import org.vadere.simulator.projects.VadereProject;
import org.vadere.simulator.utils.cache.ScenarioCache;
import org.vadere.state.types.OptimizationType;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashMap;

public class RunOSMSimulation {

    private static Logger logger = Logger.getLogger(RunOSMSimulation.class);

    private static final String projectPath = "/home/acer/Bureau/Vadere/Scenarios/DifferentScenarios/";
    private static final Path path = Paths.get(projectPath, IOUtils.SCENARIO_DIR);

    private Scenario scenario;
    private VadereProject vadereProject;
    private String scenarioName;
    private boolean keepTrackTrajectories;

    private final HashMap<String, Double> results = new HashMap<>();

    private Simulation simulation = null;

    public RunOSMSimulation(VadereProject project, int idx, String optimizer) throws IOException {
        this.vadereProject = project;
        this.scenarioName = "Scenario"+idx;
        this.scenario = vadereProject.getScenarioByName(this.scenarioName);
        this.scenario.getScenarioStore().getAttributesOSM().setOptimizationType(OptimizationType.valueOf(optimizer));
        this.keepTrackTrajectories = false;
        run();

    }

    public RunOSMSimulation(VadereProject project, String scenarioName) throws IOException {
        this(project, scenarioName, false);
        run();
    }

    public RunOSMSimulation(VadereProject project, String scenarioName, boolean keepTrackTrajectories) throws IOException {
        this.vadereProject = project;
        this.scenarioName = scenarioName;
        this.scenario = vadereProject.getScenarioByName(this.scenarioName);
        this.keepTrackTrajectories = keepTrackTrajectories;
        run();
    }


    public void run() throws IOException {

        final ScenarioRun scenarioRun = new ScenarioRun(this.scenario, null, path.resolve(IOUtils.SCENARIO_DIR).resolve(scenario.getName() + IOUtils.SCENARIO_FILE_EXTENSION), ScenarioCache.load(this.scenario, path.resolve(IOUtils.SCENARIO_DIR)), keepTrackTrajectories);

        scenarioRun.run();

        this.simulation = scenarioRun.getSimulation();
        SimulationResult result = scenarioRun.getSimulationResult();

        double evacuationTimeSimulated = scenarioRun.getSimulation().getCurrentTime();

        Duration time = result.getRunTime();

        double simulationTime = time.toMillis() / 1000.0;

        results.put("EvacuationTime", Math.round(evacuationTimeSimulated * 100.0) / 100.0);
        results.put("SimulationTime", simulationTime);
    }

    public HashMap<String, Double> getResults() { return results; }

    public Double getEvacuationTime() { return results.get("EvacuationTime"); }

    public Double getSimulationTime() { return results.get("SimulationTime"); }

    public Simulation getSimulation() { return simulation; }

}