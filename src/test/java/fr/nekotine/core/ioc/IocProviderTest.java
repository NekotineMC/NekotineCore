package fr.nekotine.core.ioc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IocProviderTest {

	@Test
	public void testSingleton() {
		var provider = new IocProvider();
		var test = new TestClass("MonSuperSingleton");
		provider.registerSingleton(test);
		var opt = provider.tryResolve(TestClass.class);
		assertTrue(opt.isPresent());
		assertEquals(test, provider.resolve(TestClass.class));
		assertEquals(test, opt.get());
	}
	
	@Test
	public void testSingletonOverride() {
		var provider = new IocProvider();
		var test = new TestClass("MonSuperSingleton");
		provider.registerSingleton(new TestClass("MonSuperSingleton2"));
		provider.registerSingleton(test);
		var opt = provider.tryResolve(TestClass.class);
		assertTrue(opt.isPresent());
		assertEquals(test, provider.resolve(TestClass.class));
		assertEquals(test, opt.get());
	}
	
	@Test
	public void testSingletonAs() {
		var provider = new IocProvider();
		var test = new TestClass("MonSuperSingleton");
		provider.registerSingletonAs(test, Object.class);
		var opt = provider.tryResolve(Object.class);
		assertTrue(opt.isPresent());
		assertEquals(test, provider.resolve(Object.class));
		assertEquals(test, opt.get());
	}
	
	@Test
	public void testSingletonAsOverride() {
		var provider = new IocProvider();
		var test = new TestClass("MonSuperSingleton");
		provider.registerSingletonAs(new TestClass("MonSuperSingleton2"), Object.class);
		provider.registerSingletonAs(test, Object.class);
		var opt = provider.tryResolve(Object.class);
		assertTrue(opt.isPresent());
		assertEquals(test, provider.resolve(Object.class));
		assertEquals(test, opt.get());
	}
	
	@Test
	public void testTransientAs() {
		var provider = new IocProvider();
		provider.registerTransientAs(() -> new TestClass("Transient"), Object.class);
		var a1 = provider.tryResolve(Object.class);
		assertTrue(a1.isPresent());
		assertNotEquals(a1.get(), provider.resolve(Object.class));
	}
	
	@Test
	public void testTransientAsOverride() {
		var provider = new IocProvider();
		provider.registerTransientAs(() -> new TestClass("Transient"), Object.class);
		var a1 = provider.resolve(Object.class);
		provider.registerTransientAs(() -> new TestClass("Transient2"), Object.class);
		assertNotEquals(((TestClass)a1).value, ((TestClass)provider.resolve(Object.class)).value);
		assertEquals("Transient2", ((TestClass)provider.resolve(Object.class)).value);
	}
	
	@Test
	public void testResolveOptionalEmpty() {
		var provider = new IocProvider();
		assertTrue(provider.tryResolve(TestClass.class).isEmpty());
	}
	
	private class TestClass{
		
		public TestClass(String value) {
			this.value = value;
		}
		
		private String value;
	}
	
}
