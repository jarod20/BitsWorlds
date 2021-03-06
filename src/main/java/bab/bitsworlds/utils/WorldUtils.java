package bab.bitsworlds.utils;

import bab.bitsworlds.world.BWLoadedWorld;
import bab.bitsworlds.world.BWUnloadedWorld;
import bab.bitsworlds.world.BWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldUtils {
    static double minutePerTick = 16.0 + 2.0 / 3.0;

    public static List<BWorld> getWorlds() {
        List<BWorld> worlds = new ArrayList<>();

        Bukkit.getWorlds().forEach(world -> worlds.add(new BWLoadedWorld(world)));
        getStreamUnloadedWorlds().forEach(file -> worlds.add(new BWUnloadedWorld(file)));

        return worlds;
    }

    public static int countWorlds() {
        int i = 0;
        for (World ignored : Bukkit.getWorlds()) {
            i++;
        }

        for (Object ignored : getStreamUnloadedWorlds().toArray()) {
            i++;
        }

        return i;
    }

    public static Stream<File> getStreamUnloadedWorlds() {
        List<String> loadedWorldNames = Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());

        return Arrays.stream(Bukkit.getWorldContainer().listFiles(File::isDirectory))
                .filter(file -> new File(file + "/level.dat").exists())
                .filter(file -> !loadedWorldNames.contains(file.getName()));
    }

    public static List<BWorld> getUnloadedWorlds() {
        List<BWorld> list = new ArrayList<>();
        getStreamUnloadedWorlds().forEach(file -> list.add(new BWUnloadedWorld(file)));
        return list;
    }

    public static List<BWorld> getLoadedWorlds() {
        List<BWorld> list = new ArrayList<>();
        Bukkit.getWorlds().forEach(world -> list.add(new BWLoadedWorld(world)));
        return list;
    }

    public static int countUnloadedWorlds() {
        int i = 0;
        for (Object ignored : getStreamUnloadedWorlds().toArray()) {
            i++;
        }
        return i;
    }

    public static BWUnloadedWorld getUnloadedWorld(String name) {
        return new BWUnloadedWorld(new File(Bukkit.getWorldContainer() + "/" + name));
    }

    public static void copyWorld(String world, File to) throws IOException {
        FileUtils.copyContent(new File(Bukkit.getWorldContainer() + "/" + world), to);
        new File(to + "/uid.dat").delete();
    }

    public static String getValidWorldName(String string) {
        return string.replace("/", "").replace("\\", "").replace(".", "");
    }

    public static String getHours(World world) {
        return getHours(world.getTime());
    }

    public static String getHours(double ticks) {
        BigDecimal minutes = new BigDecimal(ticks / minutePerTick + 360).setScale(0, RoundingMode.FLOOR);

        String string = minutes.remainder(new BigDecimal(60)).toString();

        if (string.length() < 2)
            string = "0" + string;

        BigDecimal hours = minutes.divide(new BigDecimal(60), RoundingMode.FLOOR);

        if (hours.compareTo(new BigDecimal(24)) >= 0)
            hours = hours.subtract(new BigDecimal(24));

        boolean night = false;

        if (hours.compareTo(new BigDecimal(12)) > 0) {
            hours = hours.subtract(new BigDecimal(12));
            night = true;
        }

        return hours.toString() + ":" + string + (night ? " PM" : " AM");
    }

    public static File renameWorld(File world, String newName) {
        File file = new File(world.getParent() + "/" + newName);
        if (world.renameTo(new File(world.getParent() + "/" + newName)))
            return file;
        return null;
    }

    public static boolean isDefaultWorld(World world) {
        World defaultWorld = Bukkit.getWorlds().get(0);

        if (world.getUID().equals(defaultWorld.getUID()))
            return true;

        else if (Bukkit.getAllowNether() && Bukkit.getWorld(defaultWorld.getName() + "_nether").getUID().equals(world.getUID()))
            return true;

        else return Bukkit.getAllowEnd() && Bukkit.getWorld(defaultWorld.getName() + "_the_end").getUID().equals(world.getUID());

    }
}
