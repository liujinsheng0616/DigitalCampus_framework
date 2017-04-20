package com.cas.framework.utils.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.cas.framework.utils.ReflectionUtil;
import com.cas.framework.utils.excel.annotation.ExcelField;
import com.google.common.collect.Lists;

/**
 * 导入Excel文件（支持“XLS”和“XLSX”格式）
 * 
 * @author ThinkGem
 * @version 2013-03-10
 */
public class ImportExcel {

	private static Logger log = LoggerFactory.getLogger(ImportExcel.class);
	/**
	 * 工作薄对象
	 */
	private Workbook wb;

	/**
	 * 工作表对象
	 */
	private List<Sheet> sheetList = new ArrayList<Sheet>();

	/**
	 * 标题行号
	 */
	private int headerNum;

	/**
	 * 工作表对象
	 */
	private String[] sheetName;

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            导入文件，读取第一个工作表
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(String fileName, int headerNum)
			throws InvalidFormatException, IOException {
		this(new File(fileName), headerNum);
	}

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            导入文件对象，读取第一个工作表
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(File file, int headerNum) throws InvalidFormatException,
			IOException {
		this(file.getName(), new FileInputStream(file), headerNum);
	}

	/**
	 * 构造函数
	 * 
	 * @param file
	 *            导入文件对象
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(MultipartFile multipartFile, int headerNum)
			throws InvalidFormatException, IOException {
		this(multipartFile.getOriginalFilename(), multipartFile
				.getInputStream(), headerNum);
	}

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            导入文件对象
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(String fileName, InputStream is, int headerNum)
			throws InvalidFormatException, IOException {
		if (StringUtils.isBlank(fileName)) {
			throw new RuntimeException("导入文档为空!");
		} else if (fileName.toLowerCase().endsWith("xls")) {
			this.wb = new HSSFWorkbook(is);
		} else if (fileName.toLowerCase().endsWith("xlsx")) {
			this.wb = new XSSFWorkbook(is);
		} else {
			throw new RuntimeException("文档格式不正确!");
		}
		if (this.wb.getNumberOfSheets() < 1) {
			throw new RuntimeException("文档中没有工作表!");
		}
		if(headerNum==0)
			this.sheetList.add(this.wb.getSheetAt(0));
		else{
			for (int i = 0; i < this.wb.getNumberOfSheets(); i++) {
				this.sheetList.add(this.wb.getSheetAt(i));
			}
		}
		
		this.headerNum = headerNum;
		log.debug("Initialize success.");
	}

	/**
	 * 获取数据行号
	 * 
	 * @return
	 */
	public int getDataRowNum() {
		return headerNum;
	}

	/**
	 * 获取数据行号
	 * 
	 * @return
	 */
	public int getSheetNum() {
		return sheetList.size();
	}

