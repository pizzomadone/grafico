# Flowchart Editor - Sugiyama-style Layout Version

Questa è una versione alternativa del Flowchart Editor che utilizza l'**algoritmo Sugiyama-style** per il layout gerarchico dei diagrammi a blocchi.

## Differenze rispetto alla versione originale (`src/`)

### Versione Originale (`src/`)
- Layout ricorsivo manuale
- I blocchi condizionali calcolano manualmente le posizioni dei branch
- Bilanciamento dei branch implementato tramite calcolo dell'altezza massima
- Le posizioni sono calcolate in modo incrementale durante il layout

### Versione Sugiyama (`src_Sugiyama-style/`)
- Layout basato sull'algoritmo Sugiyama gerarchico
- Quattro fasi:
  1. **Layer Assignment**: Assegnazione dei nodi a livelli verticali
  2. **Crossing Reduction**: Riduzione degli incroci tra archi
  3. **Horizontal Positioning**: Posizionamento orizzontale ottimale (metodo barycenter)
  4. **Coordinate Assignment**: Conversione in coordinate reali
- Migliore gestione di grafi complessi con molti nodi
- Layout più ordinato e prevedibile

## Struttura

```
src_Sugiyama-style/
├── model/                      # Classi dei blocchi (uguali alla versione originale)
│   ├── FlowBlock.java          # + metodo setPosition()
│   ├── ConditionalBlock.java   # Layout semplificato
│   ├── ProcessBlock.java
│   ├── IOBlock.java
│   ├── LoopBlock.java
│   ├── StartEndBlock.java
│   └── ConnectionPoint.java
├── layout/
│   ├── FlowchartManager.java  # Usa SugiyamaLayoutManager
│   └── SugiyamaLayoutManager.java  # NUOVO: Algoritmo Sugiyama
├── view/
│   └── FlowchartCanvas.java
└── FlowchartEditorApp.java
```

## Compilazione

```bash
# Dalla directory principale del progetto
mkdir -p bin_sugiyama
javac -d bin_sugiyama src_Sugiyama-style/model/*.java src_Sugiyama-style/layout/*.java src_Sugiyama-style/view/*.java src_Sugiyama-style/FlowchartEditorApp.java
```

## Esecuzione

```bash
java -cp bin_sugiyama FlowchartEditorApp
```

## Vantaggi dell'algoritmo Sugiyama

1. **Layout più ordinato**: I nodi sono organizzati in layer orizzontali chiari
2. **Migliore leggibilità**: Riduzione degli incroci tra connessioni
3. **Scalabilità**: Funziona meglio con diagrammi grandi e complessi
4. **Prevedibilità**: Il layout è più consistente e prevedibile

## Quando usare questa versione

- **Diagrammi complessi**: Con molti nodi e connessioni
- **Presentazioni**: Quando serve un layout pulito e professionale
- **Analisi**: Per visualizzare chiaramente la struttura gerarchica

## Quando usare la versione originale

- **Diagrammi semplici**: Con pochi nodi
- **Editing interattivo**: Quando serve controllo manuale del posizionamento
- **Bilanciamento custom**: Quando servono regole specifiche per il bilanciamento dei branch

## Implementazione tecnica

### SugiyamaLayoutManager

La classe principale che implementa l'algoritmo:

```java
public void layout(FlowBlock root) {
    // Fase 1: Assegnazione layer
    assignLayers(root, 0);

    // Fase 2: Posizionamento orizzontale
    initializeHorizontalPositions();
    optimizeHorizontalPositions();  // Metodo barycenter

    // Fase 3: Assegnazione coordinate
    assignCoordinates();

    // Fase 4: Layout interno blocchi
    layoutBlocksRecursively(root);
}
```

### Metodo Barycenter

Il posizionamento orizzontale usa il metodo **barycenter**:
- Ogni nodo è posizionato come media delle posizioni dei suoi genitori/figli
- Iterazione multipla (10 iterazioni) per convergere a una soluzione ottimale
- Alternanza tra passata discendente (top-down) e ascendente (bottom-up)

## Note

- Le due versioni mantengono la stessa interfaccia utente
- I file di salvataggio sono compatibili tra le due versioni
- Tutte le funzionalità di editing sono identiche
- Solo il motore di layout è diverso
