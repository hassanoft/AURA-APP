# AURA — Réseau Social Android (Java)

Application Android native **100% Java**, sans aucun fichier XML de layout, conçue pour **CodeAssist Android** et **Android Studio**.

---

## 📋 Caractéristiques

- **Java pur** — aucune ligne de Kotlin
- **Aucun fichier XML de layout** — toute l'UI est construite programmatiquement avec Material Design 3
- **Architecture MVC** propre : `models`, `managers` (Controller), `fragments`/`activities` (View)
- **Backend Supabase** : Auth, Database (PostgREST), Storage, Realtime (WebSocket)
- **Gradle Groovy DSL** (`build.gradle`, pas `.kts`)
- Compatible **Android 8.0 (API 26)** → **Android 15 (API 35)**

---

## 🗂 Structure du projet

```
com.aura
├── activities/      → SplashActivity, AuthActivity, MainActivity, ProfileActivity,
│                       ChatActivity, PostDetailActivity
├── auth/            → LoginFragment, RegisterWizardFragment (wizard 5 étapes)
├── fragments/       → FeedFragment, SearchFragment, NotificationsFragment,
│                       MessagesFragment, ProfileFragment, CreatePostFragment,
│                       EditProfileSheet
├── feed/            → PostDetailFragment
├── chat/            → ChatFragment (messagerie temps réel)
├── profile/         → ProfileStatsView, AvatarPicker
├── notifications/   → NotificationHelper, NotificationDispatcher
├── adapters/        → PostAdapter, MessageAdapter, NotificationAdapter,
│                       UserAdapter, CommentAdapter
├── models/          → User, Post, Comment, Message, Notification, Conversation
├── managers/        → AuthManager, DatabaseManager, StorageManager
├── services/        → RealtimeService (WebSocket), EmailService
├── supabase/        → SupabaseConfig, SupabaseClient
└── utils/           → U (UI builder), PasswordUtils, AgeValidator,
                        UsernameGenerator, CodeGenerator, TimeUtils
```

---

## ⚙️ Configuration Supabase

### 1. Créer un projet Supabase
Rendez-vous sur supabase.com et créez un nouveau projet.

### 2. Exécuter le schéma SQL
Ouvrez l'éditeur SQL de votre projet et exécutez le fichier `supabase_schema.sql`
fourni à la racine de ce projet. Il crée toutes les tables, triggers de compteurs
et politiques RLS.

### 3. Créer les buckets de stockage
Dans Storage, créez 3 buckets publics : `avatars`, `posts`, `messages`.

### 4. Activer Realtime
Dans Database > Replication, activez la réplication sur les tables `messages`
et `notifications` (déjà inclus dans le script SQL).

### 5. Configurer les credentials
Ouvrez `app/build.gradle` et remplacez :

```groovy
buildConfigField "String", "SUPABASE_URL",      '"https://YOUR_PROJECT_REF.supabase.co"'
buildConfigField "String", "SUPABASE_ANON_KEY", '"YOUR_ANON_KEY_HERE"'
```

par vos valeurs réelles (Project Settings > API).

### 6. (Optionnel) Edge Function pour l'envoi de codes par e-mail
Le wizard appelle `POST {SUPABASE_URL}/functions/v1/send-confirmation-email`
avec `{ "email": "...", "code": "..." }`. Déployez une Edge Function qui
envoie l'e-mail via votre fournisseur. En son absence, le code est loggé
(`Log.d("AURA_DEV", ...)`) pour le développement.

---

## 🚀 Compilation

### Android Studio
1. Ouvrir le dossier `AURA/` comme projet existant
2. Laisser Gradle synchroniser
3. Lancer sur émulateur/appareil API 26+

### CodeAssist Android
1. Importer le dossier `AURA/` en tant que projet Gradle
2. CodeAssist détecte `build.gradle` (Groovy)
3. Compiler et exécuter

---

## 🎨 Identité visuelle

| Élément             | Valeur     |
|---------------------|------------|
| Couleur principale  | `#18B05A`  |
| Couleur secondaire  | `#FFFFFF`  |
| Couleur sombre (bg) | `#121212`  |
| Logo                | `app/src/main/res/drawable/aura_logo.png` (icône, splash, login, inscription) |

---

## 🔐 Sécurité

- Mots de passe hachés en BCrypt (coût 12)
- Validation âge : 15 à 100 ans
- Format e-mail validé + vérification d'unicité Supabase
- Code de confirmation 6 caractères (A-Z, a-z, 1-9), envoyé par e-mail
- Row Level Security sur toutes les tables Supabase
- Trafic en clair désactivé (HTTPS uniquement)

---

## 📱 Fonctionnalités

### Inscription (Wizard 5 étapes)
1. Prénom / Nom (2-25 caractères)
2. E-mail (validation + unicité)
3. Date de naissance (roues style iOS)
4. Mot de passe (8+, majuscule, minuscule, chiffre)
5. Code de confirmation par e-mail

### Génération automatique du @username
`Hassan Sougue` → `@hassansougue` (ou `@hassansougue1` si déjà pris)

### Fil d'actualité
Texte, images, vidéos, likes, commentaires, partages, sauvegardes, pagination infinie

### Messagerie privée
Temps réel via Supabase Realtime, statut en ligne, accusé de lecture

### Notifications
Likes, commentaires, abonnements, mentions
