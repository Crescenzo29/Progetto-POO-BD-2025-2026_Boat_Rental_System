package controller;

import dao.BarcaDAO;
import dao.ClienteDAO;
import dao.ManutenzioneDAO;
import dao.NoleggioDAO;
import dao.PrenotazioneDAO;
import dao.SedeDAO;
import exception.BarcaNonDisponibileException;
import exception.CapacitaPasseggeriSuperataException;
import exception.ClienteMinorenneException;
import exception.ManutenzioneInConflittoException;
import exception.ManutenzioneNonAvviabileException;
import exception.NoleggioGiaEsistenteException;
import exception.NoleggioNonAvviabileException;
import exception.PatenteNauticaRichiestaException;
import exception.PatenteNauticaScadutaException;
import exception.PrenotazioneSovrappostaException;
import exception.TransizioneStatoNonValidaException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import model.Barca;
import model.BarcaMotore;
import model.BarcaVela;
import model.Cliente;
import model.Manutenzione;
import model.Noleggio;
import model.Prenotazione;
import model.Sede;
import model.StatoBarca;
import model.StatoManutenzione;
import model.StatoNoleggio;
import model.StatoPrenotazione;

/**
 * Controller principale dell'applicazione.
 * Collega GUI, DAO e regole di business.
 */
public class NoleggioBarcheController {
    private final ClienteDAO clienteDAO;
    private final SedeDAO sedeDAO;
    private final BarcaDAO barcaDAO;
    private final PrenotazioneDAO prenotazioneDAO;
    private final NoleggioDAO noleggioDAO;
    private final ManutenzioneDAO manutenzioneDAO;
    private static final String ADMIN_EMAIL = "admin@noleggiobarche.it";
    private static final String ADMIN_PASSWORD = "Admin123!";

    // Riceve i DAO usati dal controller
    public NoleggioBarcheController(
            ClienteDAO clienteDAO,
            SedeDAO sedeDAO,
            BarcaDAO barcaDAO,
            PrenotazioneDAO prenotazioneDAO,
            NoleggioDAO noleggioDAO,
            ManutenzioneDAO manutenzioneDAO) {
        this.clienteDAO = Objects.requireNonNull(clienteDAO, "ClienteDAO non puo' essere null.");
        this.sedeDAO = Objects.requireNonNull(sedeDAO, "SedeDAO non puo' essere null.");
        this.barcaDAO = Objects.requireNonNull(barcaDAO, "BarcaDAO non puo' essere null.");
        this.prenotazioneDAO = Objects.requireNonNull(
                prenotazioneDAO, "PrenotazioneDAO non puo' essere null.");
        this.noleggioDAO = Objects.requireNonNull(noleggioDAO, "NoleggioDAO non puo' essere null.");
        this.manutenzioneDAO = Objects.requireNonNull(
                manutenzioneDAO, "ManutenzioneDAO non puo' essere null.");
    }

    // ==================== CLIENTI ====================

    /**
     * Controlla email e password dell'admin.
     */
    public boolean isAdmin(String email, String password) {
        return ADMIN_EMAIL.equalsIgnoreCase(normalizza(email))
                && ADMIN_PASSWORD.equals(password);
    }

    /**
     * Controlla email e password del cliente.
     */
    public Optional<Cliente> autenticaCliente(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        String passwordHash = generaPasswordHash(password);
        return clienteDAO.findByEmail(email.trim())
                .filter(cliente -> cliente.getPasswordHash().equals(passwordHash));
    }

