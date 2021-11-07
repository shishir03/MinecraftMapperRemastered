package io.github.shishir03.minecraftmapper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MinecraftMapperCommandExecutor implements CommandExecutor {
    private final MinecraftMapper m;

    public MinecraftMapperCommandExecutor(MinecraftMapper m) {
        this.m = m;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("getcoords")) {
            if(args.length != 0 || !(sender instanceof Player)) return false;
            Player p = (Player) sender;

            Location l = p.getLocation();
            double z = l.getZ();
            double x = l.getX();
            double y = l.getY();

            double currentLat = m.latMin - z/240;
            double currentLong = m.longMin + x/240;
            double currentElev = (y - 130)*100;

            String msg = "Current coordinates of " + p.getName() + ": " + (Math.round(currentLat*100))/100.0 + " / " +
                    (Math.round(currentLong*100))/100.0 + ", ";

            Location beneath = new Location(l.getWorld(), x, y - 1, z);
            Material blockBeneath = beneath.getBlock().getType();
            if(blockBeneath == Material.AIR || blockBeneath == Material.WATER)
                msg += Math.round(currentElev*100)/100.0 + " m";
            else msg += m.edl.getElevation(currentLat, currentLong) + " m";

            sender.sendMessage(msg);
            return true;
        } else if(command.getName().equalsIgnoreCase("tpcoords")) {
            if(args.length < 2 || args.length > 3 || !(sender instanceof Player)) return false;
            Player p = (Player) sender;

            double newLat = Double.parseDouble(args[0]);
            double newLong = Double.parseDouble(args[1]);
            short newElev = m.edl.getElevation(newLat, newLong);

            double newZ = (m.latMin - newLat)*240;
            double newX = (newLong - m.longMin)*240;
            double newY = Math.ceil(newElev / 100.0) + 129;

            if(args.length == 3) newY = Double.parseDouble(args[2])/100 + 130;

            Location l = new Location(p.getWorld(), newX, newY, newZ);
            p.teleport(l);

            sender.sendMessage("Teleported " + p.getName() + " to " + (Math.round(newLat*100))/100.0 + " / " +
                    (Math.round(newLong*100))/100.0 + ", " + newElev + " m");

            return true;
        }

        return false;
    }
}
