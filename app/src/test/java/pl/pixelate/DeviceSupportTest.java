package pl.pixelate;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DeviceSupportTest {
    @Test
    public void acceptsPhysicalPixels() {
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel 11 Pro", false));
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel Fold", false));
        assertTrue(DeviceSupport.isSupportedIdentity(
                "Google", "google", "Pixel Tablet", false));
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
