package org.molgenis.data.excel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.molgenis.EntityMetaData;
import org.molgenis.data.AbstractEntity;
import org.molgenis.data.DefaultAttributeMetaData;
import org.molgenis.data.DefaultEntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.excel.ExcelSheetReader.RowIndexEntity;
import org.molgenis.io.processor.AbstractCellProcessor;
import org.molgenis.io.processor.CellProcessor;

/**
 * ExcelSheet Repository implementation
 * 
 * It is assumed that the first row of the sheet is the header row.
 * 
 * All attributes will be of the string type. The cell values are converted to string.
 * 
 * @author erwin
 * 
 */
public class ExcelSheetReader implements Repository<RowIndexEntity>
{
	private final Sheet sheet;

	/** process cells after reading */
	private List<CellProcessor> cellProcessors;
	/** column names index */
	private Map<String, Integer> colNamesMap;
	private DefaultEntityMetaData entityMetaData;

	public ExcelSheetReader(Sheet sheet, List<CellProcessor> cellProcessors)
	{
		if (sheet == null) throw new IllegalArgumentException("sheet is null");
		this.sheet = sheet;
		this.cellProcessors = cellProcessors;
	}

	@Override
	public String getName()
	{
		return sheet.getSheetName();
	}

	public int getNrRows()
	{
		return sheet.getLastRowNum() + 1; // getLastRowNum is 0-based
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(sheet.getSheetName(), EntityMetaData.ROLE_ENTITY);

			if (colNamesMap == null)
			{
				Iterator<Row> it = sheet.iterator();
				if (it.hasNext())
				{
					// First row contains the headers
					colNamesMap = toColNamesMap(it.next());
				}
			}

			if (colNamesMap != null)
			{
				for (String colName : colNamesMap.keySet())
				{
					entityMetaData
							.addAttributeMetaData(new DefaultAttributeMetaData(colName, entityMetaData.getName()));
				}
			}
		}

		return entityMetaData;
	}

	@Override
	public Iterator<RowIndexEntity> iterator()
	{
		final Iterator<Row> it = sheet.iterator();
		if (!it.hasNext()) return Collections.<RowIndexEntity> emptyList().iterator();

		// create column header index once and reuse
		Row headerRow = it.next();
		if (colNamesMap == null)
		{
			colNamesMap = toColNamesMap(headerRow);
		}

		if (!it.hasNext()) return Collections.<RowIndexEntity> emptyList().iterator();

		return new Iterator<RowIndexEntity>()
		{
			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public RowIndexEntity next()
			{
				return new RowIndexEntity(it.next(), colNamesMap, cellProcessors, getEntityMetaData());
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	private Map<String, Integer> toColNamesMap(Row headerRow)
	{
		if (headerRow == null) return null;

		Map<String, Integer> columnIdx = new LinkedHashMap<String, Integer>();
		int i = 0;
		for (Iterator<Cell> it = headerRow.cellIterator(); it.hasNext();)
		{
			try
			{
				String header = AbstractCellProcessor.processCell(it.next().getStringCellValue(), true, cellProcessors);
				columnIdx.put(header, i++);
			}
			catch (final IllegalStateException ex)
			{
				final int row = headerRow.getRowNum();
				final String column = CellReference.convertNumToColString(i);
				throw new IllegalStateException("Invalid value at [" + sheet.getSheetName() + "] " + column + row + 1,
						ex);
			}
		}
		return columnIdx;
	}

	private static String toValue(Cell cell, List<CellProcessor> cellProcessors)
	{
		String value;
		switch (cell.getCellType())
		{
			case Cell.CELL_TYPE_BLANK:
				value = null;
				break;
			case Cell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) value = cell.getDateCellValue().toString();
				else
				{
					// excel stores integer values as double values
					// read an integer if the double value equals the
					// integer value
					double x = cell.getNumericCellValue();
					if (x == Math.rint(x) && !Double.isNaN(x) && !Double.isInfinite(x)) value = String.valueOf((int) x);
					else value = String.valueOf(x);
				}
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				value = String.valueOf(cell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_FORMULA:
				// evaluate formula
				FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
				CellValue cellValue = evaluator.evaluate(cell);
				switch (cellValue.getCellType())
				{
					case Cell.CELL_TYPE_BOOLEAN:
						value = String.valueOf(cellValue.getBooleanValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						// excel stores integer values as double values
						// read an integer if the double value equals the
						// integer value
						double x = cellValue.getNumberValue();
						if (x == Math.rint(x) && !Double.isNaN(x) && !Double.isInfinite(x)) value = String
								.valueOf((int) x);
						else value = String.valueOf(x);
						break;
					case Cell.CELL_TYPE_STRING:
						value = cellValue.getStringValue();
						break;
					case Cell.CELL_TYPE_BLANK:
						value = null;
						break;
					default:
						throw new RuntimeException("unsupported cell type: " + cellValue.getCellType());
				}
				break;
			default:
				throw new RuntimeException("unsupported cell type: " + cell.getCellType());
		}
		return AbstractCellProcessor.processCell(value, false, cellProcessors);
	}

	public static class RowIndexEntity extends AbstractEntity
	{
		private static final long serialVersionUID = 1L;

		private final transient Row row;
		private final Map<String, Integer> colNamesMap;
		private final List<CellProcessor> cellProcessors;

		public RowIndexEntity(Row row, Map<String, Integer> colNamesMap, List<CellProcessor> cellProcessors,
				EntityMetaData entityMetaData)
		{
			super(entityMetaData);

			if (row == null) throw new IllegalArgumentException("row is null");
			if (colNamesMap == null) throw new IllegalArgumentException("column names map is null");
			this.row = row;
			this.colNamesMap = colNamesMap;
			this.cellProcessors = cellProcessors;
		}

		@Override
		public Object get(String attributeName)
		{
			Integer col = colNamesMap.get(attributeName);
			return col != null ? get(col) : null;
		}

		public Object get(int col)
		{
			Cell cell = row.getCell(col);
			return cell != null ? toValue(cell, cellProcessors) : null;
		}

		@Override
		public void set(String attributeName, Object value)
		{
			throw new UnsupportedOperationException();
		}
	}

}
