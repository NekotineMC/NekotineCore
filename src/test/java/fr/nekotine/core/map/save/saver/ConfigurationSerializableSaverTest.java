package fr.nekotine.core.map.save.saver;

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

import fr.nekotine.core.map.MapIdentifier;
import fr.nekotine.core.map.MapTest;

class ConfigurationSerializableSaverTest {
	
	private ConfigurationSerializableSaver saver;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp(@TempDir(cleanup = CleanupMode.ON_SUCCESS) File mapFolder) throws Exception {
		saver = new ConfigurationSerializableSaver(mapFolder);
	}

	@AfterEach
	void tearDown() throws Exception {
		saver = null;
	}

	@Test
	void testDeserialisation() {
		var mapInst = new MapTest();
		mapInst.getPoseUnnamed().setXYZ(10,20,30);
		var mapId = new MapIdentifier();
		mapId.setName("TestMap");
		mapId.setDescription("Une carte de test");
		mapId.setType(MapTest.class);
		mapId.setIcon(Material.STONE);
		saver.save(mapId, mapInst);
		var deserialized = saver.load(mapId);
		
		assertInstanceOf(MapTest.class, deserialized);
		var map = (MapTest)deserialized;
		var xyz = map.getPoseUnnamed().getXYZ();
		assertEquals(10, xyz.a());
		assertEquals(20, xyz.b());
		assertEquals(30, xyz.c());
	}

}
