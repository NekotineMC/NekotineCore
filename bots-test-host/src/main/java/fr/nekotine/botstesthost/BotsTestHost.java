package fr.nekotine.botstesthost;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.nekotine.core.NekotinePlugin;
import fr.nekotine.core.bot.BotRoster;
import fr.nekotine.core.ioc.Ioc;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.command.CommandSender;

/**
 * Minimal Paper plugin hosting the bots library for NekotineCore's integration
 * tests. Lives outside {@code fr.nekotine.core} so Core's module scan never
 * sees it.
 */
public class BotsTestHost extends NekotinePlugin {

	/** The test command's name: {@code /bottest <word>}. */
	public static final String COMMAND = "bottest";

	/** One run of {@code /bottest <word>}: who ran it, with which word. */
	public record Invocation(CommandSender sender, String word) {
	}

	/** Every run of {@code /bottest}, in order, for tests to read and clear. */
	public static final Queue<Invocation> INVOCATIONS = new ConcurrentLinkedQueue<>();

	@Override
	public void onEnable() {
		super.onEnable();
		// Eager: runs the bots self-test at enable (Core loads modules lazily).
		Ioc.resolve(BotRoster.class);
		getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
				event -> event.registrar().register(Commands.literal(COMMAND)
						.then(Commands.argument("word", StringArgumentType.word()).executes(context -> {
							INVOCATIONS.add(new Invocation(context.getSource().getSender(),
									StringArgumentType.getString(context, "word")));
							return Command.SINGLE_SUCCESS;
						})).build(), "Records its sender and word (bots integration tests)"));
	}
}
