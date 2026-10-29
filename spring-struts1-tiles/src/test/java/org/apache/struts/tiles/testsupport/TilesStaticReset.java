package org.apache.struts.tiles.testsupport;

import java.lang.reflect.Field;

import org.apache.struts.tiles.TilesUtil;
import org.apache.struts.tiles.TilesUtilImpl;

/**
 * Test helper resetting the classic static {@link TilesUtil} state. The
 * classic <code>setTilesUtil</code> is deliberately set-once (a real webapp
 * initializes one implementation), but several tests run in one JVM, so
 * between test classes the static state must be neutralized.
 */
public final class TilesStaticReset {

    private TilesStaticReset() {
    }

    public static void resetTilesUtil() {
        try {
            Field impl = TilesUtil.class.getDeclaredField("tilesUtilImpl");
            impl.setAccessible(true);
            impl.set(null, new TilesUtilImpl());

            Field alreadySet =
                TilesUtil.class.getDeclaredField("implAlreadySet");
            alreadySet.setAccessible(true);
            alreadySet.set(null, false);

        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                "Cannot reset TilesUtil static state", ex);
        }
    }
}
