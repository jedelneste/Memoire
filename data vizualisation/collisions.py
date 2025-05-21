import pandas as pd
import numpy as np
from itertools import combinations
from utils import get_subfolders

def count_collisions(txt_file):
    df = pd.read_csv(txt_file, delim_whitespace=True)
    collision_distance = 0.4  
    collisions_per_timestep = {}
    
    for time_step, group in df.groupby("timeStep"):
        positions = group[["pedestrianId", "x-PID5", "y-PID5"]].values
        collision_count = 0
        
        for (id1, x1, y1), (id2, x2, y2) in combinations(positions, 2):
            if np.hypot(x2 - x1, y2 - y1) < collision_distance:
                collision_count += 1
        collisions_per_timestep[time_step] = collision_count
    
    total_collisions = sum(collisions_per_timestep.values())
    average_collisions = total_collisions / len(collisions_per_timestep)
    return total_collisions, average_collisions


def main():
    directory = "../Scenarios/Collisions/output/"
    names = get_subfolders(directory)

    for name in names:
        model = name.split('_')[0]
        txt_file = directory + name + "/positions.txt"  # Remplacez par votre fichier
        total, average = count_collisions(txt_file)
        print(f"Nombre total de collisions pour {model} : {total}")
        print(f"Nombre moyen de collisions par pas de temps pour {model} : {average}")
        
if __name__ == "__main__":
    main()

    