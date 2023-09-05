package fr.nekotine.core.game.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.logging.FormatingRemoteLogger;
import fr.nekotine.core.text.Text;
import fr.nekotine.core.util.Stopwatch;
import fr.nekotine.core.util.TypeHashMap;
import fr.nekotine.core.util.TypeMap;

public class PhaseMachine implements IPhaseMachine{

	private static Logger LOGGER = new FormatingRemoteLogger(NekotineCore.LOGGER,Text.namedLoggerFormat(PhaseMachine.class.getSimpleName()));
	
	private boolean running;
	
	private final Map<Object, Object> registeredPhases = new HashMap<>();
	
	private final TypeMap runningPhases = new TypeHashMap();
	
	@SuppressWarnings("rawtypes")
	private final List<Class<? extends IPhase>> phaseOrder = new ArrayList<>();
	
	private int currentPhaseIndex;
	
	@SuppressWarnings("rawtypes")
	private IPhase currentPhase;

	@SuppressWarnings("rawtypes")
	@Override
	public <P extends Phase, T extends IPhase<P>> void registerPhase(Class<T> type, Function<IPhaseMachine, T> phaseSupplier) {
		registeredPhases.put(type, phaseSupplier);
		phaseOrder.add(type);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <P extends Phase, T extends IPhase<P>> void goTo(Class<T> phase, Object inputData) {
		if (!phaseOrder.contains(phase)) {
			throw new IllegalArgumentException("Cette phase n'est pas enregistree");
		}
		if (currentPhase == null) {
			currentPhase = makePhase(phase);
			currentPhaseIndex = phaseOrder.indexOf(phase);
			var parents = getParents(currentPhase);
			for (var p : parents) {
				try (var watch = new Stopwatch(w -> LOGGER.log(Level.INFO,"La phase "+p.getClass()+" est setup ("+w.elapsedMillis()+" ms)"))){
					p.setup(inputData);
				}catch(Exception e) {
					LOGGER.log(Level.SEVERE, "Une erreur est survenue lors du setup de la phase "+p.getClass(), e);
				}
			}
			currentPhase.setup(inputData);
			return;
		}
		if (phase == currentPhase.getClass()) {
			return;
		}
		var nextPhase = makePhase(phase);
		var nextParents = getParents(nextPhase);
		nextParents.addLast(nextPhase);
		var curParents = getParents(currentPhase);
		curParents.addLast(currentPhase);
		while (curParents.getFirst() == nextParents.getFirst()) {
			curParents.pollFirst();
			nextParents.pollFirst();
		}
		Collections.reverse(curParents);
		for (var p : curParents) {
			try (var watch = new Stopwatch(w -> LOGGER.log(Level.INFO,"La phase "+p.getClass()+" est teardown ("+w.elapsedMillis()+" ms)"))){
				p.tearDown();
			}catch(Exception e) {
				LOGGER.log(Level.SEVERE, "Une erreur est survenue lors du teardown de la phase "+p.getClass(), e);
			}
			runningPhases.remove(p.getClass());
		}
		for (var p : nextParents) {
			try (var watch = new Stopwatch(w -> LOGGER.log(Level.INFO,"La phase "+p.getClass()+" est setup ("+w.elapsedMillis()+" ms)"))){
				p.setup(inputData);
			}catch(Exception e) {
				LOGGER.log(Level.SEVERE, "Une erreur est survenue lors du setup de la phase "+p.getClass(), e);
			}
		}
		running = true;
	}

	@Override
	public void end() {
		if (currentPhase == null || !running) {
			running = false;
			return;
		}
		var all = getParents(currentPhase);
		all.add(currentPhase);
		for (var p : all) {
			try (var watch = new Stopwatch(w -> LOGGER.log(Level.INFO,"La phase "+p.getClass()+" est teardown ("+w.elapsedMillis()+" ms)"))){
				p.tearDown();
			}catch(Exception e) {
				LOGGER.log(Level.SEVERE, "Une erreur est survenue lors du teardown de la phase "+p.getClass(), e);
			}
			runningPhases.remove(p.getClass());
		}
	}

	@Override
	public <T> T getPhase(Class<T> phaseType) {
		return runningPhases.get(phaseType);
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> void onPhaseComplete(IPhase<P> phase, Object outData) {
		if (++currentPhaseIndex >= phaseOrder.size()) {
			end();
		}
		goTo(phaseOrder.get(currentPhaseIndex), outData);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <P, T extends IPhase<P>> T makePhase(Class<T> phaseType) {
		var oNextPhaseSupplier = registeredPhases.get(phaseType);
		if (oNextPhaseSupplier == null) {
			try {
				var ctor = phaseType.getConstructor(IPhaseMachine.class);
				oNextPhaseSupplier = (Function<IPhaseMachine,T>)m -> {
					try {
						return ctor.newInstance(m);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				};
				registeredPhases.put(phaseType, oNextPhaseSupplier);
			} catch (Exception e) {
				throw new IllegalArgumentException("Impossible de trouver le constructeur pour "+phaseType+", donnez un Supplier ou ajoutez le");
			}
		}
		T phase;
		if (oNextPhaseSupplier instanceof Function func) {
			phase = phaseType.cast(func.apply(this));
		}else {
			throw new IllegalArgumentException("Le supplier pour la phase "+phaseType+" n'est pas valide");
		}
		if (phase == null) {
			throw new IllegalArgumentException("Le supplier pour la phase "+phaseType+" n'est pas valide (retourne null)");
		}
		var parent = runningPhases.get(phase.getParentType());
		if (parent == null && phase.getParentType() != Void.class) {
			parent = (P) makePhase((Class<T>) phase.getParentType());
			phase.setParent(parent);
		}
		runningPhases.put(phase);
		return phase;
	}
	
	@SuppressWarnings("rawtypes")
	private LinkedList<IPhase> getParents(IPhase phase){
		var list = new LinkedList<IPhase>();
		var cur = phase.getParent();
		while (cur != null && cur instanceof IPhase p) {
			list.addFirst(p);
			cur = p.getParent();
		}
		return list;
	}

}
