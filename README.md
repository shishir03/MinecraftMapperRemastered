# Minecraft Mapper

This is a Bukkit plugin I coded that can represent any given region of the Earth, specified by a range of longitude and latitude coordinates, in Minecraft. It uses a scale of roughly 1:460 (each block represents 1/240 degrees of latitude by 1/240 degrees of longitude) horizontally, and 1:100 on the vertical axis. The latitude and longitude can be specified by editing the seed in the server.properties file to match the following format: [latmin],[longmin],[latmax],[longmax].

The world generation in Minecraft doesn't quite accurately portray real world features yet; some areas below sea level that should be land are filled with water (like parts of Death Valley). In contrast, some lakes and rivers above sea level become flat surfaces. Biome generation is based on PRISM climate data for the location, though since this data is only available for locations in the US, areas outside the US feature a more simplistic biome generation that doesn't take into account the actual temperature and precipitation and is only based on elevation.

The jar file can be found in the target folder. In order to use the plugin, simply create a Craftbukkit server, create a plugins folder, and move the jar file into the plugins folder. Then, update bukkit.yml and add these three lines to the end:

```
worlds:
  world:
    generator: MinecraftMapper
```

Enjoy!
