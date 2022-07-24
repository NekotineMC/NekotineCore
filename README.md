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

- `ProjectileManager` ✓

	Un module pour faire des projectiles personnalisés facilement.  
	***Hyez doit donner de plus amples informations***
	
	> XxGoldenbluexX devra jetter un oeil.

- `UsableManager` ✓

	Module pour faciliter la gestion des interactions entre le joueur et l'item dans sa main. 	
	
	> XxGoldenbluexX devra jetter un oeil.

- `SwordChargeManager` ✓

	Module pour gèrer une charge, par exemple pour charger un sort.  
	***Hyez doit donner de plus amples informations***  
	
	> XxGoldenbluexX devra jetter un oeil.
	
- `CustomEffectModule` ⧖

	Module pour gerer des effets s'appliquant à une cible pour une personne.  
	Les effets sont superposables et sont définits par une duration, une puissance, une cible et un observateur

- `DamageManager` ✓

	Module ayant pour objectif de remplacer le système de dégats par défaut de *minecraft* pour éviter les temps
	d'invincibilités et permettre une meilleur flexibilitée.
	
	> XxGoldenbluexX devra jetter un oeil.

- `ChargeManager` ✓

	Module pour faire facilement des temps de recharge.  
	***Hyez doit donner de plus amples informations***  

***

## Autres

Le NekotineCore propose aussi d'autres outils tel que:

- `Text` et `TextColor` pour unifier la disposition et la couleur des messages dans le chat, pour plus de cohérance pour l'utilisateur.  
- `UtilEntity` ***Hyez doit donner de plus amples informations***  
- `UtilEvent` ***Hyez doit donner de plus amples informations***  
- `UtilGear` ***Hyez doit donner de plus amples informations***  
- `UtilMath` ***Hyez doit donner de plus amples informations***  
- `UtilMobAi` Classe utilitaire pour faciliter la gestion des IA des mobs.
- `UtilTime` ***Hyez doit donner de plus amples informations***  
- `AutoWrapper` et `WrapperBase` pour avoir un système de Wrapping générique.

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