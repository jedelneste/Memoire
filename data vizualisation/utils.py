import os

def get_subfolders(main_folder):
    try:
        # Liste des noms de sous-dossiers dans le dossier principal
        subfolders = [f.name for f in os.scandir(main_folder) if f.is_dir()]
        return subfolders
    except FileNotFoundError:
        print("Le dossier spécifié n'existe pas.")
        return []
    except PermissionError:
        print("Permission refusée pour accéder au dossier.")
        return []
    
def remove_prefix(text, prefix):
    if text.startswith(prefix):
        return text[len(prefix):]
    return text

def get_files(main_folder):
    try:
        # Liste des noms de fichiers dans le dossier principal
        files = [f.name for f in os.scandir(main_folder) if f.is_file()]
        return files
    except FileNotFoundError:
        print("Le dossier spécifié n'existe pas.")
        return []
    except PermissionError:
        print("Permission refusée pour accéder au dossier.")
        return []
    
def get_scenario_name(main_folder):
    
    files = get_files(main_folder)
    for file in files:
        if (len(file) >= 10):
            if file[-9:] == ".scenario":
                return file[:-9]
            
    print("Error : filename not found")
    return -1

def get_name(execution_name):
    name = ""
    i = 0
    while i < len(execution_name):
        c = execution_name[i]
        if c == "_":
            break
        name += c
        i += 1
    return name