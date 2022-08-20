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
						return lobby;
					}
				}
				throw new CustomArgumentException(
						new MessageBuilder("Nom de lobby invalide: ").appendArgInput()
						);
			}).replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(info -> {
				List<StringTooltip> list = new LinkedList<>();
				for (Lobby lobby : lobbyList) {
					if (lobby.freeToJoin()) {
						list.add(StringTooltip.of(
								MiniMessage.miniMessage().stripTags(lobby.getName()),
								String.format("[%d/%d]", lobby.getPlayerList().size(), lobby.getPlayerCap())));
					}
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
	private void registerCommands() {
		// lobby create
		CommandAPICommand c_lobby_create = new CommandAPICommand("create");
		c_lobby_create.withArguments(new StringArgument("lobbyName"));
		c_lobby_create.executes((sender, args) -> {
			lobbyList.add(new Lobby(this, (String)args[0]));
			});
		// lobby join
		CommandAPICommand c_lobby_join = new CommandAPICommand("join");
		c_lobby_create.withArguments(freeToJoinLobbyArgument);
		c_lobby_create.executesPlayer((sender, args) -> {
			Lobby lobby = (Lobby) args[0];
			if (lobby != null) {
				lobby.AddPlayer(sender);
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// lobby join @a
		CommandAPICommand c_lobby_join_player = new CommandAPICommand("join");
		c_lobby_create.withArguments(freeToJoinLobbyArgument, new EntitySelectorArgument<Collection<Player>>("players" ,EntitySelector.MANY_PLAYERS));
		c_lobby_create.executesPlayer((sender, args) -> {
			Lobby lobby = (Lobby) args[0];
			if (lobby != null) {
				for (Player p : (Collection<Player>) args[1]) {
					if (!isPlayerInLobby(p)) {
						lobby.AddPlayer(p);
					}
				}
			}else {
				throw CommandAPI.fail("Lobby invalide");
			}
		});
		// lobby
		CommandAPICommand c_lobby = new CommandAPICommand("lobby");
		c_lobby.executesPlayer((sender, args) -> {
			sender.sendMessage(Component.text("Un menu de type inventaire doit être fait ici").color(NamedTextColor.YELLOW));
			sendLobbyList(sender, args);
		});
		c_lobby.withSubcommands(
				c_lobby_create,
				c_lobby_join,
				c_lobby_join_player
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
}
