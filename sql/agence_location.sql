-- Base de données de démonstration — Agence de location
-- Données personnelles anonymisées (comptes de démo, voir README).

CREATE DATABASE IF NOT EXISTS `agence_location` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `agence_location`;

-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : sam. 19 sep. 2026 à 17:35
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `agence_location`
--

-- --------------------------------------------------------

--
-- Structure de la table `client`
--

CREATE TABLE `client` (
  `idClient` int(11) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `telephone` varchar(20) NOT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `dateInscription` timestamp NOT NULL DEFAULT current_timestamp(),
  `actif` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `client`
--

INSERT INTO `client` (`idClient`, `nom`, `prenom`, `email`, `telephone`, `adresse`, `dateInscription`, `actif`) VALUES
(1, 'Admin', 'System', 'admin@agence.com', '0102030405', 'Agence principale', '2026-09-03 10:46:27', 1),
(10, 'Diop', 'Awa', 'client@demo.sn', '771234567', 'Dakar Plateau', '2026-09-05 04:31:35', 1);

-- --------------------------------------------------------

--
-- Structure de la table `contratlocation`
--

CREATE TABLE `contratlocation` (
  `idContrat` int(11) NOT NULL,
  `idReservation` int(11) NOT NULL,
  `dateSignature` datetime NOT NULL,
  `dateDebutEffective` datetime NOT NULL,
  `dateFinEffective` datetime NOT NULL,
  `etatRetour` enum('BON','MOYEN','MAUVAIS') DEFAULT 'BON',
  `kilometrageFinal` int(11) NOT NULL DEFAULT 0,
  `nombreJours` int(11) NOT NULL DEFAULT 0,
  `prixJournalier` decimal(10,2) NOT NULL DEFAULT 0.00,
  `sousTotal` decimal(10,2) NOT NULL DEFAULT 0.00,
  `fraisRetourDiffere` decimal(10,2) DEFAULT 0.00,
  `fraisCarburant` decimal(10,2) DEFAULT 0.00,
  `fraisRetard` decimal(10,2) DEFAULT 0.00,
  `fraisDegradation` decimal(10,2) DEFAULT 0.00,
  `remise` decimal(10,2) DEFAULT 0.00,
  `montantTotal` decimal(10,2) NOT NULL,
  `montantProvisoire` decimal(10,2) DEFAULT 0.00,
  `actif` tinyint(1) DEFAULT 1,
  `dateCreation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `contratlocation`
--

INSERT INTO `contratlocation` (`idContrat`, `idReservation`, `dateSignature`, `dateDebutEffective`, `dateFinEffective`, `etatRetour`, `kilometrageFinal`, `nombreJours`, `prixJournalier`, `sousTotal`, `fraisRetourDiffere`, `fraisCarburant`, `fraisRetard`, `fraisDegradation`, `remise`, `montantTotal`, `montantProvisoire`, `actif`, `dateCreation`) VALUES
(13, 27, '2026-09-19 14:59:26', '2026-09-20 10:00:00', '2026-09-25 10:00:00', 'BON', 40000, 6, 3500.00, 21000.00, 0.00, 0.00, 0.00, 3000.00, 0.00, 24000.00, 21000.00, 0, '2026-09-19 14:59:26'),
(14, 28, '2026-09-19 15:01:06', '2026-09-25 10:00:00', '2026-09-30 10:00:00', 'BON', 5000, 6, 4500.00, 27000.00, 0.00, 0.00, 0.00, 5000.00, 0.00, 32000.00, 27000.00, 0, '2026-09-19 15:01:06');

--
-- Déclencheurs `contratlocation`
--
DELIMITER $$
CREATE TRIGGER `check_contrat_dates_insert` BEFORE INSERT ON `contratlocation` FOR EACH ROW BEGIN
    IF NEW.dateFinEffective <= NEW.dateDebutEffective THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Erreur: La date de fin effective doit être postérieure à la date de début effective';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `check_contrat_dates_update` BEFORE UPDATE ON `contratlocation` FOR EACH ROW BEGIN
    IF NEW.dateFinEffective <= NEW.dateDebutEffective THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Erreur: La date de fin effective doit être postérieure à la date de début effective';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `check_contrat_kilometrage_insert` BEFORE INSERT ON `contratlocation` FOR EACH ROW BEGIN
    IF NEW.kilometrageFinal < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Erreur: Le kilométrage final ne peut pas être négatif';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `check_contrat_kilometrage_update` BEFORE UPDATE ON `contratlocation` FOR EACH ROW BEGIN
    IF NEW.kilometrageFinal < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Erreur: Le kilométrage final ne peut pas être négatif';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `check_contrat_montant_insert` BEFORE INSERT ON `contratlocation` FOR EACH ROW BEGIN
    IF NEW.montantTotal < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Erreur: Le montant total ne peut pas être négatif';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `check_contrat_montant_update` BEFORE UPDATE ON `contratlocation` FOR EACH ROW BEGIN
    IF NEW.montantTotal < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Erreur: Le montant total ne peut pas être négatif';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `facture`
--

CREATE TABLE `facture` (
  `idFacture` int(11) NOT NULL,
  `idContrat` int(11) NOT NULL,
  `dateEmission` datetime NOT NULL,
  `montantTotal` decimal(10,2) NOT NULL CHECK (`montantTotal` >= 0),
  `montantPaye` decimal(10,2) DEFAULT 0.00,
  `resteAPayer` decimal(10,2) DEFAULT 0.00,
  `statut` enum('IMPAYEE','PARTIELLE','PAYEE') DEFAULT 'IMPAYEE',
  `pdfPath` varchar(255) DEFAULT NULL,
  `datePaiement` datetime DEFAULT NULL,
  `dateCreation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `facture`
--

INSERT INTO `facture` (`idFacture`, `idContrat`, `dateEmission`, `montantTotal`, `montantPaye`, `resteAPayer`, `statut`, `pdfPath`, `datePaiement`, `dateCreation`) VALUES
(5, 13, '2026-09-19 14:59:55', 24000.00, 24000.00, 0.00, 'PAYEE', NULL, '2026-09-19 15:00:15', '2026-09-19 14:59:55'),
(6, 14, '2026-09-19 15:01:40', 32000.00, 32000.00, 0.00, 'PAYEE', NULL, '2026-09-19 15:29:21', '2026-09-19 15:01:40');

-- --------------------------------------------------------

--
-- Structure de la table `paiement`
--

CREATE TABLE `paiement` (
  `idPaiement` int(11) NOT NULL,
  `idFacture` int(11) NOT NULL,
  `mode` enum('ESPECE','CARTE','VIREMENT','CHEQUE','MOBILE_MONEY','WAVE','ORANGE_MONEY','FREE_MONEY') NOT NULL,
  `montantPaye` decimal(10,2) NOT NULL CHECK (`montantPaye` > 0),
  `devise` varchar(3) DEFAULT 'XOF',
  `datePaiement` datetime NOT NULL,
  `referenceTransaction` varchar(100) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `dateCreation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `paiement`
--

INSERT INTO `paiement` (`idPaiement`, `idFacture`, `mode`, `montantPaye`, `devise`, `datePaiement`, `referenceTransaction`, `notes`, `dateCreation`) VALUES
(4, 5, 'WAVE', 24000.00, 'XOF', '2026-09-19 15:00:15', NULL, NULL, '2026-09-19 15:00:15'),
(5, 6, 'WAVE', 27000.00, 'XOF', '2026-09-19 15:02:03', NULL, NULL, '2026-09-19 15:02:03'),
(10, 6, 'WAVE', 5000.00, 'XOF', '2026-09-19 15:29:21', NULL, NULL, '2026-09-19 15:29:21');

-- --------------------------------------------------------

--
-- Structure de la table `parking`
--

CREATE TABLE `parking` (
  `idParking` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `adresse` varchar(255) NOT NULL,
  `capacite` int(11) NOT NULL CHECK (`capacite` >= 0),
  `actif` tinyint(1) DEFAULT 1,
  `dateCreation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `parking`
--

INSERT INTO `parking` (`idParking`, `nom`, `adresse`, `capacite`, `actif`, `dateCreation`) VALUES
(1, 'Parking Central', '123 Avenue de la République, Dakar', 50, 1, '2026-09-05 01:38:09'),
(2, 'Parking Nord', '45 Rue des Fleurs, Dakar', 35, 1, '2026-09-05 01:38:09'),
(3, 'Parking Sud', '78 Boulevard du Sud, Dakar', 40, 1, '2026-09-05 01:38:09'),
(4, 'Parking Aéroport', 'Aéroport International, Dakar', 60, 1, '2026-09-05 01:38:09'),
(5, 'Parking Gare', 'Gare Routière, Dakar', 30, 1, '2026-09-05 01:38:09');

-- --------------------------------------------------------

--
-- Structure de la table `reservation`
--

CREATE TABLE `reservation` (
  `idReservation` int(11) NOT NULL,
  `idClient` int(11) NOT NULL,
  `idVehicule` int(11) NOT NULL,
  `dateDebut` datetime NOT NULL,
  `dateFin` datetime NOT NULL,
  `dateDemande` timestamp NOT NULL DEFAULT current_timestamp(),
  `statut` enum('EN_ATTENTE','ACCEPTEE','REFUSEE','ANNULEE') DEFAULT 'EN_ATTENTE',
  `lieuPriseEnCharge` varchar(255) NOT NULL,
  `lieuRetour` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `reservation`
--

INSERT INTO `reservation` (`idReservation`, `idClient`, `idVehicule`, `dateDebut`, `dateFin`, `dateDemande`, `statut`, `lieuPriseEnCharge`, `lieuRetour`) VALUES
(27, 10, 3, '2026-09-20 10:00:00', '2026-09-25 10:00:00', '2026-09-19 14:59:26', 'ACCEPTEE', 'Parking Aéroport', 'Parking Aéroport'),
(28, 10, 1, '2026-09-25 10:00:00', '2026-09-30 10:00:00', '2026-09-19 15:01:06', 'ACCEPTEE', 'Parking Aéroport', 'Parking Nord');

--
-- Déclencheurs `reservation`
--
DELIMITER $$
CREATE TRIGGER `check_reservation_dates_insert` BEFORE INSERT ON `reservation` FOR EACH ROW BEGIN
    IF NEW.dateFin <= NEW.dateDebut THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Erreur: La date de fin doit être postérieure à la date de début';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `check_reservation_dates_update` BEFORE UPDATE ON `reservation` FOR EACH ROW BEGIN
    IF NEW.dateFin <= NEW.dateDebut THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Erreur: La date de fin doit être postérieure à la date de début';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `sanction`
--

CREATE TABLE `sanction` (
  `idSanction` int(11) NOT NULL,
  `idClient` int(11) NOT NULL,
  `idContrat` int(11) DEFAULT NULL,
  `type` enum('RETARD','DEGRADATION','AUTRE') NOT NULL,
  `montant` decimal(10,2) NOT NULL CHECK (`montant` >= 0),
  `description` text DEFAULT NULL,
  `dateEnregistrement` datetime NOT NULL,
  `statut` enum('ACTIVE','LEVEE','REDUITE') DEFAULT 'ACTIVE',
  `dateCreation` timestamp NOT NULL DEFAULT current_timestamp(),
  `idFacture` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `sanction`
--

INSERT INTO `sanction` (`idSanction`, `idClient`, `idContrat`, `type`, `montant`, `description`, `dateEnregistrement`, `statut`, `dateCreation`, `idFacture`) VALUES
(5, 10, 13, 'DEGRADATION', 3000.00, 'Frais de dégradation constatés au retour du véhicule. État du retour : BON', '2026-09-19 14:59:55', 'LEVEE', '2026-09-19 14:59:55', 5),
(6, 10, 14, 'DEGRADATION', 5000.00, 'Frais de dégradation constatés au retour du véhicule. État du retour : BON', '2026-09-19 15:01:40', 'LEVEE', '2026-09-19 15:01:40', 6);

-- --------------------------------------------------------

--
-- Structure de la table `utilisateur`
--

CREATE TABLE `utilisateur` (
  `idUtilisateur` int(11) NOT NULL,
  `email` varchar(100) NOT NULL,
  `motDePasseHash` varchar(255) NOT NULL,
  `role` enum('ADMIN','CLIENT') NOT NULL DEFAULT 'CLIENT',
  `idClient` int(11) DEFAULT NULL,
  `dateCreation` timestamp NOT NULL DEFAULT current_timestamp(),
  `dernierAcces` timestamp NULL DEFAULT NULL,
  `actif` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `utilisateur`
--

INSERT INTO `utilisateur` (`idUtilisateur`, `email`, `motDePasseHash`, `role`, `idClient`, `dateCreation`, `dernierAcces`, `actif`) VALUES
(4, 'admin@agence.com', 'admin123', 'ADMIN', NULL, '2026-09-03 10:54:50', '2026-09-19 15:28:59', 1),
(5, 'client@demo.sn', 'client123', 'CLIENT', 10, '2026-09-05 04:31:35', '2026-09-19 15:31:07', 1);

-- --------------------------------------------------------

--
-- Structure de la table `vehicule`
--

CREATE TABLE `vehicule` (
  `idVehicule` int(11) NOT NULL,
  `marque` varchar(50) NOT NULL,
  `modele` varchar(50) NOT NULL,
  `annee` int(11) NOT NULL CHECK (`annee` >= 1900),
  `immatriculation` varchar(20) NOT NULL,
  `carburant` enum('ESSENCE','DIESEL','ELECTRIQUE','HYBRIDE','GPL') NOT NULL,
  `kilometrage` int(11) NOT NULL DEFAULT 0 CHECK (`kilometrage` >= 0),
  `prixJournalier` decimal(10,2) NOT NULL CHECK (`prixJournalier` > 0),
  `statut` enum('DISPONIBLE','LOUE','MAINTENANCE','NETTOYAGE','RESERVE') DEFAULT 'DISPONIBLE',
  `idParking` int(11) DEFAULT NULL,
  `actif` tinyint(1) DEFAULT 1,
  `dateCreation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `vehicule`
--

INSERT INTO `vehicule` (`idVehicule`, `marque`, `modele`, `annee`, `immatriculation`, `carburant`, `kilometrage`, `prixJournalier`, `statut`, `idParking`, `actif`, `dateCreation`) VALUES
(1, 'Peugeot', '208', 2022, 'AB-123-CD', 'DIESEL', 25000, 4500.00, 'DISPONIBLE', 1, 1, '2026-09-05 01:38:41'),
(2, 'Renault', 'Clio', 2023, 'EF-456-GH', 'ESSENCE', 15000, 4000.00, 'DISPONIBLE', 1, 1, '2026-09-05 01:38:41'),
(3, 'Citroën', 'C3', 2021, 'IJ-789-KL', 'DIESEL', 35000, 3500.00, 'DISPONIBLE', 2, 1, '2026-09-05 01:38:41'),
(4, 'Toyota', 'Yaris', 2022, 'MN-012-OP', 'HYBRIDE', 20000, 5000.00, 'DISPONIBLE', 2, 1, '2026-09-05 01:38:41'),
(5, 'Ford', 'Fiesta', 2020, 'QR-345-ST', 'ESSENCE', 45000, 3000.00, 'DISPONIBLE', 3, 1, '2026-09-05 01:38:41'),
(6, 'Mercedes', 'Classe A', 2023, 'UV-678-WX', 'DIESEL', 10000, 7000.00, 'DISPONIBLE', 3, 1, '2026-09-05 01:38:41'),
(7, 'BMW', 'Série 1', 2022, 'YZ-901-AB', 'ESSENCE', 18000, 6500.00, 'DISPONIBLE', 4, 1, '2026-09-05 01:38:41'),
(8, 'Audi', 'A3', 2021, 'CD-234-EF', 'DIESEL', 30000, 6000.00, 'DISPONIBLE', 4, 1, '2026-09-05 01:38:41'),
(9, 'Volkswagen', 'Golf', 2023, 'GH-567-IJ', 'ESSENCE', 8000, 5500.00, 'DISPONIBLE', 5, 1, '2026-09-05 01:38:41'),
(10, 'Nissan', 'Qashqai', 2022, 'KL-890-MN', 'DIESEL', 22000, 4800.00, 'DISPONIBLE', 5, 1, '2026-09-05 01:38:41');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `client`
--
ALTER TABLE `client`
  ADD PRIMARY KEY (`idClient`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Index pour la table `contratlocation`
--
ALTER TABLE `contratlocation`
  ADD PRIMARY KEY (`idContrat`),
  ADD UNIQUE KEY `idReservation` (`idReservation`),
  ADD KEY `idx_contrat_reservation` (`idReservation`);

--
-- Index pour la table `facture`
--
ALTER TABLE `facture`
  ADD PRIMARY KEY (`idFacture`),
  ADD UNIQUE KEY `idContrat` (`idContrat`),
  ADD KEY `idx_facture_contrat` (`idContrat`),
  ADD KEY `idx_facture_statut` (`statut`);

--
-- Index pour la table `paiement`
--
ALTER TABLE `paiement`
  ADD PRIMARY KEY (`idPaiement`),
  ADD KEY `idx_paiement_facture` (`idFacture`);

--
-- Index pour la table `parking`
--
ALTER TABLE `parking`
  ADD PRIMARY KEY (`idParking`);

--
-- Index pour la table `reservation`
--
ALTER TABLE `reservation`
  ADD PRIMARY KEY (`idReservation`),
  ADD KEY `idx_reservation_client` (`idClient`),
  ADD KEY `idx_reservation_vehicule` (`idVehicule`),
  ADD KEY `idx_reservation_statut` (`statut`),
  ADD KEY `idx_reservation_dates` (`dateDebut`,`dateFin`);

--
-- Index pour la table `sanction`
--
ALTER TABLE `sanction`
  ADD PRIMARY KEY (`idSanction`),
  ADD KEY `idx_sanction_client` (`idClient`),
  ADD KEY `idx_sanction_contrat` (`idContrat`),
  ADD KEY `idx_sanction_statut` (`statut`),
  ADD KEY `fk_sanction_facture` (`idFacture`);

--
-- Index pour la table `utilisateur`
--
ALTER TABLE `utilisateur`
  ADD PRIMARY KEY (`idUtilisateur`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idClient` (`idClient`);

--
-- Index pour la table `vehicule`
--
ALTER TABLE `vehicule`
  ADD PRIMARY KEY (`idVehicule`),
  ADD UNIQUE KEY `immatriculation` (`immatriculation`),
  ADD KEY `idParking` (`idParking`),
  ADD KEY `idx_vehicule_statut` (`statut`),
  ADD KEY `idx_vehicule_marque` (`marque`),
  ADD KEY `idx_vehicule_immatriculation` (`immatriculation`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `client`
--
ALTER TABLE `client`
  MODIFY `idClient` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `contratlocation`
--
ALTER TABLE `contratlocation`
  MODIFY `idContrat` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT pour la table `facture`
--
ALTER TABLE `facture`
  MODIFY `idFacture` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `paiement`
--
ALTER TABLE `paiement`
  MODIFY `idPaiement` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT pour la table `parking`
--
ALTER TABLE `parking`
  MODIFY `idParking` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `reservation`
--
ALTER TABLE `reservation`
  MODIFY `idReservation` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT pour la table `sanction`
--
ALTER TABLE `sanction`
  MODIFY `idSanction` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `utilisateur`
--
ALTER TABLE `utilisateur`
  MODIFY `idUtilisateur` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `vehicule`
--
ALTER TABLE `vehicule`
  MODIFY `idVehicule` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `contratlocation`
--
ALTER TABLE `contratlocation`
  ADD CONSTRAINT `contratlocation_ibfk_1` FOREIGN KEY (`idReservation`) REFERENCES `reservation` (`idReservation`) ON DELETE CASCADE;

--
-- Contraintes pour la table `facture`
--
ALTER TABLE `facture`
  ADD CONSTRAINT `facture_ibfk_1` FOREIGN KEY (`idContrat`) REFERENCES `contratlocation` (`idContrat`) ON DELETE CASCADE;

--
-- Contraintes pour la table `paiement`
--
ALTER TABLE `paiement`
  ADD CONSTRAINT `paiement_ibfk_1` FOREIGN KEY (`idFacture`) REFERENCES `facture` (`idFacture`) ON DELETE CASCADE;

--
-- Contraintes pour la table `reservation`
--
ALTER TABLE `reservation`
  ADD CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`idClient`) REFERENCES `client` (`idClient`) ON DELETE CASCADE,
  ADD CONSTRAINT `reservation_ibfk_2` FOREIGN KEY (`idVehicule`) REFERENCES `vehicule` (`idVehicule`) ON DELETE CASCADE;

--
-- Contraintes pour la table `sanction`
--
ALTER TABLE `sanction`
  ADD CONSTRAINT `fk_sanction_facture` FOREIGN KEY (`idFacture`) REFERENCES `facture` (`idFacture`) ON DELETE CASCADE,
  ADD CONSTRAINT `sanction_ibfk_1` FOREIGN KEY (`idClient`) REFERENCES `client` (`idClient`) ON DELETE CASCADE,
  ADD CONSTRAINT `sanction_ibfk_2` FOREIGN KEY (`idContrat`) REFERENCES `contratlocation` (`idContrat`) ON DELETE SET NULL;

--
-- Contraintes pour la table `utilisateur`
--
ALTER TABLE `utilisateur`
  ADD CONSTRAINT `utilisateur_ibfk_1` FOREIGN KEY (`idClient`) REFERENCES `client` (`idClient`) ON DELETE CASCADE;

--
-- Contraintes pour la table `vehicule`
--
ALTER TABLE `vehicule`
  ADD CONSTRAINT `vehicule_ibfk_1` FOREIGN KEY (`idParking`) REFERENCES `parking` (`idParking`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
