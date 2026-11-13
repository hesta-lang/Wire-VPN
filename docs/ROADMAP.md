# USWire roadmap

## 0.1.0-alpha — project foundation
- Android application structure and CI workflow.
- Import and basic validation of WireGuard `.conf` files.
- Encrypted local profile persistence.
- Unit tests for configuration validation.
- No real VPN connection yet.

## 0.2.0-alpha — WireGuard engine
- Integrate the official WireGuard Tunnel backend.
- Add Android VPN permission flow and service lifecycle handling.
- Implement connect/disconnect and real tunnel state reporting.
- Test on Android 10 and newer.

## 0.3.0-alpha — connection management
- Multiple saved profiles.
- Connection diagnostics without exposing keys.
- Reconnect behavior and network change handling.

## Beta and release
- Security and privacy review.
- Stability and battery testing.
- Confirm licensing and attribution for third-party dependencies.

USWire is a client only. It does not provide VPN servers, free bandwidth, or unlimited data; those depend on the chosen server or provider.
