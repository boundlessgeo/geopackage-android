package mil.nga.geopackage.tiles;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.R;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionFactory;

/**
 * Creates a set of tiles within a GeoPackage by downloading the tiles from a
 * URL
 * 
 * @author osbornb
 */
public class UrlTileGenerator extends TileGenerator {

	/**
	 * URL EPSG pattern for finding the EPSG code in a url
	 */
	private static final Pattern URL_EPSG_PATTERN = Pattern.compile(
			"EPSG:(\\d+)", Pattern.CASE_INSENSITIVE);

	/**
	 * Tile URL
	 */
	private final String tileUrl;

	/**
	 * True if the URL has x, y, or z variables
	 */
	private final boolean urlHasXYZ;

	/**
	 * True if the URL has bounding box variables
	 */
	private final boolean urlHasBoundingBox;

	/**
	 * Projection
	 */
	private Projection urlProjection;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param geoPackage
	 * @param tableName
	 * @param tileUrl
	 * @param minZoom
	 * @param maxZoom
	 */
	public UrlTileGenerator(Context context, GeoPackage geoPackage,
                            String tableName, String tileUrl, int minZoom, int maxZoom) {
        super(context, geoPackage, tableName, minZoom, maxZoom);

		try {
			this.tileUrl = URLDecoder.decode(tileUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new GeoPackageException("Failed to decode tile url: "
					+ tileUrl, e);
		}

		this.urlHasXYZ = hasXYZ(tileUrl);
		this.urlHasBoundingBox = hasBoundingBox(tileUrl);
		if (urlHasBoundingBox) {
			Matcher matcher = URL_EPSG_PATTERN.matcher(tileUrl);
			if (matcher.find()) {
				String epsgString = matcher.group(1);
				long epsg = Long.valueOf(epsgString);
				urlProjection = ProjectionFactory.getProjection(epsg);
			}
		}

		if (!this.urlHasXYZ && !this.urlHasBoundingBox) {
			throw new GeoPackageException(
					"URL does not contain x,y,z or bounding box variables: "
							+ tileUrl);
		}
	}

	/**
	 * Determine if the url has bounding box variables
	 * 
	 * @param url
	 * @return
	 */
	private boolean hasBoundingBox(String url) {

		String replacedUrl = replaceBoundingBox(url, boundingBox);
		boolean hasBoundingBox = !replacedUrl.equals(url);

		return hasBoundingBox;
	}

	/**
	 * Replace x, y, and z in the url
	 * 
	 * @param url
	 * @param z
	 * @param x
	 * @param y
	 * @return
	 */
	private String replaceXYZ(String url, int z, long x, long y) {

		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_z),
				String.valueOf(z));
		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_x),
				String.valueOf(x));
		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_y),
				String.valueOf(y));
		return url;
	}

	/**
	 * Determine if the url has x, y, or z variables
	 * 
	 * @param url
	 * @return
	 */
	private boolean hasXYZ(String url) {

		String replacedUrl = replaceXYZ(url, 0, 0, 0);
		boolean hasXYZ = !replacedUrl.equals(url);

		return hasXYZ;
	}

	/**
	 * Replace the bounding box coordinates in the url
	 * 
	 * @param url
	 * @param z
	 * @param x
	 * @param y
	 * @return
	 */
	private String replaceBoundingBox(String url, int z, long x, long y) {

		BoundingBox boundingBox = TileBoundingBoxUtils.getProjectedBoundingBox(
				urlProjection, x, y, z);

		url = replaceBoundingBox(url, boundingBox);

		return url;
	}

	/**
	 * Replace the url parts with the bounding box
	 * 
	 * @param url
	 * @param boundingBox
	 * @return
	 */
	private String replaceBoundingBox(String url, BoundingBox boundingBox) {

		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_min_lat),
				String.valueOf(boundingBox.getMinLatitude()));
		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_max_lat),
				String.valueOf(boundingBox.getMaxLatitude()));
		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_min_lon),
				String.valueOf(boundingBox.getMinLongitude()));
		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_max_lon),
				String.valueOf(boundingBox.getMaxLongitude()));

		return url;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte[] createTile(int z, long x, long y) {

		byte[] bytes = null;

		String zoomUrl = tileUrl;

		// Replace x, y, and z
		if (urlHasXYZ) {
			zoomUrl = replaceXYZ(zoomUrl, z, x, y);
		}

		// Replace bounding box
		if (urlHasBoundingBox) {
			zoomUrl = replaceBoundingBox(zoomUrl, z, x, y);
		}

		URL url;
		try {
			url = new URL(zoomUrl);
		} catch (MalformedURLException e) {
			throw new GeoPackageException("Failed to download tile. URL: "
					+ zoomUrl + ", z=" + z + ", x=" + x + ", y=" + y, e);
		}

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new GeoPackageException("Failed to download tile. URL: "
						+ zoomUrl + ", z=" + z + ", x=" + x + ", y=" + y);
			}

			InputStream geoPackageStream = connection.getInputStream();
			bytes = GeoPackageIOUtils.streamBytes(geoPackageStream);

		} catch (IOException e) {
			throw new GeoPackageException("Failed to download tile. URL: "
					+ zoomUrl + ", z=" + z + ", x=" + x + ", y=" + y, e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return bytes;
	}

}
