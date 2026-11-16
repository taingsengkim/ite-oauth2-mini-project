# Offline session locations

Account and administrator session views use the free DB-IP City Lite MMDB database. Lookups run locally: session IP addresses are not sent to an external geolocation API. Results are approximate, can reflect a VPN or proxy, and must not be used as proof of a user's physical location.

The database is licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) by [DB-IP](https://db-ip.com/db/download/ip-to-city-lite). The application displays the required DB-IP attribution link. The dataset is downloaded separately and excluded from Git.

From the repository root, install or update the monthly release:

```powershell
.\microservices\iam-service\scripts\update-geoip.ps1
```

The script defaults to the current `yyyy-MM` release. You may supply `-Release yyyy-MM` and `-ExpectedSha1 <checksum>` using the checksum published on the download page. Downloads are decompressed into `microservices/iam-service/data/geoip/dbip-city-lite.mmdb`.

Restart IAM after updating. Standard working directories (repository root, iam-service, and iam-main) are detected automatically. For deployment or a different working directory, set `IAM_GEOIP_DATABASE` to the absolute MMDB path. The dataset is not bundled into the application JAR. When no database is found, public-IP locations are unavailable; an explicitly configured missing file causes startup to fail with a configuration error.

Loopback addresses (`::1`, `127.0.0.1`) display as local. Private, link-local, and reserved networks have no geographic location. To test a public location, access IAM from a public client address. When deploying behind a reverse proxy, configure the trusted proxy to sanitize forwarding headers; never expose a backend that accepts arbitrary client-supplied forwarding headers as authoritative.
