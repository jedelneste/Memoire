import matplotlib.pyplot as plt


def plotTrajectories(directory, models, topography, target, obstacles):
    for name in models:
    
        trajectories = {}
        
        with open(directory + "positions_" + name + ".txt", "r") as file:
            data = file.readlines()
            for line in data[1:]:
                line_lst = line.split(" ")
                
                pedID = int(line_lst[1])
                xPos = float(line_lst[2])
                yPos = float(line_lst[3].strip())
                
                if pedID not in trajectories:
                    trajectories[pedID] = []
                
                trajectories[pedID].append((xPos, yPos))
        
        scene_width = topography['width']
        scene_height = topography['height']
        
        fig, ax = plt.subplots(figsize=(scene_width / 2, scene_height / 2))
        ax.set_xlim(0, scene_width)
        ax.set_ylim(0, scene_height)
        
        fig.suptitle(f"trajectories with {name}")
        
        rect = plt.Rectangle((target['x'], target['y']), target['width'], target['height'], color='red', label="target")
        ax.add_patch(rect)
        
        for obstacle in obstacles:
            rect = plt.Rectangle((obstacle['x'], obstacle['y']), obstacle['width'], obstacle['height'], color='gray', label="obstacle")
            ax.add_patch(rect)
        
        for trajectory_id, trajectory in trajectories.items():
            trajectory_x = [point[0] for point in trajectory]
            trajectory_y = [point[1] for point in trajectory]
            ax.plot(trajectory_x, trajectory_y, color='blue')
        
        plt.legend()
        plt.savefig(f"graphs/{directory}{name}.png")
        plt.show()
    

def trajectories_models():
    directory = "trajectories/models/"

    models = ["CA", "OSM", "GNM", "SFM"]

    topography = {"x" : 0.0,"y" : 0.0,"width" : 8.0,"height" : 10.0}

    target = {"x" : 3.5,"y" : 8.5,"width" : 1.0,"height" : 1.0}

    obstacles = [{"x" : 3.5,"y" : 5.0,"width" : 1.0,"height" : 1.0}]
    
    plotTrajectories(directory, models, topography, target, obstacles)  


def trajectories_parameters():
    directory = "trajectories/parameters/"
    
    parameters = ["000_000", "000_060", "000_120", "020_000", "020_060", "020_120", "045_000", "045_060", "045_120"]
    
    topography = {"x" : 0.0,"y" : 0.0,"width" : 8.0,"height" : 10.0}

    target = {"x" : 3.5,"y" : 8.5,"width" : 1.0,"height" : 1.0}

    obstacles = [{"x" : 3.5,"y" : 5.0,"width" : 1.0,"height" : 1.0}]
    
    plotTrajectories(directory, parameters, topography, target, obstacles)

def main():
    trajectories_models()
    
if __name__ == "__main__":
    main()