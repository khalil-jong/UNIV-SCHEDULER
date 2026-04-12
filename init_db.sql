-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : dim. 12 avr. 2026 à 06:20
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
(4, 'UFR-SET', 'A droite de l\'entrée principale', 0, '2026-04-05 13:55:21'),
(5, 'UFR-SES', 'Juste avant l\'entrée du campus social', 0, '2026-04-05 13:56:44');

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
(1, 'L1-informatique', 'INFORMATIQUE', 'Licence 1', 138, '2026-04-05 14:30:51');

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
  `type` enum('RESERVATION','RECLAMATION','GENERAL','ALERTE') DEFAULT 'GENERAL',
  `lu` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `destinataire_role` varchar(20) DEFAULT 'GESTIONNAIRE',
  `destinataire_id` int(11) DEFAULT NULL COMMENT 'ID utilisateur destinataire (NULL = tous du rôle)'
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
(8, '1', 150, 'Amphi', 'UFR-SET', '', 1, 1, 1, '2026-04-05 14:22:23'),
(9, '2', 100, 'Amphi', 'UFR-SET', '', 1, 1, 1, '2026-04-05 14:23:33'),
(10, '3', 130, 'Amphi', 'UFR-SET', '', 1, 1, 0, '2026-04-05 14:24:31'),
(11, '4', 150, 'Amphi', 'UFR-SET', '', 1, 1, 0, '2026-04-05 14:25:39'),
(13, '5', 70, 'TD', 'UFR-SET', '', 1, 1, 0, '2026-04-05 14:26:56'),
(14, '6', 50, 'TD', 'UFR-SET', '', 1, 1, 1, '2026-04-05 14:27:34'),
(15, '7', 60, 'TD', 'UFR-SET', '', 0, 1, 1, '2026-04-05 14:27:59'),
(16, '8', 35, 'TP', 'UFR-SET', '', 1, 1, 1, '2026-04-05 14:28:41');

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
(1, 'Ibra', 'Diongue', 'admin', 'admin123', 'ADMIN', '2026-03-13 06:23:27', NULL),
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
  ADD KEY `expediteur_id` (`expediteur_id`),
  ADD KEY `idx_dest_id` (`destinataire_id`);

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `classes`
--
ALTER TABLE `classes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `cours`
--
ALTER TABLE `cours`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `emploi_du_temps`
--
ALTER TABLE `emploi_du_temps`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `messages`
--
ALTER TABLE `messages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `salles`
--
ALTER TABLE `salles`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

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
