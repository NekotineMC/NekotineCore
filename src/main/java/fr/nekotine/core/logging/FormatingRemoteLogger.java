package fr.nekotine.core.logging;

import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class FormatingRemoteLogger extends Logger{

	private String format;
	
	private Logger remote;
	
	public FormatingRemoteLogger() {
		super(null, null);
	}
	
	public FormatingRemoteLogger(Logger remote, String format) {
		super(null, null);
		this.remote = remote;
		this.format = format;
	}
	
	public FormatingRemoteLogger(Logger remote) {
		super(null, null);
		this.remote = remote;
	}
	
	public FormatingRemoteLogger(String format) {
		super(null, null);
		this.format = format;
	}
	
	public void setRemote(Logger remote) {
		this.remote = remote;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public Logger getRemote() {
		return remote;
	}
	
	public String getFormat() {
		return format;
	}
	
	@Override
	public void addHandler(Handler handler) throws SecurityException {
		remote.addHandler(handler);
	}
	
	@Override
	public void config(String msg) {
		remote.config(String.format(format, msg));
	}
	
	@Override
	public void config(Supplier<String> msgSupplier) {
		super.config(() -> String.format(format, msgSupplier.get()));
	}
	
	@Override
	public void entering(String sourceClass, String sourceMethod) {
		remote.entering(sourceClass, sourceMethod);
	}
	
	@Override
	public void entering(String sourceClass, String sourceMethod, Object param1) {
		remote.entering(sourceClass, sourceMethod, param1);
	}
	
	@Override
	public void entering(String sourceClass, String sourceMethod, Object[] params) {
		remote.entering(sourceClass, sourceMethod, params);
	}
	
	@Override
	public void exiting(String sourceClass, String sourceMethod) {
		remote.exiting(sourceClass, sourceMethod);
	}
	
	@Override
	public void exiting(String sourceClass, String sourceMethod, Object result) {
		remote.exiting(sourceClass, sourceMethod, result);
	}
	
	@Override
	public void fine(String msg) {
		remote.fine(String.format(format, msg));
	}
	
	@Override
	public void fine(Supplier<String> msgSupplier) {
		remote.fine(() -> String.format(format, msgSupplier.get()));
	}
	
	@Override
	public void finer(String msg) {
		remote.finer(String.format(format, msg));
	}

	@Override
	public void finer(Supplier<String> msgSupplier) {
		remote.finer(() -> String.format(format, msgSupplier.get()));
	}
	
	@Override
	public void finest(String msg) {
		remote.finest(String.format(format, msg));
	}
	
	@Override
	public void finest(Supplier<String> msgSupplier) {
		remote.finest(() -> String.format(format, msgSupplier.get()));
	}
	
	@Override
	public Filter getFilter() {
		return remote.getFilter();
	}
	
	@Override
	public Handler[] getHandlers() {
		return remote.getHandlers();
	}
	
	@Override
	public Level getLevel() {
		return remote.getLevel();
	}
	
	@Override
	public String getName() {
		return remote.getName();
	}
	
	@Override
	public Logger getParent() {
		return remote.getParent();
	}
	
	@Override
	public ResourceBundle getResourceBundle() {
		return remote.getResourceBundle();
	}
	
	@Override
	public String getResourceBundleName() {
		return remote.getResourceBundleName();
	}
	
	@Override
	public boolean getUseParentHandlers() {
		return remote.getUseParentHandlers();
	}
	
	@Override
	public void info(String msg) {
		remote.info(String.format(format, msg));
	}
	
	@Override
	public void info(Supplier<String> msgSupplier) {
		remote.info(() -> String.format(format, msgSupplier.get()));
	}
	
	@Override
	public boolean isLoggable(Level level) {
		return remote.isLoggable(level);
	}
	
	@Override
	public void log(Level level, String msg) {
		remote.log(level, String.format(format, msg));
	}
	
	@Override
	public void log(Level level, String msg, Object param1) {
		remote.log(level, String.format(format, msg), param1);
	}
	
	@Override
	public void log(Level level, String msg, Object[] params) {
		remote.log(level, String.format(format, msg), params);
	}
	
	@Override
	public void log(Level level, String msg, Throwable thrown) {
		remote.log(level, String.format(format, msg), thrown);
	}
	
	@Override
	public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
		remote.log(level, thrown, () -> String.format(format, msgSupplier.get()));
	}
	
	@Override
	public void log(Level level, Supplier<String> msgSupplier) {
		remote.log(level, () -> String.format(format, msgSupplier.get()));
	}
	
	@Override
	public void log(LogRecord record) {
		remote.log(record);
	}
	
	@Override
	public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
		remote.logp(level, sourceClass, sourceMethod, String.format(format, msg));
	}
	
	@Override
	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
		remote.logp(level, sourceClass, sourceMethod, String.format(format, msg), param1);
	}
	
	@Override
	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
		remote.logp(level, sourceClass, sourceMethod, String.format(format, msg), params);
	}
	
	@Override
	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
		remote.logp(level, sourceClass, sourceMethod, String.format(format, msg), thrown);
	}
	
	@Override
	public void logp(Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
		remote.logp(level, sourceClass, sourceMethod, () -> String.format(format, msgSupplier.get()));
	}
	
	@Override
	public void logp(Level level, String sourceClass, String sourceMethod, Throwable thrown,
			Supplier<String> msgSupplier) {
		remote.logp(level, sourceClass, sourceMethod, thrown, () -> String.format(format, msgSupplier.get()));
	}
	
	@Override
	public void logrb(Level level, ResourceBundle bundle, String msg, Object... params) {
		remote.logrb(level, bundle, String.format(format, msg), params);
	}
	
	@Override
	public void logrb(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
		remote.logrb(level, bundle, String.format(format, msg), thrown);
	}
	
	@Override
	public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg,
			Object... params) {
		remote.logrb(level, sourceClass, sourceMethod, bundle, String.format(format, msg), params);
	}
	
	@Override
	public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg,
			Throwable thrown) {
		remote.logrb(level, sourceClass, sourceMethod, bundle, String.format(format, msg), thrown);
	}
	
	@Override
	public void removeHandler(Handler handler) throws SecurityException {
		remote.removeHandler(handler);
	}
	
	@Override
	public void setFilter(Filter newFilter) throws SecurityException {
		remote.setFilter(newFilter);
	}
	
	@Override
	public void setLevel(Level newLevel) throws SecurityException {
		remote.setLevel(newLevel);
	}
	
	@Override
	public void setParent(Logger parent) {
		remote.setParent(parent);
	}
	
	@Override
	public void setResourceBundle(ResourceBundle bundle) {
		remote.setResourceBundle(bundle);
	}
	
	@Override
	public void setUseParentHandlers(boolean useParentHandlers) {
		remote.setUseParentHandlers(useParentHandlers);
	}
	
	@Override
	public void severe(String msg) {
		super.severe(String.format(format, msg));
	}
	
	@Override
	public void severe(Supplier<String> msgSupplier) {
		super.severe(() -> String.format(format, msgSupplier.get()));
	}
	
	@Override
	public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
		super.throwing(sourceClass, sourceMethod, thrown);
	}
	
	@Override
	public void warning(String msg) {
		super.warning(String.format(format, msg));
	}
	
	@Override
	public void warning(Supplier<String> msgSupplier) {
		super.warning(() -> String.format(format, msgSupplier.get()));
	}
}
