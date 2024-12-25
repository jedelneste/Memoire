import pandas as pd
import matplotlib.pyplot as plt

# Charger le CSV
# Remplacez "data.csv" par le chemin vers votre fichier CSV
# Assurez-vous que votre CSV contient des colonnes 'x', 'mean', 'std'


def graph(fileName):
    directory = "../resultsSimulations/StabilityStudy/"

    data = pd.read_csv(directory + fileName + ".csv")

    # Vérification des données
    print(data.head())

    # Extraire les colonnes nécessaires
    x = data['Number of runs'].astype(int)  # Les labels ou positions sur l'axe des x
    mean = data['flow mean'].astype(float)  # Les moyennes
    std = data['flow variance'].astype(float)  # Les déviations standard

    # Dessiner le graphique
    plt.figure(figsize=(10, 6))
    plt.errorbar(x, mean, yerr=std, fmt='o', capsize=5, label='Moyenne avec écart-type')

    # Ajouter des détails au graphe
    plt.title('Graphique des moyennes avec déviations standard', fontsize=16)
    plt.xlabel('X', fontsize=14)
    plt.ylabel('Valeurs', fontsize=14)
    plt.ylim((0, max(mean)+max(std)+1))
    plt.xticks(ticks=x, labels=x.astype(int)) 
    plt.legend(fontsize=12)

    # Afficher le graphe
    plt.show()

graph("test")