package idk.plugin.welcomeform;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;

public class Main extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        getConfig();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        new NukkitRunnable() {
            public void run() {
                e.getPlayer().showFormWindow(new FormWindowSimple(getConfig().getString("formTitle"), getConfig().getString("formText")));
            }
        }.runTaskLater(this, getConfig().getInt("secondsAfterJoin") * 20);
    }
}
