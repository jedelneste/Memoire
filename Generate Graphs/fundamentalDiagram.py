import numpy as np
import matplotlib.pyplot as plt
from scipy.interpolate import interp1d
import json
import sys

def get_data(jsonFile):

    directory = "../resultsSimulations/FundamentalDiagram/"

    with open(directory + jsonFile + ".json", 'r') as file:
        data = json.load(file) 
        
    area = data["area"]
    all_speeds = data["speeds"]
    times = all_speeds.keys()
    densities = []
    speeds = []
    
    for t in times:
        numPeds = len(all_speeds[t].keys())
        if numPeds != 0:
            densities.append(numPeds/area)
            vitesse = 0
            for v in all_speeds[t].values():
                vitesse += v
            speeds.append(vitesse/numPeds)
        else:
            densities.append(0)
            speeds.append(0)
    
    times = [float(t) for t in times]
    
    return area, times, densities, speeds


def density_graph(times, densities, filename):
    
    title = f"{filename} : density of the bottleneck"
    # Tracer les points et les relier avec une ligne
    plt.figure(figsize=(10, 6))
    plt.plot(times, densities, '-', label="Densities")  # '-o' relie les points avec une ligne et marque les points

    # Ajouter des détails au graphique
    plt.title(title)
    plt.xlabel("Time (s)")
    plt.ylabel("Density (ped/m^2)")
    plt.legend()
    plt.grid(True)
    plt.savefig(f"graphs/{filename}_density.png")
    plt.show()
        

def speed_graph(times, speeds, filename):
    
    title = f"{filename} : average speeds of the bottleneck"
    
    # Tracer les points et les relier avec une ligne
    plt.figure(figsize=(10, 6))
    plt.plot(times, speeds, '-', label="Speeds") 
    
    # Ajouter des détails au graphique
    plt.title(title)
    plt.xlabel("Time (s)")
    plt.ylabel("Speeds (m/s)")
    plt.legend()
    plt.grid(True)
    plt.savefig(f"graphs/{filename}_velocities.png")
    plt.show()
    



def main():
    args = sys.argv
    if (len(args)==2):
        filename = args[1]
        
        area, times, densities, speeds = get_data(filename)
    
        density_graph(times, densities, filename)
        speed_graph(times, speeds, filename)
    else :
        names = ["fd_BHM", "fd_GN", "fd_OSM"]
        for name in names:
            area, times, densities, speeds = get_data(name)
    
            density_graph(times, densities, name)
            speed_graph(times, speeds, name)
        
    
    

    

if __name__ == "__main__":
    main()
