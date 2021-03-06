# GeoPackage Android

### GeoPackage Android Lib ####

The GeoPackage Libraries were developed at the National Geospatial-Intelligence Agency (NGA) in collaboration with [BIT Systems](https://www.bit-sys.com/index.jsp). The government has "unlimited rights" and is releasing this software to increase the impact of government investments by providing developers with the opportunity to take things in new directions. The software use, modification, and distribution rights are stipulated within the [MIT license](http://choosealicense.com/licenses/mit/).

### Pull Requests ###
If you'd like to contribute to this project, please make a pull request. We'll review the pull request and discuss the changes. All pull request contributions to this project will be released under the MIT license.

Software source code previously released under an open source license and then modified by NGA staff is considered a "joint work" (see 17 USC § 101); it is partially copyrighted, partially public domain, and as a whole is protected by the copyrights of the non-government authors and must be released according to the terms of the original open source license.

### About ###

GeoPackage Android is a SDK implementation of the Open Geospatial Consortium [GeoPackage](http://www.geopackage.org/) [spec](http://www.geopackage.org/spec/).

The GeoPackage SDK provides the ability to manage GeoPackage files providing read, write, import, export, share, and open support. Open GeoPackage files provide read and write access to features and tiles. Feature support includes Well-Known Binary and Google Map shape translations. Tile generation supports creation by URL or features. Tile providers supporting GeoPackage format, Google tile API, and feature tile generation.

### Usage ###

#### GeoPackage MapCache ####

The [GeoPackage MapCache](https://github.com/ngageoint/geopackage-mapcache-android) app provides an extensive standalone example on how to use the SDK.

#### Example ####

    // Context context = ...;
    // File geoPackageFile = ...;
    // GoogleMap map = ...;
    
    // Get a manager
    GeoPackageManager manager = GeoPackageFactory.getManager(context);
    
    // Available databases
    List<String> databases = manager.databases();
    
    // Import database
    boolean imported = manager.importGeoPackage(geoPackageFile);
    
    // Open database
    GeoPackage geoPackage = manager.open(databases.get(0));
    
    // GeoPackage Table DAOs
    SpatialReferenceSystemDao srsDao = geoPackage.getSpatialReferenceSystemDao();
    ContentsDao contentsDao = geoPackage.getContentsDao();
    GeometryColumnsDao geomColumnsDao = geoPackage.getGeometryColumnsDao();
    TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
    TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
    DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();
    DataColumnConstraintsDao dataColumnConstraintsDao = geoPackage.getDataColumnConstraintsDao();
    MetadataDao metadataDao = geoPackage.getMetadataDao();
    MetadataReferenceDao metadataReferenceDao = geoPackage.getMetadataReferenceDao();
    ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();
    
    // Feature and tile tables
    List<String> features = geoPackage.getFeatureTables();
    List<String> tiles = geoPackage.getTileTables();
    
    // Query Features
    String featureTable = features.get(0);
    FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
    GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
            featureDao.getProjection());
    FeatureCursor featureCursor = featureDao.queryForAll();
    try{
        while(featureCursor.moveToNext()){
            FeatureRow featureRow = featureCursor.getRow();
            GeoPackageGeometryData geometryData = featureRow.getGeometry();
            Geometry geometry = geometryData.getGeometry();
            GoogleMapShape shape = converter.toShape(geometry);
            GoogleMapShape mapShape = GoogleMapShapeConverter
                    .addShapeToMap(map, shape);
            // ...
        }
    }finally{
        featureCursor.close();
    }
    
    // Query Tiles
    String tileTable = tiles.get(0);
    TileDao tileDao = geoPackage.getTileDao(tileTable);
    TileCursor tileCursor = tileDao.queryForAll();
    try{
        while(tileCursor.moveToNext()){
            TileRow tileRow = tileCursor.getRow();
            byte[] tileBytes = tileRow.getTileData();
            Bitmap tileBitmap = tileRow.getTileDataBitmap();
            // ...
        }
    }finally{
        tileCursor.close();
    }
    
    // Tile Provider (GeoPackage or Google API)
    TileProvider overlay = GeoPackageOverlayFactory
            .getTileProvider(tileDao);
    TileOverlayOptions overlayOptions = new TileOverlayOptions();
    overlayOptions.tileProvider(overlay);
    overlayOptions.zIndex(-1);
    map.addTileOverlay(overlayOptions);
    
    // Feature Tile Provider
    FeatureTiles featureTiles = new FeatureTiles(context, featureDao);
    TileProvider featureOverlay = new FeatureOverlay(featureTiles);
    TileOverlayOptions featureOverlayOptions = new TileOverlayOptions();
    featureOverlayOptions.tileProvider(featureOverlay);
    featureOverlayOptions.zIndex(-1);
    map.addTileOverlay(featureOverlayOptions);
    
    // URL Tile Generator
    TileGenerator urlTileGenerator = new UrlTileGenerator(context, geoPackage,
                    "url_tile_table", "http://url/{z}/{x}/{y}.png", 2, 7);
    int urlTileCount = urlTileGenerator.generateTiles();
    
    // Feature Tile Generator
    TileGenerator featureTileGenerator = new FeatureTileGenerator(context, geoPackage,
                    featureTable + "_tiles", featureTiles, 10, 15);
    int featureTileCount = featureTileGenerator.generateTiles();
    
    // Close database when done
    geoPackage.close();

### Build ###

The following repositories must be built first (Central Repository Artifacts Coming Soon):
* [GeoPackage WKB Java] (https://github.com/ngageoint/geopackage-wkb-java)
* [GeoPackage Core Java] (https://github.com/ngageoint/geopackage-core-java)

Build this repository using Android Studio and/or Gradle.

#### Project Setup ####

Include as repositories in your project build.gradle:

    repositories {
        jcenter()
        mavenLocal()
    }

##### Normal Build #####

Include the dependency in your module build.gradle with desired version number:

    compile "mil.nga.geopackage.android:geopackage-sdk:1.0.0"
    
As part of the build process, run the "uploadArchives" task on the geopackage-android Gradle script to update the Maven local repository.
    
##### Local Build #####

Replace the normal build dependency in your module build.gradle with:

    compile project(':geopackage-sdk')
    
Include in your settings.gradle:

    include ':geopackage-sdk'
    
From your project directory, link the cloned SDK directory:

    ln -s ../geopackage-android/geopackage-sdk geopackage-sdk

### Remote Dependencies ###

* [GeoPackage Core Java](https://github.com/ngageoint/geopackage-core-java) (The MIT License (MIT)) - GeoPackage Library
* [OrmLite](http://ormlite.com/) (Open Source License) - Object Relational Mapping (ORM) Library
