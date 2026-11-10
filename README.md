# NekotineCore

Bibliotheque utilitaire pour faire des plugins minecraft avec papermc.

## Modules

Le *NekotineCore* est composé de plusieurs modules qui peuvent être chargé par le plugin.

Les modules ne sont pas indépendants et peuvent communiquer entre eux.

***

Voici la liste des modules:

- `TickingModule` ✓

	Ce module envoit un évènement bukkit chaque tick.  
	Il intègre aussi des informations pour se repèrer dans le temps sur des intervales prédéfinits.
	
	> Ce module est optimisable.

- `VisibilityModule` ✓

	Permet de rendre un joueur invisible pour d'autres.  
	**Ce module n'utilise pas d'effet d'invisibilité et n'est donc pas compatible avec l'effet de** ***Glow***.
	
	> Le sont des bruits de pas est encore là -> changer le craft player à la connexion.

- `ProjectileModule` ✓

	Un module pour faire des projectiles personnalisés facilement.  
	Il permet de détecter lorsque les entités projectiles touchent un mur, une autre entité, ou après un certain temps.
	
	> XxGoldenbluexX devra jetter un oeil.

- `UsableModule` ✓

	Module pour faciliter la gestion des interactions entre un joueur et un ItemStack.

- `ItemChargeModule` ✓

	Module pour gérer une charge, par exemple pour charger un sort.  
	Il permet de détecter lorsqu'un joueur réalise une action (avec ou sans item précis) sur une durée donnée.
	
	> XxGoldenbluexX devra jetter un oeil.
	
- `CustomEffectModule` ✓

	Module pour gérer des effets s'appliquant à une cible pour une personne.  
	Les effets sont superposables et sont définits par une duration, une puissance, une cible et un observateur.

- `DamageModule` ✓

	Module ayant pour objectif de remplacer le système de dégats par défaut de *minecraft* pour éviter les temps
	d'invincibilités et permettre une meilleur flexibilitée.
	
	> XxGoldenbluexX devra jetter un oeil.

- `ChargeModule` ✓

	Module pour faire facilement des temps de recharge.
	Il permet de faire des décomptes que l'on peut afficher ensuite aux joueurs, dans leurs barres d'expériences ou avec des sons.

- `BowChargeModule` ✓

	Module pour gérer une charge à l'arc.
	Il permet de détecter lorsqu'un joueur charge son arc sur un temps donné, lorsque celui-ci tire sa flèche, ou annulle son tir.
	
	> XxGoldenbluexX devra jetter un oeil.
	
- `LobbyModule` ⧖

	Module pour configurer une partie avant de la lancer.
	Il permet aux joueurs de créer un lobby, y rentrer et d'y configurer une partie avec des interfaces graphiques 	avant de la lancer.
	
- `MapModule` ⧖

	Module pour permettant au développeurs de créer facilement une carte pour un mode de jeu à partir de composants de    	base.
	Ce module gère les commandes pour éditer les cartes et pour les enregistrer.

