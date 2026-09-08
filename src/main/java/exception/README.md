# Package `exception`

Contiene le **eccezioni personalizzate** del progetto.

## File principali

- `NoleggioBarcheException.java`: classe base delle eccezioni del dominio.
- `ClienteMinorenneException.java`: cliente non maggiorenne.
- `PatenteNauticaRichiestaException.java`: patente necessaria ma assente.
- `PatenteNauticaScadutaException.java`: patente non valida.
- `CapacitaPasseggeriSuperataException.java`: troppi passeggeri per la barca.
- `PrenotazioneSovrappostaException.java`: prenotazioni in conflitto.
- `BarcaNonDisponibileException.java`: barca non utilizzabile nel periodo richiesto.
- `ManutenzioneInConflittoException.java`: manutenzione in conflitto con un'attività bloccante.
- `ManutenzioneNonAvviabileException.java`: manutenzione che non può essere avviata.
- `NoleggioGiaEsistenteException.java`: esiste già un noleggio per la prenotazione.
- `NoleggioNonAvviabileException.java`: noleggio che non può iniziare.
- `NoleggioNonTerminabileException.java`: noleggio che non può essere terminato.
- `PeriodoPrenotazioneNonValidoException.java`: periodo della prenotazione non valido.
- `PrenotazioneNonAnnullabileException.java`: prenotazione che non può essere annullata.
- `TransizioneStatoNonValidaException.java`: cambio di stato non consentito.
- `package-info.java`: breve descrizione del package.
