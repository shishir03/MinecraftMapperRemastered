# Minecraft Mapper

This is a Bukkit plugin I coded that can represent any given region of the Earth, specified by a range of longitude and latitude coordinates, in Minecraft. It uses a scale of roughly 1:1000 (each block represents 0.01 degrees of latitude by 0.01 degrees of longitude). The latitude and longitude can be specified by editing the seed in the server.properties file to match the following format: [latmin],[longmin],[latmax],[longmax].

The world generation in Minecraft doesn't quite accurately portray real world features yet; all areas below sea level are filled with water, but there are many land areas below sea level. In contrast, most lakes above sea level become flat surfaces. Finally, biome generation is rather simplistic and almost completely dependent on elevations. This results in many desert areas having lush green grass.

Regardless, the jar file can be found in the target folder. In order to use the plugin, simply create a Craftbukkit server, create a plugins folder, and move the jar file into the plugins folder. Then, update bukkit.yml and add these three lines to the end:

```
worlds:
  world:
    generator: MinecraftMapper
```

Enjoy!
