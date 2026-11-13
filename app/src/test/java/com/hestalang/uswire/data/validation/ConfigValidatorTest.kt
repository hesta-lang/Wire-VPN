package com.hestalang.uswire.data.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigValidatorTest {
    @Test
    fun acceptsInterfaceAndPeerSections() {
        val config = """
            [Interface]
            PrivateKey = TEST_PRIVATE_KEY
            Address = 10.0.0.2/32

            [Peer]
            PublicKey = TEST_PUBLIC_KEY
            Endpoint = 198.51.100.10:51820
            AllowedIPs = 0.0.0.0/0
        """.trimIndent()

        assertTrue(ConfigValidator.validate(config).isSuccess)
    }

    @Test
    fun rejectsConfigWithoutInterface() {
        val config = """
            [Peer]
            PublicKey = TEST_PUBLIC_KEY
        """.trimIndent()

        assertFalse(ConfigValidator.validate(config).isSuccess)
    }

    @Test
    fun rejectsConfigWithoutPeer() {
        val config = """
            [Interface]
            PrivateKey = TEST_PRIVATE_KEY
        """.trimIndent()

        assertFalse(ConfigValidator.validate(config).isSuccess)
    }

    @Test
    fun rejectsBlankConfig() {
        assertFalse(ConfigValidator.validate("   ").isSuccess)
    }
}
