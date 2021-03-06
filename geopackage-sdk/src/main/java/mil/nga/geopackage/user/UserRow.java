package mil.nga.geopackage.user;

import android.content.ContentValues;

import mil.nga.geopackage.GeoPackageException;

/**
 * User Row containing the values from a single cursor row
 * 
 * @param <TColumn>
 * @param <TTable>
 * 
 * @author osbornb
 */
public abstract class UserRow<TColumn extends UserColumn, TTable extends UserTable<TColumn>>
        extends UserCoreRow<TColumn, TTable>
{

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param columnTypes
	 * @param values
	 */
	protected UserRow(TTable table, int[] columnTypes, Object[] values) {
        super(table, columnTypes, values);
	}

	/**
	 * Constructor to create an empty row
	 * 
	 * @param table
	 */
	protected UserRow(TTable table) {
		super(table);
	}

	/**
	 * Convert the row to content values
	 * 
	 * @return
	 */
	public ContentValues toContentValues() {

		ContentValues contentValues = new ContentValues();
		for (TColumn column : table.getColumns()) {

			if (!column.isPrimaryKey()) {

				Object value = values[column.getIndex()];
				String columnName = column.getName();

				if (value == null) {
					contentValues.putNull(columnName);
				} else {
					columnToContentValue(contentValues, column, value);
				}

			}

		}

		return contentValues;
	}

	/**
	 * Map the column to the content values
	 * 
	 * @param contentValues
	 * @param column
	 * @param value
	 * @return
	 */
	protected void columnToContentValue(ContentValues contentValues,
			TColumn column, Object value) {

		String columnName = column.getName();

		if (value instanceof Number) {
			if (value instanceof Byte) {
				validateValue(column, value, Byte.class, Short.class,
						Integer.class, Long.class);
				contentValues.put(columnName, (Byte) value);
			} else if (value instanceof Short) {
				validateValue(column, value, Short.class, Integer.class,
						Long.class);
				contentValues.put(columnName, (Short) value);
			} else if (value instanceof Integer) {
				validateValue(column, value, Integer.class, Long.class);
				contentValues.put(columnName, (Integer) value);
			} else if (value instanceof Long) {
				validateValue(column, value, Long.class, Double.class);
				contentValues.put(columnName, (Long) value);
			} else if (value instanceof Float) {
				validateValue(column, value, Float.class);
				contentValues.put(columnName, (Float) value);
			} else if (value instanceof Double) {
				validateValue(column, value, Double.class);
				contentValues.put(columnName, (Double) value);
			} else {
				throw new GeoPackageException("Unsupported Number type: "
						+ value.getClass().getSimpleName());
			}
		} else if (value instanceof String) {
			validateValue(column, value, String.class);
			String stringValue = (String) value;
			if (column.getMax() != null
					&& stringValue.length() > column.getMax()) {
				throw new GeoPackageException(
						"String is larger than the column max. Size: "
								+ stringValue.length() + ", Max: "
								+ column.getMax() + ", Column: " + columnName);
			}
			contentValues.put(columnName, stringValue);
		} else if (value instanceof byte[]) {
			validateValue(column, value, byte[].class);
			byte[] byteValue = (byte[]) value;
			if (column.getMax() != null && byteValue.length > column.getMax()) {
				throw new GeoPackageException(
						"Byte array is larger than the column max. Size: "
								+ byteValue.length + ", Max: "
								+ column.getMax() + ", Column: " + columnName);
			}
			contentValues.put(columnName, byteValue);
		} else if (value instanceof Boolean) {
			validateValue(column, value, Boolean.class);
			Boolean booleanValue = (Boolean) value;
			short shortBoolean = booleanValue ? (short) 1 : (short) 0;
			contentValues.put(columnName, shortBoolean);
		} else {
			throw new GeoPackageException(
					"Unsupported update column value. column: " + columnName
							+ ", value: " + value);
		}
	}

}
