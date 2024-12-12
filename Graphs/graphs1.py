import matplotlib.pyplot as plt
import json

def draw_scene(jsonFile):
    
    directory = "../resultsSimulations/json/" 
    
    with open(directory + jsonFile + ".json", 'r') as file:
        data = json.load(file)
    
    # Dimensions de la scène
    bounds = data['bounds']
    scene_width = bounds['width']
    scene_height = bounds['height']

    # Préparation de la figure
    fig, ax = plt.subplots(figsize=(scene_width / 2, scene_height / 2))
    ax.set_xlim(0, scene_width)
    ax.set_ylim(0, scene_height)

    # Dessiner les cibles (targets) en rouge
    for target_id, target in data['targets'].items():
        if target['type'] == 'RECTANGLE':
            rect = plt.Rectangle((target['x'], target['y']), target['width'], target['height'], color='red')
            ax.add_patch(rect)

    # Dessiner les obstacles en gris
    for obstacle_id, obstacle in data['obstacles'].items():
        if obstacle['type'] == 'RECTANGLE':
            rect = plt.Rectangle((obstacle['x'], obstacle['y']), obstacle['width'], obstacle['height'], color='gray')
            ax.add_patch(rect)

    # Dessiner les trajectoires en bleu
    for trajectory_id, trajectory_str in data['trajectories'].items():
        trajectory = json.loads(trajectory_str)  # Convertir la chaîne JSON en liste Python
        trajectory_x = [point[0] for point in trajectory]
        trajectory_y = [point[1] for point in trajectory]
        ax.plot(trajectory_x, trajectory_y, color='blue')

    # Ajuster les proportions et afficher la scène
    ax.set_aspect('equal', adjustable='box')
    plt.title(jsonFile)
    #plt.gca().invert_yaxis()  # Inverser l'axe Y pour correspondre aux coordonnées du JSON
    plt.show()
   

filenames = ["test_BHM", "test_CA", "test_GN", "test_OSM", "test_SFM"]
for name in filenames:
    draw_scene(name)