package bab.bitsworlds.cmd;

import bab.bitsworlds.BitsWorlds;
import bab.bitsworlds.ChatInput;
import bab.bitsworlds.cmd.impl.BWCommand;
import bab.bitsworlds.extensions.BWCommandSender;
import bab.bitsworlds.extensions.BWPermission;
import bab.bitsworlds.extensions.BWPlayer;
import bab.bitsworlds.gui.*;
import bab.bitsworlds.multilanguage.LangCore;
import bab.bitsworlds.multilanguage.PrefixMessage;
import bab.bitsworlds.utils.WorldUtils;
import bab.bitsworlds.world.BWLoadedWorld;
import bab.bitsworlds.world.BWorld;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InteractWorldCmd implements BWCommand, ImplGUI {
    @Override
    public BWPermission getPermission() {
        return BWPermission.MAINCMD_WORLD_INTERACT;
    }

    @Override
    public void run(BWCommandSender sender, Command cmd, String alias, String[] args) {

    }

    @Override
    public List<String> tabComplete(BWCommandSender sender, Command cmd, String alias, String[] args) {
        return null;
    }

    @Override
    public BWGUI getGUI(String code, BWPlayer player) {
        return new InteractWorldGUI(
                "main_interact",
                5 * 9,
                LangCore.getClassMessage(getClass(), "gui-title").toString(),
                this,
                true,
                player
        );
    }

    @Override
    public void clickEvent(InventoryClickEvent event, BWPlayer player, BWGUI gui) {
        InteractWorldGUI interactWorldGUI = (InteractWorldGUI) gui;


        if (event.getSlot() == 36) {
            player.openGUI(new ListWorldCmd().getGUI("listworld_main", player));
            return;
        }

        if (interactWorldGUI.world instanceof BWLoadedWorld)
            switch (event.getSlot()) {
                case 11:
                    if (player.hasPermission(BWPermission.GAMERULE)) {
                        GameRuleGui gameRuleGui = (GameRuleGui) new GameRuleHandler().getGUI("", player);
                        gameRuleGui.world = (BWLoadedWorld) interactWorldGUI.world;
                        player.openGUI(gameRuleGui.init());
                    }
                    break;
                case 16:
                    if (player.hasPermission(BWPermission.UNLOAD_WITHOUT_SAVE)) {
                        player.getBukkitPlayer().closeInventory();
                        player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "unloading-world-message").setKey("%%s", interactWorldGUI.world.getName()));
                        Bukkit.unloadWorld(((BWLoadedWorld) interactWorldGUI.world).world, false);

                        if (Bukkit.getWorld(interactWorldGUI.world.getName()) != null) {
                            player.sendMessage(PrefixMessage.error.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "world-cant-be-unloaded"));
                            player.openGUI(interactWorldGUI);
                            return;
                        }

                        player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "unloaded-world-message").setKey("%%s", interactWorldGUI.world.getName()));
                        interactWorldGUI.world = WorldUtils.getUnloadedWorld(interactWorldGUI.world.getName());
                        interactWorldGUI.update();
                        player.openGUI(interactWorldGUI);
                    }
                    break;
                case 15:
                    if (player.hasPermission(BWPermission.UNLOAD)) {
                        player.getBukkitPlayer().closeInventory();
                        player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "unloading-world-message").setKey("%%s", interactWorldGUI.world.getName()));
                        Bukkit.unloadWorld(((BWLoadedWorld) interactWorldGUI.world).world, true);

                        if (Bukkit.getWorld(interactWorldGUI.world.getName()) != null) {
                            player.sendMessage(PrefixMessage.error.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "world-cant-be-unloaded"));
                            player.openGUI(interactWorldGUI);
                            return;
                        }

                        player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "unloaded-world-message").setKey("%%s", interactWorldGUI.world.getName()));
                        interactWorldGUI.world = WorldUtils.getUnloadedWorld(interactWorldGUI.world.getName());
                        interactWorldGUI.update();
                        player.openGUI(interactWorldGUI);
                    }
                    break;
                case 25:
                    if (player.hasPermission(BWPermission.SAVE)) {
                        player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "saving-world-message"));
                        ((BWLoadedWorld) interactWorldGUI.world).getWorld().save();
                        player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "world-saved-message"));
                    }
                    break;
                case 33:
                    if (player.hasPermission(BWPermission.BACKUP)) {
                        LocalDateTime localDateTime = LocalDateTime.now();
                        player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "making-backup-message"));
                        try {
                            WorldUtils.copyWorld(
                                    interactWorldGUI.world.getName(),
                                    new File(BitsWorlds.plugin.getDataFolder() + "/backups/" + interactWorldGUI.world.getName() + "." + localDateTime.getDayOfMonth() + "." + localDateTime.getMonthValue() + "." + localDateTime.getYear() + "-" + localDateTime.getHour() + ":" + localDateTime.getMinute() + ":" + localDateTime.getSecond())
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                            player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "backup-error-message"));
                        }
                        player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "maked-backup-message"));
                    }
                    break;
                case 34:
                    if (player.hasPermission(BWPermission.DUPLICATE)) {
                        Bukkit.getScheduler().runTaskAsynchronously(
                                BitsWorlds.plugin,
                                () -> {
                                    player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "duplicate-world-set-name-message"));
                                    player.getBukkitPlayer().closeInventory();

                                    String input = ChatInput.askPlayer(player);

                                    if (input.equals("!")) {
                                        player.openGUI(interactWorldGUI);
                                        return;
                                    }

                                    player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "duplicating-world-message"));

                                    try {
                                        WorldUtils.copyWorld(interactWorldGUI.world.getName(), new File(Bukkit.getWorldContainer() + "/" + input.replace("/", "").replace("\\", "")));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        player.sendMessage(PrefixMessage.error.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "cant-duplicate-world-message"));
                                    }

                                    player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "duplicate-world-message"));
                                    GUICore.updateGUI("listworld_main");
                                    player.openGUI(interactWorldGUI);
                                }
                        );
                    }
                    break;
            }
        else
            switch (event.getSlot()) {
                case 16:
                    if (player.hasPermission(BWPermission.LOAD)) {
                        player.getBukkitPlayer().closeInventory();
                        player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "loading-world-message").setKey("%%s", interactWorldGUI.world.getName()));
                        World world = Bukkit.createWorld(new WorldCreator(interactWorldGUI.world.getName()));

                        if (world == null) {
                            player.sendMessage(PrefixMessage.error.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "world-cant-be-loaded"));
                            player.openGUI(interactWorldGUI);
                            return;
                        }

                        player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "loaded-world-message").setKey("%%s", interactWorldGUI.world.getName()));
                        interactWorldGUI.world = new BWLoadedWorld(world);
                        interactWorldGUI.update();
                        player.openGUI(interactWorldGUI);
                        break;
                    }
            }
    }

    public class InteractWorldGUI extends BWGUI {
        public BWorld world;
        public BWPlayer player;
        public boolean returnItem;

        public InteractWorldGUI(String id, int size, String title, ImplGUI guiClass, boolean updatable, BWPlayer player) {
            super(id, size, title, guiClass, updatable);
            this.player = player;
            returnItem = false;
        }

        @Override
        public void setupItem(int item) {
            if (item == 36) {
                this.setItem(36, new GUIItem(
                        Material.SIGN,
                        ChatColor.GOLD + LangCore.getUtilMessage("back-item-title").toString(),
                        Collections.emptyList(),
                        LangCore.getUtilMessage("back-item-guide-mode"),
                        player
                ));
                returnItem = true;
                return;
            }

            if (world instanceof BWLoadedWorld)
                switch (item) {
                    case 4:
                            World bukWorld = ((BWLoadedWorld) world).getWorld();
                            List<String> description = new ArrayList<>();
                            Material material = Material.GRASS;

                            String dimensionWord = LangCore.getUtilMessage("dimension-word").toString();

                            description.add("");

                            switch (bukWorld.getEnvironment()) {
                                case NORMAL:
                                    description.add(ChatColor.GOLD + dimensionWord + ": " + ChatColor.WHITE + LangCore.getUtilMessage("overworld").toString());
                                    break;
                                case NETHER:
                                    description.add(ChatColor.GOLD + dimensionWord + ": " + ChatColor.WHITE + LangCore.getUtilMessage("nether").toString());
                                    material = Material.NETHERRACK;
                                    break;
                                case THE_END:
                                    description.add(ChatColor.GOLD + dimensionWord + ": " + ChatColor.WHITE + LangCore.getUtilMessage("theend").toString());
                                    material = Material.ENDER_STONE;
                                    break;
                            }

                            description.add(ChatColor.GOLD + LangCore.getClassMessage(ListWorldCmd.class, "players-in").setKey("%%c", ChatColor.WHITE + String.valueOf(bukWorld.getPlayers().size())).toString());
                            description.add(ChatColor.GOLD + LangCore.getClassMessage(MainGUI.class, "status-word").setKey("%%s", ChatColor.GREEN + LangCore.getUtilMessage("loaded-word").toString()).toString());

                            this.setItem(4, new GUIItem(material, world.getName(), description));
                        break;
                    case 11:
                        if (player.hasPermission(BWPermission.GAMERULE)) {
                            this.setItem(11, new GUIItem(
                                    Material.ENCHANTED_BOOK,
                                    ChatColor.GOLD + LangCore.getClassMessage(InteractWorldCmd.class, "gamerule-item-title").toString(),
                                    new ArrayList<>(),
                                    LangCore.getClassMessage(InteractWorldCmd.class, "gamerule-item-guide-mode"),
                                    player
                            ));
                        }
                        break;
                    case 16:
                        if (player.hasPermission(BWPermission.UNLOAD_WITHOUT_SAVE)) {
                            this.setItem(16, new GUIItem(
                                    Material.DIAMOND_HOE,
                                    ChatColor.GOLD + LangCore.getClassMessage(InteractWorldCmd.class, "unload-without-save-item-title").toString(),
                                    new ArrayList<>(GUIItem.loreJumper(
                                            LangCore.getClassMessage(InteractWorldCmd.class, "unload-without-save-item-warn").toString(),
                                            ChatColor.RED.toString(),
                                            ChatColor.RED + "" + ChatColor.BOLD + LangCore.getClassMessage(InteractWorldCmd.class, "unload-without-save-item-warn-prefix").toString()
                                    )),
                                    LangCore.getClassMessage(InteractWorldCmd.class, "unload-without-save-item-guide-mode"),
                                    player
                            ));
                        }
                        break;
                    case 15:
                        if (player.hasPermission(BWPermission.UNLOAD)) {
                            this.setItem(15, new GUIItem(
                                    Material.IRON_HOE,
                                    ChatColor.GOLD + LangCore.getClassMessage(InteractWorldCmd.class, "unload-item-title").toString(),
                                    new ArrayList<>(),
                                    LangCore.getClassMessage(InteractWorldCmd.class, "unload-item-guide-mode"),
                                    player
                            ));
                        }
                        break;
                    case 25:
                        if (player.hasPermission(BWPermission.SAVE)) {
                            this.setItem(25, new GUIItem(
                                    Material.IRON_AXE,
                                    ChatColor.GOLD + LangCore.getClassMessage(InteractWorldCmd.class, "save-world-item-tile").toString(),
                                    new ArrayList<>(),
                                    LangCore.getClassMessage(InteractWorldCmd.class, "save-world-item-guide-mode"),
                                    player
                            ));
                        }
                    case 33:
                        if (player.hasPermission(BWPermission.BACKUP)) {
                            this.setItem(33, new GUIItem(
                                    Material.EYE_OF_ENDER,
                                    ChatColor.GOLD + LangCore.getClassMessage(InteractWorldCmd.class, "backup-item-title").toString(),
                                    new ArrayList<>(),
                                    LangCore.getClassMessage(InteractWorldCmd.class, "backup-item-guide-mode"),
                                    player
                            ));
                        }
                    case 34:
                        if (player.hasPermission(BWPermission.DUPLICATE)) {
                            this.setItem(34, new GUIItem(
                                    Material.ENDER_PEARL,
                                    ChatColor.GOLD + LangCore.getClassMessage(InteractWorldCmd.class, "duplicate-world-item-title").toString(),
                                    new ArrayList<>(),
                                    LangCore.getClassMessage(InteractWorldCmd.class, "duplicate-world-item-guide-mode"),
                                    player
                            ));
                        }
                }
            else
                switch (item) {
                    case 4:
                        this.setItem(4, new GUIItem(Material.STONE, world.getName(), Arrays.asList(
                                "",
                                ChatColor.GOLD + LangCore.getClassMessage(MainGUI.class, "status-word").setKey("%%s", ChatColor.RED + LangCore.getUtilMessage("unloaded-word").toString()).toString()
                        )));
                        break;
                    case 16:
                        if (player.hasPermission(BWPermission.LOAD)) {
                            this.setItem(16, new GUIItem(
                                    Material.GOLD_HOE,
                                    ChatColor.GOLD + LangCore.getClassMessage(InteractWorldCmd.class, "load-world-item-title").toString(),
                                    new ArrayList<>(),
                                    LangCore.getClassMessage(InteractWorldCmd.class, "load-world-item-guide-mode"),
                                    player
                            ));
                            break;
                        }

                }
        }

        @Override
        public BWGUI init() {
            if (world instanceof BWLoadedWorld)
                genItems(4, 11, 15, 16, 25, 33, 34);
            else

                genItems(4, 16);

            return this;
        }

        @Override
        public void update() {
            updateGui();

            GUICore.openGUIs.values().stream()
                    .filter(
                            gui -> {
                                if (!(gui instanceof InteractWorldGUI))
                                    return false;

                                return ((InteractWorldGUI) gui).world.getName().equals(this.world.getName());
                            }
                    ).forEach(gui -> ((InteractWorldGUI) gui).update(this.world));
        }

        public void update(BWorld world) {
            this.world = world;

            updateGui();
        }

        private void updateGui() {
            for (int i = 0; i <= 44; i++)
                this.setItem(i, new ItemStack(Material.AIR));

            init();

            if (returnItem)
                genItems(36);
        }
    }

    public class GameRuleGui extends BWGUI {
        public BWLoadedWorld world;
        public BWPlayer player;
        public List<String> gamerules;

        public GameRuleGui(String id, int size, String title, ImplGUI guiClass, boolean updatable, BWPlayer player) {
            super(id, size, title, guiClass, updatable);
            this.player = player;
        }

        @Override
        public void setupItem(int item) {
            switch (item) {
                case 0:
                    gamerules = new ArrayList<>();
                    int i = 0;
                    for (String gameRule : world.world.getGameRules()) {
                        List<String> description = new ArrayList<>();
                        String value = world.world.getGameRuleValue(gameRule);

                        boolean addEffect = false;

                        if (!value.equals("true") && !value.equals("false")) {
                            description.add(ChatColor.GOLD + LangCore.getClassMessage(InteractWorldCmd.class, "value-word").setKey("%%v", ChatColor.WHITE + value).toString());
                        } else {
                            boolean boolValue = Boolean.valueOf(value);

                            if (boolValue) {
                                description.add(ChatColor.AQUA + LangCore.getUtilMessage("enabled-word").toString());
                                description.add(ChatColor.BLUE + LangCore.getUtilMessage("disabled-word").toString());
                                addEffect = true;

                            } else {
                                description.add(ChatColor.BLUE + LangCore.getUtilMessage("enabled-word").toString());
                                description.add(ChatColor.AQUA + LangCore.getUtilMessage("disabled-word").toString());
                            }
                        }

                        description.add("");
                        description.add(ChatColor.WHITE + LangCore.getClassMessage(InteractWorldCmd.class, "gamerule-click-to-change-value").toString());

                        GUIItem gameruleItem = new GUIItem(
                                Material.ENCHANTED_BOOK,
                                ChatColor.GOLD + gameRule,
                                description
                        );

                        if (addEffect)
                            gameruleItem.addEffect();

                        this.setItem(i, gameruleItem);
                        gamerules.add(i, gameRule);
                        i++;
                    }
                    break;
                case 36:
                    this.setItem(36, new GUIItem(
                            Material.SIGN,
                            ChatColor.GOLD + LangCore.getUtilMessage("back-item-title").toString(),
                            Collections.emptyList(),
                            LangCore.getUtilMessage("back-item-guide-mode"),
                            player
                    ));
                    break;
            }
        }

        @Override
        public BWGUI init() {
            genItems(0, 36);

            return this;
        }

        @Override
        public void update() {
            genItems(0);
        }
    }

    public class GameRuleHandler implements ImplGUI {
        @Override
        public BWGUI getGUI(String code, BWPlayer player) {
            return new GameRuleGui(
                    "interaction_gamerule",
                    5 * 9,
                    LangCore.getClassMessage(InteractWorldCmd.class, "gamerule-item-title").toString(),
                    this,
                    true,
                    player
            );
        }

        @Override
        public void clickEvent(InventoryClickEvent event, BWPlayer player, BWGUI gui) {
            GameRuleGui gameRuleGui = (GameRuleGui) gui;

            if (gameRuleGui.gamerules.size() - 1 >= event.getSlot()) {
                String gamerule = gameRuleGui.gamerules.get(event.getSlot());
                String value = gameRuleGui.world.world.getGameRuleValue(gamerule);

                if (!value.equals("true") && !value.equals("false")) {
                    Bukkit.getScheduler().runTaskAsynchronously(
                            BitsWorlds.plugin,
                            () -> {
                                player.sendMessage(PrefixMessage.info.getPrefix(), LangCore.getClassMessage(InteractWorldCmd.class, "gamerule-set-message"));
                                player.getBukkitPlayer().closeInventory();

                                String input = ChatInput.askPlayer(player);

                                if (input.equals("!")) {
                                    player.openGUI(gameRuleGui);
                                    return;
                                }

                                gameRuleGui.world.world.setGameRuleValue(gamerule, input);
                                GUICore.updateGUI("interaction_gamerule");
                                gameRuleGui.genItems(0);
                                player.openGUI(gameRuleGui);
                            }
                    );
                } else {
                    boolean boolValue = Boolean.valueOf(value);
                    gameRuleGui.world.world.setGameRuleValue(gamerule, String.valueOf(!boolValue));
                    GUICore.updateGUI("interaction_gamerule");
                    gameRuleGui.genItems(0);
                }
            }

            switch (event.getSlot()) {
                case 36:
                    InteractWorldCmd interactWorldCmd = new InteractWorldCmd();
                    InteractWorldCmd.InteractWorldGUI interactGui = (InteractWorldCmd.InteractWorldGUI) interactWorldCmd.getGUI("main", player);
                    interactGui.world = gameRuleGui.world;
                    player.openGUI(interactGui.init());
                    interactGui.genItems(36);
                    break;
            }
        }
    }
}
