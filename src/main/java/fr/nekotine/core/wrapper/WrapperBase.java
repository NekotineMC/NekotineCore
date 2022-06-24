package fr.nekotine.core.wrapper;

public abstract class WrapperBase<WrappedType> {

	public WrappedType wrapped;
	
	public WrapperBase(WrappedType wrapped) {
		this.wrapped = wrapped;
	}
	
	public WrappedType GetWrapped() {
		return wrapped;
	}
	
}