- `BotRoster` ⧖

	Module (dans le module Gradle `bots`, NMS, épinglé sur une version de Minecraft) pour faire apparaître des bots : de faux joueurs côté serveur que le serveur, les autres plugins et ProtocolLib voient comme de vrais joueurs (`PlayerJoinEvent`/`PlayerQuitEvent`, `getOnlinePlayers()`, UUID hors-ligne dérivé du nom).  
	Il permet de faire apparaître (`spawn`), lister (`list`) et retirer (`remove`) des bots, depuis le thread principal uniquement. Chaque tick, il fait tourner leur connexion et répond aux accusés du client (téléportation, keep-alive, joueur chargé). `unload()` retire tous les bots.  
	Chaque tick, il lance un `BotTickEvent` par bot (après les accusés, avant l'envoi) ; à leur départ, il efface leurs fichiers sauvegardés selon `BotWipe` (tout par défaut) ; une erreur de la bibliothèque sur un bot le passe `FAILED` (toujours connecté, plus tické) sans arrêter les autres.  
	À sa création, un auto-test résout chaque accès NMS des classes internes : s'il échoue (souvent après une mise à jour de Paper), une erreur nomme chaque accès cassé, les bots sont désactivés (`isAvailable()` faux) et `spawn`/`remove`/`list` lèvent `BotsUnavailableException` ; l'hôte et le serveur continuent de tourner. L'hôte doit résoudre `BotRoster` dans son `onEnable()`.  
	Chaque `Bot` se pilote comme un client (thread principal uniquement) : touches `setForward`/`setBackward`/`setLeft`/`setRight`/`setJump`/`setSneak`/`setSprint`, cap de déplacement `setHeading` (`null` = suit le regard) et regard `setLook` ; ils persistent jusqu'au prochain changement et, après le `BotTickEvent`, partent en vrais paquets (entrées, sprint, puis position calculée par la physique du serveur), d'où `PlayerMoveEvent`, `PlayerToggleSneakEvent`, `PlayerToggleSprintEvent`.  
	Mains et commandes : `setSlot` (emplacement de la barre, persiste) et des actions ponctuelles envoyées une fois, dans l'ordre d'appel, après la position : `useItem`, `useItemOn`, `leftClick`, `startBreaking`, `attack`, `interact`, `swapHands`, `sendCommand` et `respawn` (le bot répond à chaque réapparition par l'accusé « joueur chargé »).
	
	> Pas encore de mains, de commandes ni de réapparition : ils arrivent avec la story suivante.
	
***

## Autres

Le NekotineCore propose aussi d'autres outils tel que:

- `Text` et `TextColor` pour unifier la disposition et la couleur des messages dans le chat, pour plus de cohérance pour l'utilisateur.  
- `UtilEntity` pour modifier et faire des tests sur des entités ou sur leurs enfants.
- `UtilEvent` pour enregistrer des listeners ou faire des tests sur les objets en rapport avec les events Bukkit.
- `UtilGear` pour modifier et faire des tests sur tout ce qui a un rapport avec les items.
- `UtilMath` pour réaliser des calculs mathématiques précis.
- `UtilMobAi` Classe utilitaire pour faciliter la gestion des IA des mobs.
- `UtilTime` pour réaliser et faires des tests sur des données temporelles.
- `UtilParticle` pour réaliser des paternes prédéfinis avec des particules.
- `UtilInventory` pour interragire facilement avec les inventaires et pour créer facilement des interfaces graphiques (Menu type coffre).
- `AutoWrapper` et `WrapperBase` pour avoir un système de Wrapping générique.
- `Snapshot` pour permettre la sauvegarde d'un élément à un instant précis.

***

### A propos de ce document

Ce document à pour but d'avoir une vue d'ensemble sur les différentes fonctionnalités du core pour pouvoir piocher dedant facilement et ne pas refaire des choses plusieurs fois.

***

Je laisse ici des charactère spéciaux pour remplir ce document facilement.  

✓ Fonctionel  
⧖ En développement  
🞪 Non réalisé (en projet)  

***

je laisse aussi un peut de documentation pour le Markup. Pour plus d'informations voir [ici](https://www.markdownguide.org/basic-syntax).

	# Titre 1
	## Titre 2
	### Titre 3
	...
	###### Titre 6
	
	*italique*
	**gras**
	***italique + gras***
	
	Pour mettre à la ligne, il fait finir la ligne précédente avec 2 espaces
	
	> Message dans un carré (Blockquotes)
	>
	> Ces messages peuvent être formatés (italique, gras, titre, liste, etc)
	>> Ces messages peuvent être emboités comme ceci.
	
	1. Liste avec un ordre
	2. 0
	3. 0
	
	- Liste sans ordre
	- 0
	- 0
	
	- Tous les types de liste peuvent être emboités
		- En utilisant une tabulation (ou 4 espaces)
	
		Des zones de code peuvent être faites en les tabulant (ou 4 espaces)
		Ces zones ne peuvent pas être formatées (logique).
	
	On peut mettre en évidence des `mots` (comme par exemple ceux issues de languages de programation)
	
	[Un lien](http://monAdresse.com)
	
	![Une image](http://monAdresse.com)
