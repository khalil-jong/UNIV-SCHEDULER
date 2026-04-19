[README.md](https://github.com/user-attachments/files/26872123/README.md)
#  UNIV-SCHEDULER

> Application de gestion des salles et des emplois du temps universitaires, développée en Java avec JavaFX.

---

## 📋 Table des matières

- [Présentation](#présentation)
- [Fonctionnalités](#fonctionnalités)
- [Architecture du projet](#architecture-du-projet)
- [Prérequis](#prérequis)
- [Installation et configuration](#installation-et-configuration)
- [Lancement de l'application](#lancement-de-lapplication)
- [Rôles utilisateurs](#rôles-utilisateurs)
- [Structure de la base de données](#structure-de-la-base-de-données)
- [Dépendances](#dépendances)

---

## Présentation

**UNIV-SCHEDULER** est une application de bureau développée en **Java 17 + JavaFX** permettant à une université de gérer :

- les salles de cours et leur disponibilité en temps réel,
- les emplois du temps par classe et par enseignant,
- les utilisateurs selon quatre rôles distincts (Admin, Gestionnaire, Enseignant, Étudiant),
- la messagerie interne entre les acteurs,
- l'envoi et la réception d'emails via SMTP/IMAP (Gmail),
- l'export des emplois du temps en PDF et Excel,
- la détection automatique des conflits horaires.

---

## Fonctionnalités

### Tableau de bord
- Vue synthétique : nombre de salles, créneaux actifs, cours du jour, comptes actifs
- Liste des cours planifiés pour la journée
- Affichage des conflits horaires détectés
- État d'occupation de chaque salle avec jauge de taux d'utilisation

### Gestion des utilisateurs *(Admin)*
- Création, modification et suppression de comptes (Admin, Gestionnaire, Enseignant, Étudiant)
- Attribution des rôles, classes et matières enseignées

### Gestion de l'infrastructure *(Admin / Gestionnaire)*
- Gestion des bâtiments (nom, localisation, étages)
- Gestion des salles (numéro, type, capacité, équipements)
- Recherche avancée de salles selon les critères souhaités

### Emplois du temps *(Gestionnaire)*
- Création et gestion des classes et des filières
- Ajout de créneaux EDT avec sélection de l'enseignant (nom + matière), de la salle et de l'horaire
- Détection automatique des conflits (salle ou enseignant déjà occupé)
- Visualisation de l'EDT par classe
- Export des emplois du temps en **PDF** et **Excel**

### Calendrier des cours
- Vue calendrier interactive avec filtrage par classe
- Affichage journalier des créneaux planifiés

### Réservation de salles
- Consultation des salles disponibles en temps réel
- Réservation directe depuis l'interface

### Messagerie interne
- Envoi de messages entre utilisateurs (enseignants, gestionnaires)
- Boîte de réception avec historique
- Alertes automatiques en cas de conflit horaire

### Emails SMTP/IMAP *(Admin / Gestionnaire)*
- Configuration du compte Gmail (SMTP + mot de passe d'application)
- Envoi d'emails directement depuis l'application
- Réception et lecture des emails avec affichage en cartes structurées

### Alertes et notifications
- Détection des conflits horaires
- Rappel des cours imminents (dans les 2 heures)
- Programme du jour en temps réel

---

## Architecture du projet

```
UNIV-SCHEDULER/
├── src/
│   ├── dao/                        # Couche d'accès aux données (DAO)
│   │   ├── BatimentDAO.java
│   │   ├── ClasseDAO.java
│   │   ├── CoursDAO.java
│   │   ├── EmploiDuTempsDAO.java
│   │   ├── MessageDAO.java
│   │   ├── SalleDAO.java
│   │   └── UtilisateurDAO.java
│   ├── database/
│   │   └── DatabaseConnection.java # Connexion MySQL
│   ├── models/                     # Modèles de données
│   │   ├── Batiment.java
│   │   ├── Classe.java
│   │   ├── Cours.java
│   │   ├── EmploiDuTemps.java
│   │   ├── Message.java
│   │   ├── Salle.java
│   │   └── Utilisateur.java
│   └── ui/                         # Interface JavaFX (panels)
│       ├── UnivSchedulerApp.java   # Point d'entrée (main)
│       ├── LoginPanel.java
│       ├── Design.java             # Système de design global
│       ├── DashboardPanel.java
│       ├── AdminPanel.java
│       ├── GestionnairePanel.java
│       ├── EnseignantPanel.java
│       ├── EtudiantPanel.java
│       ├── GestionCoursEDTPanel.java
│       ├── CalendrierPanel.java
│       ├── SallesDisponiblesPanel.java
│       ├── EmailGestionPanel.java
│       ├── EmailService.java
│       └── ...
├── lib/
│   ├── mysql-connector-j-9.6.0.jar
│   ├── javax.mail.jar
│   └── javax.activation.jar
├── init_db.sql                     # Script d'initialisation de la base
└── README.md
```

---

## Prérequis

| Outil | Version minimale |
|---|---|
| Java (JDK) | 17 |
| JavaFX SDK | 25.0.2 |
| MySQL / MariaDB | 10.4+ |
| IDE recommandé | Eclipse 2023+ |

---

## Installation et configuration

### 1. Cloner le projet

```bash
git clone https://github.com/votre-utilisateur/UNIV-SCHEDULER.git
cd UNIV-SCHEDULER
```

### 2. Créer la base de données

Ouvrez **phpMyAdmin** ou un client MySQL, puis exécutez le script fourni :

```sql
SOURCE init_db.sql;
```

Cela crée la base `univ_scheduler` avec toutes les tables et des données initiales.

### 3. Configurer la connexion MySQL

Ouvrez le fichier `src/database/DatabaseConnection.java` et adaptez les paramètres si nécessaire :

```java
private static final String URL      = "jdbc:mysql://localhost:3306/univ_scheduler?useSSL=false&serverTimezone=UTC";
private static final String USER     = "root";
private static final String PASSWORD = "";
```

### 4. Configurer Eclipse

1. Importez le projet dans Eclipse : `File → Import → Existing Projects into Workspace`
2. Ajoutez le **JavaFX SDK** au Build Path :
   - `Project → Properties → Java Build Path → Libraries`
   - Ajoutez le dossier `lib/` de votre installation JavaFX SDK
3. Vérifiez que les JARs suivants sont bien dans le Build Path :
   - `mysql-connector-j-9.6.0.jar`
   - `javax.mail.jar`
   - `javax.activation.jar`
4. Ajoutez les arguments VM pour JavaFX dans la configuration de lancement :
   ```
   --module-path "C:/chemin/vers/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
   ```

---

## Lancement de l'application

Exécutez la classe principale :

```
src/ui/UnivSchedulerApp.java → Run As → Java Application
```

La fenêtre de connexion s'affiche. Connectez-vous avec les identifiants d'un compte créé en base.

---

## Rôles utilisateurs

| Rôle | Accès |
|---|---|
| **ADMIN** | Tableau de bord, gestion de tous les utilisateurs, bâtiments, salles, alertes, emails |
| **GESTIONNAIRE** | Tableau de bord, calendrier, emplois du temps, salles disponibles, messagerie, export, emails |
| **ENSEIGNANT** | Ses cours à venir, réservation de salle, messagerie, emploi du temps de sa classe |
| **ÉTUDIANT** | Emploi du temps de sa classe, salles disponibles, messagerie reçue |

---

## Structure de la base de données

| Table | Description |
|---|---|
| `utilisateurs` | Comptes utilisateurs avec rôle, classe et matière |
| `batiments` | Bâtiments universitaires |
| `salles` | Salles avec type, capacité et équipements |
| `classes` | Classes par filière et niveau |
| `cours` | Cours liés à une salle, un enseignant et une classe |
| `emplois_du_temps` | Créneaux hebdomadaires (jour, heure, durée) |
| `messages` | Messages internes entre utilisateurs |

---

## Dépendances

| Bibliothèque | Rôle |
|---|---|
| **JavaFX 25.0.2** | Interface graphique |
| **mysql-connector-j 9.6.0** | Connexion à la base de données MySQL |
| **javax.mail** | Envoi et réception d'emails (SMTP/IMAP) |
| **javax.activation** | Dépendance de javax.mail pour les pièces jointes |

---

*Projet universitaire — UNIV-SCHEDULER © 2026*