    /**
     * Calcola l'hash SHA-256 della password.
     */
    public String generaPasswordHash(String password) {
        Objects.requireNonNull(password, "Password non puo' essere null.");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo SHA-256 non disponibile.", ex);
        }
    }

    public int prossimoIdCliente() {
        return clienteDAO.findAll().stream()
                .mapToInt(Cliente::getIdCliente)
                .max()
                .orElse(0) + 1;
    }

    // Carica tutti i clienti
    public List<Cliente> getClienti() {
        return clienteDAO.findAll();
    }

    // Cerca un cliente tramite ID
    public Optional<Cliente> getCliente(int idCliente) {
        return clienteDAO.findById(idCliente);
    }

    // Cerca un cliente tramite email
    public Optional<Cliente> getClientePerEmail(String email) {
        return clienteDAO.findByEmail(email);
    }

    // Salva un nuovo cliente
    /**
     * Registra un nuovo cliente.
     */
    public void aggiungiCliente(Cliente cliente) {
        Cliente clienteDaSalvare = Objects.requireNonNull(cliente, "Cliente non puo' essere null.");
        verificaEmailDisponibilePerNuovoCliente(clienteDaSalvare);
        clienteDAO.save(clienteDaSalvare);
    }

    // Aggiorna un cliente
    public void aggiornaCliente(Cliente cliente) {
        Cliente clienteDaAggiornare = Objects.requireNonNull(cliente, "Cliente non puo' essere null.");
        verificaEmailDisponibilePerAggiornamento(clienteDaAggiornare);
        clienteDAO.update(clienteDaAggiornare);
    }

    // ==================== SEDI ====================

    // Carica tutte le sedi
    public List<Sede> getSedi() {
        return sedeDAO.findAll();
    }

    // Cerca una sede tramite ID
    public Optional<Sede> getSede(int idSede) {
        return sedeDAO.findById(idSede);
    }

    // Salva una nuova sede
    public void aggiungiSede(Sede sede) {
        sedeDAO.save(Objects.requireNonNull(sede, "Sede non puo' essere null."));
    }

    // Aggiorna una sede
    public void aggiornaSede(Sede sede) {
        sedeDAO.update(Objects.requireNonNull(sede, "Sede non puo' essere null."));
    }

    // ==================== BARCHE ====================

    // Carica tutte le barche
    public List<Barca> getBarche() {
        return barcaDAO.findAll();
    }

    // Cerca una barca tramite matricola
    public Optional<Barca> getBarca(String matricola) {
        return barcaDAO.findById(matricola);
    }

    // Cerca le barche di una sede
    public List<Barca> getBarchePerSede(int idSede) {
        return barcaDAO.findBySedeId(idSede);
    }

    // Salva una nuova barca
    public void aggiungiBarca(Barca barca) {
        barcaDAO.save(Objects.requireNonNull(barca, "Barca non puo' essere null."));
    }

    // Aggiorna una barca
    public void aggiornaBarca(Barca barca) {
        barcaDAO.update(Objects.requireNonNull(barca, "Barca non puo' essere null."));
    }

    /**
     * Cerca le barche usando i filtri del catalogo.
     */
    public List<Barca> cercaBarche(
            String tipo,
            Boolean richiedePatente,
            Integer numeroPasseggeri,
            Double tariffaMassima,
            Integer idSede,
            LocalDate dataInizio,
            LocalDate dataFine) {
        return barcaDAO.findAll().stream()
                .filter(barca -> tipoBarcaCompatibile(barca, tipo))
                .filter(barca -> richiedePatente == null || barca.isRichiedePatente() == richiedePatente)
                .filter(barca -> numeroPasseggeri == null || barca.getCapacitaPasseggeri() >= numeroPasseggeri)
                .filter(barca -> tariffaMassima == null || barca.getTariffaGiornaliera() <= tariffaMassima)
                .filter(barca -> idSede == null || barca.getSede().getIdSede() == idSede)
                .filter(barca -> barcaUtilizzabileNelPeriodo(barca, dataInizio, dataFine))
                .toList();
    }

    /**
     * Mette una barca fuori servizio.
     */
    public void mettiBarcaFuoriServizio(String matricola, LocalDate indisponibileFinoAl) {
        Barca barca = barcaDAO.findById(matricola)
                .orElseThrow(() -> new IllegalArgumentException("Barca non trovata."));

        barca.setStato(StatoBarca.FUORI_SERVIZIO);
        barca.setIndisponibileFinoAl(indisponibileFinoAl);

        for (Noleggio noleggio : noleggioDAO.findAll()) {
            if (riguardaBarca(noleggio, matricola) && noleggio.getStato() == StatoNoleggio.IN_CORSO) {
                noleggio.setStato(StatoNoleggio.SOSPESO);
                noleggioDAO.update(noleggio);
            }
        }

        barcaDAO.update(barca);
    }

    /**
     * Rende disponibile una barca senza blocchi attivi.
     */
    public void ripristinaBarca(String matricola) {
        Barca barca = barcaDAO.findById(matricola)
                .orElseThrow(() -> new IllegalArgumentException("Barca non trovata."));

        if (haNoleggioAttivo(matricola) || haManutenzioneInCorso(matricola)) {
            throw new BarcaNonDisponibileException("La barca ha ancora blocchi attivi.");
        }

        barca.setIndisponibileFinoAl(null);
        barca.setStato(StatoBarca.DISPONIBILE);
        barcaDAO.update(barca);
    }

    // ==================== PRENOTAZIONI ====================

    // Carica tutte le prenotazioni
    public List<Prenotazione> getPrenotazioni() {
        return prenotazioneDAO.findAll();
    }

    // Cerca una prenotazione tramite ID
    public Optional<Prenotazione> getPrenotazione(int idPrenotazione) {
        return prenotazioneDAO.findById(idPrenotazione);
    }

    public int prossimoIdPrenotazione() {
        return prenotazioneDAO.findAll().stream()
                .mapToInt(Prenotazione::getIdPrenotazione)
                .max()
                .orElse(0) + 1;
    }

    // Carica le prenotazioni di un cliente
    public List<Prenotazione> getPrenotazioniCliente(int idCliente) {
        return prenotazioneDAO.findByClienteId(idCliente);
    }

    // Carica le prenotazioni di una barca
    public List<Prenotazione> getPrenotazioniBarca(String matricola) {
        return prenotazioneDAO.findByBarcaMatricola(matricola);
    }

    // Salva una nuova prenotazione
    /**
     * Crea una prenotazione valida.
     */
    public void aggiungiPrenotazione(Prenotazione prenotazione) {
        Prenotazione prenotazioneDaSalvare = Objects.requireNonNull(
                prenotazione, "Prenotazione non puo' essere null.");
        verificaPrenotazioneCreabile(prenotazioneDaSalvare);
        prenotazioneDAO.save(prenotazioneDaSalvare);
    }

    // Aggiorna una prenotazione
    public void aggiornaPrenotazione(Prenotazione prenotazione) {
        prenotazioneDAO.update(
                Objects.requireNonNull(prenotazione, "Prenotazione non puo' essere null."));
    }

    /**
     * Annulla una prenotazione.
     */
    public void annullaPrenotazione(int idPrenotazione) {
        Prenotazione prenotazione = prenotazioneDAO.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata."));
        prenotazione.annulla();
        prenotazioneDAO.update(prenotazione);
    }

    // ==================== NOLEGGI ====================

    // Carica tutti i noleggi
    public List<Noleggio> getNoleggi() {
        return noleggioDAO.findAll();
    }

    // Cerca un noleggio tramite ID
    public Optional<Noleggio> getNoleggio(int idNoleggio) {
        return noleggioDAO.findById(idNoleggio);
    }

    public int prossimoIdNoleggio() {
        return noleggioDAO.findAll().stream()
                .mapToInt(Noleggio::getIdNoleggio)
                .max()
                .orElse(0) + 1;
    }

    // Cerca il noleggio di una prenotazione
    public Optional<Noleggio> getNoleggioPerPrenotazione(int idPrenotazione) {
        return noleggioDAO.findByPrenotazioneId(idPrenotazione);
    }

    // Salva un nuovo noleggio
    public void aggiungiNoleggio(Noleggio noleggio) {
        noleggioDAO.save(Objects.requireNonNull(noleggio, "Noleggio non puo' essere null."));
    }

    // Aggiorna un noleggio
    public void aggiornaNoleggio(Noleggio noleggio) {
        noleggioDAO.update(Objects.requireNonNull(noleggio, "Noleggio non puo' essere null."));
    }

    /**
     * Avvia il noleggio di una prenotazione.
     */
    public Noleggio avviaNoleggio(
            int idNoleggio,
            int idPrenotazione,
            LocalDateTime dataOraRitiro,
            String noteRitiro) {
        LocalDateTime ritiro = Objects.requireNonNull(
                dataOraRitiro, "Data e ora ritiro non possono essere null.");
        Prenotazione prenotazione = prenotazioneDAO.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata."));

        if (prenotazione.getStato() != StatoPrenotazione.CONFERMATA) {
            throw new NoleggioNonAvviabileException("La prenotazione non puo' avviare un noleggio.");
        }
        if (noleggioDAO.findByPrenotazioneId(idPrenotazione).isPresent()) {
            throw new NoleggioGiaEsistenteException("Esiste gia' un noleggio per questa prenotazione.");
        }

        verificaBarcaOperativaPerRitiro(prenotazione.getBarca(), ritiro);
        verificaRitiroNelPeriodo(prenotazione, ritiro);
        verificaMaggioreEta(prenotazione.getCliente(), prenotazione.getDataInizio());
        verificaPatente(prenotazione);

        Noleggio noleggio = new Noleggio(
                idNoleggio,
                ritiro,
                null,
                noteRitiro,
                null,
                false,
                StatoNoleggio.IN_CORSO,
                prenotazione);

        prenotazione.avviaNoleggio();
        prenotazione.getBarca().setStato(StatoBarca.NOLEGGIATA);

        noleggioDAO.save(noleggio);
        prenotazioneDAO.update(prenotazione);
        barcaDAO.update(prenotazione.getBarca());
        return noleggio;
    }

    /**
     * Sospende un noleggio in corso.
     */
    public void sospendiNoleggio(int idNoleggio) {
        Noleggio noleggio = noleggioDAO.findById(idNoleggio)
                .orElseThrow(() -> new IllegalArgumentException("Noleggio non trovato."));
        noleggio.setStato(StatoNoleggio.SOSPESO);
        noleggioDAO.update(noleggio);
    }

    /**
     * Riprende un noleggio sospeso.
     */
    public void riprendiNoleggio(int idNoleggio) {
        Noleggio noleggio = noleggioDAO.findById(idNoleggio)
                .orElseThrow(() -> new IllegalArgumentException("Noleggio non trovato."));
        noleggio.setStato(StatoNoleggio.IN_CORSO);
        noleggioDAO.update(noleggio);
    }

    /**
     * Termina un noleggio.
     */
    public void terminaNoleggio(
            int idNoleggio,
            LocalDateTime dataOraRestituzione,
            String noteRestituzione,
            boolean completatoCorrettamente) {
        Noleggio noleggio = noleggioDAO.findById(idNoleggio)
                .orElseThrow(() -> new IllegalArgumentException("Noleggio non trovato."));
        LocalDateTime restituzione = Objects.requireNonNull(
                dataOraRestituzione, "Data e ora restituzione non possono essere null.");
        Prenotazione prenotazione = noleggio.getPrenotazione();
        Barca barca = prenotazione.getBarca();
        boolean barcaGiaFuoriServizio = barca.getStato() == StatoBarca.FUORI_SERVIZIO;

        noleggio.setDataOraRestituzione(restituzione);
        noleggio.setNoteRestituzione(noteRestituzione);
        noleggio.setCompletatoCorrettamente(completatoCorrettamente);
        noleggio.terminaNoleggio();
        prenotazione.completa();

        if (!completatoCorrettamente) {
            barca.setStato(StatoBarca.FUORI_SERVIZIO);
        } else if (!barcaGiaFuoriServizio
                && puoTornareDisponibile(barca, restituzione.toLocalDate(), idNoleggio)) {
            barca.setStato(StatoBarca.DISPONIBILE);
        }

        noleggioDAO.update(noleggio);
        prenotazioneDAO.update(prenotazione);
        barcaDAO.update(barca);
    }

    // ==================== MANUTENZIONI ====================

    // Carica tutte le manutenzioni
    public List<Manutenzione> getManutenzioni() {
        return manutenzioneDAO.findAll();
    }

    // Cerca una manutenzione tramite ID
    public Optional<Manutenzione> getManutenzione(int idManutenzione) {
        return manutenzioneDAO.findById(idManutenzione);
    }

    public int prossimoIdManutenzione() {
        return manutenzioneDAO.findAll().stream()
                .mapToInt(Manutenzione::getIdManutenzione)
                .max()
                .orElse(0) + 1;
    }

    // Carica le manutenzioni di una barca
    public List<Manutenzione> getManutenzioniBarca(String matricola) {
        return manutenzioneDAO.findByBarcaMatricola(matricola);
    }

    // Salva una nuova manutenzione
    /**
     * Programma una manutenzione.
     */
    public void aggiungiManutenzione(Manutenzione manutenzione) {
        Manutenzione manutenzioneDaSalvare = Objects.requireNonNull(
                manutenzione, "Manutenzione non puo' essere null.");
        if (manutenzioneDaSalvare.getStato() != StatoManutenzione.PROGRAMMATA) {
            throw new TransizioneStatoNonValidaException(
                    "Una nuova manutenzione deve essere programmata.");
        }
        verificaManutenzioneNonGiaPresente(manutenzioneDaSalvare);
        verificaConflittiManutenzione(manutenzioneDaSalvare);
        List<Prenotazione> prenotazioniDaAnnullare =
                prenotazioniConfermateSovrapposte(manutenzioneDaSalvare);
        for (Prenotazione prenotazione : prenotazioniDaAnnullare) {
            prenotazione.annulla();
            prenotazioneDAO.update(prenotazione);
        }
        manutenzioneDAO.save(manutenzioneDaSalvare);
    }

    // Aggiorna una manutenzione
    public void aggiornaManutenzione(Manutenzione manutenzione) {
        manutenzioneDAO.update(
                Objects.requireNonNull(manutenzione, "Manutenzione non puo' essere null."));
    }

    /**
     * Avvia una manutenzione.
     */
    public void avviaManutenzione(int idManutenzione) {
        Manutenzione manutenzione = manutenzioneDAO.findById(idManutenzione)
                .orElseThrow(() -> new IllegalArgumentException("Manutenzione non trovata."));
        Barca barca = barcaAggiornataPer(manutenzione);
        String matricola = barca.getMatricola();

        if (manutenzione.getStato() != StatoManutenzione.PROGRAMMATA
                || barca.getStato() == StatoBarca.NOLEGGIATA
                || barca.getStato() == StatoBarca.FUORI_SERVIZIO
                || barca.getStato() == StatoBarca.MANUTENZIONE
                || haNoleggioAttivo(matricola)
                || haManutenzioneInCorso(matricola, idManutenzione)) {
            throw new ManutenzioneNonAvviabileException("La manutenzione non puo' essere avviata.");
        }

        manutenzione.avvia();
        barca.setStato(StatoBarca.MANUTENZIONE);

        // Imposta la data di fine manutenzione
        barca.setIndisponibileFinoAl(manutenzione.getDataFine());

        manutenzioneDAO.update(manutenzione);
        barcaDAO.update(barca);
    }

    /**
     * Completa una manutenzione.
     */
    public void completaManutenzione(int idManutenzione) {
        Manutenzione manutenzione = manutenzioneDAO.findById(idManutenzione)
                .orElseThrow(() -> new IllegalArgumentException("Manutenzione non trovata."));
        Barca barca = barcaAggiornataPer(manutenzione);

        manutenzione.completa();
        manutenzioneDAO.update(manutenzione);

        // Rimuove la data di indisponibilita'
        barca.setIndisponibileFinoAl(null);

        if (puoTornareDisponibile(barca, LocalDate.now())) {
            barca.setStato(StatoBarca.DISPONIBILE);
        }

        barcaDAO.update(barca);
    }

    private Barca barcaAggiornataPer(Manutenzione manutenzione) {
        String matricola = manutenzione.getBarca().getMatricola();
        Barca barca = barcaDAO.findById(matricola)
                .orElseThrow(() -> new IllegalArgumentException("Barca non trovata."));
        manutenzione.setBarca(barca);
        return barca;
    }

    private void verificaEmailDisponibilePerNuovoCliente(Cliente cliente) {
        if (clienteDAO.findByEmail(cliente.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Esiste già un cliente registrato con questa email.");
        }
    }

    private void verificaEmailDisponibilePerAggiornamento(Cliente cliente) {
        clienteDAO.findByEmail(cliente.getEmail())
                .filter(clienteTrovato -> clienteTrovato.getIdCliente() != cliente.getIdCliente())
                .ifPresent(clienteTrovato -> {
                    throw new IllegalArgumentException("Esiste già un cliente registrato con questa email.");
                });
    }

    private void verificaPrenotazioneCreabile(Prenotazione prenotazione) {
        if (prenotazione.getStato() != StatoPrenotazione.CONFERMATA) {
            throw new TransizioneStatoNonValidaException(
                    "Una nuova prenotazione deve partire dallo stato CONFERMATA.");
        }
        verificaMaggioreEta(prenotazione.getCliente(), prenotazione.getDataInizio());
        verificaPatente(prenotazione);
        verificaCapacita(prenotazione);
        verificaDisponibilitaPerPrenotazione(prenotazione);
        verificaPrenotazioniSovrapposte(prenotazione);
        verificaManutenzioniSuPrenotazione(prenotazione);
    }

    private void verificaMaggioreEta(Cliente cliente, LocalDate dataInizio) {
        if (Period.between(cliente.getDataNascita(), dataInizio).getYears() < 18) {
            throw new ClienteMinorenneException(
                    "Il cliente deve essere maggiorenne per effettuare il noleggio.");
        }
    }

    private void verificaPatente(Prenotazione prenotazione) {
        boolean patenteNecessaria = prenotazione.getBarca().isRichiedePatente();
        boolean patenteDichiarata = prenotazione.isConPatente();

        if (patenteNecessaria && !patenteDichiarata) {
            throw new PatenteNauticaRichiestaException(
                    "La barca richiede una patente nautica valida.");
        }
        if (patenteNecessaria || patenteDichiarata) {
            Cliente cliente = prenotazione.getCliente();
            if (!cliente.haPatenteNautica()) {
                throw new PatenteNauticaRichiestaException(
                        "Il cliente non possiede una patente nautica registrata.");
            }
            if (!cliente.patenteValida(prenotazione.getDataFine())) {
                throw new PatenteNauticaScadutaException(
                        "La patente nautica non copre tutto il periodo del noleggio.");
            }
        }
    }

    private void verificaCapacita(Prenotazione prenotazione) {
        if (prenotazione.getNumeroPasseggeri() > prenotazione.getBarca().getCapacitaPasseggeri()) {
            throw new CapacitaPasseggeriSuperataException(
                    "Il numero di passeggeri supera la capacita' massima della barca.");
        }
    }

    private void verificaDisponibilitaPerPrenotazione(Prenotazione prenotazione) {
        Barca barca = prenotazione.getBarca();
        if (barca.getStato() == StatoBarca.FUORI_SERVIZIO) {
            throw new BarcaNonDisponibileException("La barca non e' disponibile per il periodo richiesto.");
        }
        LocalDate indisponibileFinoAl = barca.getIndisponibileFinoAl();
        if (indisponibileFinoAl != null && !prenotazione.getDataInizio().isAfter(indisponibileFinoAl)) {
            throw new BarcaNonDisponibileException("La barca non e' disponibile per il periodo richiesto.");
        }
    }

    private void verificaPrenotazioniSovrapposte(Prenotazione prenotazione) {
        String matricola = prenotazione.getBarca().getMatricola();
        for (Prenotazione esistente : prenotazioneDAO.findByBarcaMatricola(matricola)) {
            if (esistente.getIdPrenotazione() != prenotazione.getIdPrenotazione()
                    && esistente.getStato() != StatoPrenotazione.ANNULLATA
                    && intervalliSovrapposti(
                            esistente.getDataInizio(),
                            esistente.getDataFine(),
                            prenotazione.getDataInizio(),
                            prenotazione.getDataFine())) {
                throw new PrenotazioneSovrappostaException(
                        "La barca è già prenotata nel periodo selezionato.");
            }
        }
    }

    private void verificaManutenzioniSuPrenotazione(Prenotazione prenotazione) {
        String matricola = prenotazione.getBarca().getMatricola();
        for (Manutenzione manutenzione : manutenzioneDAO.findByBarcaMatricola(matricola)) {
            if (manutenzione.getStato() != StatoManutenzione.COMPLETATA
                    && intervalliSovrapposti(
                            manutenzione.getDataInizio(),
                            manutenzione.getDataFine(),
                            prenotazione.getDataInizio(),
                            prenotazione.getDataFine())) {
                throw new BarcaNonDisponibileException(
                        "La barca è interessata da una manutenzione nel periodo selezionato.");
            }
        }
    }

    private void verificaBarcaOperativaPerRitiro(Barca barca, LocalDateTime dataOraRitiro) {
        if (barca.getStato() == StatoBarca.FUORI_SERVIZIO
                || barca.getStato() == StatoBarca.MANUTENZIONE
                || barca.getStato() == StatoBarca.NOLEGGIATA) {
            throw new BarcaNonDisponibileException("La barca non e' operativa al momento del ritiro.");
        }
        LocalDate indisponibileFinoAl = barca.getIndisponibileFinoAl();
        if (indisponibileFinoAl != null && !dataOraRitiro.toLocalDate().isAfter(indisponibileFinoAl)) {
            throw new BarcaNonDisponibileException("La barca non e' operativa al momento del ritiro.");
        }
    }

    private void verificaRitiroNelPeriodo(Prenotazione prenotazione, LocalDateTime dataOraRitiro) {
        LocalDate dataRitiro = dataOraRitiro.toLocalDate();
        if (dataRitiro.isBefore(prenotazione.getDataInizio())
                || dataRitiro.isAfter(prenotazione.getDataFine())) {
            throw new NoleggioNonAvviabileException(
                    "La data di ritiro deve rientrare nel periodo prenotato.");
        }
    }

    private void verificaConflittiManutenzione(Manutenzione manutenzione) {
        String matricola = manutenzione.getBarca().getMatricola();
        for (Manutenzione esistente : manutenzioneDAO.findByBarcaMatricola(matricola)) {
            if (esistente.getIdManutenzione() != manutenzione.getIdManutenzione()
                    && esistente.getStato() != StatoManutenzione.COMPLETATA
                    && intervalliSovrapposti(
                            esistente.getDataInizio(),
                            esistente.getDataFine(),
                            manutenzione.getDataInizio(),
                            manutenzione.getDataFine())) {
                throw new ManutenzioneInConflittoException(
                        "La manutenzione si sovrappone a un altro intervento.");
            }
        }
        verificaPrenotazioniNoleggiateSovrapposte(manutenzione);
        verificaNoleggiAttiviSovrapposti(manutenzione);
    }

    private void verificaManutenzioneNonGiaPresente(Manutenzione manutenzione) {
        if (manutenzioneDAO.findById(manutenzione.getIdManutenzione()).isPresent()) {
            throw new IllegalStateException(
                    "Esiste gia' una manutenzione con identificativo: "
                            + manutenzione.getIdManutenzione());
        }
    }

    private void verificaPrenotazioniNoleggiateSovrapposte(Manutenzione manutenzione) {
        String matricola = manutenzione.getBarca().getMatricola();
        for (Prenotazione prenotazione : prenotazioneDAO.findByBarcaMatricola(matricola)) {
            if (prenotazione.getStato() == StatoPrenotazione.NOLEGGIATA
                    && sovrappostaAllaManutenzione(prenotazione, manutenzione)) {
                throw new ManutenzioneInConflittoException(
                        "La manutenzione si sovrappone a una prenotazione gia' noleggiata.");
            }
        }
    }

    private void verificaNoleggiAttiviSovrapposti(Manutenzione manutenzione) {
        String matricola = manutenzione.getBarca().getMatricola();
        for (Noleggio noleggio : noleggioDAO.findAll()) {
            if (riguardaBarca(noleggio, matricola)
                    && (noleggio.getStato() == StatoNoleggio.IN_CORSO
                            || noleggio.getStato() == StatoNoleggio.SOSPESO)
                    && sovrappostaAllaManutenzione(noleggio.getPrenotazione(), manutenzione)) {
                throw new ManutenzioneInConflittoException(
                        "La manutenzione si sovrappone a un noleggio attivo.");
            }
        }
    }

    private List<Prenotazione> prenotazioniConfermateSovrapposte(Manutenzione manutenzione) {
        return prenotazioneDAO.findByBarcaMatricola(manutenzione.getBarca().getMatricola()).stream()
                .filter(prenotazione -> prenotazione.getStato() == StatoPrenotazione.CONFERMATA)
                .filter(prenotazione -> sovrappostaAllaManutenzione(prenotazione, manutenzione))
                .toList();
    }

    private boolean sovrappostaAllaManutenzione(Prenotazione prenotazione, Manutenzione manutenzione) {
        return intervalliSovrapposti(
                prenotazione.getDataInizio(),
                prenotazione.getDataFine(),
                manutenzione.getDataInizio(),
                manutenzione.getDataFine());
    }

    private boolean intervalliSovrapposti(
            LocalDate primoInizio,
            LocalDate primoFine,
            LocalDate secondoInizio,
            LocalDate secondoFine) {
        return !primoInizio.isAfter(secondoFine) && !secondoInizio.isAfter(primoFine);
    }

    private boolean puoTornareDisponibile(Barca barca, LocalDate dataRiferimento) {
        return puoTornareDisponibile(barca, dataRiferimento, -1);
    }

    private boolean puoTornareDisponibile(
            Barca barca,
            LocalDate dataRiferimento,
            int idNoleggioDaIgnorare) {
        String matricola = barca.getMatricola();
        return !haNoleggioAttivo(matricola, idNoleggioDaIgnorare)
                && !haManutenzioneInCorso(matricola)
                && (barca.getIndisponibileFinoAl() == null
                        || dataRiferimento.isAfter(barca.getIndisponibileFinoAl()));
    }

    private boolean haNoleggioAttivo(String matricola) {
        return haNoleggioAttivo(matricola, -1);
    }

    private boolean haNoleggioAttivo(String matricola, int idNoleggioDaIgnorare) {
        for (Noleggio noleggio : noleggioDAO.findAll()) {
            if (noleggio.getIdNoleggio() != idNoleggioDaIgnorare
                    && riguardaBarca(noleggio, matricola)
                    && (noleggio.getStato() == StatoNoleggio.IN_CORSO
                            || noleggio.getStato() == StatoNoleggio.SOSPESO)) {
                return true;
            }
        }
        return false;
    }

    private boolean haManutenzioneInCorso(String matricola) {
        return haManutenzioneInCorso(matricola, -1);
    }

    private boolean haManutenzioneInCorso(String matricola, int idManutenzioneDaIgnorare) {
        for (Manutenzione manutenzione : manutenzioneDAO.findByBarcaMatricola(matricola)) {
            if (manutenzione.getIdManutenzione() != idManutenzioneDaIgnorare
                    && manutenzione.getStato() == StatoManutenzione.IN_CORSO) {
                return true;
            }
        }
        return false;
    }

    private boolean riguardaBarca(Noleggio noleggio, String matricola) {
        return noleggio.getPrenotazione()
                .getBarca()
                .getMatricola()
                .equals(matricola);
    }

    private String normalizza(String testo) {
        return testo == null ? "" : testo.trim();
    }

    private boolean tipoBarcaCompatibile(Barca barca, String tipo) {
        String tipoNormalizzato = normalizza(tipo);
        if (tipoNormalizzato.isEmpty() || "Tutti".equalsIgnoreCase(tipoNormalizzato)) {
            return true;
        }
        if ("Barca a vela".equalsIgnoreCase(tipoNormalizzato)) {
            return barca instanceof BarcaVela;
        }
        if ("Barca a motore".equalsIgnoreCase(tipoNormalizzato)) {
            return barca instanceof BarcaMotore;
        }
        return true;
    }

    private boolean barcaUtilizzabileNelPeriodo(Barca barca, LocalDate dataInizio, LocalDate dataFine) {
        if (dataInizio == null && dataFine == null) {
            return true;
        }
        if (dataInizio == null || dataFine == null) {
            throw new IllegalArgumentException("Inserire sia data inizio sia data fine.");
        }
        if (dataFine.isBefore(dataInizio)) {
            throw new IllegalArgumentException("La data fine non puo' precedere la data inizio.");
        }
        if (barca.getStato() == StatoBarca.FUORI_SERVIZIO) {
            return false;
        }
        LocalDate indisponibileFinoAl = barca.getIndisponibileFinoAl();
        if (indisponibileFinoAl != null && !dataInizio.isAfter(indisponibileFinoAl)) {
            return false;
        }

        String matricola = barca.getMatricola();
        for (Prenotazione prenotazione : prenotazioneDAO.findByBarcaMatricola(matricola)) {
            if (prenotazione.getStato() != StatoPrenotazione.ANNULLATA
                    && intervalliSovrapposti(
                            prenotazione.getDataInizio(),
                            prenotazione.getDataFine(),
                            dataInizio,
                            dataFine)) {
                return false;
            }
        }
        for (Manutenzione manutenzione : manutenzioneDAO.findByBarcaMatricola(matricola)) {
            if (manutenzione.getStato() != StatoManutenzione.COMPLETATA
                    && intervalliSovrapposti(
                            manutenzione.getDataInizio(),
                            manutenzione.getDataFine(),
                            dataInizio,
                            dataFine)) {
                return false;
            }
        }
        return true;
    }
}
