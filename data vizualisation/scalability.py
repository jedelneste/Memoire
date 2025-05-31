from utils import get_files
import matplotlib.pyplot as plt
import csv


def plotsSimpleScenario(directory, csvFiles, colors, markers):
    
    data = {}

    for name in csvFiles:
        d = {}
        peds = []
        executionTimes = []
        with open(directory + name + ".csv", "r") as file:
            csvFile = csv.DictReader(file)
            for line in csvFile:
                peds.append(float(line["numOfPeds actual"]))
                executionTimes.append(float(line["executionTime"]))
        d["peds"] = peds
        d["executionTime"] = executionTimes
        data[name] = d

        
    for idx, tupl in enumerate(data.items()):  
        n = tupl[0]
        x = tupl[1]["peds"]
        y = tupl[1]["executionTime"]
        plt.plot(x, y, '-'+markers[idx], color=colors[idx], label=n)
    
    plt.legend()
    plt.title("Execution times of one simulation with different \n numbers of pedestrians", fontsize=16)
    plt.xlabel("Number of pedestrians", fontsize=14)
    plt.ylabel("Execution time [s]", fontsize=14)
    plt.savefig("graphs/scalability/executionTimesSimpleScenario.pdf")
    plt.show()
        



def plotsComplexScenario(directory, csvFiles, colors, markers):
    data = {}
    
    
    
    for name in csvFiles:
        d = {}
        peds = []
        executionTimes = []
        with open(directory + name + ".csv", "r") as file:
            csvFile = csv.DictReader(file)
            for line in csvFile:
                peds.append(float(line["numOfPeds actual"]))
                executionTimes.append(float(line["executionTime"]))
        d["peds"] = peds
        d["executionTime"] = executionTimes
        data[name] = d


    def plotExecutionTimes():
        
        for idx, tupl in enumerate(data.items()):  
            n = tupl[0]
            x = tupl[1]["peds"]
            y = tupl[1]["executionTime"]
            plt.plot(x, y, '-'+markers[idx], color=colors[idx], label=n)
        
        plt.legend()
        plt.title("Execution times of one simulation with different \n numbers of pedestrians", fontsize=16)
        plt.xlabel("Number of pedestrians", fontsize=14)
        plt.ylabel("Execution time [s]", fontsize=14)
        plt.savefig("graphs/scalability/executionTimesComplexScenario.pdf")
        plt.show()
        
    def plotExecutionTimesWithoutSFM():
        
        for idx, tupl in enumerate(data.items()):  
            n = tupl[0]
            x = tupl[1]["peds"]
            y = tupl[1]["executionTime"]
            if n == "SFM_Obs":
                x = tupl[1]["peds"][0:4]
                y = tupl[1]["executionTime"][0:4]
            plt.plot(x, y, '-'+markers[idx], color=colors[idx], label=n)
        
        plt.legend()
        plt.title("Execution times of one simulation with different \n numbers of pedestrians", fontsize=16)
        plt.xlabel("Number of pedestrians", fontsize=14)
        plt.ylabel("Execution time [s]", fontsize=14)
        plt.savefig("graphs/scalability/executionTimesComplexScenarioWithoutSFM.pdf")
        plt.show()
        

    plotExecutionTimes()
    plotExecutionTimesWithoutSFM()


def plotHugeScenario(directory, csvFiles, colors, markers):

    data = {}
    
    names = ["OSM", "CA", "GNM"]
    
    for name in csvFiles:
        d = {}
        peds = []
        executionTimes = []
        with open(directory + name + ".csv", "r") as file:
            csvFile = csv.DictReader(file)
            for line in csvFile:
                peds.append(float(line["numOfPeds actual"]))
                executionTimes.append(float(line["executionTime"]))
        d["peds"] = peds
        d["executionTime"] = executionTimes
        data[name] = d
        
        
    for idx, tupl in enumerate(data.items()):  
        x = tupl[1]["peds"]
        y = tupl[1]["executionTime"]
        plt.plot(x, y, '-'+markers[idx], color=colors[idx], label=names[idx])
    
    plt.legend()
    plt.title("Execution times of one simulation with huge \n numbers of pedestrians", fontsize=16)
    plt.xlabel("Number of pedestrians", fontsize=14)
    plt.ylabel("Execution time [s]", fontsize=14)
    plt.savefig("graphs/scalability/executionTimesHugeScenario.pdf")
    plt.show()
    

def main():
    
    directory = "../Simulation results/ExecutionTime/"
    
    csvSimple = ["OSM", "CA", "SFM", "GNM"]
    csvComplex = ["OSM_Obs", "CA_Obs", "SFM_Obs", "GNM_Obs"]
    csvHuge = ["OSM_Huge", "CA_Huge", "GNM_Huge"]
    
    colors = ["tab:cyan", "tab:orange", "tab:green", "tab:red"]
    colorsHuge = ["tab:cyan", "tab:orange", "tab:red"]
    
    markers = ["o", "^", "*", "s"]
    markersHuge = ["o", "^", "s"]
    

    plotsSimpleScenario(directory, csvSimple, colors, markers)
    plotsComplexScenario(directory, csvComplex, colors, markers)
    plotHugeScenario(directory, csvHuge, colorsHuge, markersHuge)
    
    


if __name__ == "__main__":
    main()