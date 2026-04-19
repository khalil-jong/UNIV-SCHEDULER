-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : dim. 19 avr. 2026 à 04:05
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;


-- Base de données : `univ_scheduler`


-- --------------------------------------------------------


-- Structure de la table `batiments`

CREATE TABLE `batiments` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `localisation` varchar(200) NOT NULL,
  `nombre_etages` int(11) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- Déchargement des données de la table `batiments`

INSERT INTO `batiments` (`id`, `nom`, `localisation`, `nombre_etages`, `created_at`) VALUES
(4, 'UFR-SET', 'A droite de l\'entrée principale', 0, '2026-04-05 13:55:21'),
(5, 'UFR-SES', 'Juste avant l\'entrée du campus social', 1, '2026-04-05 13:56:44'),
(7, 'UFR-SI', 'a gauche de la porte centrale', 1, '2026-04-18 23:21:37');

-- --------------------------------------------------------


-- Structure de la table `classes`

CREATE TABLE `classes` (
  `id` int(11) NOT NULL,
  `nom` varchar(60) NOT NULL,
  `filiere` varchar(80) DEFAULT NULL,
  `niveau` varchar(20) DEFAULT NULL,
  `effectif` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Déchargement des données de la table `classes`

INSERT INTO `classes` (`id`, `nom`, `filiere`, `niveau`, `effectif`, `created_at`) VALUES
(2, 'L1-mathématique', 'MATHEMATIQUE', 'Licence 1', 101, '2026-04-15 10:16:44'),
(3, 'L2-informatique', 'INFORMATIQUE', 'Licence 2', 90, '2026-04-15 10:18:19');

-- --------------------------------------------------------


-- Structure de la table `cours`

CREATE TABLE `cours` (
  `id` int(11) NOT NULL,
  `matiere` varchar(100) NOT NULL,
  `enseignant` varchar(100) NOT NULL,
  `classe` varchar(50) NOT NULL,
  `groupe` varchar(50) DEFAULT '',
  `date_debut` datetime NOT NULL,
  `duree` int(11) NOT NULL,
  `salle_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- Déchargement des données de la table `cours`

INSERT INTO `cours` (`id`, `matiere`, `enseignant`, `classe`, `groupe`, `date_debut`, `duree`, `salle_id`, `created_at`) VALUES
(9, 'algo', 'Jean Martin', 'L2-informatique', '', '2026-04-13 08:00:00', 90, 11, '2026-04-17 20:21:44'),
(10, '[srgg', 'alssainy diallo', 'L2-informatique', '', '2026-04-15 08:00:00', 90, 23, '2026-04-18 23:44:38'),
(11, 'mATH', 'alssainy diallo', 'L1-mathématique', '', '2026-04-13 08:00:00', 90, 22, '2026-04-18 23:52:13'),
(12, 'cdddre', 'Jean Martin', 'L1-mathématique', '', '2026-04-13 21:00:00', 90, 9, '2026-04-18 23:55:35');

-- --------------------------------------------------------


-- Structure de la table `emploi_du_temps`

CREATE TABLE `emploi_du_temps` (
  `id` int(11) NOT NULL,
  `classe` varchar(60) NOT NULL,
  `matiere` varchar(80) NOT NULL,
  `enseignant` varchar(100) NOT NULL,
  `salle_id` int(11) NOT NULL,
  `jour_semaine` tinyint(4) NOT NULL COMMENT '1=Lundi 2=Mardi 3=Mercredi 4=Jeudi 5=Vendredi 6=Samedi',
  `heure_debut` time NOT NULL,
  `duree` int(11) NOT NULL COMMENT 'en minutes',
  `type_cours` varchar(20) NOT NULL DEFAULT 'CM' COMMENT 'CM TD TP',
  `actif` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Déchargement des données de la table `emploi_du_temps`

INSERT INTO `emploi_du_temps` (`id`, `classe`, `matiere`, `enseignant`, `salle_id`, `jour_semaine`, `heure_debut`, `duree`, `type_cours`, `actif`, `created_at`) VALUES
(9, 'L2-informatique', 'algo', 'Jean Martin', 11, 1, '08:00:00', 90, 'CM', 1, '2026-04-17 20:21:44'),
(11, 'L1-mathématique', 'mATH', 'alssainy diallo', 22, 1, '08:00:00', 90, 'CM', 1, '2026-04-18 23:52:13'),
(12, 'L1-mathématique', 'cdddre', 'Jean Martin', 9, 1, '21:00:00', 90, 'CM', 1, '2026-04-18 23:55:35');

-- --------------------------------------------------------


-- Structure de la table `messages`

CREATE TABLE `messages` (
  `id` int(11) NOT NULL,
  `expediteur_id` int(11) NOT NULL,
  `expediteur_nom` varchar(120) NOT NULL,
  `expediteur_role` varchar(20) NOT NULL,
  `sujet` varchar(200) NOT NULL,
  `corps` text NOT NULL,
  `type` enum('RESERVATION','RECLAMATION','GENERAL','ALERTE') DEFAULT 'GENERAL',
  `lu` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `destinataire_role` varchar(20) DEFAULT 'GESTIONNAIRE',
  `destinataire_id` int(11) DEFAULT NULL COMMENT 'ID utilisateur destinataire (NULL = tous du rôle)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- Déchargement des données de la table `messages`

INSERT INTO `messages` (`id`, `expediteur_id`, `expediteur_nom`, `expediteur_role`, `sujet`, `corps`, `type`, `lu`, `created_at`, `destinataire_role`, `destinataire_id`) VALUES
(14, 2, 'Ibrahima Diallo', 'GESTIONNAIRE', '[Réponse signalement] response', 'hhhhhhhhhhh7iuy6tre', 'GENERAL', 0, '2026-04-18 23:48:04', 'ENSEIGNANT', 5),
(15, 2, 'Ibrahima Diallo', 'GESTIONNAIRE', '[Validation de réservation] hytrec', 'uuytrufeds', 'GENERAL', 0, '2026-04-18 23:48:22', 'ENSEIGNANT', 3),
(16, 2, 'Ibrahima Diallo', 'GESTIONNAIRE', '[Message général] uytgrfed', 'jjyjtgjfds', 'GENERAL', 0, '2026-04-18 23:48:35', 'ETUDIANT', 4),
(17, 2, 'Ibrahima Diallo', 'GESTIONNAIRE', '[Groupe] tous les enseignants', 'sssss', 'GENERAL', 0, '2026-04-18 23:49:26', 'ENSEIGNANT', 5),
(18, 2, 'Ibrahima Diallo', 'GESTIONNAIRE', '[Groupe] tous les enseignants', 'sssss', 'GENERAL', 0, '2026-04-18 23:49:26', 'ENSEIGNANT', 3),
(19, 2, 'Ibrahima Diallo', 'GESTIONNAIRE', '[Groupe] tous les etudiants', 'ssss', 'GENERAL', 0, '2026-04-18 23:49:38', 'ETUDIANT', 4),
(20, 2, 'Ibrahima Diallo', 'GESTIONNAIRE', '[Groupe] tous les utilisateur', 'sssss', 'GENERAL', 0, '2026-04-18 23:50:00', 'ENSEIGNANT', 5),
(21, 2, 'Ibrahima Diallo', 'GESTIONNAIRE', '[Groupe] tous les utilisateur', 'sssss', 'GENERAL', 0, '2026-04-18 23:50:00', 'ENSEIGNANT', 3),
(22, 2, 'Ibrahima Diallo', 'GESTIONNAIRE', '[Groupe] tous les utilisateur', 'sssss', 'GENERAL', 0, '2026-04-18 23:50:00', 'ETUDIANT', 4);

-- --------------------------------------------------------


-- Structure de la table `salles`

CREATE TABLE `salles` (
  `id` int(11) NOT NULL,
  `numero` varchar(50) NOT NULL,
  `capacite` int(11) NOT NULL,
  `type` varchar(20) NOT NULL,
  `batiment` varchar(50) NOT NULL,
  `etage` varchar(20) NOT NULL,
  `videoprojecteur` tinyint(1) DEFAULT 0,
  `tableau_interactif` tinyint(1) DEFAULT 0,
  `climatisation` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Déchargement des données de la table `salles`

INSERT INTO `salles` (`id`, `numero`, `capacite`, `type`, `batiment`, `etage`, `videoprojecteur`, `tableau_interactif`, `climatisation`, `created_at`) VALUES
(8, '1', 150, 'Amphi', 'UFR-SET', 'Rez-de-chaussée', 1, 1, 1, '2026-04-05 14:22:23'),
(9, '2', 100, 'Amphi', 'UFR-SET', '', 1, 1, 1, '2026-04-05 14:23:33'),
(10, '3', 130, 'Amphi', 'UFR-SET', '', 1, 1, 0, '2026-04-05 14:24:31'),
(11, '4', 150, 'Amphi', 'UFR-SET', '', 1, 1, 0, '2026-04-05 14:25:39'),
(13, '5', 70, 'TD', 'UFR-SET', '', 1, 1, 0, '2026-04-05 14:26:56'),
(14, '6', 50, 'TD', 'UFR-SET', '', 1, 1, 1, '2026-04-05 14:27:34'),
(15, '7', 60, 'TD', 'UFR-SET', '', 0, 1, 1, '2026-04-05 14:27:59'),
(16, '8', 35, 'TP', 'UFR-SET', '', 1, 1, 1, '2026-04-05 14:28:41'),
(22, '1', 255, 'Amphi', 'UFR-SES', 'Rez-de-chaussée', 1, 0, 0, '2026-04-14 18:54:36'),
(23, '2', 150, 'Amphi', 'UFR-SES', 'Rez-de-chaussée', 1, 1, 1, '2026-04-14 18:55:29'),
(24, '3', 80, 'TP', 'UFR-SES', 'Rez-de-chaussée', 1, 1, 0, '2026-04-15 09:40:41'),
(25, '4', 90, 'TP', 'UFR-SES', '1er étage', 1, 1, 0, '2026-04-15 09:41:06'),
(26, '1', 50, 'Amphi', 'UFR-SI', '1er étage', 1, 1, 1, '2026-04-18 23:22:50');

-- --------------------------------------------------------


-- Structure de la table `utilisateurs`

CREATE TABLE `utilisateurs` (
  `id` int(11) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `login` varchar(50) NOT NULL,
  `mot_de_passe` varchar(100) NOT NULL,
  `role` enum('ADMIN','GESTIONNAIRE','ENSEIGNANT','ETUDIANT') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `classe` varchar(60) DEFAULT NULL,
  `matiere` varchar(200) DEFAULT NULL COMMENT 'Matière(s) enseignée(s) — enseignants uniquement'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- Déchargement des données de la table `utilisateurs`

INSERT INTO `utilisateurs` (`id`, `nom`, `prenom`, `login`, `mot_de_passe`, `role`, `created_at`, `classe`, `matiere`) VALUES
(1, 'Ibra', 'Diongue', 'admin', 'admin123', 'ADMIN', '2026-03-13 06:23:27', NULL, NULL),
(2, 'Diallo', 'Ibrahima', 'gestionnaire', 'gest123', 'GESTIONNAIRE', '2026-03-13 06:23:27', NULL, NULL),
(3, 'Martin', 'Jean', 'enseignant', 'ens123', 'ENSEIGNANT', '2026-03-13 06:23:27', NULL, NULL),
(4, 'Ndiaye', 'Fatou', 'etudiant', 'etu123', 'ETUDIANT', '2026-03-13 06:23:27', NULL, NULL),
(5, 'diallo', 'alssainy', 'alssainy', 'alssainy', 'ENSEIGNANT', '2026-03-14 02:14:31', NULL, NULL);

-- ----------------------------------------------------------




-- INDEX POUR LES TABLES DÉCHARGÉES



-- Index pour la table `batiments`

ALTER TABLE `batiments`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `classes`
--
ALTER TABLE `classes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nom` (`nom`);


-- Index pour la table `cours`

ALTER TABLE `cours`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_classe` (`classe`),
  ADD KEY `idx_enseignant` (`enseignant`),
  ADD KEY `idx_salle` (`salle_id`),
  ADD KEY `idx_date` (`date_debut`);


-- Index pour la table `emploi_du_temps`

ALTER TABLE `emploi_du_temps`
  ADD PRIMARY KEY (`id`),
  ADD KEY `salle_id` (`salle_id`);


-- Index pour la table `messages`

ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_dest_id` (`destinataire_id`),
  ADD KEY `expediteur_id` (`expediteur_id`);


-- Index pour la table `salles`

ALTER TABLE `salles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_batiment` (`numero`,`batiment`);


-- Index pour la table `utilisateurs`

ALTER TABLE `utilisateurs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `login` (`login`);


-- AUTO_INCREMENT pour les tables déchargées



-- AUTO_INCREMENT pour la table `batiments`

ALTER TABLE `batiments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;


-- AUTO_INCREMENT pour la table `classes`

ALTER TABLE `classes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;


-- AUTO_INCREMENT pour la table `cours`

ALTER TABLE `cours`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;


-- AUTO_INCREMENT pour la table `emploi_du_temps`

ALTER TABLE `emploi_du_temps`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;


-- AUTO_INCREMENT pour la table `messages`

ALTER TABLE `messages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;


-- AUTO_INCREMENT pour la table `salles`

ALTER TABLE `salles`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;


-- AUTO_INCREMENT pour la table `utilisateurs`

ALTER TABLE `utilisateurs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;




-- CONTRAINTES POUR LES TABLES DÉCHARGÉES


-- Contraintes pour la table `cours`

ALTER TABLE `cours`
  ADD CONSTRAINT `cours_ibfk_1` FOREIGN KEY (`salle_id`) REFERENCES `salles` (`id`) ON DELETE CASCADE;


-- Contraintes pour la table `emploi_du_temps`

ALTER TABLE `emploi_du_temps`
  ADD CONSTRAINT `emploi_du_temps_ibfk_1` FOREIGN KEY (`salle_id`) REFERENCES `salles` (`id`);


-- Contraintes pour la table `messages`

ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`expediteur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
