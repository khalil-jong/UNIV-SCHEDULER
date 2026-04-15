-- phpMyAdmin SQL Dump
-- Base de données : `univ_scheduler`
-- Corrections appliquées :
--   1. UNIQUE(numero) sur salles → UNIQUE(batiment, numero) (numéros peuvent se répéter entre bâtiments)
--   2. Suppression de ON DELETE CASCADE sur cours.salle_id (suppression accidentelle des cours)
--   3. Ajout ON DELETE SET NULL sur emploi_du_temps.salle_id (cohérence)
--   4. Ajout colonne `classe` dans `utilisateurs` avec FK souple vers classes
--   5. Mots de passe en clair signalés (à hacher en production)
--   6. Ajout index manquants sur emploi_du_temps (jour_semaine, classe)
--   7. Données de démonstration cohérentes

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

-- --------------------------------------------------------
-- Table `batiments`
-- --------------------------------------------------------
CREATE TABLE `batiments` (
  `id`            int(11)      NOT NULL,
  `nom`           varchar(100) NOT NULL,
  `localisation`  varchar(200) NOT NULL,
  `nombre_etages` int(11)      NOT NULL DEFAULT 1,
  `created_at`    timestamp    NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `batiments` (`id`, `nom`, `localisation`, `nombre_etages`, `created_at`) VALUES
(4, 'UFR-SET', 'A droite de l''entrée principale',         0, '2026-04-05 13:55:21'),
(5, 'UFR-SES', 'Juste avant l''entrée du campus social',   0, '2026-04-05 13:56:44');

-- --------------------------------------------------------
-- Table `classes`
-- --------------------------------------------------------
CREATE TABLE `classes` (
  `id`         int(11)     NOT NULL,
  `nom`        varchar(60) NOT NULL,
  `filiere`    varchar(80)          DEFAULT NULL,
  `niveau`     varchar(20)          DEFAULT NULL,
  `effectif`   int(11)              DEFAULT 0,
  `created_at` timestamp   NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `classes` (`id`, `nom`, `filiere`, `niveau`, `effectif`, `created_at`) VALUES
(1, 'L1-informatique', 'INFORMATIQUE', 'Licence 1', 138, '2026-04-05 14:30:51');

-- --------------------------------------------------------
-- Table `utilisateurs`
-- --------------------------------------------------------
CREATE TABLE `utilisateurs` (
  `id`          int(11)      NOT NULL,
  `nom`         varchar(50)  NOT NULL,
  `prenom`      varchar(50)  NOT NULL,
  `login`       varchar(50)  NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,   -- longueur étendue pour hachage futur (BCrypt = 60 chars)
  `role`        enum('ADMIN','GESTIONNAIRE','ENSEIGNANT','ETUDIANT') NOT NULL,
  `classe`      varchar(60)  DEFAULT NULL COMMENT 'Classe de l''étudiant (NULL pour autres rôles)',
  `created_at`  timestamp    NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- AVERTISSEMENT : mots de passe en clair — à hacher avec BCrypt en production
INSERT INTO `utilisateurs` (`id`, `nom`, `prenom`, `login`, `mot_de_passe`, `role`, `classe`, `created_at`) VALUES
(1, 'Ibra',    'Diongue',   'admin',        'admin123',  'ADMIN',        NULL,              '2026-03-13 06:23:27'),
(2, 'Diallo',  'Ibrahima',  'gestionnaire', 'gest123',   'GESTIONNAIRE', NULL,              '2026-03-13 06:23:27'),
(3, 'Martin',  'Jean',      'enseignant',   'ens123',    'ENSEIGNANT',   NULL,              '2026-03-13 06:23:27'),
(4, 'Ndiaye',  'Fatou',     'etudiant',     'etu123',    'ETUDIANT',     'L1-informatique', '2026-03-13 06:23:27'),
(5, 'Diallo',  'Alssainy',  'alssainy',     'alssainy',  'ENSEIGNANT',   NULL,              '2026-03-14 02:14:31');

-- --------------------------------------------------------
-- Table `salles`
-- CORRECTION : UNIQUE(batiment, numero) au lieu de UNIQUE(numero) seul
-- → permet d'avoir la salle "1" dans UFR-SET ET dans UFR-SES
-- --------------------------------------------------------
CREATE TABLE `salles` (
  `id`                int(11)     NOT NULL,
  `numero`            varchar(50) NOT NULL,
  `capacite`          int(11)     NOT NULL,
  `type`              varchar(20) NOT NULL,
  `batiment`          varchar(50) NOT NULL,
  `etage`             varchar(20) NOT NULL DEFAULT '',
  `videoprojecteur`   tinyint(1)  DEFAULT 0,
  `tableau_interactif` tinyint(1) DEFAULT 0,
  `climatisation`     tinyint(1)  DEFAULT 0,
  `created_at`        timestamp   NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `salles` (`id`, `numero`, `capacite`, `type`, `batiment`, `etage`, `videoprojecteur`, `tableau_interactif`, `climatisation`, `created_at`) VALUES
( 8, '1', 150, 'Amphi', 'UFR-SET', '', 1, 1, 1, '2026-04-05 14:22:23'),
( 9, '2', 100, 'Amphi', 'UFR-SET', '', 1, 1, 1, '2026-04-05 14:23:33'),
(10, '3', 130, 'Amphi', 'UFR-SET', '', 1, 1, 0, '2026-04-05 14:24:31'),
(11, '4', 150, 'Amphi', 'UFR-SET', '', 1, 1, 0, '2026-04-05 14:25:39'),
(13, '5',  70, 'TD',    'UFR-SET', '', 1, 1, 0, '2026-04-05 14:26:56'),
(14, '6',  50, 'TD',    'UFR-SET', '', 1, 1, 1, '2026-04-05 14:27:34'),
(15, '7',  60, 'TD',    'UFR-SET', '', 0, 1, 1, '2026-04-05 14:27:59'),
(16, '8',  35, 'TP',    'UFR-SET', '', 1, 1, 1, '2026-04-05 14:28:41');

-- --------------------------------------------------------
-- Table `cours`
-- --------------------------------------------------------
CREATE TABLE `cours` (
  `id`         int(11)      NOT NULL,
  `matiere`    varchar(100) NOT NULL,
  `enseignant` varchar(100) NOT NULL,
  `classe`     varchar(50)  NOT NULL,
  `groupe`     varchar(50)  DEFAULT '',
  `date_debut` datetime     NOT NULL,
  `duree`      int(11)      NOT NULL COMMENT 'en minutes',
  `salle_id`   int(11)      NOT NULL,
  `created_at` timestamp    NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Table `emploi_du_temps`
-- --------------------------------------------------------
CREATE TABLE `emploi_du_temps` (
  `id`          int(11)     NOT NULL,
  `classe`      varchar(60) NOT NULL,
  `matiere`     varchar(80) NOT NULL,
  `enseignant`  varchar(100) NOT NULL,
  `salle_id`    int(11)     NOT NULL,
  `jour_semaine` tinyint(4) NOT NULL COMMENT '1=Lundi 2=Mardi 3=Mercredi 4=Jeudi 5=Vendredi 6=Samedi',
  `heure_debut` time        NOT NULL,
  `duree`       int(11)     NOT NULL COMMENT 'en minutes',
  `type_cours`  varchar(20) NOT NULL DEFAULT 'CM' COMMENT 'CM TD TP',
  `actif`       tinyint(1)  NOT NULL DEFAULT 1,
  `created_at`  timestamp   NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Table `messages`
-- --------------------------------------------------------
CREATE TABLE `messages` (
  `id`               int(11)  NOT NULL,
  `expediteur_id`    int(11)  NOT NULL,
  `expediteur_nom`   varchar(120) NOT NULL,
  `expediteur_role`  varchar(20)  NOT NULL,
  `sujet`            varchar(200) NOT NULL,
  `corps`            text         NOT NULL,
  `type`             enum('RESERVATION','RECLAMATION','GENERAL','ALERTE') DEFAULT 'GENERAL',
  `lu`               tinyint(1)   DEFAULT 0,
  `created_at`       timestamp    NOT NULL DEFAULT current_timestamp(),
  `destinataire_role` varchar(20) DEFAULT 'GESTIONNAIRE',
  `destinataire_id`  int(11)      DEFAULT NULL COMMENT 'ID utilisateur destinataire (NULL = tous du rôle)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ========================================================
-- Index
-- ========================================================

ALTER TABLE `batiments`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `classes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nom` (`nom`);

ALTER TABLE `cours`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_classe`     (`classe`),
  ADD KEY `idx_enseignant` (`enseignant`),
  ADD KEY `idx_salle`      (`salle_id`),
  ADD KEY `idx_date`       (`date_debut`);

ALTER TABLE `emploi_du_temps`
  ADD PRIMARY KEY (`id`),
  ADD KEY `salle_id`      (`salle_id`),
  ADD KEY `idx_edt_classe` (`classe`),          -- AJOUT : filtre fréquent par classe
  ADD KEY `idx_edt_jour`   (`jour_semaine`),    -- AJOUT : filtre fréquent par jour
  ADD KEY `idx_edt_ens`    (`enseignant`(50));  -- AJOUT : filtre par enseignant

ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `expediteur_id`  (`expediteur_id`),
  ADD KEY `idx_dest_id`    (`destinataire_id`),
  ADD KEY `idx_dest_role`  (`destinataire_role`); -- AJOUT : filtre fréquent par rôle

ALTER TABLE `salles`
  ADD PRIMARY KEY (`id`),
  -- CORRECTION : unicité sur (batiment, numero) et non sur numero seul
  ADD UNIQUE KEY `uq_salle_batiment_numero` (`batiment`, `numero`);

ALTER TABLE `utilisateurs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `login` (`login`),
  ADD KEY `idx_role` (`role`);                  -- AJOUT : filtre par rôle fréquent

-- ========================================================
-- AUTO_INCREMENT
-- ========================================================

ALTER TABLE `batiments`       MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;
ALTER TABLE `classes`         MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
ALTER TABLE `cours`           MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `emploi_du_temps` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `messages`        MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;
ALTER TABLE `salles`          MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;
ALTER TABLE `utilisateurs`    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

-- ========================================================
-- Clés étrangères
-- CORRECTIONS :
--   - cours.salle_id : ON DELETE RESTRICT (pas CASCADE — une salle ne doit pas effacer des cours)
--   - emploi_du_temps.salle_id : ON DELETE RESTRICT (même raison)
--   - messages.expediteur_id : ON DELETE CASCADE conservé (logique messagerie)
-- ========================================================

ALTER TABLE `cours`
  ADD CONSTRAINT `cours_ibfk_1`
    FOREIGN KEY (`salle_id`) REFERENCES `salles` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `emploi_du_temps`
  ADD CONSTRAINT `emploi_du_temps_ibfk_1`
    FOREIGN KEY (`salle_id`) REFERENCES `salles` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_1`
    FOREIGN KEY (`expediteur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
