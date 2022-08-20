package fr.nekotine.core.lobby;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import dev.jorel.commandapi.arguments.EntitySelector;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.nekotine.core.minigame.Game;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

@ModuleNameAnnotation(Name = "EntityVisibilityModule")
public class LobbyModule extends PluginModule{
	
	private final List<Lobby> lobbyList = new LinkedList<>();
	
	private final Argument<Lobby> freeToJoinLobbyArgument =
			new CustomArgument<Lobby,String>(new StringArgument("lobbyName"), info -> {
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
				return lobbyList.toArray(StringTooltip[]::new);
			}));
	
	private final Argument<Lobby> anyLobbyArgument =
			new CustomArgument<Lobby,String>(new StringArgument("lobbyName"), info -> {
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
				return lobbyList.toArray(StringTooltip[]::new);
			}));
	
	@Override
	protected void onEnable() {
		super.onEnable();
		try {
			registerCommands();
		}catch(Exception e) {
			logException(Level.WARNING, "Une erreur est survenue lors de l'enregistrement des commandes", e);
		}
	}
	
	@Override
	protected void onDisable() {
		for (Lobby lobby : lobbyList) {
			try {
				lobby.Remove();
			}catch(Exception e) {
				logException(Level.WARNING, "Une erreur est survenue lors de la suppression du lobby "+lobby.getName(), e);
			}
		}
		lobbyList.clear();
		super.onDisable();
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Le retour des commandes n'agissant pas sur soit est de la couleur dark_purple
	 */
	private void registerCommands() {
		// create
		CommandAPICommand c_create = new CommandAPICommand("create");
		c_create.withArguments(new StringArgument("lobbyName"));
		c_create.executes((sender, args) -> {
			String rawName = (String) args[0];
			lobbyList.add(new Lobby(this, rawName));
			sender.sendMessage(
					Component.text("Un lobby nommé ").color(NamedTextColor.DARK_PURPLE)
					.append(MiniMessage.miniMessage().deserialize(rawName))
					.append(Component.text(" a été créé").color(NamedTextColor.DARK_PURPLE)));
			});
		// join
		CommandAPICommand c_join = new CommandAPICommand("join");
		c_join.withArguments(freeToJoinLobbyArgument);
		c_join.executesPlayer((sender, args) -> {
			Lobby lobby = (Lobby) args[0];
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
				new EntitySelectorArgument<Collection<Player>>("players" ,EntitySelector.MANY_PLAYERS)/*FEATURE suggestion*/);
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
						Component.text(String.format("%d joueurs ont été ajoutés au lobby ",nbAdded)).color(NamedTextColor.DARK_PURPLE)
						.append(MiniMessage.miniMessage().deserialize(lobby.getName())));
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// leave
		CommandAPICommand c_leave = new CommandAPICommand("leave");
		c_leave.withRequirement(sender -> isPlayerInLobby((Player)sender));
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
				new EntitySelectorArgument<Collection<Player>>("players" ,EntitySelector.MANY_PLAYERS)/*FEATURE suggestion*/);
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
						Component.text(String.format("%d joueurs ont été retirés du lobby ",nbKicked)).color(NamedTextColor.DARK_PURPLE)
						.append(MiniMessage.miniMessage().deserialize(lobby.getName())));
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// remove
		CommandAPICommand c_remove = new CommandAPICommand("remove");
		c_remove.withArguments(anyLobbyArgument);
		c_remove.executes((sender, args) -> {
			Lobby lobby = (Lobby) args[0];
			if (lobby != null) {
				lobby.Remove();
				sender.sendMessage(
						Component.text("Le lobby nommé ").color(NamedTextColor.DARK_PURPLE)
						.append(MiniMessage.miniMessage().deserialize(lobby.getName()))
						.append(Component.text(" a été supprimé").color(NamedTextColor.DARK_PURPLE)));
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// list
		CommandAPICommand c_list = new CommandAPICommand("lobby");
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
				c_join,
				c_join_player,
				c_leave,
				c_leave_player,
				c_remove,
				c_list
				);
		c_lobby.register();
	}
	
	private void sendLobbyList(CommandSender e, Object[] args) {
		e.sendMessage(Component.text("[- Liste des lobby -]").color(NamedTextColor.DARK_AQUA));
		for (Lobby lobby : lobbyList) {
			
			int nbPlayer = lobby.getPlayerList().size();
			int playerCap = lobby.getPlayerCap();
			Game game = lobby.getGame();
			String name = lobby.getName();
			// Prefix
			Component prefix = Component.text(String.format("[%d/%d] ", nbPlayer, playerCap));
			if (game != null && game.isPlaying()) {
				prefix.color(NamedTextColor.GOLD);
			}else {
				if (nbPlayer >= playerCap) {
					prefix.color(NamedTextColor.YELLOW);
				}else {
					prefix.color(NamedTextColor.DARK_GREEN);
				}
			}
			// final construction
			prefix.append(MiniMessage.miniMessage().deserialize(name));
			if (game != null && game.isPlaying()) {
				prefix.hoverEvent(HoverEvent.showText(Component.text("La partie est lancée").color(NamedTextColor.RED)));
			}else {
				if (nbPlayer >= playerCap) {
					prefix.hoverEvent(HoverEvent.showText(Component.text("Le lobby est plein").color(NamedTextColor.RED)));
				}else {
					prefix.clickEvent(ClickEvent.runCommand("lobby join " + name));
					prefix.hoverEvent(HoverEvent.showText(Component.text("Cliquez pour rejoindre le lobby").color(NamedTextColor.GRAY)));
				}
			}
			e.sendMessage(prefix);
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
}
