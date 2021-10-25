# Minecraft Mapper

This is a Bukkit plugin I coded that can represent any given region of the Earth, specified by a range of longitude and latitude coordinates, in Minecraft. It uses a scale of roughly 1:1000 (each block represents 0.01 degrees of latitude by 0.01 degrees of longitude). The latitude and longitude can be specified by editing the seed in the server.properties file to match the following format: [latmin],[longmin],[latmax],[longmax].

The world generation in Minecraft doesn't quite accurately portray real world features yet; all areas below sea level are filled with water, but there are many land areas below sea level. In contrast, most lakes above sea level become flat surfaces. Biome generation, however, has been improved and now uses PRISM climate data in order to generate more realistic snow levels, as well as generating sand over deserts. However, given that the climate data is of a lower resolution than the elevation data, the transition between biomes is not as smooth.

Regardless, the jar file can be found in the target folder. In order to use the plugin, simply create a Craftbukkit server, create a plugins folder, and move the jar file into the plugins folder. Then, update bukkit.yml and add these three lines to the end:

```
worlds:
  world:
    generator: MinecraftMapper
```

Enjoy!
