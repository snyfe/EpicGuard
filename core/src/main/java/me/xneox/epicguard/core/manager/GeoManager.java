/*
 * EpicGuard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EpicGuard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package me.xneox.epicguard.core.manager;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import me.xneox.epicguard.core.EpicGuard;
import me.xneox.epicguard.core.util.FileUtils;
import me.xneox.epicguard.core.util.LogUtils;
import me.xneox.epicguard.core.util.TextUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.jetbrains.annotations.NotNull;

/**
 * This class manages the GeoLite2's databases, downloads and updates them if needed. It also
 * contains methods for easy database access.
 */
public final class GeoManager {
  private final EpicGuard epicGuard;

  private DatabaseReader countryReader;
  private DatabaseReader cityReader;

  public GeoManager(EpicGuard epicGuard) {
    this.epicGuard = epicGuard;
    epicGuard.logger().info("This product includes GeoLite2 data created by MaxMind, available from https://www.maxmind.com");

    final var parent = epicGuard.getFolderPath().resolve("data");
    if (Files.notExists(parent)) {
      try {
        Files.createDirectory(parent);
      } catch (IOException e) {
        // ignored
      }
    }

    final var countryDatabase = parent.resolve("GeoLite2-Country.mmdb");
    final var cityDatabase = parent.resolve("GeoLite2-City.mmdb");

    final var countryArchive = parent.resolve("GeoLite2-Country.tar.gz");
    final var cityArchive = parent.resolve("GeoLite2-City.tar.gz");

    try {
      if (epicGuard.config().misc().geoDatabaseDownload()) {
        final String geoIpKey = epicGuard.config().misc().getGeoDatabaseKey();
        if (geoIpKey.isBlank()) {
          return;
        }
        this.downloadDatabase(
                countryDatabase,
                countryArchive,
                "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&license_key="+geoIpKey+"&suffix=tar.gz");
        this.downloadDatabase(
                cityDatabase,
                cityArchive,
                "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City&license_key="+geoIpKey+"&suffix=tar.gz");

      } else {
        epicGuard.logger().info("GeoIP database download is disabled, skipping...");

        // This is to allow initialization of the GeoManager Database without downloading the databases making it independent from 'geoDatabaseDownload' value
        if (!epicGuard.config().misc().geoDatabaseApiCall()) {
          return;
        }
      }

      this.countryReader = new DatabaseReader.Builder(countryDatabase.toFile()).withCache(new CHMCache()).build();
      this.cityReader = new DatabaseReader.Builder(cityDatabase.toFile()).withCache(new CHMCache()).build();
    } catch (IOException ex) {
      LogUtils.catchException("Error while getting the GeoIP databases, please check your internet connection.", ex);
    }
  }

  private void downloadDatabase(@NotNull Path database, @NotNull Path archive, @NotNull String url) throws IOException {
    if (Files.notExists(database) || System.currentTimeMillis() - Files.getLastModifiedTime(database).to(TimeUnit.MILLISECONDS) > TimeUnit.DAYS.toMillis(7L)) {
      // Database does not exist or is outdated, and need to be downloaded.
      this.epicGuard.logger().info("Downloading the GeoIP database file: {}", database.getFileName());
      FileUtils.downloadFile(url, archive);

      this.epicGuard.logger().info("Extracting the database from the tar archive...");
      try (var tarInput = new TarArchiveInputStream(new GZIPInputStream(Files.newInputStream(archive)))) {
        TarArchiveEntry entry;
        while ((entry = tarInput.getNextEntry()) != null) {
          // Extracting the database (.mmdb) database we are looking for.
          if (entry.getName().endsWith(database.getFileName().toString())) {
            Files.copy(tarInput, database, StandardCopyOption.REPLACE_EXISTING);
          }
        }
      }

      Files.delete(archive);
      this.epicGuard.logger().info("Database ({}) has been extracted successfully.", database.getFileName());
    }
  }

  @NotNull
  public String countryCode(final @NotNull String address) {
    final var inetAddress = TextUtils.parseAddress(address);
    if (inetAddress != null && this.countryReader != null) {
      try {
        final String isoCode = this.countryReader.country(inetAddress).getCountry().getIsoCode();
        if (isoCode != null) {
          return isoCode;
        }
      } catch (IOException | GeoIp2Exception ex) {
        this.epicGuard.logger().warn("Couldn't find the country for the address {}: {}", address, ex.getMessage());
      }
    }
    return "unknown";
  }

  @NotNull
  public String city(final @NotNull String address) {
    final var inetAddress = TextUtils.parseAddress(address);
    if (inetAddress != null && this.cityReader != null) {
      try {
        final String city = this.cityReader.city(inetAddress).getCity().getName();
        if (city != null) {
          return city;
        }
      } catch (IOException | GeoIp2Exception ex) {
        this.epicGuard.logger().warn("Couldn't find the city for the address {}: {}", address, ex.getMessage());
      }
    }
    return "unknown";
  }
}
