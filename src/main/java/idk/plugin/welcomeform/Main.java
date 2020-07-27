package idk.plugin.welcomeform;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.network.protocol.SetLocalPlayerAsInitializedPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;

public class Main extends PluginBase implements Listener {
    
    boolean button;
    boolean command;
    int delay;
    String title;
    String text;
    String buttonText;
    String buttonCommand;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        if (getConfig().getInt("config") < 2) {
            getLogger().warning("Outdated config file detected! Please delete the old config to use all new features");
        }
        button = getConfig().getBoolean("showButton", true);
        command = getConfig().getBoolean("buttonAction", false);
        delay = getConfig().getInt("secondsAfterJoin", 0) * 20;
        title = getConfig().getString("formTitle", "§aExample").replace("§", "\u00A7").replace("%n", "\n");
        text= getConfig().getString("formText", "§eYou can edit this text in config. %n%n%n%n%n%n%n%n%n%n%n%n §r§bPlugin created by §dPetteriM1").replace("§", "\u00A7").replace("%n", "\n");
        buttonText = getConfig().getString("buttonText", "§6Okay").replace("§", "\u00A7").replace("%n", "\n");
        buttonCommand = getConfig().getString("buttonCommand", "");
    }

    @EventHandler(ignoreCancelled = true)
    public void onDataPk(DataPacketReceiveEvent e) {
        if (e.getPacket() instanceof SetLocalPlayerAsInitializedPacket) {
            Player p = e.getPlayer();
            if (delay <= 0) {
                showForm(p);
            } else {
                new NukkitRunnable() {
                    public void run() {
                        if (p.isOnline()) {
                            showForm(p);
                        }
                    }
                }.runTaskLater(this, delay);
            }
        }
    }

    private void showForm(Player p) {
        FormWindowSimple form = new FormWindowSimple(title, text);
        if (button) form.addButton(new ElementButton(buttonText));
        p.showFormWindow(form);
    }

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent e) {
        if (!button || !command) return;
        if (e.getResponse() == null) return;
        if (e.getWindow().wasClosed()) return;
        if (e.getWindow() instanceof FormWindowSimple) {
            if (title.equals(((FormWindowSimple) e.getWindow()).getTitle())) {
                if (buttonText.equals(((FormWindowSimple) e.getWindow()).getResponse().getClickedButton().getText())) {
                    getServer().dispatchCommand(e.getPlayer(), buttonCommand);
                }
            }
        }
    }
}
