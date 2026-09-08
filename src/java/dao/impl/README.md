# Package `dao.impl`

Contiene le **implementazioni CSV dei DAO**.

## File

- `AbstractCsvDAO.java`: contiene le operazioni comuni usate dai DAO CSV.
- `CsvUtils.java`: gestisce lettura, scrittura, date, booleani e campi dei CSV.
- `ClienteCsvDAO.java`: legge e salva i clienti.
- `SedeCsvDAO.java`: legge e salva le sedi.
- `BarcaCsvDAO.java`: legge e salva le barche.
- `PrenotazioneCsvDAO.java`: legge e salva le prenotazioni.
- `NoleggioCsvDAO.java`: legge e salva i noleggi.
- `ManutenzioneCsvDAO.java`: legge e salva le manutenzioni.
- `package-info.java`: breve descrizione del package.

Queste classi sono il collegamento tra i **DAO** e i file presenti in `src/main/resources/data/`.
