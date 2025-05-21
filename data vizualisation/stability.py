import pandas as pd
import matplotlib.pyplot as plt
import csv
import numpy as np


def plotBox(directory):
    filenames = ["OSM", "GNM", "SFM"]
    
    data = []
    labels = ["OSM", "GNM", "SFM"]
    colors = ["cyan", "red", "green"]
    
    for name in filenames:
        with open(directory + name + ".csv", "r") as file:
            csvFile = csv.DictReader(file)
            x = []
            for line in csvFile:
                x.append(float(line["evacuationTime"]))
            data.append(x)
            print(f"Mean of {name} : {np.mean(x)}")
            print(f"Variance of {name} : {np.var(x)}")
            print(f"Standard deviation of {name} : {np.std(x)}")
    
    fig, ax = plt.subplots(figsize=(8, 6))
    box = ax.boxplot(data, patch_artist=True, widths=0.4)
    
    for patch, color in zip(box['boxes'], colors):
        patch.set_facecolor(color)

    for median in box['medians']:
        median.set(color='black', linewidth=2)
        
    ax.set_xticklabels(labels)

    plt.title("Distribution of the evacuation time")
    plt.ylabel("Evacuation time [s]")
    
    plt.savefig("graphs/stability/evacutionTime.png")
    plt.show()
    

def main():
    directory = "../Simulation results/StabilityStudy/"
    
    plotBox(directory)
    
if __name__ == "__main__":
    main()