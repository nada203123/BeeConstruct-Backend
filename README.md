
# 🏗️ BeeConstruct – Backend (Microservices)

BeeConstruct est une application web de gestion dans le domaine de la construction, organisée autour d’une **architecture microservices** afin d’assurer scalabilité, modularité et maintenabilité.
Ce dépôt contient la partie **backend**, composée de plusieurs microservices Spring Boot et de composants d’infrastructure dédiés (Gateway, découverte de services, etc.)



## 🧩 Architecture & Pile technologique

### Architecture microservices

L’architecture backend se compose de plusieurs services indépendants :

- **API Gateway** :  
  Point d’entrée unique pour toutes les requêtes du frontend. Il route les appels vers les microservices appropriés selon des règles de routage prédéfinies.

- **Microservices métier** :  
  - Service Utilisateur  
  - Service Client  
  - Service Offre  
  - Service Employé  
  - Service Chantier  
  Chaque service gère son propre domaine fonctionnel et peut utiliser des technologies adaptées à ses besoins.

- **Service de découverte Eureka** :  
  Chaque microservice s’enregistre dynamiquement auprès d’Eureka, ce qui permet la découverte automatique et la communication entre services.

- **Communication inter‑services** :  
  - Protocole : REST (HTTP)  
  - Feign Client : facilite les appels interservices en encapsulant les requêtes HTTP et en s’intégrant avec Eureka pour la résolution de services
  - Format de données : JSON

### Technologies principales

- Langage : Java  
- Framework : Spring Boot (microservices)
- Spring Cloud : Gateway, Eureka Discovery, OpenFeign
- Base(s) de données : PostgreSQL (par microservice) 
- Sécurité : Spring Security / JWT / Keycloak
- Build : Maven 
- Conteneurisation : Docker 
- Orchestration : Kubernetes  



## ✨ Fonctionnalités principales côté backend

Les microservices exposent des API REST pour :

- Gestion des utilisateurs (authentification, rôles, profils).  
- Gestion des **clients** (création, mise à jour, recherche, suppression).  
- Gestion des **offres** (création d’offres, mise à jour, consultation).  
- Gestion des **employés** (affectation, informations, etc.).  
- Gestion des **chantiers** (création, planification, suivi d’avancement, liens avec clients, offres et employés), incluant la gestion des **marchandises**, le **pointage** des employés et le calcul des **salaires**.

L’API Gateway agrège ces services et fournit un point d’accès unifié pour le frontend Angular.




