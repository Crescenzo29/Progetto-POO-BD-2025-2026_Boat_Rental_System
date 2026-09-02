# Package `model`

Contiene le **classi del dominio** del progetto.

## Entità principali

- `Cliente.java`: dati del cliente e informazioni sulla patente nautica.
- `Barca.java`: classe astratta con le caratteristiche comuni delle barche.
- `BarcaMotore.java`: barca a motore.
- `BarcaVela.java`: barca a vela.
- `Sede.java`: sede in cui si trovano le barche.
- `Prenotazione.java`: prenotazione effettuata da un cliente.
- `Noleggio.java`: noleggio effettivo della barca.
- `Manutenzione.java`: intervento di manutenzione su una barca.

## Stati

- `StatoBarca.java`
- `StatoPrenotazione.java`
- `StatoNoleggio.java`
- `StatoManutenzione.java`

Gli enum definiscono gli stati validi delle principali entità. `package-info.java` descrive brevemente il package.
