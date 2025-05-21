# Liste des scripts Python
SCRIPTS = collisions.py evacuation_times.py fd.py stability.py scalability.py trajectories.py

# Commande Python
PYTHON = python3

# Par défaut, ajoute le dossier courant au PYTHONPATH
export PYTHONPATH := $(CURDIR)

# Définir une target pour chaque script
collisions:
	@echo "==> Running collisions.py"
	$(PYTHON) collisions.py

OSM_variation:
	@echo "==> Running evacuation_times.py"
	$(PYTHON) execution_times.py

fundamental_diagram:
	@echo "==> Running fd.py"
	$(PYTHON) fd.py

stability:
	@echo "==> Running stability.py"
	$(PYTHON) stability.py

scalability:
	@echo "==> Running scalability.py"
	$(PYTHON) scalability.py

trajectories:
	@echo "==> Running trajectories.py"
	$(PYTHON) trajectories.py

# Target pour tout exécuter
all: $(SCRIPTS:.py=)

.PHONY: all collisions execution_times fd stability scalability trajectories
