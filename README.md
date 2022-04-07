Minecraft Mapper
===================

This is a Bukkit plugin I coded that can represent any given region of the Earth, specified by a range of longitude and latitude coordinates, in Minecraft. It uses a scale of roughly 1:460 (each block represents 1/240 degrees of latitude by 1/240 degrees of longitude) horizontally, and 1:100 on the vertical axis. The latitude and longitude can be specified by editing the seed in the server.properties file to match the following format: [latmin],[longmin],[latmax],[longmax].

The world generation in Minecraft doesn't quite accurately portray real world features yet; some areas below sea level that should be land are filled with water (like parts of Death Valley). In contrast, some lakes and rivers above sea level become flat surfaces. Biome generation is based on PRISM climate data for the location, though since this data is only available for locations in the US, areas outside the US feature a more simplistic biome generation that doesn't take into account the actual temperature and precipitation and is only based on elevation.

Currently for testing purposes, there are a whole bunch of other big files you need that take a while to download and set up.

Implementation
------------------

The elevation data is acquired from [GEBCO bathymetry data](https://www.gebco.net/data_and_products/gridded_bathymetry_data/). I downloaded and used the netcdf files provided, along with UCAR's netcdf reader library to read the elevation data for the specified region. In order to determine the correct biome for a given location, I used the ACIS API to obtain PRISM climate data for the location (using the 1981-2010 climate averages). I then used the temperature and precipitation to determine the correct biome: cold areas become a tundra; temperate areas could be either a desert, a savanna, or a forest depending on rainfall; and warm areas are either a desert or a jungle.

In order to determine which areas should have water, I used [NASA satellite imagery](https://visibleearth.nasa.gov/) and downloaded some high resolution files of various regions of the Earth during March. The resolution was such that a single pixel corresponds to a block in the Minecraft world (specifically 21600 by 21600). By looking at the color of each pixel, I could see whether it was close enough to the color of the ocean and then determine whether to generate water over that area. This algorithm is still not perfect, but it's still better than simply filling in all areas below sea level with water and leaving everywhere else dry.

Future Tasks
-------------

 - Include city labels
 - Add some more features to the terrain, like trees and shrubs. Right now, even though the ground cover matches the biome, it still looks pretty boring.
 - Figure out a more efficient way of reading the images. My current approach uses a lot of memory in favor of a faster runtime, and more efficient memory usage leads to impractically long runtime. This would also allow me to generate larger areas and potentially the entire world.
 - Find a source for water surface elevation data - right now, bodies of water above sea level have an uneven surface since I'm only generating a single block of water over the ground.
