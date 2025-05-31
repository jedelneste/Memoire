import csv


def extraxt_time(directory, b, csvFile, nPeds):
    
    with open(csvFile, 'w', newline='') as csvfile:
        fieldnames = ['b_cor', 'delta t', 'flow', 'specific flow']
        writer = csv.writer(csvfile)
        writer.writerow(fieldnames)
    
        for b_name in b:
            with open(directory + "b_" + b_name + ".txt", "r") as file:
                lines = file.readlines()[1:]
                times = [float(lines[i].split(" ")[1].rstrip()) for i in range(len(lines))]
                b_cor = int(b_name)/100
                deltat = max(times) - min(times)
                flow = nPeds/deltat
                specificFlow = nPeds/(deltat * b_cor)
                writer.writerow([b_cor, deltat, flow, specificFlow])
        
     
     
     
def main():
    
    directory = "line cross/"
    fileNames = ["100", "150", "200", "250", "300", "350", "400"]
    csvFile = "flows.csv"
    nPeds = 350
    
    extraxt_time(directory, fileNames, csvFile, nPeds)
    
    
       
if __name__ == "__main__":
    main()
