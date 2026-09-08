# Package `dao`

Contiene le **interfacce DAO** usate per accedere ai dati senza legare il Controller direttamente ai file CSV.

## File

- `CrudDAO.java`: definisce le operazioni base di lettura, salvataggio, modifica ed eliminazione.
- `ClienteDAO.java`: operazioni sui clienti.
- `SedeDAO.java`: operazioni sulle sedi.
- `BarcaDAO.java`: operazioni sulle barche.
- `PrenotazioneDAO.java`: operazioni sulle prenotazioni.
- `NoleggioDAO.java`: operazioni sui noleggi.
- `ManutenzioneDAO.java`: operazioni sulle manutenzioni.
- `package-info.java`: breve descrizione del package.

Le implementazioni concrete si trovano nella sottocartella `impl/`.
