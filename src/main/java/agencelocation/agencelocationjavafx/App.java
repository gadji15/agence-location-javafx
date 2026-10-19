package agencelocation.agencelocationjavafx;

import Controller.MainController;
import javafx.application.Application;
import javafx.stage.Stage;
import utils.DatabaseConnection;
import utils.SessionManager;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("🚀 Lancement de l'application...");

        // Initialiser la connexion à la base de données
        DatabaseConnection.getInstance();

        // Créer un compte admin par défaut si nécessaire
        SessionManager.getInstance().createDefaultAdmin();

        // Démarrer avec le contrôleur principal (qui gère la navigation)
        MainController mainController = new MainController(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}