	/**
	 * 获取单元格值
	 * 
	 * @param row
	 *            获取的行
	 * @param column
	 *            获取单元格列号
	 * @return 单元格值
	 */
	public Object getCellValue(Row row, int column) {
		Object val = "";
		try {
			Cell cell = row.getCell(column);
			if (cell != null) {
				if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					val = cell.getNumericCellValue();
				} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
					val = cell.getStringCellValue();
				} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
					val = cell.getCellFormula();
				} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
					val = cell.getBooleanCellValue();
				} else if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
					val = cell.getErrorCellValue();
				}
			}
		} catch (Exception e) {
			return val;
		}
		return val;
	}

	// public <T> Collection<T> importExcel(File file, String...pattern) {
	public <T> List<T> getDataList(Class<T> clazz)
			throws InstantiationException, IllegalAccessException {
		try {
			/**
			 * 类反射得到调用方法
			 */
			// 得到目标目标类的所有的字段列表
			Field[] fields = clazz.getDeclaredFields();
			// 将所有标有Annotation的字段，也就是允许导入数据的字段,放入到一个map中
			// 循环读取所有字段
			Map<String, Method> fieldMap = new HashMap<String, Method>();
			// 循环读取所有字段
			for (Field field : fields) {
				// 得到单个字段上的Annotation
				ExcelField ef = field.getAnnotation(ExcelField.class);
				// 如果标识了Annotationd
				if (ef != null && (ef.type() == 0 || ef.type() == 2)) {
					String fieldName = field.getName();
					// 构造设置了Annotation的字段的Setter方法
					String setMethodName = "set"
							+ fieldName.substring(0, 1).toUpperCase()
							+ fieldName.substring(1);
					// 构造调用的method
					Method setMethod = clazz.getMethod(setMethodName,
							new Class[] { field.getType() });
					// 将这个method以Annotaion的名字为key来存入
					fieldMap.put(ef.title(), setMethod);
				}
			}
			DecimalFormat df = new DecimalFormat("0");
			List<T> dataList = Lists.newArrayList();
			sheetName = new String[sheetList.size()];
			for (int index = 0; index < sheetList.size(); index++) {
				Sheet sheet = sheetList.get(index);
				sheetName[index] = sheet.getSheetName();
				Iterator<Row> row = sheet.rowIterator();
				// 标题
				Row titleRow = row.next();
				while (row.hasNext()) {
					// 标题下的第一行
					Row rown = row.next();
					// 行的所有列
					Iterator<Cell> cellBody = rown.cellIterator();
					T tObject = (T) clazz.newInstance();
					while (cellBody.hasNext()) {
						Cell cell = (Cell) cellBody.next();
						if (cell.getColumnIndex() >= titleRow.getLastCellNum() || StringUtils.isEmpty(getCellFormatValue(cell))) {
							break;
						}
						// 这里得到此列的对应的标题
						String titleString = titleRow.getCell(
								cell.getColumnIndex()).getStringCellValue();
						// 如果这一列的标题和类中的某一列的Annotation相同，那么则调用此类的的set方法，进行设值
						if (fieldMap.containsKey(titleString)) {
							Method setMethod = fieldMap.get(titleString);
							// 得到setter方法的参数
							Type[] types = setMethod.getGenericParameterTypes();
							// 只要一个参数
							String xclass = String.valueOf(types[0]);
							if ("class java.lang.String".equals(xclass)) {
								if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									setMethod.invoke(tObject,
											cell.getStringCellValue());
								} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									// 判断参数类型
									setMethod.invoke(tObject, df.format(cell
											.getNumericCellValue()));
									// setMethod.invoke(tObject,String.valueOf(cell.getNumericCellValue()));
								}
							} else if ("class java.lang.Integer".equals(xclass)) {
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									setMethod
											.invoke(tObject,
													new Integer(
															String.valueOf((int) cell
																	.getNumericCellValue())));
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									setMethod.invoke(
											tObject,
											new Integer(cell
													.getStringCellValue()));
								}
							} else if ("class java.lang.Long".equals(xclass)) {
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									setMethod.invoke(
											tObject,
											new Long(String.valueOf((long) cell
													.getNumericCellValue())));
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									setMethod
											.invoke(tObject,
													new Long(
															cell.getStringCellValue()));
								}
							} else if ("class java.math.BigDecimal"
									.equals(xclass)) {
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									setMethod
											.invoke(tObject,
													new BigDecimal(
															String.valueOf((double) cell
																	.getNumericCellValue())));
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									setMethod.invoke(tObject, new BigDecimal(
											cell.getStringCellValue()));
								}
							} else if ("int".equals(xclass)) {
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									setMethod
											.invoke(tObject,
													new Integer(
															String.valueOf((int) cell
																	.getNumericCellValue())));
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									setMethod.invoke(
											tObject,
											new Integer(cell
													.getStringCellValue()));
								}
							} else if ("class java.util.Date".equals(xclass)) {
								setMethod.invoke(tObject,
										cell.getDateCellValue());
							} else if ("class java.lang.Boolean".equals(xclass)) {
								Boolean boolName = true;
								if ("否".equals(cell.getStringCellValue())) {
									boolName = false;
								}
								setMethod.invoke(tObject, boolName);
							}
						}
					}
					ReflectionUtil.invokeSetter(tObject, "gradeName",
							sheet.getSheetName());
					ReflectionUtil.invokeSetter(tObject, "gradeIndex",
							index + 1);
					dataList.add(tObject);
				}
			}
			return dataList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public <T> Map<String, List<T>> getDataMap(Class<T> clazz)
			throws InstantiationException, IllegalAccessException {
		try {
			/**
			 * 类反射得到调用方法
			 */
			// 得到目标目标类的所有的字段列表
			Field[] fields = clazz.getDeclaredFields();
			// 将所有标有Annotation的字段，也就是允许导入数据的字段,放入到一个map中
			// 循环读取所有字段
			Map<String, Method> fieldMap = new HashMap<String, Method>();
			Method setMethod = null;
			// 循环读取所有字段
			for (Field field : fields) {
				// 得到单个字段上的Annotation
				ExcelField ef = field.getAnnotation(ExcelField.class);
				// 如果标识了Annotationd
				if (ef != null && (ef.type() == 0 || ef.type() == 2)) {
					String fieldName = field.getName();
					// 构造设置了Annotation的字段的Setter方法
					String setMethodName = "set"
							+ fieldName.substring(0, 1).toUpperCase()
							+ fieldName.substring(1);
					// 构造调用的method
					setMethod = clazz.getMethod(setMethodName,
							new Class[] { field.getType() });
					// 将这个method以Annotaion的名字为key来存入
					fieldMap.put(ef.title(), setMethod);
				}
			}
			Map<String, List<T>> dataMap = new HashMap<String, List<T>>();
			sheetName = new String[sheetList.size()];
			for (int index = 0; index < sheetList.size(); index++) {
				Sheet sheet = sheetList.get(index);
				sheetName[index] = sheet.getSheetName();
				List<T> dataList = Lists.newArrayList();
				Iterator<Row> row = sheet.rowIterator();
				if(!row.hasNext())
					break;
				// 标题
				Row titleRow = row.next();
				while (row.hasNext()) {
					// 标题下的第一行
					Row rown = row.next();
					// 行的所有列
					Iterator<Cell> cellBody = rown.cellIterator();
					T tObject = (T) clazz.newInstance();
					while (cellBody.hasNext()) {
						Cell cell = (Cell) cellBody.next();
						if (cell.getColumnIndex() >= titleRow.getLastCellNum()) {
							break;
						}
						// 这里得到此列的对应的标题
						String titleString = titleRow.getCell(
								cell.getColumnIndex()).getStringCellValue();
						/*
						 * if(cell.getColumnIndex() == 1 &&
						 * StringUtil.isEmpty(titleString)){ break; }
						 */
						// 如果这一列的标题和类中的某一列的Annotation相同，那么则调用此类的的set方法，进行设值
						if (fieldMap.containsKey(titleString)) {
							setMethod = fieldMap.get(titleString);
							// 得到setter方法的参数
							Type[] types = setMethod.getGenericParameterTypes();
							// 只要一个参数
							String xclass = String.valueOf(types[0]);
							// 判断参数类型
							DecimalFormat df = new DecimalFormat("0");
							if ("class java.lang.String".equals(xclass)) {
								if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									setMethod.invoke(tObject,
											cell.getStringCellValue().trim());
								} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									setMethod.invoke(tObject, df.format(cell
											.getNumericCellValue()));
									// setMethod.invoke(tObject,String.valueOf(cell.getNumericCellValue()));
								}
							} else if ("class java.lang.Integer".equals(xclass)) {
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									setMethod
											.invoke(tObject,
													new Integer(
															String.valueOf((int) cell
																	.getNumericCellValue())));
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									setMethod.invoke(
											tObject,
											new Integer(cell
													.getStringCellValue()));
								}
							} else if ("class java.lang.Long".equals(xclass)) {
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									setMethod.invoke(
											tObject,
											new Long(String.valueOf((long) cell
													.getNumericCellValue())));
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									setMethod
											.invoke(tObject,
													new Long(
															cell.getStringCellValue()));
								}
							} else if ("class java.math.BigDecimal"
									.equals(xclass)) {
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									setMethod
											.invoke(tObject,
													new BigDecimal(
															String.valueOf((double) cell
																	.getNumericCellValue())));
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									setMethod.invoke(tObject, new BigDecimal(
											cell.getStringCellValue()));
								}
							} else if ("class java.lang.Double".equals(xclass)) {
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									setMethod
											.invoke(tObject,
													new Double(
															String.valueOf((Double) cell
																	.getNumericCellValue())));
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									setMethod.invoke(
											tObject,
											new Integer(cell
													.getStringCellValue()));
								}
							} else if ("class java.util.Date".equals(xclass)) {
								setMethod.invoke(tObject,
										cell.getDateCellValue());
							} else if ("class java.lang.Boolean".equals(xclass)) {
								Boolean boolName = true;
								if ("否".equals(cell.getStringCellValue())) {
									boolName = false;
								}
								setMethod.invoke(tObject, boolName);
							}
						}
					}
					ReflectionUtil.invokeSetter(tObject, "gradeName",
							sheet.getSheetName());
					ReflectionUtil.invokeSetter(tObject, "gradeIndex",
							index + 1);
					dataList.add(tObject);
				}
				dataMap.put(sheet.getSheetName(), dataList);
			}
			return dataMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 返回excel标题头
	 * 
	 * @param is
	 * @return
	 */
	public Map<String, List<Object>> getExcelTitle() {
		Map<String, List<Object>> map = new HashMap<String, List<Object>>();
		for (Sheet sheet : sheetList) {
			Row row = sheet.getRow(0);
			if(null!=row){
				// 标题总列数
				int colNum = row.getPhysicalNumberOfCells();
				List<Object> list = new ArrayList<Object>();
				for (int i = 0; i < colNum; i++) {
					String title = getCellFormatValue(row.getCell((short) i)).trim();
					if(StringUtils.isNotEmpty(title))
						list.add(title);
				}
				map.put(sheet.getSheetName(), list);
			}
		}
		return map;
	}

	/**
	 * 返回除标题外的其他行数据
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, List<Object>> getExcelData() {
		Map<String, List<Object>> map = new HashMap<String, List<Object>>();
		for (Sheet sheet : sheetList) {
			Row row = sheet.getRow(0);
			int rowNum = sheet.getLastRowNum();// 总行数
			int colNum = row.getPhysicalNumberOfCells();// 标题总列数
			List<Object> list = new ArrayList<Object>();
			for (int i = 1; i <= rowNum; i++) {
				List obj = new ArrayList();;
				row = sheet.getRow(i);
				int j = 0;
				while (j < colNum) {
					String title = getCellFormatValue(row.getCell((short) j)).trim();
					obj.add(title);
					j++;
				}
				list.add(obj);
			}
			map.put(sheet.getSheetName(), list);
		}
		return map;
	}

	public String getCellFormatValue(Cell cell) {
		String cellvalue = "";
		if (cell != null) {
			// 判断当前Cell的Type
			switch (cell.getCellType()) {
			// 如果当前Cell的Type为NUMERIC
			case Cell.CELL_TYPE_NUMERIC:
			case Cell.CELL_TYPE_FORMULA: {
				// 判断当前的cell是否为Date
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					// 如果是Date类型则，转化为Data格式

					// 方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
					// cellvalue = cell.getDateCellValue().toLocaleString();

					// 方法2：这样子的data格式是不带带时分秒的：2011-10-12
					Date date = cell.getDateCellValue();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					cellvalue = sdf.format(date);

				} else {
					// 取得当前Cell的数值
					DecimalFormat df = new DecimalFormat("0");
					cellvalue = df.format(cell.getNumericCellValue());
				}
				break;
			}
			// 如果当前Cell的Type为STRIN
			case Cell.CELL_TYPE_STRING:
				// 取得当前的Cell字符串
				cellvalue = cell.getRichStringCellValue().getString();
				break;
			// 默认的Cell值
			default:
				cellvalue = " ";
			}
		} else {
			cellvalue = "";
		}
		return cellvalue;

	}

	public String[] getSheetName() {
		return sheetName;
	}

	public void setSheetName(String[] sheetName) {
		this.sheetName = sheetName;
	}

	public static void main(String[] args) throws Exception {
		ImportExcel ie = new ImportExcel("D:\\test2.xlsx", 1);
		Map<String, List<Object>> map = ie.getExcelTitle();
		System.out.println(map);
	}
}
