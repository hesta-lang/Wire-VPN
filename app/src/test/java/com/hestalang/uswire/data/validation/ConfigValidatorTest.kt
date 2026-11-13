package com.hestalang.uswire.data.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigValidatorTest {
    private val validConfig = """
        [Interface]
        PrivateKey = AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyA=
        Address = 10.0.0.2/32
        DNS = 1.1.1.1

        [Peer]
        PublicKey = QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVpbXF1eX2A=
        Endpoint = 198.51.100.10:51820
        AllowedIPs = 0.0.0.0/0, ::/0
        PersistentKeepalive = 25
    """.trimIndent()

    @Test
    fun acceptsValidWireGuardConfig() {
        assertTrue(ConfigValidator.validate(validConfig).isSuccess)
    }

    @Test
    fun rejectsConfigWithoutInterface() {
        val config = """
            [Peer]
            PublicKey = QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVpbXF1eX2A=
            Endpoint = 198.51.100.10:51820
            AllowedIPs = 0.0.0.0/0
        """.trimIndent()

        assertFalse(ConfigValidator.validate(config).isSuccess)
    }

    @Test
    fun rejectsConfigWithoutPeer() {
        val config = """
            [Interface]
            PrivateKey = AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyA=
            Address = 10.0.0.2/32
        """.trimIndent()

        assertFalse(ConfigValidator.validate(config).isSuccess)
    }

    @Test
    fun rejectsMalformedPrivateKey() {
        val config = validConfig.replace(
            "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyA=",
            "not-a-wireguard-key"
        )

        assertFalse(ConfigValidator.validate(config).isSuccess)
    }

    @Test
    fun rejectsBlankConfig() {
        assertFalse(ConfigValidator.validate("   ").isSuccess)
    }
}
