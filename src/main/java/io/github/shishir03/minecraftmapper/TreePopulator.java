package io.github.shishir03.minecraftmapper;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class TreePopulator extends BlockPopulator {
    @Override
    public void populate(World world, Random random, Chunk source) {
        int treeCount = (int)(Math.random()*3 + 4);
        for(int i = 0; i < treeCount; i++) {
            int x = (int)(Math.random()*15);
            int z = (int)(Math.random()*15);
            int y = world.getHighestBlockYAt(x, z);

            Location l = source.getBlock(x, y, z).getLocation();
            Biome b = world.getBiome(x, y, z);

            if(b == Biome.FOREST) {
                int a = (int)(Math.random()*5);
                if(a == 0) world.generateTree(l, TreeType.BIG_TREE);
                else if(a == 1) world.generateTree(l, TreeType.BIRCH);
                else world.generateTree(l, TreeType.TREE);
            } else if(b == Biome.GIANT_SPRUCE_TAIGA || b == Biome.SNOWY_TAIGA) {
                world.generateTree(l, TreeType.REDWOOD);
            } else if(b == Biome.PLAINS || b == Biome.SAVANNA){
                int a = (int)(Math.random()*50);
                if(a == 0) world.generateTree(l, TreeType.TREE);
                else if(a == 1) world.generateTree(l, TreeType.BIG_TREE);
            } else if(b == Biome.JUNGLE) {
                int a = (int)(Math.random()*5);
                if(a == 0) world.generateTree(l, TreeType.BIG_TREE);
                else if(a == 1) world.generateTree(l, TreeType.SMALL_JUNGLE);
                else world.generateTree(l, TreeType.TREE);
            }
        }
    }
}
