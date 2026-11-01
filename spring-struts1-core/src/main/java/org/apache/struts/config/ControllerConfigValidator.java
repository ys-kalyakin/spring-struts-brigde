package org.apache.struts.config;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the <code>&lt;controller&gt;</code> configuration of a module
 * against the controller properties this engine implements. The parser
 * accepts every attribute of the classic DTD, but the engine implements only
 * part of them; a property the engine ignores would otherwise be silently
 * skipped, and a custom request processor's logic (authentication, audit,
 * output post-processing) would simply not run.
 *
 * <p>Validation runs for every module at configuration registration, on both
 * startup paths that share the configuration parser.</p>
 */
public final class ControllerConfigValidator {

    private static final Logger LOG =
        LoggerFactory.getLogger(ControllerConfigValidator.class);

    /** The classic default request processor, implemented by the engine. */
    static final String DEFAULT_PROCESSOR_CLASS =
        "org.apache.struts.action.RequestProcessor";

    /** The Tiles request processor, implemented natively by the engine. */
    static final String TILES_PROCESSOR_CLASS =
        "org.apache.struts.tiles.TilesRequestProcessor";

    private static final Set<String> SUPPORTED_PROCESSOR_CLASSES =
        Set.of(DEFAULT_PROCESSOR_CLASS, TILES_PROCESSOR_CLASS);

    private static final String DEFAULT_CATALOG = "struts";

    private static final String DEFAULT_COMMAND = "servlet-standard";

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private ControllerConfigValidator() {
    }

    /**
     * Validate the controller configuration of the given module: fail for
     * properties the engine does not implement, warn for properties whose
     * only difference is tuning.
     *
     * @param moduleConfig the module configuration to validate
     * @throws UnsupportedControllerConfigException when the configuration
     *         relies on controller behavior the engine does not implement
     */
    public static void validate(ModuleConfig moduleConfig) {
        ControllerConfig controller = moduleConfig.getControllerConfig();
        String prefix = moduleConfig.getPrefix();
        validateProcessorClass(controller, prefix);
        validateChainSettings(controller, prefix);
        validateBufferSize(controller, prefix);
    }

    private static void validateProcessorClass(ControllerConfig controller,
        String prefix) {
        String value = controller.getProcessorClass();
        if (value == null) {
            return;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || SUPPORTED_PROCESSOR_CLASSES.contains(trimmed)) {
            return;
        }
        throw new UnsupportedControllerConfigException(describeModule(prefix)
            + " sets processorClass to \"" + trimmed + "\", which the engine"
            + " does not implement. Supported processor classes: "
            + SUPPORTED_PROCESSOR_CLASSES + ". Port the processor's logic to"
            + " Spring MVC interceptors or filters (see the migration guide).");
    }

    private static void validateChainSettings(ControllerConfig controller,
        String prefix) {
        checkChainProperty(controller.getCatalog(), DEFAULT_CATALOG,
            "catalog", prefix);
        checkChainProperty(controller.getCommand(), DEFAULT_COMMAND,
            "command", prefix);
    }

    private static void checkChainProperty(String value, String expected,
        String property, String prefix) {
        if (value == null || value.trim().equals(expected)) {
            return;
        }
        throw new UnsupportedControllerConfigException(describeModule(prefix)
            + " sets " + property + " to \"" + value.trim()
            + "\", which the engine does not implement (commons-chain"
            + " configuration has no meaning on the engine). Remove the"
            + " attribute or port the chain logic to Spring MVC interceptors"
            + " or filters (see the migration guide).");
    }

    private static void validateBufferSize(ControllerConfig controller,
        String prefix) {
        if (controller.getBufferSize() == DEFAULT_BUFFER_SIZE) {
            return;
        }
        LOG.warn("{} sets bufferSize to {}, which the engine does not"
                + " implement; the application starts and uploads use the"
                + " container's multipart settings instead.",
            describeModule(prefix), controller.getBufferSize());
    }

    private static String describeModule(String prefix) {
        return prefix.isEmpty() ? "The default module" : "Module " + prefix;
    }
}
