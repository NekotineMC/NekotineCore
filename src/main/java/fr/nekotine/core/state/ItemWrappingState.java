package fr.nekotine.core.state;

import java.util.function.Function;

import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.wrapper.WrapperBase;
import fr.nekotine.core.wrapper.WrappingModule;

public class ItemWrappingState<U, T extends WrapperBase<U>> implements ItemState<U>{

	private final Function<U,T> wrapperProvider;
	
	private final Class<T> wrapperType;
	
	@SuppressWarnings("unchecked")
	public ItemWrappingState(Function<U,T> wrapperProvider) {
		this.wrapperProvider = wrapperProvider;
		var tempWrap = this.wrapperProvider.apply(null);
		wrapperType = (Class<T>) tempWrap.getClass();
	}
	
	@Override
	public void setup(U item) {
		NekotineCore.MODULES.get(WrappingModule.class).makeWrapper(item, wrapperProvider);
	}

	@Override
	public void teardown(U item) {
		NekotineCore.MODULES.get(WrappingModule.class).removeWrapper(item, wrapperType);
	}

}
