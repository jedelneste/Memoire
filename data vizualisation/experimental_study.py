import matplotlib.pyplot as plt


b_seyfried = [0.8, 0.9, 1, 1.1, 1.2]
seyfried = [1.77*0.8, 1.91*0.9, 2.08, 1.93*1.1, 1.81*1.2]

b_kretz = [0.4, 0.5, 0.60, 0.70, 0.80, 0.90, 1, 1.20, 1.40, 1.6]
kretz = [0.9, 1.05, 1.1, 1.25, 1.45, 1.6, 1.8, 2, 2.1, 2.2]

b_rupprecht = [0.90, 1.00, 1.10, 1.20, 1.40, 1.60, 1.80, 2.00, 2.20, 2.50]
rupprecht = [1.8, 2.1, 2.3, 2.6, 3, 3.4, 4.1, 4.2, 4.7, 6]

b_liao = [2.4, 3.0, 3.6, 4.4]
liao = [6, 6.4, 8.3, 9.5]

b = [1, 1.5, 2, 2.5, 3, 3.5, 4]
data = [2.1, 3.13, 3.9, 4.5, 5, 5.7, 6]


plt.scatter(b_seyfried, seyfried, marker="h", color="gold", label="Seyfried")
plt.scatter(b_kretz, kretz, marker="^", color="violet", label="Kretz")
plt.scatter(b_rupprecht, rupprecht, marker="*", color="greenyellow", label="Rupprecht")
plt.scatter(b_liao, liao, marker="d", color="turquoise", label="Liao")
plt.scatter(b, data, marker="o", color="red", label="Our model")
plt.title("Influence of bottleneck width on flow according to \n different empirical studies", fontsize=16)
plt.xlabel("Size of the bottleneck [m]", fontsize=14)
plt.ylabel("Flow of pedestrians [$s^{-1}$]", fontsize=14)
plt.legend()
plt.savefig("graphs/empirical study.pdf")
plt.show()