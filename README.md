# Flowchart Editor

Un editor di diagrammi di flusso (flowchart) interattivo con bilanciamento automatico dei rami condizionali.

![Java](https://img.shields.io/badge/Java-11+-orange.svg)
![Swing](https://img.shields.io/badge/GUI-Swing-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

## 🎯 Caratteristiche Principali

- **Bilanciamento Automatico dei Rami**: Quando aggiungi un blocco a un ramo condizionale, l'altro ramo si estende automaticamente per mantenere la simmetria
- **Multipli Tipi di Blocchi**:
  - 📦 **Process** (Rettangolo): Azioni e processi
  - 💎 **Conditional** (Rombo): Decisioni if/else
  - 🔲 **I/O** (Parallelogramma): Input/Output
  - 🔁 **Loop** (Esagono): Cicli while/for
  - 🟢 **Start/End** (Rettangolo arrotondato): Inizio e fine
- **Editor Interattivo**: Clicca sui punti di connessione (+) per aggiungere blocchi
- **Serializzazione JSON**: Salva e carica i flowchart
- **Interfaccia Intuitiva**: Drag per navigare, doppio click per editare

## 🏗️ Architettura

### 1. Modello Dati (`model/`)

Gerarchia di classi per rappresentare i blocchi:

```
FlowBlock (abstract)
├── ProcessBlock
├── ConditionalBlock  ← Gestisce il bilanciamento dei rami
├── IOBlock
├── LoopBlock
└── StartEndBlock
```

**Classi chiave:**

- `FlowBlock.java`: Classe base astratta con metodi per:
  - `calculateHeight()`: Calcola l'altezza ricorsiva del sottoalbero
  - `calculateWidth()`: Calcola la larghezza necessaria
  - `layout(x, y)`: Posiziona il blocco e i suoi figli
  - `draw(Graphics2D)`: Renderizza il blocco

- `ConditionalBlock.java`: **CORE del bilanciamento**
  ```java
  @Override
  public void layout(int startX, int startY) {
      // Calcola altezze dei rami
      int trueHeight = trueBranch != null ? trueBranch.calculateHeight() : 0;
      int falseHeight = falseBranch != null ? falseBranch.calculateHeight() : 0;
      int maxHeight = Math.max(trueHeight, falseHeight);

      // Il punto di merge è sempre all'altezza del ramo più lungo
      mergePointY = startY + height + VERTICAL_SPACING + maxHeight;

      // Entrambi i rami convergono al merge point
  }
  ```

### 2. Gestione Layout (`layout/`)

- `FlowchartManager.java`: Gestisce la struttura del flowchart
  - Aggiunge/rimuove blocchi
  - Ricalcola il layout quando necessario
  - Fornisce metodi di ricerca (blocchi, connection points)
  - Include esempi predefiniti

### 3. Vista (`view/`)

- `FlowchartCanvas.java`: Canvas Swing interattivo
  - Rendering dei blocchi
  - Gestione eventi mouse (click, hover, pan)
  - Mostra i connection points cliccabili
  - Supporta modifica e cancellazione

### 4. Serializzazione (`serialization/`)

- `FlowchartSerializer.java`: Usa Gson per serializzare/deserializzare
  - Adapter personalizzato per gestire polimorfismo
  - Formato JSON leggibile

## 🔧 Come Funziona il Bilanciamento

### Problema Originale

Quando aggiungi blocchi al ramo destro di un condizionale, il ramo sinistro deve estendersi della stessa altezza per mantenere il punto di merge simmetrico.

### Soluzione Implementata

1. **Calcolo Altezze Ricorsivo**:
   ```java
   int calculateHeight() {
       int trueHeight = trueBranch != null ? trueBranch.calculateHeight() : 0;
       int falseHeight = falseBranch != null ? falseBranch.calculateHeight() : 0;
       return height + Math.max(trueHeight, falseHeight) + MERGE_SPACING;
   }
   ```

2. **Layout Basato su Altezza Massima**:
   ```java
   int maxBranchHeight = Math.max(trueHeight, falseHeight);
   mergePointY = startY + height + VERTICAL_SPACING + maxBranchHeight;
   ```

3. **Linee di Connessione Dinamiche**:
   - Se un ramo è più corto, la sua linea di connessione scende verticalmente fino al merge point
   - Garantisce che entrambi i rami convergano allo stesso punto

### Esempio Visivo

```
          ┌─────────┐
          │ x > 0?  │  ← ConditionalBlock
          └──┬───┬──┘
         True │   │ False
      ┌───────┘   └───────┐
      │                   │
  ┌───▼────┐          ┌───▼────┐
  │ y > 0? │          │result=0│
  └──┬──┬──┘          └───┬────┘
     │  │                 │
     │  └─────────┐       │ ← Estensione automatica
     └────────┐   │       │
              │   │       │
           ┌──▼───▼───────▼──┐
           │   Merge Point   │ ← Sempre allo stesso livello
           └─────────────────┘
```

## 🚀 Come Usare

### IMPORTANTE: Versione Semplificata per Eclipse

Questo progetto ora usa una **struttura semplificata** compatibile con Eclipse:

```
src/
├── FlowchartEditorApp.java    # Main class
├── model/                      # Blocchi (7 file)
├── layout/                     # Manager (1 file)
└── view/                       # Canvas (1 file)
```

### Importazione in Eclipse (RACCOMANDATO)

1. **File → Import...**
2. **General → Existing Projects into Workspace**
3. **Browse** → Seleziona questa cartella
4. **Finish**
5. Click destro su `FlowchartEditorApp.java` → **Run As → Java Application**

📖 **Guida completa:** Vedi `README_ECLIPSE.md`

### Versione Maven (Opzionale)

Se preferisci usare Maven (per save/load con Gson), vedi il branch con struttura Maven.

### Utilizzo

1. **Creare un Flowchart**:
   - File → New (Ctrl+N)
   - Scegli se partire vuoto o con un blocco Start

2. **Aggiungere Blocchi**:
   - Clicca sui simboli `+` (connection points)
   - Scegli il tipo di blocco
   - Inserisci il testo

3. **Editare**:
   - Doppio click sul blocco per modificare il testo
   - Click destro per menu contestuale (Edit/Delete)
   - Delete key per rimuovere blocco selezionato

4. **Navigare**:
   - Ctrl+Click o Middle-click per fare pan della canvas
   - Scorri con i bordi della finestra

5. **Salvare/Caricare**:
   - File → Save (Ctrl+S)
   - File → Open (Ctrl+O)
   - Formato: `.flowchart` (JSON)

## 🎨 Esempi Inclusi

L'applicazione include 3 esempi predefiniti:

1. **Simple Conditional**: Esempio base con if/else
2. **Loop Example**: Flowchart con ciclo while
3. **Nested Conditional**: Condizionali annidati (dimostra il bilanciamento)

Accedi da: **Examples → [nome esempio]**

## 📐 Struttura del Progetto

```
src/main/java/com/flowchart/
├── FlowchartEditorApp.java          # Main class, finestra principale
├── model/
│   ├── FlowBlock.java                # Classe base astratta
│   ├── ProcessBlock.java             # Blocco rettangolare
│   ├── ConditionalBlock.java         # Blocco rombo (CORE)
│   ├── IOBlock.java                  # Blocco parallelogramma
│   ├── LoopBlock.java                # Blocco esagono
│   ├── StartEndBlock.java            # Blocco arrotondato
│   └── ConnectionPoint.java          # Punto di connessione cliccabile
├── layout/
│   └── FlowchartManager.java         # Gestione struttura e layout
├── view/
│   └── FlowchartCanvas.java          # Canvas Swing interattivo
└── serialization/
    └── FlowchartSerializer.java      # Salvataggio/caricamento JSON
```

## 🛠️ Tecnologie Utilizzate

- **Java 11+**: Linguaggio di programmazione
- **Swing**: Framework GUI
- **Gson 2.10.1**: Serializzazione JSON
- **Maven**: Build e gestione dipendenze

## 💡 Logiche Implementate

### 1. **Approccio Ricorsivo per il Layout**

Ogni blocco implementa `calculateHeight()` e `layout()` che vengono chiamati ricorsivamente sull'albero del flowchart.

### 2. **Coordinate Assolute**

Dopo il layout, ogni blocco conosce la sua posizione assoluta (x, y) sulla canvas, semplificando il rendering.

### 3. **Connection Points**

Ogni blocco espone una lista di `ConnectionPoint` che rappresentano dove nuovi blocchi possono essere aggiunti. Questi sono:
- Renderizzati come rettangoli semi-trasparenti con `+`
- Evidenziati al hover del mouse
- Cliccabili per aggiungere blocchi

### 4. **Event Handling**

- `MouseMotionListener`: Traccia hover per evidenziare connection points
- `MouseListener`: Gestisce click per aggiungere/editare/cancellare
- `KeyListener`: Supporta scorciatoie (Delete, numeri per tipo blocco)

### 5. **Serializzazione Polimorfica**

Gson adapter personalizzato gestisce la gerarchia di `FlowBlock`, salvando il tipo concreto nel JSON.

## 🎓 Concetti Avanzati Implementati

1. **Pattern Composite**: `FlowBlock` e le sue sottoclassi formano un albero
2. **Visitor Pattern** (implicito): I metodi `draw()`, `calculateHeight()` visitano l'albero
3. **Template Method**: `FlowBlock` definisce il template, sottoclassi implementano dettagli
4. **Observer Pattern** (semplificato): La canvas osserva i cambiamenti e si aggiorna

## 🔮 Possibili Estensioni

- [ ] Zoom in/out funzionante
- [ ] Export a PNG/SVG
- [ ] Undo/Redo stack
- [ ] Drag & drop dei blocchi
- [ ] Temi di colore personalizzabili
- [ ] Generazione codice da flowchart
- [ ] Validazione struttura (cicli, percorsi non raggiungibili)

## 📝 Note Tecniche

### Perché Swing?

Swing è stato scelto per:
- Nessuna dipendenza esterna pesante
- Controllo completo sul rendering con `Graphics2D`
- Facile gestione eventi
- Cross-platform

### Performance

Il ricalcolo del layout è O(n) dove n è il numero di blocchi. Per flowchart molto grandi (>1000 blocchi), potrebbe essere necessario ottimizzare con caching.

### Thread Safety

L'applicazione usa il EDT (Event Dispatch Thread) di Swing per tutte le operazioni UI, garantendo thread safety.

## 🤝 Contribuire

Suggerimenti e contributi sono benvenuti! Le aree di miglioramento includono:

1. Implementare zoom funzionante
2. Aggiungere export immagini
3. Migliorare l'algoritmo di layout per flowchart molto complessi
4. Aggiungere più forme/stili

## 📄 Licenza

MIT License - Sentiti libero di usare questo codice per scopi educativi o commerciali.

## ✨ Autore

Implementato come soluzione al problema del bilanciamento dinamico dei rami nei flowchart editor.

---

**Buon Diagramming! 📊✨**
