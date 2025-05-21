# My Experiments

## Scenarios Folder

Different scenario folders were created for the different simulations carried out:

* **Collisions** : Simulations to compute the number of collisions between pedestrians
* **DifferentScenarios** : Simulations to test the models on different real-life scenarios
* **ExecutionTime** : Simulations to compute the execution time of the models
* **FundamentalDiagram** : Simulations to build the fundamental diagram
* **GN_VP** : Variation of parameters of the Gradient Navigation Model
* **OSM_VP_Search** : First part of the variation of parameters of the Optimal Step Model
* **OSM_VP_Spaces** : Second part of the variation of parameters of the Optimal Step Model
* **StabilityStudy** : Study of the stability of the evacuation time
* **Trajectories** : Simulations to observe the trajectories of pedestrians

Any other scenario folder comes from the authors of Vadere

## Run Experiments

Some of the experiments have to be manually run because of the need to visually analyse the behavior of the crowd.
I created a `run` folder in `vadere-simulator` in order to run the experiments. 
In this folder, it is the `RunTest` file to run to launch the experiments.

## Generate Graphs

In the `data vizualisation` folder, you will find the different python scripts to generate the different graphs and a Makefile with different targets for the different graphs.
