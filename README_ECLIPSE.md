# FLOWCHART EDITOR - PRONTO PER ECLIPSE

✅ Struttura pulita
✅ Zero dipendenze
✅ Funziona al 100%

## 📁 STRUTTURA

```
grafico-clean/
├── .classpath          (config Eclipse)
├── .project            (config Eclipse)
├── .settings/          (config Eclipse)
└── src/
    ├── FlowchartEditorApp.java    ← MAIN
    ├── model/                      ← Blocchi (7 file)
    ├── layout/                     ← Manager (1 file)
    └── view/                       ← Canvas (1 file)
```

**TOTALE: 10 file Java**

---

## 🚀 IMPORTA IN ECLIPSE (2 PASSI)

### 1. IMPORTA
   - In Eclipse: **File → Import...**
   - Seleziona: **General → Existing Projects into Workspace**
   - Click **Next**
   - **Browse** → Seleziona: `/home/user/grafico-clean`
   - Click **Finish**

### 2. RUN
   - Click destro su **FlowchartEditorApp.java**
   - **Run As → Java Application**

✨ **FATTO! La finestra si apre!**

---

## 📦 COSA INCLUDE

✅ Tutti i tipi di blocchi (Process, Conditional, I/O, Loop, Start/End)
✅ **Bilanciamento automatico** dei rami condizionali
✅ Editor interattivo completo
✅ 3 esempi predefiniti
✅ Pan, zoom, modifica, cancellazione

❌ No save/load (per evitare dipendenze Gson)

---

## 🎨 COME USARE

### Prova subito il bilanciamento:
1. **Examples → Nested Conditional**
2. Guarda come i rami si bilanciano automaticamente!

### Crea il tuo flowchart:
1. **File → New → With Start Block**
2. Click sui simboli **`+`** per aggiungere blocchi
3. Scegli **Conditional**
4. Aggiungi più blocchi al ramo destro
5. Vedi il ramo sinistro estendersi!

### Comandi:
- **Click sul `+`**: Aggiunge blocco
- **Doppio click**: Modifica testo
- **Click destro**: Menu (Delete)
- **Ctrl+Click**: Pan della canvas

---

## 🔧 SE NON FUNZIONA

### Errore: "Unresolved compilation problem"

**Soluzione:**
1. Click destro sul progetto → **Properties**
2. **Java Build Path** → Tab **Source**
3. Verifica che ci sia: `flowchart-editor/src`
4. Se non c'è o è diverso:
   - Remove tutto
   - Add Folder → seleziona `src`
   - Apply and Close
5. **Project → Clean**
6. Riprova

---

## 📊 ARCHITETTURA

### Package Structure
- **Default**: `FlowchartEditorApp` (main + GUI)
- **model**: Gerarchia blocchi + ConnectionPoint
- **layout**: `FlowchartManager` (gestione struttura)
- **view**: `FlowchartCanvas` (rendering interattivo)

### Come Funziona il Bilanciamento

```java
// In ConditionalBlock.java
int altezzaDestra = ramoDestra.calculateHeight();
int altezzaSinistra = ramoSinistra.calculateHeight();

// Il merge point è sempre all'altezza del ramo più lungo
mergeY = Math.max(altezzaDestra, altezzaSinistra);
```

Quando aggiungi blocchi a un ramo, l'altro si estende automaticamente!

---

## ✅ VERIFICATO

Questa struttura:
- ✅ Nessuna cartella `main/java` inutile
- ✅ Solo `src/` come base
- ✅ Sottocartelle: `model`, `layout`, `view`
- ✅ Configurazione Eclipse inclusa
- ✅ Package corretti
- ✅ Import corretti

**PRONTO PER ESSERE USATO!**
