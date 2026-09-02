# 02 — Primo Homework: Analisi e Progettazione del Dominio

Questa cartella contiene il materiale prodotto durante il **primo Homework**, dedicato all'analisi iniziale e alla modellazione del dominio del progetto **Gestione Noleggio Barche**.

In questa fase il lavoro è concentrato principalmente sulla definizione del sistema, delle entità, delle relazioni e delle principali regole di dominio, prima dell'implementazione completa dell'applicazione Java.

## Contenuto

- `Presentazione_e_descrizione_del_sistema_informativo_per_noleggio_barche.pdf`  
  Documento del primo Homework con presentazione del dominio, descrizione del sistema informativo, principali entità e riferimento alla repository GitHub.

- `Class_diagram_Noleggio_barche.png`  
  Class Diagram UML del dominio.

## Dominio modellato

Il sistema rappresenta un servizio organizzato di **noleggio di imbarcazioni** gestito tramite più sedi operative.

Le principali classi individuate sono:

- `Cliente`;
- `Sede`;
- `Barca`;
- `BarcaMotore`;
- `BarcaVela`;
- `Prenotazione`;
- `Noleggio`;
- `Manutenzione`.

La classe `Barca` costituisce la superclasse astratta e viene specializzata nelle due tipologie concrete `BarcaMotore` e `BarcaVela`.

## Regole principali individuate

Durante l'analisi sono stati definiti vincoli relativi a:

- maggiore età del cliente;
- presenza e validità della patente nautica quando richiesta;
- capacità massima dei passeggeri;
- validità del periodo di prenotazione;
- disponibilità dell'imbarcazione;
- sovrapposizioni tra prenotazioni e periodi di indisponibilità;
- gestione degli stati di barche, prenotazioni, noleggi e manutenzioni.

## Ruolo nel progetto

Il primo Homework costituisce la **base progettuale** utilizzata nelle fasi successive per trasformare il modello concettuale in classi Java, Controller, DAO, persistenza e interfaccia grafica.
