# USWire architecture

## Android application layers

- `core/model`: shared models such as `VpnConfig` and `VpnState`.
- `core/result`: common success/failure result type.
- `domain/repository`: contracts used by application logic.
- `domain/usecase`: user-oriented operations such as importing a profile.
- `data/local`: encrypted local profile storage using Android Keystore and AES-GCM.
- `data/validation`: structural validation of WireGuard configuration files.
- `vpn`: adapter boundary for the official WireGuard Tunnel backend.
- `ui/home`: initial import screen.

## Security notes

Configuration files contain private keys. This alpha stores the imported profile encrypted with an AES key managed by Android Keystore and places the encrypted file in the app's no-backup directory. The app must not log raw configuration text or private keys.

## VPN engine status

The `WireGuardController` is only an interface in this alpha. It does not create a tunnel. A later change must integrate the official WireGuard Tunnel backend and its Android VPN service lifecycle, then report connection state only from the real backend.
