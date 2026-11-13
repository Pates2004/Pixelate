package pl.pixelate;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DeviceSupportTest {
    @Test
    public void acceptsPhysicalPixels() {
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel", false));
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel XL", false));
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel 2 XL", false));
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel 3a", false));
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel 4a (5G)", false));
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel 11 Pro", false));
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel Fold", false));
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel Tablet", false));
    }

    @Test
    public void legacyGoogleEmulatorNamesDoNotPermitPhysicalNonPixels() {
        for (String model : new String[]{"Android SDK built for x86", "Android SDK built for x86_64"}) {
            assertTrue(DeviceSupport.isSupportedIdentity("Google", "google", model, true));
            assertFalse(DeviceSupport.isSupportedIdentity("Google", "google", model, false));
            assertFalse(DeviceSupport.isSupportedIdentity("Samsung", "samsung", model, true));
        }
    }

    @Test
    public void rejectsNonPixelPhonesEvenWhenModelNameIsSpoofedCasually() {
        assertFalse(DeviceSupport.isSupportedIdentity(
                "Samsung", "samsung", "Pixel 11 Pro", false));
        assertFalse(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Nexus 6P", false));
        assertFalse(DeviceSupport.isSupportedIdentity(
                "Xiaomi", "google", "Pixel 11", false));
    }

    @Test
    public void permitsOnlyTheExplicitGoogleSdkEmulatorException() {
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "sdk_gphone64_x86_64", true));
        assertFalse(DeviceSupport.isSupportedIdentity(
                "Google", "google", "sdk_gphone64_x86_64", false));
        assertFalse(DeviceSupport.isSupportedIdentity(
                "Samsung", "samsung", "sdk_gphone64_x86_64", true));
    }
}
