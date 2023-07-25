package fr.nekotine.core.map.save.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.File;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import fr.nekotine.core.map.MapHandle;
import fr.nekotine.core.map.MapMetadata;
import fr.nekotine.core.map.MapTest;
import net.kyori.adventure.text.Component;

class ConfigurationSerializableMapMetadataSaverTest {
	
	private ConfigurationSerializableMapMetadataSaver saver;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp(@TempDir(cleanup = CleanupMode.ON_SUCCESS) File mapFolder) throws Exception {
		saver = new ConfigurationSerializableMapMetadataSaver(mapFolder);
	}

	@AfterEach
	void tearDown() throws Exception {
		saver = null;
	}

	@Test
	void testDeserialisation() {
		// SETUP
		var handle = new MapHandle<MapTest>(MapTest.class, "TestMap", null, null, saver);
		var metadata = new MapMetadata();
		var description = Component.text("Une super description");
		metadata.setDescription(description);
		var displayName = Component.text("MonDisplayName");
		metadata.setDisplayName(displayName);
		metadata.setIcon(Material.STONE);
		
		
		// SAVING
		saver.saveMetadata(handle, metadata);
		
		// LOADING
		var deserialized = saver.loadMetadata(handle);
		
		// TESTS
		assertInstanceOf(MapMetadata.class, deserialized);
		assertEquals(description, deserialized.getDescription());
		assertEquals(displayName, deserialized.getDisplayName());
		assertEquals(Material.STONE, deserialized.getIcon());
	}

}
