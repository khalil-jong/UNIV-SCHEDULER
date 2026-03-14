-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : sam. 14 mars 2026 à 14:24
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
-- Base de données : `univ_scheduler`
--

-- --------------------------------------------------------

--
-- Structure de la table `batiments`
--

CREATE TABLE `batiments` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `localisation` varchar(200) NOT NULL,
  `nombre_etages` int(11) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `batiments`
--

INSERT INTO `batiments` (`id`, `nom`, `localisation`, `nombre_etages`, `created_at`) VALUES
(1, 'Bâtiment A', 'Campus principal - Bloc A', 3, '2026-03-13 06:23:27'),
(2, 'Bâtiment B', 'Campus principal - Bloc B', 2, '2026-03-13 06:23:27'),
(3, 'Bâtiment C', 'Campus secondaire', 1, '2026-03-13 06:23:27');

-- --------------------------------------------------------

--
-- Structure de la table `classes`
--

CREATE TABLE `classes` (
  `id` int(11) NOT NULL,
  `nom` varchar(60) NOT NULL,
  `filiere` varchar(80) DEFAULT NULL,
  `niveau` varchar(20) DEFAULT NULL,
  `effectif` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `classes`
--

INSERT INTO `classes` (`id`, `nom`, `filiere`, `niveau`, `effectif`, `created_at`) VALUES
(1, 'L1-Informatique', 'Informatique', 'Licence 1', 45, '2026-03-14 13:24:07'),
(2, 'L2-Informatique', 'Informatique', 'Licence 2', 38, '2026-03-14 13:24:07'),
(3, 'L3-Informatique', 'Informatique', 'Licence 3', 30, '2026-03-14 13:24:07'),
(4, 'L1-Mathématiques', 'Mathématiques', 'Licence 1', 40, '2026-03-14 13:24:07'),
(5, 'M1-Réseaux', 'Réseaux', 'Master 1', 22, '2026-03-14 13:24:07');

-- --------------------------------------------------------

--
-- Structure de la table `cours`
--

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

--
-- Déchargement des données de la table `cours`
--

INSERT INTO `cours` (`id`, `matiere`, `enseignant`, `classe`, `groupe`, `date_debut`, `duree`, `salle_id`, `created_at`) VALUES
(1, 'Mathématiques', 'Martin Jean', 'L2-Informatique', 'Groupe A', '2026-03-16 08:00:00', 90, 1, '2026-03-13 06:23:27'),
(2, 'Programmation Java', 'Dupont Paul', 'L2-Informatique', 'Groupe A', '2026-03-16 10:00:00', 120, 2, '2026-03-13 06:23:27'),
(3, 'Base de Données', 'Bernard Alice', 'L2-Informatique', 'Groupe B', '2026-03-16 14:00:00', 90, 3, '2026-03-13 06:23:27'),
(4, 'Algorithmes', 'Leclerc Marc', 'L2-Informatique', 'Groupe A', '2026-03-17 09:00:00', 120, 1, '2026-03-13 06:23:27'),
(5, 'Réseaux', 'Thomas Sophie', 'L2-Informatique', 'Groupe B', '2026-03-17 14:00:00', 90, 4, '2026-03-13 06:23:27'),
(6, 'Mathématiques', 'Martin Jean', 'L1-Informatique', 'Groupe A', '2026-03-18 08:00:00', 90, 5, '2026-03-13 06:23:27'),
(7, 'mécanique', 'Mr Fall', 'L2-Physique Chimique', '', '2026-03-16 10:00:00', 120, 4, '2026-03-13 07:17:58'),
(8, 'algo', 'Mr Diallo', 'L2-Physique Chimique', '', '2026-03-16 12:00:00', 120, 6, '2026-03-13 07:25:07'),
(9, 'Mathématiques', 'Martin Jean', 'L1-Informatique', '', '2026-03-13 08:00:00', 120, 2, '2026-03-13 07:32:48'),
(11, 'réseau télécomme', 'Mr Sarr', 'L3-informatique', '', '2026-03-09 08:00:00', 60, 1, '2026-03-13 17:59:12'),
(12, 'infographie', 'Mr Ndiaye', 'L2-Physique Chimique', '', '2026-03-09 08:00:00', 60, 4, '2026-03-14 01:04:31'),
(13, 'pc', 'Mr diallo', 'l1.resaux', '', '2026-03-03 08:00:00', 60, 1, '2026-03-14 02:01:30'),
(14, 'phylosophie', 'alssainy diallo', 'L3-Physique Chimique', '', '2026-03-09 08:00:00', 60, 5, '2026-03-14 02:16:36'),
(15, 'chimie nucléaire', 'diallo alssainy', 'L2-physique chimique', '', '2026-03-21 08:00:00', 60, 6, '2026-03-14 02:21:49');

-- --------------------------------------------------------

--
-- Structure de la table `emploi_du_temps`
--

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

--
-- Déchargement des données de la table `emploi_du_temps`
--

INSERT INTO `emploi_du_temps` (`id`, `classe`, `matiere`, `enseignant`, `salle_id`, `jour_semaine`, `heure_debut`, `duree`, `type_cours`, `actif`, `created_at`) VALUES
(1, 'L2-Informatique', 'Algorithmique', 'Martin Jean', 1, 1, '08:00:00', 90, 'CM', 1, '2026-03-13 22:55:52'),
(2, 'L2-Informatique', 'Algorithmique', 'Martin Jean', 2, 3, '10:00:00', 90, 'TD', 1, '2026-03-13 22:55:52'),
(3, 'L2-Informatique', 'Bases de données', 'Dupont Marie', 1, 2, '08:00:00', 90, 'CM', 1, '2026-03-13 22:55:52'),
(4, 'L2-Informatique', 'Bases de données', 'Dupont Marie', 3, 4, '14:00:00', 90, 'TD', 1, '2026-03-13 22:55:52'),
(5, 'L2-Informatique', 'Réseaux', 'Koné Ibrahima', 2, 5, '10:00:00', 90, 'CM', 1, '2026-03-13 22:55:52'),
(6, 'L3-Informatique', 'Génie Logiciel', 'Martin Jean', 1, 1, '10:00:00', 90, 'CM', 1, '2026-03-13 22:55:52'),
(7, 'L3-Informatique', 'Systèmes', 'Koné Ibrahima', 2, 3, '08:00:00', 90, 'CM', 1, '2026-03-13 22:55:52'),
(8, 'L1-Mathématiques', 'Analyse', 'Dupont Marie', 1, 2, '10:00:00', 90, 'CM', 1, '2026-03-13 22:55:52'),
(9, 'L1-Mathématiques', 'Algèbre', 'Martin Jean', 2, 4, '08:00:00', 90, 'CM', 1, '2026-03-13 22:55:52'),
(11, 'l2:pc', 'math', 'diallo alssainy', 2, 1, '08:00:00', 90, 'TD', 1, '2026-03-14 02:33:56');

-- --------------------------------------------------------

--
-- Structure de la table `messages`
--

CREATE TABLE `messages` (
  `id` int(11) NOT NULL,
  `expediteur_id` int(11) NOT NULL,
  `expediteur_nom` varchar(120) NOT NULL,
  `expediteur_role` varchar(20) NOT NULL,
  `sujet` varchar(200) NOT NULL,
  `corps` text NOT NULL,
  `type` enum('RESERVATION','RECLAMATION','GENERAL') DEFAULT 'GENERAL',
  `lu` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `salles`
--

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

--
-- Déchargement des données de la table `salles`
--

INSERT INTO `salles` (`id`, `numero`, `capacite`, `type`, `batiment`, `etage`, `videoprojecteur`, `tableau_interactif`, `climatisation`, `created_at`) VALUES
(1, 'A101', 50, 'TD', 'Bâtiment A', '1er étage', 1, 0, 1, '2026-03-13 06:23:27'),
(2, 'A102', 30, 'TP', 'Bâtiment A', '1er étage', 1, 1, 0, '2026-03-13 06:23:27'),
(3, 'A201', 100, 'Amphi', 'Bâtiment A', '2e étage', 1, 1, 1, '2026-03-13 06:23:27'),
(4, 'B101', 45, 'TD', 'Bâtiment B', '1er étage', 0, 0, 0, '2026-03-13 06:23:27'),
(5, 'B102', 60, 'TP', 'Bâtiment B', '1er étage', 1, 0, 1, '2026-03-13 06:23:27'),
(6, 'C101', 25, 'TD', 'Bâtiment C', '1er étage', 0, 0, 0, '2026-03-13 06:23:27');

-- --------------------------------------------------------

--
-- Structure de la table `utilisateurs`
--

CREATE TABLE `utilisateurs` (
  `id` int(11) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `login` varchar(50) NOT NULL,
  `mot_de_passe` varchar(100) NOT NULL,
  `role` enum('ADMIN','GESTIONNAIRE','ENSEIGNANT','ETUDIANT') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `classe` varchar(60) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `utilisateurs`
--

INSERT INTO `utilisateurs` (`id`, `nom`, `prenom`, `login`, `mot_de_passe`, `role`, `created_at`, `classe`) VALUES
(1, 'Admin', 'Système', 'admin', 'admin123', 'ADMIN', '2026-03-13 06:23:27', NULL),
(2, 'Diallo', 'Ibrahima', 'gestionnaire', 'gest123', 'GESTIONNAIRE', '2026-03-13 06:23:27', NULL),
(3, 'Martin', 'Jean', 'enseignant', 'ens123', 'ENSEIGNANT', '2026-03-13 06:23:27', NULL),
(4, 'Ndiaye', 'Fatou', 'etudiant', 'etu123', 'ETUDIANT', '2026-03-13 06:23:27', NULL),
(5, 'diallo', 'alssainy', 'alssainy', 'alssainy', 'ENSEIGNANT', '2026-03-14 02:14:31', NULL);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `batiments`
--
ALTER TABLE `batiments`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `classes`
--
ALTER TABLE `classes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nom` (`nom`);

--
-- Index pour la table `cours`
--
ALTER TABLE `cours`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_classe` (`classe`),
  ADD KEY `idx_enseignant` (`enseignant`),
  ADD KEY `idx_salle` (`salle_id`),
  ADD KEY `idx_date` (`date_debut`);

--
-- Index pour la table `emploi_du_temps`
--
ALTER TABLE `emploi_du_temps`
  ADD PRIMARY KEY (`id`),
  ADD KEY `salle_id` (`salle_id`);

--
-- Index pour la table `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `expediteur_id` (`expediteur_id`);

--
-- Index pour la table `salles`
--
ALTER TABLE `salles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero` (`numero`);

--
-- Index pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `login` (`login`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `batiments`
--
ALTER TABLE `batiments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `classes`
--
ALTER TABLE `classes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `cours`
--
ALTER TABLE `cours`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT pour la table `emploi_du_temps`
--
ALTER TABLE `emploi_du_temps`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT pour la table `messages`
--
ALTER TABLE `messages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `salles`
--
ALTER TABLE `salles`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `cours`
--
ALTER TABLE `cours`
  ADD CONSTRAINT `cours_ibfk_1` FOREIGN KEY (`salle_id`) REFERENCES `salles` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `emploi_du_temps`
--
ALTER TABLE `emploi_du_temps`
  ADD CONSTRAINT `emploi_du_temps_ibfk_1` FOREIGN KEY (`salle_id`) REFERENCES `salles` (`id`);

--
-- Contraintes pour la table `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`expediteur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
