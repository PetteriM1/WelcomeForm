package me.petterim1.welcomeform;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerLocallyInitializedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.Config;
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI;

import java.util.List;
import java.util.function.BiFunction;

public class Main extends PluginBase implements Listener {

    private boolean showButton;
    private boolean buttonAction;
    private boolean showOnlyOnce;
    private int secondsAfterJoin;
    private String formTitle;
    private String formText;
    private String buttonText;
    private String buttonCommand;
    private BiFunction<String, Player, String> placeholderFunc;
    private List<String> formRead;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        if (getConfig().getInt("config") < 3) {
            getLogger().warning("Outdated config file detected! Please delete the old config to use all new features");
        }
        showButton = getConfig().getBoolean("showButton", true);
        buttonAction = getConfig().getBoolean("buttonAction", false);
        showOnlyOnce = getConfig().getBoolean("showOnlyOnce", false);
        secondsAfterJoin = getConfig().getInt("secondsAfterJoin", 0) * 20;
        formTitle = getConfig().getString("formTitle", "§2WelcomeForm");
        formText = getConfig().getString("formText", "§eYou can edit this text in config.\n\n\n\n\n\n\n\n\n\n\n\n§r§bPlugin created by §dPetteriM1").replace("%n", "\n");
        buttonText = getConfig().getString("buttonText", "§6Okay").replace("%n", "\n");
        buttonCommand = getConfig().getString("buttonCommand", "");
        if (showOnlyOnce) {
            formRead = new Config(getDataFolder() + "/formRead.yml", Config.YAML).getStringList("formRead");
        }
        initPlaceholders();
    }

    @Override
    public void onDisable() {
        if (showOnlyOnce) {
            Config cfg = new Config(getDataFolder() + "/formRead.yml", Config.YAML);
            cfg.set("formRead", formRead);
            cfg.save(false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSpawned(PlayerLocallyInitializedEvent e) {
        Player p = e.getPlayer();
        if (showOnlyOnce) {
            if (formRead.contains(p.getName())) {
                return;
            } else {
                formRead.add(p.getName());
            }
        }
        if (secondsAfterJoin <= 0) {
            showForm(p);
        } else {
            new NukkitRunnable() {
                public void run() {
                    if (p.isOnline()) {
                        showForm(p);
                    }
                }
            }.runTaskLater(this, secondsAfterJoin);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFormResponse(PlayerFormRespondedEvent e) {
        if (!showButton || !buttonAction) return;
        if (e.getResponse() == null) return;
        if (e.getWindow().wasClosed()) return;
        if (e.getWindow() instanceof FormWindowSimple) {
            Player p = e.getPlayer();
            if (placeholderFunc.apply(formTitle, p).equals(((FormWindowSimple) e.getWindow()).getTitle())) {
                if (placeholderFunc.apply(buttonText, p).equals(((FormWindowSimple) e.getWindow()).getResponse().getClickedButton().getText())) {
                    getServer().dispatchCommand(p, buttonCommand.replace("%player%", "\"" + p.getName()) + "\"");
                }
            }
        }
    }

    private void showForm(Player p) {
        FormWindowSimple form = new FormWindowSimple(placeholderFunc.apply(formTitle, p), placeholderFunc.apply(formText, p));
        if (showButton) form.addButton(new ElementButton(placeholderFunc.apply(buttonText, p)));
        p.showFormWindow(form);
    }

    private void initPlaceholders() {
        try {
            Class<?> placeholderAPI = Class.forName("com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI");
            placeholderFunc = (s, p) -> {
                try {
                    return (String) PlaceholderAPI.getInstance().translateString(s, p);
                } catch (Exception e) {
                    getLogger().error("Error with PlaceholderAPI", e);
                    return s;
                }
            };
        } catch (ClassNotFoundException ignored) {
            placeholderFunc = (s, p) -> s;
        }
    }
}
