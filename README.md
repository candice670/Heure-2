# Hourly Chime — app Wear OS pour Galaxy Watch 4

App de vibrations rythmiques :
- **:15** → 1 vibration
- **:30** → 2 vibrations
- **:45** → 3 vibrations
- **:00 (heure pile)** → 1 vibration longue

Interrupteur ON/OFF dans l'app. Une fois activée, elle continue de fonctionner
en arrière-plan (via des alarmes système exactes), même après un redémarrage
de la montre.

## Tout se fait depuis ton Pixel 9, sans ordinateur

Deux étapes : compiler le projet dans le cloud (GitHub Actions), puis
installer l'APK sur la montre depuis le téléphone (Termux + ADB Wi-Fi).

### Étape 1 — Compiler l'APK avec GitHub Actions (gratuit)

1. Crée un compte sur https://github.com (si tu n'en as pas déjà un) — faisable depuis le navigateur du téléphone.
2. Crée un nouveau repository (bouton "New repository"), par exemple nommé `HourlyChime`. Laisse-le public ou privé, peu importe.
3. Sur la page du repo, utilise "Add file" → "Upload files", puis dépose **tout le contenu** du dossier `HourlyChime` (dézippe d'abord le zip avec une app comme *Fichiers* ou *ZArchiver* sur ton téléphone). Attention à bien conserver l'arborescence des dossiers pendant l'upload (GitHub le permet en glissant un dossier entier depuis certains navigateurs/apps, sinon fais-le via l'app GitHub ou `git` dans Termux — voir astuce plus bas).
4. Une fois les fichiers envoyés (commit), va dans l'onglet **Actions** du repo : le workflow "Build APK" se lance automatiquement.
5. Attends 3-5 minutes que le build passe au vert ✅.
6. Clique sur le run terminé → en bas, section **Artifacts** → télécharge `HourlyChime-debug-apk` (c'est un .zip contenant `app-debug.apk`). Dézippe-le pour récupérer l'APK.

   **Astuce upload plus simple** : installe l'app **Termux** (voir étape 2), puis dans Termux :
   ```
   pkg install git
   cd HourlyChime   # dossier où tu as dézippé le projet
   git init
   git add .
   git commit -m "premier envoi"
   git branch -M main
   git remote add origin https://github.com/TON_UTILISATEUR/HourlyChime.git
   git push -u origin main
   ```
   (GitHub te demandera de te connecter — utilise un *personal access token* à la place du mot de passe, généré dans Settings → Developer settings → Personal access tokens).

### Étape 2 — Installer l'APK sur la montre via Termux

1. Installe **Termux** depuis F-Droid (pas la version Play Store, obsolète) : https://f-droid.org/packages/com.termux/
2. Ouvre Termux sur ton Pixel 9 et installe les outils ADB :
   ```
   pkg update
   pkg install android-tools
   ```
3. Sur la montre : *Paramètres* → *À propos* → tape 5 fois sur *Numéro de build* pour activer le mode développeur.
4. *Paramètres* → *Développeur* → active *Débogage ADB* et *Débogage via Wi-Fi*.
5. Note l'adresse IP affichée sur la montre (ex. `192.168.1.42:5555`). Assure-toi que le Pixel 9 et la montre sont sur le **même réseau Wi-Fi**.
6. Dans Termux, connecte-toi et installe l'APK (adapte le chemin vers où l'APK a été téléchargé, en général `~/storage/downloads/` après avoir lancé `termux-setup-storage`) :
   ```
   termux-setup-storage
   adb connect 192.168.1.42:5555
   adb install ~/storage/downloads/app-debug.apk
   ```
7. L'app "Hourly Chime" apparaît dans la liste des apps de la montre 🎉

## Étape 3 — Autoriser les alarmes exactes (important)

À la première activation de l'interrupteur, l'app ouvre automatiquement l'écran
système pour autoriser les "alarmes et rappels exacts". Accepte cette
autorisation — sans elle, Android peut retarder légèrement les vibrations.

## Notes techniques

- L'app n'utilise pas de service permanent qui viderait la batterie : elle
  programme une alarme système exacte pour le prochain quart d'heure, se
  déclenche, vibre, puis reprogramme la suivante. C'est la méthode recommandée
  par Google pour ce genre de rappel périodique précis.
- Un `BootReceiver` reprogramme automatiquement les alarmes si la montre
  redémarre pendant que l'app est activée.
- Le nom de package est `com.claude.hourlychime` — libre à toi de le changer
  dans `app/build.gradle.kts` et `AndroidManifest.xml` si besoin.
