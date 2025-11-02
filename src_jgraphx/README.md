# Flowchart Editor - JGraphX Version

Questa è una versione del Flowchart Editor che usa **JGraphX** (JGraph 6) e **solo Swing** (no JavaFX).

## Installazione in Eclipse

### Passo 1: Importare la libreria JGraphX

1. Assicurati di aver già scaricato la libreria JGraphX (jgraphx.jar)
2. In Eclipse, fai clic destro sul progetto → **Build Path** → **Configure Build Path**
3. Vai alla tab **Libraries**
4. Clicca **Add External JARs...** (o **Add JARs...** se il file è già nel progetto)
5. Seleziona il file `jgraphx.jar`
6. Clicca **Apply and Close**

### Passo 2: Copiare la cartella src_jgraphx

1. Copia l'intera cartella `src_jgraphx` nel tuo progetto Eclipse
2. Eclipse dovrebbe riconoscere automaticamente i file `.java`
3. Se non vengono riconosciuti, fai clic destro sulla cartella → **Build Path** → **Use as Source Folder**

### Passo 3: Eseguire il programma

1. Apri il file `FlowchartEditorApp.java`
2. Fai clic destro → **Run As** → **Java Application**
3. L'applicazione dovrebbe partire!

## Struttura del Progetto

```
src_jgraphx/
├── FlowchartEditorApp.java   # Applicazione principale con menu e toolbar
├── FlowchartPanel.java        # Pannello con JGraphX per visualizzare il flowchart
└── README.md                  # Questo file
```

## Caratteristiche

- ✅ **Solo Swing** - Nessuna dipendenza da JavaFX
- ✅ **JGraphX** - Usa la libreria JGraphX per i grafi
- ✅ **Tipi di blocchi**:
  - Process (Rettangolo blu)
  - Conditional (Diamante giallo)
  - I/O (Parallelogramma verde)
  - Loop (Esagono arancione)
  - Start/End (Rettangolo arrotondato grigio)
- ✅ **Layout automatico** - Layout gerarchico automatico
- ✅ **Editing interattivo**:
  - Click per selezionare
  - Double-click per editare il testo
  - Right-click per menu contestuale
  - Delete per eliminare
- ✅ **Zoom e Pan**:
  - Rotellina del mouse per zoom
  - Ctrl+Click e trascina per pan
  - Ctrl+Plus/Minus per zoom in/out
- ✅ **Esempi predefiniti** - Menu Examples con flowchart di esempio

## Come Usare

### Creare Blocchi

1. Usa i pulsanti nella toolbar (+ Process, + Conditional, etc.)
2. Inserisci il testo del blocco
3. Il blocco viene aggiunto e connesso automaticamente

### Editare Blocchi

- **Double-click** su un blocco per editare il testo
- **Click** per selezionare
- **Delete** per eliminare
- **Right-click** per menu contestuale

### Navigazione

- **Mouse wheel** - Zoom in/out
- **Ctrl+Click e trascina** - Pan (spostare la vista)
- **Ctrl+0** - Reset zoom

### Menu

- **File → New** - Nuovo flowchart vuoto
- **File → Exit** - Esci
- **Examples** - Carica flowchart di esempio
- **Edit** - Modifica/elimina blocchi
- **View** - Zoom in/out/reset
- **Help** - Aiuto e informazioni

## Differenze dalla Versione Originale

La versione originale (cartella `src`) disegna i blocchi manualmente usando Graphics2D.

Questa versione (cartella `src_jgraphx`) usa JGraphX che:
- ✅ Gestisce automaticamente il rendering dei blocchi
- ✅ Fornisce zoom e pan integrati
- ✅ Ha layout automatico gerarchico
- ✅ Permette di spostare i blocchi con drag & drop
- ✅ Gestisce automaticamente le connessioni

## Requisiti

- Java 8 o superiore
- JGraphX (jgraphx.jar) - libreria già importata in Eclipse
- Nessuna altra dipendenza esterna

## Note

- Questa versione non salva/carica file (versione semplificata)
- Il layout automatico può essere riapplicato dal menu contestuale
- I blocchi possono essere spostati manualmente trascinandoli

## Esempi Disponibili

1. **Simple Conditional** - Esempio con un singolo if/else
2. **Loop Example** - Esempio con un ciclo while
3. **Nested Conditional** - Esempio con condizionali annidati

Buon divertimento con il Flowchart Editor! 🎨
