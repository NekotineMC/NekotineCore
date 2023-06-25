package fr.nekotine.core.lobby;

import java.util.LinkedList;
import java.util.List;

import dev.jorel.commandapi.CommandAPI;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

/**
 * Module permettant aux joueurs de créer des lobby pour héberger des parties.
 * 
 * Ce module utilise CommandAPI, veillez a l'activer via {@link CommandAPI#onEnable CommandAPI::onEnable} 
 * 
 * @author XxGoldenbluexX
 *
 */
@ModuleNameAnnotation(Name = "LobbyModule")
public class LobbyModule extends PluginModule{
	
	private final List<Lobby> lobbyList = new LinkedList<>();
	/*
	private final Argument<Lobby> freeToJoinLobbyArgument =
			new CustomArgument<Lobby,String>(new TextArgument("lobbyName"), info -> {
				for (Lobby lobby : lobbyList) {
					if (MiniMessage.miniMessage().stripTags(lobby.getName()).equals(info.currentInput())) {
						if (lobby.isFreeToJoin()) {
							return lobby;
						}else {
							throw new CustomArgumentException(
									new MessageBuilder("Ce lobby ne peut être rejoint: ").appendArgInput()
									);
						}
					}
				}
				throw new CustomArgumentException(
						new MessageBuilder("Nom de lobby invalide: ").appendArgInput()
						);
			}).replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(info -> {
				List<StringTooltip> list = new LinkedList<>();
				for (Lobby lobby : lobbyList) {
					if (lobby.isFreeToJoin()) {
						list.add(StringTooltip.of(
								MiniMessage.miniMessage().stripTags(lobby.getName()),
								String.format("[%d/%d]", lobby.getPlayerList().size(), lobby.getPlayerCap())));
					}
				}
				return list.toArray(StringTooltip[]::new);
			}));
	
	private final Argument<Lobby> anyLobbyArgument =
			new CustomArgument<Lobby,String>(new TextArgument("lobbyName"), info -> {
				for (Lobby lobby : lobbyList) {
					if (MiniMessage.miniMessage().stripTags(lobby.getName()).equals(info.currentInput())) {
						return lobby;
					}
				}
				throw new CustomArgumentException(
						new MessageBuilder("Nom de lobby invalide: ").appendArgInput()
						);
			}).replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(info -> {
				List<StringTooltip> list = new LinkedList<>();
				for (Lobby lobby : lobbyList) {
					list.add(StringTooltip.of(
							MiniMessage.miniMessage().stripTags(lobby.getName()),
							String.format("[%d/%d]", lobby.getPlayerList().size(), lobby.getPlayerCap())));
				}
				return list.toArray(StringTooltip[]::new);
			}));
	
	private final Argument<Lobby> anyStoppedLobbyArgument =
			new CustomArgument<Lobby,String>(new TextArgument("lobbyName"), info -> {
				for (Lobby lobby : lobbyList) {
					if (MiniMessage.miniMessage().stripTags(lobby.getName()).equals(info.currentInput())) {
						if (lobby.isGameLaunched()) {
							throw new CustomArgumentException(
									new MessageBuilder("La partie est en cours: ").appendArgInput()
									);
						}
						return lobby;
					}
				}
				throw new CustomArgumentException(
						new MessageBuilder("Nom de lobby invalide: ").appendArgInput()
						);
			}).replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(info -> {
				return lobbyList.stream()
				.filter(l -> !l.isGameLaunched())
				.map(l -> StringTooltip.of(
						MiniMessage.miniMessage().stripTags(l.getName()),
						String.format("[%d/%d]", l.getPlayerList().size(), l.getPlayerCap()))).toArray(StringTooltip[]::new);
			}));
	
	private final Argument<Lobby> anyStartedLobbyArgument =
			new CustomArgument<Lobby,String>(new TextArgument("lobbyName"), info -> {
				for (Lobby lobby : lobbyList) {
					if (MiniMessage.miniMessage().stripTags(lobby.getName()).equals(info.currentInput())) {
						if (!lobby.isGameLaunched()) {
							throw new CustomArgumentException(
									new MessageBuilder("La partie n'est pas en cours: ").appendArgInput()
									);
						}
						return lobby;
					}
				}
				throw new CustomArgumentException(
						new MessageBuilder("Nom de lobby invalide: ").appendArgInput()
						);
			}).replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(info -> {
				return lobbyList.stream()
				.filter(l -> l.isGameLaunched())
				.map(l -> StringTooltip.of(
						MiniMessage.miniMessage().stripTags(l.getName()),
						String.format("[%d/%d]", l.getPlayerList().size(), l.getPlayerCap()))).toArray(StringTooltip[]::new);
			}));
	
	private final Argument<GameMode<? extends GameData>> availableGameModeArgument =
			new CustomArgument<GameMode<? extends GameData>,String>(new StringArgument("gamemodeName"), info -> {
				var gamemode = GetPluginModule(GameModeModule.class).getGameMode(info.currentInput());
				if (gamemode == null) {
					throw new CustomArgumentException(
							new MessageBuilder("Nom de gamemode invalide: ").appendArgInput()
							);
				}
				return gamemode;
			}).replaceSuggestions(ArgumentSuggestions.strings(info -> {
				return GetPluginModule(GameModeModule.class).getGameModesKeys().toArray(String[]::new);
			}));
	
	@Override
	protected void onEnable() {
		super.onEnable();
		try {
			
		}catch(Exception e) {
			logException(Level.WARNING, "Une erreur est survenue lors de l'enregistrement des commandes", e);
		}
	}
	
	@Override
	protected void onDisable() {
		for (Lobby lobby : new LinkedList<Lobby>(lobbyList)/*Éviter les ConcurentModificationException*//*) {
			try {
				lobby.unregister();
			}catch(Exception e) {
				logException(Level.WARNING, "Une erreur est survenue lors de la suppression du lobby "+lobby.getName(), e);
			}
		}
		lobbyList.clear();
		super.onDisable();
	}
	
	public List<Lobby> getLobbyList(){
		return lobbyList;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Le retour des commandes n'agissant pas sur soit est de la couleur dark_purple
	 *//*
	public void registerCommands() {
		// create
		CommandAPICommand c_create = new CommandAPICommand("create");
		c_create.withArguments(new TextArgument("lobbyName"));
		c_create.executes((sender, args) -> {
			String rawName = (String) args[0];
			new Lobby(rawName).register(this);
			sender.sendMessage(
					Component.text("Un lobby nommé ").color(Colors.COMMAND_FEEDBACK)
					.append(MiniMessage.miniMessage().deserialize(rawName))
					.append(Component.text(" a été créé").color(Colors.COMMAND_FEEDBACK)));
			});
		// create with gamemode
		CommandAPICommand c_create_gm = new CommandAPICommand("create");
		c_create_gm.withArguments(new TextArgument("lobbyName"));
		c_create_gm.withArguments(availableGameModeArgument);
		c_create_gm.executes((sender, args) -> {
			String rawName = (String) args[0];
			var gm = (GameMode<? extends GameData>) args[1];
			var lobby = new Lobby(rawName);
			lobby.setGameMode(gm);
			lobby.register(this);
			sender.sendMessage(
					Component.text("Un lobby nommé ").color(Colors.COMMAND_FEEDBACK)
					.append(MiniMessage.miniMessage().deserialize(rawName))
					.append(Component.text(String.format(" a été créé")).color(Colors.COMMAND_FEEDBACK)));
			});
		// join
		CommandAPICommand c_join = new CommandAPICommand("join");
		c_join.withArguments(freeToJoinLobbyArgument);
		c_join.executesPlayer((sender, args) -> {
			Lobby lobby = (Lobby) args[0];
			if (isPlayerInLobby(sender)) {
				throw CommandAPI.fail("Vous êtes déja dans un lobby");
			}
			if (lobby != null) {
				lobby.AddPlayer(sender);
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// join @a
		CommandAPICommand c_join_player = new CommandAPICommand("join");
		c_join_player.withArguments(
				freeToJoinLobbyArgument,
				new EntitySelectorArgument<Collection<Player>>("players" ,EntitySelector.MANY_PLAYERS)
					.replaceSuggestions(ArgumentSuggestions.strings(
							info -> getPlayersWithNoLobby().stream()
								.map(p -> p.getName())
								.toArray(String[]::new)
							)));
		c_join_player.executes((sender, args) -> {
			Lobby lobby = (Lobby) args[0];
			if (lobby != null) {
				int nbAdded = 0;
				for (Player p : (Collection<Player>) args[1]) {
					if (!isPlayerInLobby(p)) {
						lobby.AddPlayer(p);
					}
				}
				sender.sendMessage(
						Component.text(String.format("%d joueurs ont été ajoutés au lobby ",nbAdded)).color(Colors.COMMAND_FEEDBACK)
						.append(MiniMessage.miniMessage().deserialize(lobby.getName())));
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// leave
		CommandAPICommand c_leave = new CommandAPICommand("leave");
		c_leave.executesPlayer((sender, args) -> {
			Lobby lobby = getPlayerLobby(sender);
			if (lobby != null) {
				lobby.RemovePlayer(sender);
			}else {
				throw CommandAPI.fail("Vous n'êtes pas dans un lobby");
			}
		});
		// leave @a
		CommandAPICommand c_leave_player = new CommandAPICommand("leave");
		c_leave_player.withArguments(
				anyLobbyArgument,
				new EntitySelectorArgument<Collection<Player>>("players" ,EntitySelector.MANY_PLAYERS)
				.replaceSuggestions(ArgumentSuggestions.strings(
						info -> ((Lobby)info.previousArgs()[0]).getPlayerList().stream()
							.map(p -> p.getName())
							.toArray(String[]::new)
						)));
		c_leave_player.executes((sender, args) -> {
			Lobby lobby = (Lobby) args[0];
			if (lobby != null) {
				int nbKicked = 0;
				for (Player p : (Collection<Player>) args[1]) {
					if (lobby.getPlayerList().contains(p)) {
						lobby.RemovePlayer(p);
						nbKicked++;
					}
				}
				sender.sendMessage(
						Component.text(String.format("%d joueurs ont été retirés du lobby ",nbKicked)).color(Colors.COMMAND_FEEDBACK)
						.append(MiniMessage.miniMessage().deserialize(lobby.getName())));
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// remove
		CommandAPICommand c_remove = new CommandAPICommand("remove");
		c_remove.withArguments(anyLobbyArgument);
		c_remove.executes((sender, args) -> {
			if (args[0] instanceof Lobby lobby) {
				lobby.unregister();
				sender.sendMessage(
						Component.text("Le lobby nommé ").color(Colors.COMMAND_FEEDBACK)
						.append(MiniMessage.miniMessage().deserialize(lobby.getName()))
						.append(Component.text(" a été supprimé").color(Colors.COMMAND_FEEDBACK)));
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// start
		CommandAPICommand c_start = new CommandAPICommand("start");
		c_start.withArguments(anyStoppedLobbyArgument);
		c_start.executes((sender, args) -> {
			if (args[0] instanceof Lobby lobby) {
				lobby.getGame().start();
				sender.sendMessage(
						Component.text("Le lobby nommé ").color(Colors.COMMAND_FEEDBACK)
						.append(MiniMessage.miniMessage().deserialize(lobby.getName()))
						.append(Component.text(" a été lancé").color(Colors.COMMAND_FEEDBACK)));
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// stop
		CommandAPICommand c_stop = new CommandAPICommand("stop");
		c_stop.withArguments(anyStartedLobbyArgument);
		c_stop.executes((sender, args) -> {
			if (args[0] instanceof Lobby lobby) {
				lobby.getGame().abort();
				sender.sendMessage(
						Component.text("Le lobby nommé ").color(Colors.COMMAND_FEEDBACK)
						.append(MiniMessage.miniMessage().deserialize(lobby.getName()))
						.append(Component.text(" a été arreté").color(Colors.COMMAND_FEEDBACK)));
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// gamemode get 
		CommandAPICommand c_gamemode_get = new CommandAPICommand("gamemode");
		c_gamemode_get.withArguments(anyLobbyArgument);
		c_gamemode_get.executes((sender, args) -> {
			if (args[0] instanceof Lobby lobby) {
				sender.sendMessage(
						Component.text("Le mode de jeu du lobby nommé ")
						.color(Colors.COMMAND_FEEDBACK)
						.append(MiniMessage.miniMessage().deserialize(lobby.getName()))
						.append(Component.text(String.format(" est : %s",lobby.getGame().getGameMode().getRegisteringKey()))
								.color(Colors.COMMAND_FEEDBACK)));
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// gamemode set
		CommandAPICommand c_gamemode_set = new CommandAPICommand("gamemode");
		c_gamemode_set.withArguments(anyStoppedLobbyArgument);
		c_gamemode_set.withArguments(availableGameModeArgument);
		c_gamemode_set.executes((sender, args) -> {
			if (args[0] instanceof Lobby lobby) {
				var gm = (GameMode<? extends GameData>) args[1];
				lobby.setGameMode(gm);
				sender.sendMessage(
						Component.text("Le mode de jeu du lobby nommé ")
						.color(Colors.COMMAND_FEEDBACK)
						.append(MiniMessage.miniMessage().deserialize(lobby.getName()))
						.append(Component.text(String.format(" est désormais: %s",lobby.getGame().getGameMode().getRegisteringKey()))
								.color(Colors.COMMAND_FEEDBACK)));
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// list
		CommandAPICommand c_list = new CommandAPICommand("list");
		c_list.executesPlayer((sender, args) -> {
			sender.sendMessage(Component.text("Un menu de type inventaire doit être fait ici").color(NamedTextColor.YELLOW));
			sendLobbyList(sender, args);
		});
		c_list.executesConsole((sender, args) -> {
			sendLobbyList(sender, args);
		});
		// lobby
		CommandAPICommand c_lobby = new CommandAPICommand("lobby");
		c_lobby.executesPlayer((sender, args) -> {
			sender.sendMessage(Component.text("Un menu de type inventaire doit être fait ici").color(NamedTextColor.YELLOW));
			sendLobbyList(sender, args);
		});
		c_lobby.executesConsole((sender, args) -> {
			sendLobbyList(sender, args);
		});
		c_lobby.withSubcommands(
				c_create,
				c_create_gm,
				c_join,
				c_join_player,
				c_leave,
				c_leave_player,
				c_remove,
				c_start,
				c_stop,
				c_gamemode_get,
				c_gamemode_set,
				c_list
				);
		c_lobby.register();
	}
	
	private void sendLobbyList(CommandSender e, Object[] args) {
		e.sendMessage(Component.text("[- Liste des lobby -]").color(NamedTextColor.DARK_AQUA));
		for (Lobby lobby : lobbyList) {
			
			int nbPlayer = lobby.getNumberOfPlayer();
			int playerCap = lobby.getPlayerCap();
			String name = lobby.getName();
			// Prefix
			var builder = Component.text();
			builder.append(Component.text(String.format("(%s)", lobby.getGame().getGameMode().getRegisteringKey())));
			builder.color(NamedTextColor.DARK_GRAY);
			builder.append(Component.text(String.format("[%d/%d] ", nbPlayer, playerCap)));
			if (lobby.isGameLaunched()) {
				builder.color(NamedTextColor.GOLD);
			}else {
				if (nbPlayer >= playerCap) {
					builder.color(NamedTextColor.YELLOW);
				}else {
					builder.color(NamedTextColor.DARK_GREEN);
				}
			}
			// final construction
			builder.append(MiniMessage.miniMessage().deserialize(name).clickEvent(null));
			if (lobby.isGameLaunched()) {
				builder.hoverEvent(HoverEvent.showText(Component.text("La partie est lancée").color(Colors.HOVER_INVALID)));
			}else {
				if (nbPlayer >= playerCap) {
					builder.hoverEvent(HoverEvent.showText(Component.text("Le lobby est plein").color(Colors.HOVER_INVALID)));
				}else {
					builder.clickEvent(ClickEvent.runCommand(String.format("/lobby join \"%s\"", MiniMessage.miniMessage().stripTags(name))));
					builder.hoverEvent(HoverEvent.showText(Component.text("Cliquez pour rejoindre le lobby").color(Colors.HOVER_INFO)));
				}
			}
			e.sendMessage(builder);
		}
		e.sendMessage(Component.text("[- Fin de la liste -]").color(NamedTextColor.DARK_AQUA));
	}
	
	public boolean isPlayerInLobby(Player player) {
		for (Lobby lobby : lobbyList) {
			if (lobby.getPlayerList().contains(player)) {
				return true;
			}
		}
		return false;
	}
	
	public Lobby getPlayerLobby(Player player) {
		for (Lobby lobby : lobbyList) {
			if (lobby.getPlayerList().contains(player)) {
				return lobby;
			}
		}
		return null;
	}
	
	public List<Player> getPlayersWithLobby(){
		List<Player> list = new LinkedList<>();
		for (Lobby lobby : lobbyList) {
			list.addAll(lobby.getPlayerList());
		}
		return list;
	}
	
	public List<Player> getPlayersWithNoLobby(){
		List<Player> list = new LinkedList<>();
		list.addAll(Bukkit.getOnlinePlayers());
		for (Lobby lobby : lobbyList) {
			list.removeAll(lobby.getPlayerList());
		}
		return list;
	}*/
}
