# Agence de location de véhicules — JavaFX

Application desktop Java (JavaFX + MySQL/MariaDB) qui informatise la gestion d'une agence de location de véhicules : clients, véhicules, parkings, réservations, contrats, factures, paiements et sanctions, avec un **espace administrateur** et un **espace client**.

## Fonctionnalités

**Espace administrateur**
- Tableau de bord avec indicateurs
- Clients : ajout, modification, désactivation / suppression, recherche
- Véhicules : CRUD, recherche (marque, modèle, immatriculation), statut (disponible, réservé, loué, maintenance…)
- Parkings : CRUD, capacité
- Réservations : validation / refus / annulation, création directe pour un client au comptoir, contrôle de disponibilité du véhicule sur la période
- Contrats : créés automatiquement à la validation d'une réservation ; clôture avec état du retour, kilométrage, frais (retard, carburant, dégradation, remise)
- Factures : générées automatiquement à la clôture du contrat, statut impayée / partielle / payée
- Paiements : espèces, carte, virement, chèque, Wave, Orange Money, Free Money (montants en FCFA)
- Sanctions : créées automatiquement en cas de dégradation, levées au paiement de la facture
- Statistiques : chiffre d'affaires, évolution mensuelle, top véhicules / clients

**Espace client**
- Inscription et connexion
- Demande de réservation (véhicule, dates, lieux de prise en charge et de retour)
- Suivi de ses réservations, contrats, factures et sanctions (uniquement les siennes)
- Profil et changement de mot de passe

## Stack technique

| Élément | Choix |
|---|---|
| Langage | Java 17+ (testé avec JDK 21) |
| Interface | JavaFX 21, ControlsFX (notifications), CSS |
| Base de données | MySQL / MariaDB via JDBC (MySQL Connector/J 8) |
| Build | Maven (wrapper `mvnw` inclus) |
| Packaging | `jpackage` (application Windows autonome avec Java embarqué) |

## Architecture

```
src/main/java/
├── agencelocation/agencelocationjavafx/   App (point d'entrée JavaFX), Launcher
├── model/        Entités : Client, Vehicule, Parking, Reservation, Contrat, Facture, Paiement, Sanction
├── dao/          Accès aux données (JDBC, requêtes préparées) — une classe DAO par table
├── view/         Écrans JavaFX construits en Java (sans FXML)
├── Controller/   Logique de présentation et règles métier (navigation, validations, enchaînements)
└── utils/        Connexion BD (singleton), session utilisateur, notifications, génération de documents
```

Cycle métier principal :

```
Demande client (EN_ATTENTE) → Validation admin (ACCEPTEE) → Contrat (montant provisoire)
→ Restitution / clôture du contrat (frais éventuels) → Facture → Paiement(s) → Sanction levée
```

La modélisation (MEA, MLD) et le cahier des charges sont dans [`docs/`](docs/).

## Installation

### 1. Base de données

**Option A — XAMPP (recommandé sous Windows)**
1. Démarrer **MySQL** dans le panneau XAMPP.
2. Ouvrir phpMyAdmin → onglet **Importer** → choisir [`sql/agence_location.sql`](sql/agence_location.sql).
   Le script crée la base `agence_location` avec des données de démonstration.

**Option B — Docker**
```bash
docker compose up -d
```
La base est exposée sur le port **3307** (pour ne pas entrer en conflit avec XAMPP). Lancer ensuite l'application avec `DB_URL=jdbc:mysql://localhost:3307/agence_location` et `DB_PASSWORD=root` (voir *Configuration*).

### 2. Lancer l'application

**Depuis les sources** (JDK 17+ requis) :
```bash
./mvnw clean javafx:run
```

**Version Windows prête à l'emploi** : télécharger `AgenceLocation-windows.zip` depuis la page **Releases** du dépôt, décompresser, puis lancer `AgenceLocation.exe` (Java est inclus, aucune installation nécessaire).

Pour reconstruire ce paquet localement (Git Bash) :
```bash
bash scripts/package-windows.sh
```

## Comptes de démonstration

| Rôle | E-mail | Mot de passe |
|---|---|---|
| Administrateur | `admin@agence.com` | `admin123` |
| Client | `client@demo.sn` | `client123` |

Si aucun administrateur n'existe, l'application crée automatiquement le compte `admin@agence.com` au démarrage.

## Configuration

Par défaut, l'application se connecte à `jdbc:mysql://localhost:3306/agence_location` avec l'utilisateur `root` sans mot de passe (configuration XAMPP). Ces valeurs se surchargent par variables d'environnement :

| Variable | Défaut |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/agence_location` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | *(vide)* |

## Limites connues

Projet pédagogique — points à améliorer avant un usage réel :
- les mots de passe sont stockés en clair (prévoir un hachage BCrypt, la dépendance `jbcrypt` est déjà présente) ;
- les « PDF » de contrats et factures sont générés au format texte dans `pdfs/` ;
- l'envoi d'e-mails est simulé (affichage console).
