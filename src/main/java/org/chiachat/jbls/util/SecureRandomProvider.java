package org.chiachat.jbls.util;

import java.security.SecureRandom;

/**
 * Avoid direct construction of SecureRandom in Teku code, instead use this class to obtain
 * SecureRandom instance
 */
public class SecureRandomProvider {
    private static final SecureRandom publicSecureRandom = secureRandom();

    // Returns a shared instance of secure random intended to be used where the value is used publicly
    public static SecureRandom publicSecureRandom() {
        return publicSecureRandom;
    }

    public static SecureRandom createSecureRandom() {
        return secureRandom();
    }

    @SuppressWarnings("DoNotCreateSecureRandomDirectly")
    private static SecureRandom secureRandom() {
        return new SecureRandom();
    }
}