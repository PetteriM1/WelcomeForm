package idk.plugin.welcomeform;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;

public class Main extends PluginBase implements Listener {
    
    boolean button;
    int delay;
    String title;
    String text;
    String buttonText;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        button = getConfig().getBoolean("showButton", true);
        delay = getConfig().getInt("secondsAfterJoin", 5);
        title = getConfig().getString("formTitle", "§aExample").replace("§", "\u00A7").replace("%n", "\n");
        text= getConfig().getString("formText", "§eYou can edit this text in config. %n%n%n%n%n%n%n%n%n%n%n%n §r§bPlugin created by §dPetteriM1").replace("§", "\u00A7").replace("%n", "\n");
        buttonText = getConfig().getString("buttonText", "§6Okay").replace("§", "\u00A7").replace("%n", "\n");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        new NukkitRunnable() {
            public void run() {
                FormWindowSimple form = new FormWindowSimple(title, text);
                if (button) form.addButton(new ElementButton(buttonText));
                e.getPlayer().showFormWindow(form);
            }
        }.runTaskLater(this, delay * 20);
    }
}
