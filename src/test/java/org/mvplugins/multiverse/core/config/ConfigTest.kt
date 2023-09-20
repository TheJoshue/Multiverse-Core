package org.mvplugins.multiverse.core.config

import org.mvplugins.multiverse.core.TestWithMockBukkit
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.test.*

class ConfigTest : TestWithMockBukkit() {

    private lateinit var config : MVCoreConfig

    @BeforeTest
    fun setUp() {
        config = multiverseCore.getService(MVCoreConfig::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("MVCoreConfig is not available as a service") }

        val defaultConfig = getResourceAsText("/default_config.yml")
        assertNotNull(defaultConfig)
        File(Path.of(multiverseCore.dataFolder.absolutePath, "config.yml").absolutePathString()).writeText(defaultConfig)

        assertTrue(config.load().isSuccess)
        assertTrue(config.save().isSuccess)
    }

    @Test
    fun `Config is loaded`() {
        assertTrue(config.isLoaded)
    }

    @Test
    fun `Old config is migrated`() {
        val oldConfig = getResourceAsText("/old_config.yml")
        assertNotNull(oldConfig)
        multiverseCore.dataFolder.toPath().resolve("config.yml").toFile().writeText(oldConfig)
        assertTrue(config.load().isSuccess)
        assertTrue(config.save().isSuccess)

        assertEquals(true, config.enforceAccess)
        assertEquals(false, config.isEnablePrefixChat)
        assertEquals("[%world%]>>%chat%", config.prefixChatFormat)
        assertEquals(false, config.teleportIntercept)
        assertEquals(true, config.firstSpawnOverride)
        assertEquals(2, config.globalDebug)
        assertEquals(false, config.silentStart)
        assertEquals("world", config.firstSpawnLocation)
        assertEquals(false, config.isUsingCustomPortalSearch)
        assertEquals(128, config.customPortalSearchRadius)
        assertEquals(true, config.isAutoPurgeEntities)
        assertEquals(false, config.isShowingDonateMessage)
    }

    @Test
    fun `Getting existing config property with getProperty returns expected value`() {
        assertEquals(false, config.getProperty("enforce-access").get())
        assertEquals("world", config.getProperty("first-spawn-location").get())
    }

    @Test
    fun `Getting non-existing config property with getProperty returns null`() {
        assertTrue(config.getProperty("invalid-property").isFailure)
        assertTrue(config.getProperty("version").isFailure)
    }

    @Test
    fun `Getting existing config property by getter returns expected value`() {
        assertEquals(false, config.enforceAccess)
        assertEquals("world", config.firstSpawnLocation)
    }

    @Test
    fun `Updating an existing config property with setProperty reflects the changes in getProperty`() {
        assertTrue(config.setProperty("enforce-access", true).isSuccess)
        assertEquals(true, config.getProperty("enforce-access").get())

        assertTrue(config.setProperty("first-spawn-location", "world2").isSuccess)
        assertEquals("world2", config.getProperty("first-spawn-location").get())

        assertTrue(config.setProperty("global-debug", 1).isSuccess)
        assertEquals(1, config.getProperty("global-debug").get())
    }

    @Test
    fun `Updating an existing config property with setPropertyString reflects the changes in getProperty`() {
        assertTrue(config.setPropertyString("enforce-access", "true").isSuccess)
        assertEquals(true, config.getProperty("enforce-access").get())

        assertTrue(config.setPropertyString("first-spawn-location", "world2").isSuccess)
        assertEquals("world2", config.getProperty("first-spawn-location").get())

        assertTrue(config.setPropertyString("global-debug", "1").isSuccess)
        assertEquals(1, config.getProperty("global-debug").get())
    }

    @Test
    fun `Updating a non-existing property with setProperty returns false`() {
        assertTrue(config.setProperty("invalid-property", false).isFailure)
        assertTrue(config.setProperty("version", 1.1).isFailure)
    }
}
