package mil.nga.geopackage;

import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * A single GeoPackage database connection
 *
 * @author osbornb
 */
public interface GeoPackage extends GeoPackageCore {

    /**
     * Get a Feature DAO from Geometry Columns
     *
     * @param geometryColumns
     * @return
     */
    public FeatureDao getFeatureDao(GeometryColumns geometryColumns);

    /**
     * Get a Feature DAO from Contents
     *
     * @param contents
     * @return
     */
    public FeatureDao getFeatureDao(Contents contents);

    /**
     * Get a Feature DAO from a table name
     *
     * @param tableName
     * @return
     */
    public FeatureDao getFeatureDao(String tableName);

    /**
     * Get a Tile DAO from Tile Matrix Set
     *
     * @param tileMatrixSet
     * @return
     */
    public TileDao getTileDao(TileMatrixSet tileMatrixSet);

    /**
     * Get a Tile DAO from Contents
     *
     * @param contents
     * @return
     */
    public TileDao getTileDao(Contents contents);

    /**
     * Get a Tile DAO from a table name
     *
     * @param tableName
     * @return
     */
    public TileDao getTileDao(String tableName);

}
