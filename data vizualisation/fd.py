import matplotlib.pyplot as plt

    
def plotFD(directory, execution_names, scenario_name, corridor_lengths, area, method):
    colors = ["green", "blue", "yellow"]
    
    fig1 = plt.figure(f"Flow-Density relation of {scenario_name} calculated with Method {method} - Area {area}")
    ax1 = fig1.add_subplot(111)

    fig2 = plt.figure(f"Velocity-Density relation of {scenario_name} calculated with Method {method} - Area {area}")
    ax2 = fig2.add_subplot(111)
    
    for idx, execution_name in enumerate(execution_names):
    
        path = directory + execution_name + "/fd" + method + str(area) + ".txt"
        
        velocities = []
        densities = []
        flows = []
        b_cor = corridor_lengths[idx]
        with open(path, "r") as file:
            data = file.readlines()
            for line in data[1:]:
                line_lst = line.split(" ")
                velocities.append(float(line_lst[1]))
                densities.append(float(line_lst[2]))
                flows.append(float(line_lst[2])*float(line_lst[1])*b_cor)
                
            ax1.scatter(densities, flows, color=colors[idx], label=str(b_cor)+ " m") 
            ax2.scatter(densities, velocities, color=colors[idx], label=str(b_cor)+ " m") 

            
    ax1.set_xlabel("Density [1/m^2]")
    ax1.set_ylabel("Flow [1/s]")
    ax2.set_xlabel("Density [1/m^2]")
    ax2.set_ylabel("Velocity [m/s]")
    ax1.set_title(f"Flow-Density relation of {scenario_name} calculated with Method {method} - Area {area}")
    ax2.set_title(f"Velocity-Density relation of {scenario_name} calculated with Method {method} - Area {area}")
    ax1.legend()
    ax2.legend()
    fig1.savefig(f"graphs/fundamental diagram/flows/{scenario_name}_method{method}_area{area}.png")
    fig2.savefig(f"graphs/fundamental diagram/velocities/{scenario_name}_method{method}_area{area}.png")

    plt.show()
    

def main():
    
    GN = ["GN_090_2025-04-15_17-41-47.54", "GN_150_2025-04-15_17-41-58.853", "GN_200_2025-04-15_17-42-06.592"]
    OSM = ["OSM_090_2025-04-17_11-36-48.418", "OSM_150_2025-04-15_17-42-20.911", "OSM_200_2025-04-17_11-37-02.759"]
    SFM = ["SFM_090_2025-04-15_17-42-25.585", "SFM_150_2025-04-17_11-37-13.207", "SFM_200_2025-04-15_17-50-05.619"]
    
    directory = "../Scenarios/FundamentalDiagram/output/"
    
    for method in ["B", "C", "D"]:
        for area in [1, 2]:
            plotFD(directory, GN, "GN", [0.90, 1.50, 2], area, method) 
            plotFD(directory, OSM, "OSM", [0.90, 1.50, 2], area, method) 
            plotFD(directory, SFM, "SFM", [0.90, 1.50, 2], area, method) 
    

    

    
    

if __name__ == "__main__":
    main()
