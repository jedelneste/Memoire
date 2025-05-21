from utils import get_files
import matplotlib.pyplot as plt
import csv


def plotsSimpleScenario(directory, csvFiles, colors):
    
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
        plt.plot(x, y, '-o', color=colors[idx], label=n)
    
    plt.legend()
    plt.title("Execution times of one simulation with different numbers of pedestrians")
    plt.xlabel("Number of pedestrians")
    plt.ylabel("Execution time [s]")
    plt.savefig("graphs/scalability/executionTimesSimpleScenario.png")
    plt.show()
        



def plotsComplexScenario(directory, csvFiles, colors):
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
            plt.plot(x, y, '-o', color=colors[idx], label=n)
        
        plt.legend()
        plt.title("Execution times of one simulation with different numbers of pedestrians")
        plt.xlabel("Number of pedestrians")
        plt.ylabel("Execution time [s]")
        plt.savefig("graphs/scalability/executionTimesComplexScenario.png")
        plt.show()
        
    def plotExecutionTimesWithoutSFM():
        
        for idx, tupl in enumerate(data.items()):  
            n = tupl[0]
            x = tupl[1]["peds"]
            y = tupl[1]["executionTime"]
            if n == "SFM_Obs":
                x = tupl[1]["peds"][0:4]
                y = tupl[1]["executionTime"][0:4]
            plt.plot(x, y, '-o', color=colors[idx], label=n)
        
        plt.legend()
        plt.title("Execution times of one simulation with different numbers of pedestrians")
        plt.xlabel("Number of pedestrians")
        plt.ylabel("Execution time [s]")
        plt.savefig("graphs/scalability/executionTimesComplexScenarioWithoutSFM.png")
        plt.show()
        

    plotExecutionTimes()
    plotExecutionTimesWithoutSFM()


def plotHugeScenario(directory, csvFiles, colors):

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
        plt.plot(x, y, '-o', color=colors[idx], label=names[idx])
    
    plt.legend()
    plt.title("Execution times of one simulation with huge numbers of pedestrians")
    plt.xlabel("Number of pedestrians")
    plt.ylabel("Execution time [s]")
    plt.savefig("graphs/scalability/executionTimesHugeScenario.png")
    plt.show()
    

def main():
    
    directory = "../Simulation results/ExecutionTime/"
    
    csvSimple = ["OSM", "CA", "SFM", "GNM"]
    csvComplex = ["OSM_Obs", "CA_Obs", "SFM_Obs", "GNM_Obs"]
    csvHuge = ["OSM_Huge", "CA_Huge", "GNM_Huge"]
    
    colors = ["tab:cyan", "tab:orange", "tab:green", "tab:red"]
    colorsHuge = ["tab:cyan", "tab:orange", "tab:red"]
    

    plotsSimpleScenario(directory, csvSimple, colors)
    plotsComplexScenario(directory, csvComplex, colors)
    plotHugeScenario(directory, csvHuge, colorsHuge)
    
    


if __name__ == "__main__":
    main()