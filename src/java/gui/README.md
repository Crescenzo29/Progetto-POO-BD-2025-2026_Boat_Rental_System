# Package `gui`

Contiene l'**interfaccia grafica Swing** dell'applicazione.

## File

- `MainFrame.java`: finestra principale e navigazione tra le schermate.
- `StartPanel.java`: schermata iniziale.
- `LoginPanel.java`: accesso Cliente o Admin.
- `RegistrazionePanel.java`: registrazione di un nuovo Cliente.
- `HomeClientePanel.java`: home del Cliente e riepilogo prenotazioni.
- `CatalogoPanel.java`: visualizzazione e filtro delle barche.
- `PrenotazionePanel.java`: creazione di una prenotazione.
- `HomeAdminPanel.java`: home dell'Amministratore.
- `GestioneNoleggioPanel.java`: avvio e termine dei noleggi.
- `ManutenzioneBarchePanel.java`: elenco delle barche per la gestione manutenzioni.
- `GestioneManutenzionePanel.java`: programmazione, avvio e completamento delle manutenzioni.
- `package-info.java`: breve descrizione del package.

La GUI raccoglie gli input e richiama il **Controller**, senza accedere direttamente ai CSV.
