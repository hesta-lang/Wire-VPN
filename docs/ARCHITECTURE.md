# USWire architecture

## Android application layers

- core/model: shared models such as VpnConfig and VpnState.
- core/result: common success/failure result type.
- domain/repository: contracts used by application logic.
- domain/usecase: user-oriented operations such as importing a profile.
- data/local: encrypted local profile storage using Android Keystore and AES-GCM.
- data/validation: structural checks followed by the official WireGuard configuration parser.
- vpn: singleton adapter around the official WireGuard Tunnel GoBackend.
- ui/home: profile import and VPN connect/disconnect controls.

## Tunnel lifecycle

1. The user imports a provider-issued WireGuard profile through Android's document picker.
2. USWire validates it with the WireGuard parser and stores the profile encrypted in app-private storage.
3. The user taps Connect. The Activity requests Android's VpnService authorization when needed.
4. WireGuardController parses the stored profile and calls GoBackend.setState with Tunnel.State.UP on a worker thread.
5. The WireGuard AAR contributes its GoBackend VpnService declaration through Android manifest merging and establishes the TUN interface. State callbacks are mapped to the UI.
6. Disconnect calls GoBackend.setState with Tunnel.State.DOWN.

The GoBackend and Tunnel objects are held by a process-wide singleton because GoBackend associates its VPN service with the backend instance that started it.

## Security notes

Configuration files contain private keys. This alpha stores the imported profile encrypted with an AES key managed by Android Keystore and places the encrypted file in the app's no-backup directory. The app must not log raw configuration text or private keys. Never commit provider .conf profiles to the repository.

## Validation status

The real backend and permission flow are now wired into the app. A successful CI build validates compilation and unit tests only; it does not prove that a provider endpoint accepts a tunnel. Physical-device testing remains necessary for connection, DNS, IPv6 routing, disconnect, and network-change behavior.
