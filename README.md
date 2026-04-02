# 🛡 EpicGuard [![GitHub stars](https://img.shields.io/github/stars/snyfe/EpicGuard)](https://github.com/snyfe/EpicGuard/stargazers) [![GitHub forks](https://img.shields.io/github/forks/snyfe/EpicGuard)](https://github.com/snyfe/EpicGuard/network) [![GitHub issues](https://img.shields.io/github/issues/snyfe/EpicGuard)](https://github.com/snyfe/EpicGuard/issues) [![GitHub license](https://img.shields.io/github/license/snyfe/EpicGuard)](https://github.com/snyfe/EpicGuard/blob/master/LICENSE)
<!-- [![Java CI](https://github.com/snyfe/EpicGuard/actions/workflows/gradle.yml/badge.svg)](https://github.com/snyfe/EpicGuard/actions/workflows/gradle.yml) -->
A simple AntiBot plugin for newest Minecraft versions.

> [!IMPORTANT]
> This is a forked version if EpicGuard with minor changes.
<!-- > This source version of EpicGuard has been discontinued; the last published version will be 7.6.1.
> I don't feel that I am the developer this project needs.
> Any other developer can take over the project, just as I did when I forked the original project. -->

## ✅ Supported platforms / Latest release requirements
* [Paper 1.20.1+](https://papermc.io/) *(all paper forks are supported)*
* [Velocity 3.4+](https://velocitypowered.com/)
* Java **21**

[![](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_46h.png)](https://modrinth.com/plugin/epicguard)

## ✨ Features
* A total of **8** configurable antibot checks:
  * Geographical check - country/city blacklist or whitelist.
  * VPN/Proxy check - configurable services and caching.
  * Nickname check - block certain nickname patterns using regex.
  * Reconnect check - require re-joining the server with an identical pair of address and nickname.
  * Server list check - require pinging the server before connecting (adding it to the server list).
  * Settings check - make sure that player sends a settings packet after joining (vanilla client behaviour).
  * Lockdown - temporarily block incoming connections if there are too many of them.
  * Name similarity check (BETA)
  * Account limit.
* SQLite/MySQL support.
* Live actionbar statistics.
* Automatic whitelisting.
* Console filter.

## 📚 Commands & Permissions
To be able to use commands, give yourself the **epicguard.admin** permission.  
On different platforms there are additional aliases available, such as **/guardvelocity** or **/epicguardpaper**

| Command                                          | Description                                                            |
|--------------------------------------------------|------------------------------------------------------------------------|
| /epicguard help                                  | Displays all available commands.                                       |
| /epicguard reload                                | Reloads config and messages.                                           |
| /epicguard whitelist <add/remove> <nick/address> | Whitelist/unwhitelist an address or nickname.                          |
| /epicguard blacklist <add/remove> <nick/address> | Blacklist/unblacklist an address or nickname.                          |
| /epicguard analyze <nick/address>                | Displays detailed information about the specified address or nickname. |
| /epicguard status                                | Toggles live attack information on actionbar.                          |
| /epicguard save                                  | Forces save to the database.                                           |

## 🔧 Using EpicGuard API in your project:
The api is not very advanced, and there is not much you can do with it for now.

<details>
<summary>Gradle (Kotlin)</summary>

```kotlin
repositories {
    // Snapshots
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    // Releases
    mavenCentral()
}
dependencies {
    compileOnly("com.github.snyfe:epicguard-api:[VERSION HERE]")
}
```
</details>

<details>
<summary>Gradle (Groovy)</summary>

```groovy
repositories {
    maven {
      url = 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
    }
  mavenCentral()
}
dependencies {
    compileOnly 'io.github.snyfe:epicguard-api:[VERSION OR COMMIT ID HERE]'
}
```
</details>

<details>
<summary>Maven</summary>

```xml
  <repositories>
    <!-- Only for Snapshots-->
    <repository>
      <id>sonatype-oss-snapshots1</id>
      <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
    </repository>
  </repositories>
  <dependencies>
      <dependency>
         <groupId>io.github.snyfe</groupId>
         <artifactId>epicguard-api</artifactId>
         <version>[VERSION HERE]</version>
         <scope>provided</scope>
     </dependency>
  </dependencies>
```
</details>

<details>
<summary>Using the API</summary>
Make sure that EpicGuard is fully loaded before your plugin.

[Click to see the API class](https://github.com/xxneox/EpicGuard/blob/master/core/src/main/java/me/xneox/epicguard/core/EpicGuardAPI.java)

```java
// Importing the API class.
import me.xneox.epicguard.core.EpicGuardAPI;
import me.xneox.epicguard.core.manager.AttackManager;
public class EpicGuardAPIExample {
  // Accessing the EpicGuardAPI instance.
  EpicGuardAPI api = EpicGuardAPI.INSTANCE;
  // Obtaining the AttackManager instance:
  AttackManager attackManager = api.attackManager();
  // Checking if server is under attack.
  boolean isUnderAttack = attackManager.isUnderAttack();
  // checking current connections per second.
  int cps = attackManager.connectionCounter();
  
  // Checking user's country:
  String countryId = api.geoManager().countryCode("127.0.0.1");
}
```
</details>

## 🕵️ Privacy disclaimers
* This plugin connect to various external services, to fully work as intended.
  * [MaxMind's Geolite2 databases](https://dev.maxmind.com/geoip/geoip2/geolite2) (country and city) are downloaded at the first startup and updated every week. Geolocation of your users is checked locally on your server.
  * *In the default configuration*, IP addresses of connecting users are sent to https://proxycheck.io/ to check if they're not using a proxy or a VPN.
  * IPs and nicknames associated with them are stored in the local database *(as plain text(!))*.
  * This plugin periodically checks the latest version released in this repository. *This feature can be disabled.*
  * *There is no metrics system or any other kind of data collection.*