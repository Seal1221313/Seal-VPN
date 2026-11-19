# Seal-VPN

Android VPN on top of Tor. Fork of [Orbot](https://github.com/guardianproject/orbot-android) (Nathan Freitas, The Guardian Project and contributors), built on Tor.

- All device traffic goes through Tor via a local VPN tunnel
- The device never relays other people's traffic: Kindness Mode / Snowflake proxy is removed
- Snowflake, obfs4 and other bridges remain available as client transports for your own connection

See `NOTICE.md` and `LICENSE` for credits and license.

## Build

```
git submodule update --init --recursive
./gradlew assembleFullpermDebug
```
