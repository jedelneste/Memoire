package org.vadere.simulator.control.simulation;

import org.vadere.simulator.Run;
import org.vadere.simulator.projects.Scenario;
import org.vadere.simulator.projects.SimulationResult;
import org.vadere.simulator.projects.VadereProject;
import org.vadere.simulator.projects.io.IOVadere;
import org.vadere.simulator.utils.cache.ScenarioCache;
import org.vadere.state.types.OptimizationType;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimulationRun {

    private static Logger logger = Logger.getLogger(SimulationRun.class);

    private static final String projectPath = "/home/acer/Bureau/Vadere/Scenarios/DifferentScenarios/";
    private static final Path path = Paths.get(projectPath, IOUtils.SCENARIO_DIR);

    private static Scenario scenario;
    private int scenarioIdx;
    private String optimizerName;

    private final HashMap<String, Double> results = new HashMap<>();

    public SimulationRun(VadereProject project, int idx, String optimizer) {
        this.scenarioIdx = idx;
        this.optimizerName = optimizer;
        scenario = project.getScenarioByName("Scenario"+scenarioIdx);
        scenario.getScenarioStore().getAttributesOSM().setOptimizationType(OptimizationType.valueOf(optimizer));
    }


    public void run() throws IOException {

        logger.info("Running scenario {} with optimizer {}", scenarioIdx, optimizerName);

        final ScenarioRun scenarioRun = new ScenarioRun(scenario, null, path.resolve(IOUtils.SCENARIO_DIR).resolve(scenario.getName() + IOUtils.SCENARIO_FILE_EXTENSION), ScenarioCache.load(scenario, path.resolve(IOUtils.SCENARIO_DIR)));

        scenarioRun.run();

        SimulationResult result = scenarioRun.getSimulationResult();

        double evacuationTimeSimulated = scenarioRun.getSimulation().getCurrentTime();

        Duration time = result.getRunTime();

        double simulationTime = time.toMillis() / 1000.0;

        results.put("EvacuationTime", evacuationTimeSimulated);
        results.put("SimulationTime", simulationTime);
    }

    public HashMap<String, Double> getResults() { return results; }

    public Double getEvacuationTime() { return results.get("EvacuationTime"); }

    public Double getSimulationTime() { return results.get("SimulationTime"); }

    public int getScenarioIdx() { return scenarioIdx; }

    public String getOptimizerName() { return optimizerName; }
}