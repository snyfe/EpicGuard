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

package me.xneox.epicguard.velocity;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.xneox.epicguard.core.EpicGuard;
import me.xneox.epicguard.core.Platform;
import me.xneox.epicguard.core.command.CommandHandler;
import me.xneox.epicguard.core.placeholder.Placeholders;
import me.xneox.epicguard.core.util.Constants;
import me.xneox.epicguard.velocity.listener.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

@Plugin(
        id = "epicguard",
        name = "EpicGuard",
        version = Constants.CURRENT_VERSION,
        description = "A fork of the original Bot protection system for Minecraft servers.",
        url = "https://github.com/snyfe/EpicGuard",
        authors = {"neox", "4drian3d", "snyfe"})
public final class EpicGuardVelocity implements Platform {
    @Inject
    private ProxyServer server;
    @Inject
    private PluginManager pluginManager;
    @Inject
    private Logger logger;
    @Inject
    @DataDirectory
    private Path dataFolder;
    @Inject
    private Injector injector;
    private EpicGuard epicGuard;

    @Subscribe
    public void onEnable(ProxyInitializeEvent e) {
        this.injector.getInstance(Libraries.class).register();
        this.epicGuard = new EpicGuard(this, dataFolder);
        this.injector = injector.createChildInjector(
                binder -> binder.bind(EpicGuard.class).toInstance(epicGuard)
        );

        this.injector.getInstance(CommandRegister.class).register();

        Stream.of(
                PostLoginListener.class,
                PreLoginListener.class,
                DisconnectListener.class,
                ServerPingListener.class,
                PlayerSettingsListener.class
        ).map(injector::getInstance).forEach(Listener::register);

        if (this.pluginManager.isLoaded("miniplaceholders")) {
            Placeholders.register();
        }
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent e) {
        this.epicGuard.shutdown();
    }

    @Override
    public @NotNull String platformVersion() {
        return this.server.getVersion().getName() + " " + this.server.getVersion().getVersion();
    }

    @Override
    public @NotNull Logger logger() {
        return this.logger;
    }

    @Override
    public Audience audience(@NotNull UUID uuid) {
        return this.server.getPlayer(uuid).orElse(null);
    }

    @Override
    public void disconnectUser(@NotNull UUID uuid, @NotNull Component message) {
        this.server.getPlayer(uuid).ifPresent(player -> player.disconnect(message));
    }

    @Override
    public void runTaskLater(@NotNull Runnable task, long seconds) {
        this.server.getScheduler().buildTask(this, task).delay(seconds, TimeUnit.SECONDS).schedule();
    }

    @Override
    public void scheduleRepeatingTask(@NotNull Runnable task, long seconds) {
        this.server.getScheduler().buildTask(this, task).repeat(seconds, TimeUnit.SECONDS).schedule();
    }


    private record CommandRegister(PluginContainer pluginContainer, ProxyServer proxyServer, EpicGuard epicGuard) {
        @Inject
        private CommandRegister {}

        private void register() {
            final VelocityCommandManager<CommandSource> commandManager = new VelocityCommandManager<>(
                    pluginContainer,
                    proxyServer,
                    AsynchronousCommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity()
            );

            new CommandHandler<>(epicGuard, commandManager).register();
        }
    }
}
