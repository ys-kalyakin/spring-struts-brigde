package org.apache.struts.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the startup validation of controller configuration
 * properties the engine does not implement.
 */
class ControllerConfigValidatorTest {

    private ModuleConfig module(String prefix) {
        return ModuleConfigFactory.createFactory().createModuleConfig(prefix);
    }

    @Test
    void customProcessorClassFailsStartup() {
        ModuleConfig config = module("");
        config.getControllerConfig()
            .setProcessorClass("com.example.SecureRequestProcessor");

        UnsupportedControllerConfigException thrown = assertThrows(
            UnsupportedControllerConfigException.class,
            () -> ControllerConfigValidator.validate(config));
        String message = thrown.getMessage();
        assertTrue(message.contains("processorClass"));
        assertTrue(message.contains("com.example.SecureRequestProcessor"));
        assertTrue(message.contains("The default module"));
        assertTrue(message.contains("org.apache.struts.tiles.TilesRequestProcessor"));
    }

    @Test
    void supportedProcessorClassesStart() {
        ModuleConfig classic = module("");
        classic.getControllerConfig()
            .setProcessorClass(ControllerConfigValidator.DEFAULT_PROCESSOR_CLASS);
        ModuleConfig tiles = module("");
        tiles.getControllerConfig()
            .setProcessorClass(ControllerConfigValidator.TILES_PROCESSOR_CLASS);

        assertDoesNotThrow(() -> ControllerConfigValidator.validate(classic));
        assertDoesNotThrow(() -> ControllerConfigValidator.validate(tiles));
    }

    @Test
    void emptyAndAbsentProcessorClassStart() {
        ModuleConfig empty = module("");
        empty.getControllerConfig().setProcessorClass("");
        ModuleConfig absent = module("");

        assertDoesNotThrow(() -> ControllerConfigValidator.validate(empty));
        assertDoesNotThrow(() -> ControllerConfigValidator.validate(absent));
    }

    @Test
    void customChainCommandFailsStartup() {
        ModuleConfig config = module("");
        config.getControllerConfig().setCommand("custom-chain");

        UnsupportedControllerConfigException thrown = assertThrows(
            UnsupportedControllerConfigException.class,
            () -> ControllerConfigValidator.validate(config));
        String message = thrown.getMessage();
        assertTrue(message.contains("command"));
        assertTrue(message.contains("custom-chain"));
        assertTrue(message.contains("The default module"));
    }

    @Test
    void customChainCatalogFailsStartup() {
        ModuleConfig config = module("");
        config.getControllerConfig().setCatalog("my-catalog");

        assertThrows(UnsupportedControllerConfigException.class,
            () -> ControllerConfigValidator.validate(config));
    }

    @Test
    void defaultChainValuesStart() {
        ModuleConfig config = module("");
        config.getControllerConfig().setCatalog("struts");
        config.getControllerConfig().setCommand("servlet-standard");

        assertDoesNotThrow(() -> ControllerConfigValidator.validate(config));
    }

    @Test
    void modulePrefixIsNamedInMessages() {
        ModuleConfig config = module("/exercise");
        config.getControllerConfig()
            .setProcessorClass("com.example.SecureRequestProcessor");

        UnsupportedControllerConfigException thrown = assertThrows(
            UnsupportedControllerConfigException.class,
            () -> ControllerConfigValidator.validate(config));
        assertTrue(thrown.getMessage().contains("/exercise"));
    }

    @Test
    void nonDefaultBufferSizeWarnsAndStarts() {
        ModuleConfig config = module("");
        config.getControllerConfig().setBufferSize(8192);

        // The warning itself goes to the log; the application must start.
        assertDoesNotThrow(() -> ControllerConfigValidator.validate(config));
    }

    @Test
    void defaultBufferSizeStartsSilently() {
        ModuleConfig config = module("");
        config.getControllerConfig().setBufferSize(4096);

        assertDoesNotThrow(() -> ControllerConfigValidator.validate(config));
    }
}
