package fr.nekotine.core.game.phase.eventargs;

import fr.nekotine.core.game.phase.reason.PhaseFailureType;

public record PhaseFailureEventArgs(PhaseFailureType reason, String info, Exception exception) {
}
