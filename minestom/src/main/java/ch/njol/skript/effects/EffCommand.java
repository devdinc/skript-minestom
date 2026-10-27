package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.StringMode;
import ch.njol.util.Kleenean;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.RuntimeErrorProducer;

@Name("Command")
@Description("""
	Executes a command. This can be useful to run commands from other parts of the server in triggers.
	If no sender is given, the command is executed by the console.

	A player's command goes through the normal command pipeline, so the command event fires and can cancel or change it.
	A console command that is not a registered command is handled like one typed into the console,
	so it runs as an effect command if it starts with the effect command token.""")
@Examples("""
	make player execute command "/home"

	execute console command "/say Hello everyone!"

	execute the commands "/skript reload all" and "/say done\"""")
@Keywords({"command", "execute", "run", "dispatch"})
public class EffCommand extends Effect {

	static {
		Skript.registerEffect(EffCommand.class,
			"execute [the] command[s] %strings% [by %-senders%]",
			"execute [the] %senders% command[s] %strings%",
			"(let|make) %senders% execute [[the] command[s]] %strings%");
	}

	private Expression<String> commands;
	private @Nullable Expression<CommandSender> senders;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			commands = (Expression<String>) exprs[0];
			senders = (Expression<CommandSender>) exprs[1];
		} else {
			senders = (Expression<CommandSender>) exprs[0];
			commands = (Expression<String>) exprs[1];
		}
		commands = VariableString.setStringMode(commands, StringMode.COMMAND);
		return true;
	}

	@Override
	protected void execute(Event event) {
		CommandSender[] senders = this.senders == null
			? new CommandSender[]{MinecraftServer.getCommandManager().getConsoleSender()}
			: this.senders.getArray(event);
		for (String command : commands.getArray(event)) {
			// the command manager expects the command without its leading slash
			if (command.startsWith("/"))
				command = command.substring(1);
			for (CommandSender sender : senders)
				dispatchCommand(sender, command, this);
		}
	}

	/**
	 * Executes a command as the given sender through Minestom's command manager.
	 * For players this fires the command event, which may cancel or change the command.
	 *
	 * @param sender The sender to execute the command as
	 * @param command The command, without its leading slash
	 * @param errorProducer Where to report a runtime error if the command throws
	 * @return Whether the command was found and executed successfully
	 */
	@ApiStatus.Internal
	public static boolean dispatchCommand(CommandSender sender, String command, RuntimeErrorProducer errorProducer) {
		try {
			CommandResult result = MinecraftServer.getCommandManager().execute(sender, command);
			return result.getType() == CommandResult.Type.SUCCESS;
		} catch (Exception ex) {
			errorProducer.error("Failed to execute command: " + ex.getMessage());
			return false;
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String string = "execute the command " + commands.toString(event, debug);
		if (senders != null)
			string += " by " + senders.toString(event, debug);
		return string;
	}

}
