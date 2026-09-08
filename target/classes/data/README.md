# Cartella `data`

Contiene i **file CSV** utilizzati dal programma come archivio dei dati.

## File

- `clienti.csv`: clienti registrati.
- `sedi.csv`: sedi disponibili.
- `barche.csv`: barche a motore e a vela.
- `prenotazioni.csv`: prenotazioni dei clienti.
- `noleggi.csv`: noleggi avviati o terminati.
- `manutenzioni.csv`: manutenzioni programmate, in corso o completate.

I DAO presenti in `dao.impl` leggono e aggiornano questi file. Le date sono salvate nel formato italiano e i valori booleani come `sì` / `no`.
