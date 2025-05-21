from matplotlib import pyplot as plt

def plot_evac_time():
    
    spaces = [i*0.1 for i in range(13)]

    times = [100.4, 112, 122, 137.2, 154.4, 169.2, 175.2, 173.2, 172, 157.6, 150, 141.2, 133.2]
    
    
    plt.plot(spaces, times, '-o', color="red")
    plt.title("Evacuation time according to the personal space of pedestrians")
    plt.xlabel("Personal Space of pedestrians [m]")
    plt.ylabel("Evacuation Time [s]")
    plt.savefig("graphs/evac.png")

    plt.show()
    
plot_evac_time()