# 03 — Secondo Homework: Progettazione e Prima Versione Applicativa

Questa cartella raccoglie documentazione di supporto relativa al **secondo Homework** del progetto **Gestione Noleggio Barche**.

In questa fase il modello definito nel primo Homework viene trasformato in una vera applicazione Java, organizzata secondo l'architettura **BCE + DAO** e collegata alla persistenza su file CSV.

## Contenuto

- `Flusso_Applicativo_Gestione_Noleggio_Barche.pdf`  
  Descrive il flusso completo dell'applicazione, distinguendo il percorso del Cliente da quello dell'Amministratore.

- `Dati_Demo_Noleggio_Barche.pdf`  
  Raccoglie i dati dimostrativi utilizzati nei file CSV del progetto.

## Flusso applicativo

Il sistema parte da una sezione comune e si divide successivamente in due percorsi.

### Cliente

Il Cliente può:

- registrarsi;
- effettuare il login;
- visualizzare la propria Home;
- consultare le prenotazioni effettuate;
- accedere al catalogo;
- filtrare le barche;
- selezionare un'imbarcazione;
- creare una nuova prenotazione.

Una prenotazione valida viene registrata inizialmente nello stato `CONFERMATA`.

### Amministratore

L'Amministratore può:

- accedere tramite login;
- visualizzare le prenotazioni del sistema;
- avviare e terminare i noleggi;
- registrare ritiro e restituzione;
- controllare lo stato della flotta;
- programmare, avviare e completare le manutenzioni.

## Dati demo

La documentazione contiene un dataset dimostrativo composto da:

- 6 clienti;
- 3 sedi;
- 6 barche;
- 8 prenotazioni;
- 3 noleggi;
- 4 manutenzioni.

I dati sono **fittizi** e sono stati creati esclusivamente per verificare e mostrare il funzionamento dell'applicazione.

## Ruolo nel progetto

Questa cartella documenta il passaggio dalla progettazione concettuale alla **prima versione applicativa funzionante**, includendo il flusso utente e i dati utilizzati per le prove del sistema.
