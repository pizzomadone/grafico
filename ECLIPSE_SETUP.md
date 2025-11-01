# Guida Setup Eclipse

## Metodo 1: Importare come Progetto Maven (RACCOMANDATO)

### Prerequisiti
- Eclipse con plugin M2E (Maven) installato (incluso in Eclipse IDE for Java Developers)

### Passi:

1. **Scarica/Clona il progetto completo**
   - Assicurati di avere sia `src/` che `pom.xml`

2. **In Eclipse:**
   - File → Import...
   - Espandi "Maven"
   - Seleziona "Existing Maven Projects"
   - Click "Next"

3. **Seleziona il progetto:**
   - Click "Browse..."
   - Seleziona la cartella contenente `pom.xml`
   - Click "Open"

4. **Importa:**
   - Eclipse dovrebbe rilevare automaticamente il `pom.xml`
   - Assicurati che il checkbox accanto al progetto sia selezionato
   - Click "Finish"

5. **Attendi:**
   - Eclipse scaricherà automaticamente le dipendenze (Gson)
   - Guarda la barra di progresso in basso a destra
   - Attendi il messaggio "Build Success"

6. **Esegui:**
   - Click destro su `FlowchartEditorApp.java`
   - Run As → Java Application

---

## Metodo 2: Convertire Progetto Esistente

Se hai già creato un progetto Java normale in Eclipse:

1. **Copia `pom.xml`** nella root del progetto (stesso livello di `src/`)

2. **In Eclipse:**
   - Click destro sul progetto
   - Configure → Convert to Maven Project
   - Oppure: Maven → Update Project (Alt+F5)

3. **Aggiorna dipendenze:**
   - Click destro sul progetto
   - Maven → Update Project
   - Seleziona "Force Update of Snapshots/Releases"
   - Click OK

---

## Metodo 3: Senza Maven (Dipendenze Manuali)

Se non vuoi usare Maven:

### 1. Scarica Gson:
- Vai su https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/
- Scarica `gson-2.10.1.jar`

### 2. Aggiungi a Eclipse:
- Click destro sul progetto
- Build Path → Configure Build Path
- Libraries tab
- Click "Add External JARs..."
- Seleziona `gson-2.10.1.jar`
- Click "Apply and Close"

### 3. Modifica il package:
Il problema è che hai `main.java.com.flowchart` invece di `com.flowchart`

**IMPORTANTE:** La struttura delle directory deve essere:
```
src/
└── main/
    └── java/
        └── com/
            └── flowchart/
                ├── FlowchartEditorApp.java
                ├── model/
                ├── layout/
                ├── view/
                └── serialization/
```

**NON:**
```
src/
└── main/           ← Eclipse sta interpretando "main" come parte del package
    └── java/       ← Questo dovrebbe essere ignorato da Eclipse
        └── com/
```

---

## Risoluzione Errore "Unresolved compilation problem"

### Causa:
Eclipse ha interpretato male la struttura delle cartelle.

### Soluzione Rapida:

1. **In Eclipse, verifica Source Folder:**
   - Click destro sul progetto → Properties
   - Java Build Path → Source tab
   - Dovresti vedere: `src/main/java` come source folder
   - Se vedi `src` o altro, rimuovilo e aggiungi `src/main/java`

2. **Pulisci il progetto:**
   - Project → Clean...
   - Seleziona il tuo progetto
   - Click "Clean"

3. **Ricostruisci:**
   - Project → Build Project

---

## Verifica Setup Corretto

### Controllo 1: Package Explorer
Dovresti vedere:
```
📁 flowchart-editor
 ├── 📁 src/main/java
 │   └── 📦 com.flowchart
 │       ├── FlowchartEditorApp.java
 │       ├── 📦 model
 │       ├── 📦 layout
 │       ├── 📦 view
 │       └── 📦 serialization
 ├── 📁 src/main/resources (può essere vuoto)
 ├── 📁 Maven Dependencies
 │   └── gson-2.10.1.jar
 ├── 📁 JRE System Library
 └── pom.xml
```

### Controllo 2: Nessun Errore Rosso
- Non devono esserci X rosse sui file
- Nessun import non risolto

### Controllo 3: Esegui
- Click destro su `FlowchartEditorApp.java`
- Run As → Java Application
- Dovrebbe aprirsi la finestra del programma

---

## Problemi Comuni

### Errore: "Gson cannot be resolved"
**Soluzione:** Dipendenze Maven non scaricate
- Click destro sul progetto → Maven → Update Project
- Oppure aggiungi Gson manualmente (vedi Metodo 3)

### Errore: "The declared package does not match the expected package"
**Soluzione:** Struttura cartelle sbagliata
- Verifica che `com` sia dentro `src/main/java/`
- NON dentro `src/main.java.com/`

### Errore: "Source folder is not a Java project"
**Soluzione:**
- Click destro sul progetto → Configure → Convert to Java Project
- Poi segui Metodo 1 o 2

---

## Quick Fix per il Tuo Errore Specifico

Il tuo errore mostra: `main.java.com.flowchart.FlowchartEditorApp`

Questo significa che Eclipse pensa che il package sia `main.java.com.flowchart` invece di `com.flowchart`.

### Fix:
1. Verifica la struttura delle cartelle
2. In Eclipse Build Path, assicurati che la Source Folder sia `src/main/java`
3. NON `src`

Oppure ricrea il progetto con Metodo 1.
