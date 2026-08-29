# Gestione Noleggio Barche

## Descrizione del progetto

Il progetto riguarda la realizzazione di un sistema per la **gestione del noleggio di barche**, sviluppato nell'ambito del corso di **Programmazione Object Oriented**.

L'obiettivo è modellare in modo chiaro e coerente le principali operazioni legate al noleggio, dalla registrazione del cliente fino alla prenotazione, al ritiro e alla restituzione della barca, includendo anche la gestione delle sedi e delle manutenzioni.

Il sistema è progettato secondo i principi della programmazione ad oggetti, rappresentando gli elementi principali del dominio tramite classi, relazioni, stati e regole che ne descrivono il comportamento.

## Dominio del sistema

Il dominio scelto è quello del **noleggio nautico**. Il sistema gestisce clienti, barche, prenotazioni, noleggi, sedi e interventi di manutenzione.

Le barche sono rappresentate attraverso una classe generale `Barca`, specializzata nelle due tipologie principali previste dal progetto:

- `BarcaMotore`, caratterizzata da informazioni come la potenza del motore e la capacità del serbatoio;
- `BarcaVela`, caratterizzata da informazioni come la superficie velica e l'altezza dell'albero.

Ogni barca possiede inoltre una **capacità massima di passeggeri**, una **tariffa giornaliera**, uno **stato operativo** e l'indicazione relativa all'eventuale necessità della **patente nautica**.

## Funzionamento generale

Un `Cliente` può registrarsi nel sistema indicando i propri dati personali e, se posseduta, la patente nautica.

Il cliente può effettuare una `Prenotazione` scegliendo una barca e un determinato periodo. Durante questa operazione il sistema tiene conto di elementi fondamentali come:

- disponibilità della barca;
- numero di passeggeri;
- capacità massima della barca;
- eventuale obbligo di patente nautica;
- validità della patente per il periodo richiesto.

Da una prenotazione confermata può essere avviato un `Noleggio`, attraverso il quale vengono registrati il ritiro e la successiva restituzione della barca.

Il sistema gestisce inoltre le `Manutenzioni`, che permettono di rappresentare i periodi durante i quali una barca non può essere utilizzata, e le `Sedi`, alle quali le diverse barche sono assegnate.

## Obiettivo della modellazione

La progettazione del dominio mira a garantire una rappresentazione coerente delle principali entità del sistema e delle loro relazioni, sfruttando concetti Object Oriented come:

- **incapsulamento**;
- **ereditarietà**;
- **generalizzazione e specializzazione**;
- **classi astratte**;
- **enumerazioni**;
- **associazioni e cardinalità**;
- **gestione degli stati**;
- **regole di dominio e di business**;
- **eccezioni personalizzate**.

Il modello è stato progettato in modo da mantenere separate le responsabilità delle diverse classi e rendere il sistema facilmente estendibile nelle successive fasi di sviluppo.
