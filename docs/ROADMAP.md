# USWire roadmap

## 0.1.0-alpha — project foundation
- Android application structure and CI workflow.
- Import and basic validation of WireGuard .conf files.
- Encrypted local profile persistence.
- Unit tests for configuration validation.
- Foundation only; no VPN engine.

## 0.2.0-alpha — WireGuard engine
- [x] Integrate the official WireGuard Tunnel backend.
- [x] Add Android VPN permission flow and use the VpnService entry supplied by the WireGuard AAR manifest.
- [x] Implement connect/disconnect controls and backend state reporting.
- [x] Parse imported profiles with the official WireGuard parser.
- [ ] Confirm CI build and unit tests.
- [ ] Test on a physical Android 10+ device with a provider-issued profile.

## 0.3.0-alpha — interface refresh\n- [x] Add launcher-style navigation and profile management screens.\n- [x] Add privacy information and app identity assets.\n\n## 0.3.1-alpha — import stability\n- [x] Move file reading, parsing, encryption, and saved-profile loading off the Android main thread.\n- [x] Improve SAF MIME filters and add a fallback picker action.\n- [x] Bound imported configuration size and show import progress.\n\n## Next — connection management
- Multiple saved profiles.
- Connection diagnostics without exposing keys.
- Reconnect behavior and network change handling.
- UI improvements and connection statistics.

## Beta and release
- Security and privacy review.
- Stability, DNS, IPv6, and battery testing.
- Confirm licensing and attribution for third-party dependencies.

USWire is a client only. It does not provide VPN servers, free bandwidth, or unlimited data; those depend on the chosen server or provider.
