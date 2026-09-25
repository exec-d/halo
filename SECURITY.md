# Sécurité

## Versions suivies

Seule la dernière version publiée sur la page
[Releases](https://github.com/exec-d/WUX/releases) reçoit des corrections.

## Signaler une faille

**N'ouvrez pas de ticket public.** Utilisez le
[signalement privé de GitHub](https://github.com/exec-d/WUX/security/advisories/new)
(onglet *Security → Report a vulnerability*), en décrivant :

- ce qui est en jeu (donnée exposée, action possible sans autorisation…) ;
- comment le reproduire (version de Halo, téléphone, version d'Android) ;
- l'effet constaté.

Le signalement sera examiné dès que possible. Une fois la faille corrigée et la
version publiée, elle sera décrite dans le [journal des versions](CHANGELOG.md),
avec votre nom si vous le souhaitez.

## Ce qui est déjà connu

- **Signature** : les APK sont signés avec une clé de debug publique
  (`android/app/debug.keystore`). N'importe qui peut signer un APK avec la
  même clé ; n'installez Halo que depuis ce dépôt.
- **Accès sensibles** : l'accès aux données d'utilisation, la liste des
  applications et les appareils Bluetooth ne servent qu'à l'affichage, sur le
  téléphone ; voir [PRIVACY.md](PRIVACY.md).
