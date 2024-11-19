package org.vadere.simulator;

import au.com.bytecode.opencsv.CSVWriter;
import org.vadere.simulator.control.simulation.SimulationRun;
import org.vadere.simulator.projects.VadereProject;
import org.vadere.simulator.projects.io.IOVadere;
import org.vadere.util.io.IOUtils;
import org.vadere.util.logging.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Run {


    private static Logger logger = Logger.getLogger(Run.class);

    public static String projectPath = "/home/acer/Documents/Master INFO/Mémoire/vadere/Scenarios/Different Scenarios/";

    public static Path path = Paths.get(projectPath, IOUtils.SCENARIO_DIR);

    public static List<String> optimizers = Arrays.asList("DISCRETE", "BRENT", "PSO", "GRADIENT", "NELDER_MEAD_CIRCLE", "NELDER_MEAD", "EVOLUTION_STRATEGY", "PATTERN_SEARCH", "POWELL");

    private static VadereProject project;


    public static void main(String[] args) throws IOException {
        project = IOVadere.readProject(projectPath);

        LinkedList<SimulationRun> simulationsToRun = new LinkedList<>();
        int scenarioIdx;
        String csvFile;
        switch (args.length){
            case 0:
                logger.info("Run all the scenarios with all the optimizers");
                csvFile = "resultsSimulations/allscenarios.csv";
                for (int i=1; i<11; i++){
                    for (String opti : optimizers){
                        simulationsToRun.add(new SimulationRun(project, i, opti));
                    }
                }
                break;
            case 1:
                scenarioIdx = Integer.parseInt(args[0]);
                csvFile = "resultsSimulations/scenario" + scenarioIdx + ".csv";
                logger.info("Run scenario {} with all optimizers", scenarioIdx);
                for (String opti : optimizers){
                    simulationsToRun.add(new SimulationRun(project, scenarioIdx, opti));
                }
                break;
            case 2:
                scenarioIdx = Integer.parseInt(args[0]);
                String optimizerName = args[1];
                csvFile = "resultsSimulations/scenario" + scenarioIdx + "_" + optimizerName + ".csv";
                logger.info("Run scenario {} with optimizer {}", scenarioIdx, optimizerName);
                try {
                    simulationsToRun.add(new SimulationRun(project, scenarioIdx, optimizerName));
                } catch (Exception e) {
                    logger.error(e);
                    e.printStackTrace();
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid number of arguments");
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {

            String[] header = {"Scenario", "Optimizer", "EvacuationTime", "SimulationTime"};
            writer.writeNext(header);

            for (SimulationRun simulationRun : simulationsToRun){
                simulationRun.run();

                logger.info(simulationRun.getOptimizerName());
                String[] line = {Integer.toString(simulationRun.getScenarioIdx()), simulationRun.getOptimizerName(), Double.toString(simulationRun.getEvacuationTime()), Double.toString(simulationRun.getSimulationTime())};
                writer.writeNext(line);

            }


        } catch (IOException e){
            logger.error(e);
            e.printStackTrace();
        }

    }

}
