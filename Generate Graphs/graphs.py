import matplotlib.pyplot as plt
import json
import ast


""" 
Plot le graphe des trajectoires, jsonFile est le nom du fichier correspondant à la simulation
"""
def trajectory_graph(jsonFile):
    
    directory = "../resultsSimulations/" 
    
    with open(directory + jsonFile + ".json", 'r') as file:
        data = json.load(file)
    
    topo = data["topography"]
    # Dimensions de la scène
    bounds = topo['bounds']
    scene_width = bounds['width']
    scene_height = bounds['height']

    # Préparation de la figure
    fig, ax = plt.subplots(figsize=(scene_width / 2, scene_height / 2))
    ax.set_xlim(0, scene_width)
    ax.set_ylim(0, scene_height)

    # Dessiner les cibles (targets) en rouge
    for target_id, target in topo['targets'].items():
        if target['type'] == 'RECTANGLE':
            rect = plt.Rectangle((target['x'], target['y']), target['width'], target['height'], color='red')
            ax.add_patch(rect)

    # Dessiner les obstacles en gris
    for obstacle_id, obstacle in topo['obstacles'].items():
        if obstacle['type'] == 'RECTANGLE':
            rect = plt.Rectangle((obstacle['x'], obstacle['y']), obstacle['width'], obstacle['height'], color='gray')
            ax.add_patch(rect)
            
    firstPedId = data["first pedestrian id"]
    lastPedId = data["last pedestrian id"]
    trajectories_dict = {}
    
    for i in range(firstPedId, lastPedId+1):
        trajectories_dict[i] = []
    
    trajectories = data["trajectories"]
    for tmps in trajectories.keys():
        for id in trajectories[tmps].keys():
            lst_traj = ast.literal_eval(trajectories[tmps][id])
            trajectories_dict[int(id)].append((lst_traj[0], lst_traj[1]))

    #
    # Dessiner les trajectoires en bleu
    for i in range(firstPedId, lastPedId+1):
        trajectory_x = [point[0] for point in trajectories_dict[i]]
        trajectory_y = [point[1] for point in trajectories_dict[i]]
        ax.plot(trajectory_x, trajectory_y, color='blue')
    

    # Ajuster les proportions et afficher la scène
    ax.set_aspect('equal', adjustable='box')
    plt.title(jsonFile)
    #plt.gca().invert_yaxis()  # Inverser l'axe Y pour correspondre aux coordonnées du JSON
    plt.show()
  
    
def fundamental_diagram_graph(jsonFile):
    
    directory = "../resultsSimulations/" 
    
    with open(directory + jsonFile + ".json", 'r') as file:
        data = json.load(file)
    
    zone = data["density zone"]
    if(zone is None):
        print("La zone de densité n'est pas précisée")
        return -1

    x = zone["x"]
    y = zone["y"]
    width = zone["width"]
    height = zone["height"]
    
    area = width*height
    
    firstPedId = data["first pedestrian id"]
    lastPedId = data["last pedestrian id"]
    trajectories_dict = {}
    velocities_dict = {}
    
    for i in range(firstPedId, lastPedId+1):
        trajectories_dict[i] = []
    
    trajectories = data["trajectories"]
    velocities = data["velocities"]
    for tmps in trajectories.keys():
        for id in trajectories[tmps].keys():
            lst_traj = ast.literal_eval(trajectories[tmps][id])
            trajectories_dict[int(id)].append((lst_traj[0], lst_traj[1]))

    densities = []
    speeds = []
    
    times = trajectories.keys()
    
    for t in times:
        numPeds = 0
        vitesse = 0
        for pedId in trajectories[t].keys():
            pos = ast.literal_eval(trajectories[t][pedId])
            if (isInZone(pos, x, y, width, height)):
                numPeds += 1
                vitesse += velocities[t][pedId]
        if numPeds != 0:
            densities.append(numPeds/area)
            speeds.append(vitesse/numPeds)
        else:
            densities.append(0)
            speeds.append(0)
            
    times = [float(t) for t in times]
    
    fig, axs = plt.subplots(1, 2, figsize=(12, 6)) 
    
    # Premier graphe
    axs[0].plot(times, speeds, color="blue")
    axs[0].set_title("Vitesse en fonction du temps")
    axs[0].set_xlabel("Temps [s]")
    axs[0].set_ylabel("Vitesse [m/s]")
    axs[0].legend()
    
    # Deuxième graphe
    axs[1].plot(times, densities, color="green")
    axs[1].set_title("Densité en fonction du temps")
    axs[1].set_xlabel("Temps [s]")
    axs[1].set_ylabel("Densité [piéton/m^2]")
    axs[1].legend()
    
    # Ajouter un titre commun à l'ensemble de l'image
    fig.suptitle(f"{jsonFile}", fontsize=16)

    # Ajuster l'espacement entre les sous-graphes
    fig.tight_layout(rect=[0, 0, 1, 0.95])  # Laisser de l'espace pour le titre global

    # Afficher l'image
    plt.show()

                
        
    
def isInZone(position, x, y, width, height):
    if (position[0] >= x and position[0] <= x+width):
        if(position[1] >= y and position[1] <= y+height):
            return True
    return False  


fundamental_diagram_graph("test_fundamentalDiagram_OSM")


