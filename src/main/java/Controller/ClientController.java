package Controller;


import dao.ClientDao;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Client;
import view.ClientManagementView;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ClientController {

    private ClientManagementView view;
    private ClientDao clientDAO;

    // Liste des domaines autorisés
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
            // Domaines internationaux
            "gmail.com",
            "yahoo.com",
            "yahoo.fr",
            "outlook.com",
            "hotmail.com",
            "live.com",
            "msn.com",
            "aol.com",
            "icloud.com",
            "me.com",
            "mac.com",
            "protonmail.com",
            "protonmail.ch",
            "zoho.com",
            "mail.com",
            "yandex.com",
            "yandex.ru",
            "gmx.com",
            "gmx.fr",

            // Domaines français
            "orange.fr",
            "sfr.fr",
            "free.fr",
            "laposte.net",
            "wanadoo.fr",
            "bbox.fr",
            "numericable.fr",
            "club-internet.fr",
            "neuf.fr",
            "noos.fr",
            "cegetel.net",
            "tele2.fr",
            "aliceadsl.fr",
            "dartybox.com",
            "bouyguestelecom.fr",

            // Domaines éducatifs (Sénégal et autres)
            "edu.sn",
            "ucad.edu.sn",
            "ugb.edu.sn",
            "uasz.edu.sn",
            "univ-thies.sn",
            "edu.gouv.sn",
            "education.sn",
            "univ-dakar.sn",
            "ust.edu.sn",
            "edu.ma",
            "edu.tn",
            "edu.cm",
            "edu.ci",

            // Domaines gouvernementaux et institutionnels
            "gouv.sn",
            "gov.sn",
            "presidence.sn",
            "primature.sn",
            "assemblee.sn",
            "sante.sn",
            "economie.sn",
            "finances.gouv.sn",
            "interieur.gouv.sn",
            "defense.gouv.sn",
            "douanes.sn",

            // Entreprises et autres (Sénégal)
            "sonatel.sn",
            "orange.sn",
            "free.sn",
            "tigo.sn",
            "expresso.sn",
            "cba.sn",
            "ecobank.sn",
            "bics.sn",
            "senelec.sn",
            "sen-eau.sn",
            "aeroport.sn",
            "aibd.sn",

            // Domaines internationaux supplémentaires
            "amazon.com",
            "apple.com",
            "microsoft.com",
            "google.com",
            "facebook.com",
            "linkedin.com",
            "twitter.com",
            "instagram.com",

            // Domaines africains
            "yahoo.africa",
            "africa.com",
            "afrinet.africa",
            "african.net",
            "kaymu.sn",
            "jumia.sn",
            "expat-dakar.com"
    );

    public ClientController(ClientManagementView view) {
        this.view = view;
        this.clientDAO = new ClientDao();

        view.setDeleteCallback(new ClientManagementView.DeleteCallback() {
            @Override
            public void onDelete(Client client) {
                view.getClientTable().getSelectionModel().select(client);
                view.showEditForm(client);
                deleteClient();
            }
        });

        initEventHandlers();
        loadClients();
    }

    private void initEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchClients());
        view.getSearchField().setOnAction(e -> searchClients());
        view.getRefreshButton().setOnAction(e -> loadClients());
        view.getSaveButton().setOnAction(e -> saveClient());
        view.getCancelButton().setOnAction(e -> view.hideForm());
        view.getDeleteButton().setOnAction(e -> deleteClient());
    }

    private void loadClients() {
        List<Client> clients = clientDAO.getAll();
        view.refreshTable(clients);
    }

    private void searchClients() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadClients();
        } else {
            List<Client> clients = clientDAO.search(keyword);
            view.refreshTable(clients);
        }
    }

    private void saveClient() {
        String nom = view.getNomField().getText().trim();
        String prenom = view.getPrenomField().getText().trim();
        String email = view.getEmailField().getText().trim();
        String telephone = view.getTelephoneField().getText().trim();
        String adresse = view.getAdresseArea().getText().trim();
        boolean actif = view.getActifCheckBox().isSelected();

        // Validation des champs obligatoires
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || telephone.isEmpty()) {
            view.showError("⚠️ Les champs Nom, Prénom, Email et Téléphone sont obligatoires !");
            return;
        }

        // Validation email avec domaine autorisé
        String emailError = validateEmailWithDomain(email);
        if (emailError != null) {
            view.showError(emailError);
            return;
        }

        // Validation du téléphone
        if (!isValidPhoneNumber(telephone)) {
            view.showError("⚠️ Format de téléphone invalide !\n" +
                    "Formats acceptés :\n" +
                    "• 0612345678 (10 chiffres)\n" +
                    "• +33612345678 (international)\n" +
                    "• 06 12 34 56 78 (avec espaces)\n" +
                    "• 06.12.34.56.78 (avec points)\n" +
                    "• 06-12-34-56-78 (avec tirets)");
            return;
        }

        view.clearError();

        Client selected = view.getSelectedClient();

        if (selected == null) {
            // Ajout
            if (clientDAO.emailExists(email)) {
                view.showError("⚠️ Cet email est déjà utilisé par un autre client !");
                return;
            }

            Client newClient = new Client(nom, prenom, email, telephone, adresse, actif);

            if (clientDAO.create(newClient)) {
                showSuccess("✅ Client ajouté avec succès !");
                view.hideForm();
                loadClients();
            } else {
                view.showError("❌ Erreur lors de l'ajout du client.");
            }
        } else {
            // Modification
            if (clientDAO.emailExistsExcept(email, selected.getIdClient())) {
                view.showError("⚠️ Cet email est déjà utilisé par un autre client !");
                return;
            }

            selected.setNom(nom);
            selected.setPrenom(prenom);
            selected.setEmail(email);
            selected.setTelephone(telephone);
            selected.setAdresse(adresse);
            selected.setActif(actif);

            if (clientDAO.update(selected)) {
                showSuccess("✅ Client modifié avec succès !");
                view.hideForm();
                loadClients();
            } else {
                view.showError("❌ Erreur lors de la modification du client.");
            }
        }
    }

    /**
     * Valide un email et vérifie que le domaine est autorisé
     * @return null si valide, sinon le message d'erreur
     */
    private String validateEmailWithDomain(String email) {
        if (email == null || email.isEmpty()) {
            return "⚠️ L'email est obligatoire !";
        }

        // Vérifier le format de base
        if (!email.contains("@")) {
            return "⚠️ Format d'email invalide !\n" +
                    "L'email doit contenir '@'.\n\n" +
                    "Exemples d'emails valides :\n" +
                    "• jean.dupont@gmail.com\n" +
                    "• marie@edu.sn\n" +
                    "• contact@orange.sn";
        }

        // Extraire le domaine
        String[] parts = email.split("@");
        if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
            return "⚠️ Format d'email invalide !\n\n" +
                    "Exemples d'emails valides :\n" +
                    "• jean.dupont@gmail.com\n" +
                    "• marie@edu.sn\n" +
                    "• contact@orange.sn";
        }

        String domain = parts[1].trim().toLowerCase();
        String localPart = parts[0].trim();

        // Vérifier que le local part ne contient pas de caractères invalides
        if (!localPart.matches("^[A-Za-z0-9+_.-]+$")) {
            return "⚠️ Format d'email invalide !\n" +
                    "La partie avant '@' contient des caractères non autorisés.\n\n" +
                    "Caractères autorisés : lettres, chiffres, +, _, ., -";
        }

        // Vérifier que le domaine est dans la liste autorisée
        boolean domainAllowed = false;
        for (String allowedDomain : ALLOWED_DOMAINS) {
            if (domain.equals(allowedDomain) || domain.endsWith("." + allowedDomain)) {
                domainAllowed = true;
                break;
            }
        }

        if (!domainAllowed) {
            return "⚠️ Domaine email non autorisé !\n\n" +
                    "Les domaines suivants sont acceptés :\n" +
                    "📧 Email : Gmail, Yahoo, Outlook, Hotmail, Orange, SFR, Free...\n" +
                    "🎓 Éducatif : edu.sn, ucad.edu.sn, ugb.edu.sn, uasz.edu.sn...\n" +
                    "🏛️ Institutionnel : gouv.sn, gouvernement.sn...\n" +
                    "📱 Sénégal : orange.sn, sonatel.sn, tigo.sn, expresso.sn...\n\n" +
                    "❌ Les emails avec des domaines personnalisés ou inconnus ne sont pas acceptés.\n\n" +
                    "Veuillez utiliser une adresse email professionnelle ou personnelle avec un domaine reconnu.";
        }

        return null; // Valide
    }

    /**
     * Valide un numéro de téléphone
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }

        String cleaned = phone.replaceAll("[\\s.\\-]", "");

        if (cleaned.matches("^[0-9]{10}$")) {
            return true;
        }

        if (cleaned.matches("^\\+[0-9]{11,13}$")) {
            return true;
        }

        if (cleaned.matches("^00[0-9]{11,13}$")) {
            return true;
        }

        return false;
    }

    private void deleteClient() {
        Client client = view.getSelectedClient();
        if (client == null) {
            Client selected = view.getClientTable().getSelectionModel().getSelectedItem();
            if (selected != null) {
                client = selected;
                view.showEditForm(client);
            } else {
                view.showError("⚠️ Veuillez sélectionner un client dans le tableau !");
                return;
            }
        }

        String message = client.isActif()
                ? "Cette action marquera le client comme inactif.\nVoulez-vous continuer ?"
                : "Ce client est déjà inactif.\nVoulez-vous le supprimer définitivement ?\n\n⚠️ Cette action est irréversible !";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le client " + client.getFullName() + " ?");
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success;
            if (client.isActif()) {
                success = clientDAO.delete(client.getIdClient());
                if (success) {
                    showSuccess("✅ Client désactivé avec succès !");
                }
            } else {
                success = clientDAO.deletePermanently(client.getIdClient());
                if (success) {
                    showSuccess("✅ Client supprimé définitivement !");
                }
            }

            if (success) {
                view.hideForm();
                loadClients();
            } else {
                view.showError("❌ Erreur lors de la suppression.");
            }
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}