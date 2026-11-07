package fr.nekotine.core.text;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * @author XxGoldenbluexX
 */
public final class Colors {

	public static final TextColor COMMAND_FEEDBACK = NamedTextColor.DARK_PURPLE;

	public static final TextColor SELF_STATUS_CHANGE = NamedTextColor.LIGHT_PURPLE;

	public static final TextColor HOVER_INFO = NamedTextColor.GRAY;

	public static final TextColor HOVER_INVALID = NamedTextColor.RED;

	public final class Command {

		// Couleurs volées honteusement à
		// https://www.mudblazor.com/customization/default-theme#palette

		public static final TextColor SUCCESS = TextColor.color(0, 200, 83);

		public static final TextColor WARNING = TextColor.color(255, 152, 0);

		public static final TextColor ERROR = TextColor.color(244, 67, 54);

		public static final TextColor INFO = TextColor.color(33, 150, 243);
	}
}
