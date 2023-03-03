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
    private boolean buttonActionOnClose;
    private boolean buttonActionAsConsole;
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
        if (getConfig().getInt("config") < 4) {
            getLogger().warning("Outdated config file detected! Please delete or update the old config.yml to use all new features");
        }
        showButton = getConfig().getBoolean("showButton", true);
        buttonAction = getConfig().getBoolean("buttonAction", false);
        buttonActionOnClose = getConfig().getBoolean("buttonActionOnClose", false);
        buttonActionAsConsole = getConfig().getBoolean("buttonActionAsConsole", false);
        showOnlyOnce = getConfig().getBoolean("showOnlyOnce", false);
        secondsAfterJoin = getConfig().getInt("secondsAfterJoin", 0) * 20;
        formTitle = getConfig().getString("formTitle", "§2WelcomeForm");
        formText = getConfig().getString("formText", "§eYou can edit this text in config.\n\n\n\n\n\n\n\n\n\n\n\n§r§bPlugin created by §dPetteriM1").replace("%n", "\n");
        buttonText = getConfig().getString("buttonText", "§6Okay").replace("%n", "\n");
        buttonCommand = getConfig().getString("buttonCommand", "");
        if (showOnlyOnce) {
            formRead = new Config(getDataFolder() + "/formRead.yml", Config.YAML).getStringList("formRead");
        }
        if (buttonAction && buttonCommand.isEmpty()) {
            getLogger().warning("[Config] buttonAction is enabled but buttonCommand is empty");
        }
        if (buttonActionOnClose && buttonCommand.isEmpty()) {
            getLogger().warning("[Config] buttonActionOnClose is enabled but buttonCommand is empty");
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
        if (e.getWindow() instanceof FormWindowSimple) {
            Player p = e.getPlayer();
            if (e.getResponse() == null || e.getWindow().wasClosed()) {
                if (buttonActionOnClose && placeholderFunc.apply(formTitle, p).equals(((FormWindowSimple) e.getWindow()).getTitle()))  {
                    getServer().dispatchCommand(buttonActionAsConsole ? getServer().getConsoleSender() : p, buttonCommand.replace("%player%", "\"" + p.getName()) + "\"");
                }
                return;
            }
            if (showButton && buttonAction && placeholderFunc.apply(formTitle, p).equals(((FormWindowSimple) e.getWindow()).getTitle()) &&
                    placeholderFunc.apply(buttonText, p).equals(((FormWindowSimple) e.getWindow()).getResponse().getClickedButton().getText())) {
                getServer().dispatchCommand(buttonActionAsConsole ? getServer().getConsoleSender() : p, buttonCommand.replace("%player%", "\"" + p.getName()) + "\"");
            }
        }
    }

    private void showForm(Player p) {
        FormWindowSimple form = new FormWindowSimple(placeholderFunc.apply(formTitle, p), placeholderFunc.apply(formText, p));
        if (showButton) form.addButton(new ElementButton(placeholderFunc.apply(buttonText, p)));
        p.showFormWindow(form);
    }

    private void initPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info("PlaceholderAPI not found. Placeholders are not available");
            placeholderFunc = (s, p) -> s;
        } else {
            placeholderFunc = (s, p) -> {
                try {
                    return PlaceholderAPI.getInstance().translateString(s, p);
                } catch (Exception e) {
                    getLogger().error("Error with PlaceholderAPI", e);
                    return s;
                }
            };
        }
    }
}
