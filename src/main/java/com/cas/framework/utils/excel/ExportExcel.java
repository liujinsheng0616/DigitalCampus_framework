package com.cas.framework.utils.excel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cas.framework.utils.Encodes;
import com.cas.framework.utils.ReflectionUtil;
import com.cas.framework.utils.excel.annotation.ExcelField;
import com.google.common.collect.Lists;

/**
 * 导出Excel文件（导出“XLSX”格式，支持大数据量导出   @see org.apache.poi.ss.SpreadsheetVersion）
 * @author ThinkGem
 * @version 2013-04-21
 */
public class ExportExcel<T> {
	/**
	 * 隐藏数据源sheet
	 */
	private static String EXCEL_HIDE_SHEET_NAME = "hideDataSheet";  
	private static String HIDE_SHEET_NAME_SEX = "sexList";  
	private static String HIDE_SHEET_NAME_ROLE = "roleList";
	private static String HIDE_SHEET_NAME_GRADE = "gradeList";
	private static String HIDE_SHEET_NAME_SUBJECT = "subjectList";
	
	private static Logger log = LoggerFactory.getLogger(ExportExcel.class);
	/**
	 * 工作薄对象
	 */
	private Workbook wb;
	
	/**
	 * 工作表对象
	 */
	private Sheet sheet;
	
	/**
	 * 样式列表
	 */
	private Map<String, CellStyle> styles;
	
	/**
	 * 当前行号
	 */
	private int rownum;
	
	/**
	 * 注解列表（Object[]{ ExcelField, Field/Method }）
	 */
	List<Object[]> annotationList = Lists.newArrayList();
	
	// 格式化日期
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	

	
	public ExportExcel() {
		super();
	}
	
	/**
	 * 构造函数
	 * @param title 表格标题，传“空值”，表示无标题
	 * @param cls 实体对象，通过annotation.ExportField获取标题
	 */
	public ExportExcel(String title, Class<?> cls){
		this(title, cls, 1);
	}
	
	/**
	 * 构造函数
	 * @param title 表格标题，传“空值”，表示无标题
	 * @param headers 表头数组
	 */
	public ExportExcel(String title, String[] headers) {
		initialize(title, Lists.newArrayList(headers));
	}
	
	/**
	 * 构造函数
	 * @param title 表格标题，传“空值”，表示无标题
	 * @param headerList 表头列表
	 */
	public ExportExcel(String title, List<String> headerList) {
		initialize(title, headerList);
	}
	
	/**
	 * 构造函数
	 * @param title 表格标题，传“空值”，表示无标题
	 * @param headerList 表头列表
	 */
	public ExportExcel(String[] headers, Map<String,List<List<String>>> map) {
		initialize(Lists.newArrayList(headers),map);
	}
	
	/**
	 * 构造函数
	 * @param title 表格标题，传“空值”，表示无标题
	 * @param headerList 表头列表
	 */
	public ExportExcel(String[] headers,String[] posts,String[] gender,LinkedHashMap<String,List<Map<String,List<String>>>> gradeMap) {
		initialize(Lists.newArrayList(headers),Lists.newArrayList(posts),Lists.newArrayList(gender),gradeMap);
	}
	/**
	 * 构造函数
	 * @param title 表格标题，传“空值”，表示无标题
	 * @param cls 实体对象，通过annotation.ExportField获取标题
	 * @param type 导出类型（1:导出数据；2：导出模板）
	 * @param groups 导入分组
	 */
	public ExportExcel(String title, Class<?> cls, int type, int... groups){
		// Get annotation field 
		Field[] fs = cls.getDeclaredFields();
		for (Field f : fs){
			ExcelField ef = f.getAnnotation(ExcelField.class);
			if (ef != null && (ef.type()==0 || ef.type()==type)){
				if (groups!=null && groups.length>0){
					boolean inGroup = false;
					for (int g : groups){
						if (inGroup){
							break;
						}
						for (int efg : ef.groups()){
							if (g == efg){
								inGroup = true;
								annotationList.add(new Object[]{ef, f});
								break;
							}
						}
					}
				}else{
					annotationList.add(new Object[]{ef, f});
				}
			}
		}
		// Get annotation method
		Method[] ms = cls.getDeclaredMethods();
		for (Method m : ms){
			ExcelField ef = m.getAnnotation(ExcelField.class);
			if (ef != null && (ef.type()==0 || ef.type()==type)){
				if (groups!=null && groups.length>0){
					boolean inGroup = false;
					for (int g : groups){
						if (inGroup){
							break;
						}
						for (int efg : ef.groups()){
							if (g == efg){
								inGroup = true;
								annotationList.add(new Object[]{ef, m});
								break;
							}
						}
					}
				}else{
					annotationList.add(new Object[]{ef, m});
				}
			}
		}
		// Field sorting
		Collections.sort(annotationList, new Comparator<Object[]>() {
			public int compare(Object[] o1, Object[] o2) {
				return new Integer(((ExcelField)o1[0]).sort()).compareTo(
						new Integer(((ExcelField)o2[0]).sort()));
			};
		});
		// Initialize
		List<String> headerList = Lists.newArrayList();
		for (Object[] os : annotationList){
			String t = ((ExcelField)os[0]).title();
			// 如果是导出，则去掉注释
			if (type==1){
				String[] ss = StringUtils.split(t, "**", 2);
				if (ss.length==2){
					t = ss[0];
				}
			}
			headerList.add(t);
		}
		initialize(title, headerList);
	}
	
	
	/**
	 * 构造函数
	 * @param title 表格标题，传“空值”，表示无标题
	 * @param headerList 表头列表
	 */
	public ExportExcel(Map<String,List<String>> headerMap, Map<String,List<List<String>>> map) {
		initialize(headerMap,map);
	}
	
	
	private void initialize(Map<String, List<String>> headerMap,
			Map<String, List<List<String>>> map) {
		this.wb = new SXSSFWorkbook(2000);
		this.styles = createStyles(wb);
		if (headerMap == null){
			throw new RuntimeException("headerList not null!");
		}
		for (Map.Entry<String, List<List<String>>> entry : map.entrySet()) {
			String sheetName = entry.getKey();
			List<List<String>> dataList = entry.getValue();// 表格学生列表
			sheet = wb.createSheet(sheetName);
			rownum = 0;
			Row headerRow = sheet.createRow(rownum++);
			headerRow.setHeightInPoints(16);
			List<String> headerList = headerMap.get(sheetName);
			for (int i = 0; i < headerList.size(); i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellStyle(styles.get("header"));
				String[] ss = StringUtils.split(headerList.get(i), "**", 2);
				if (ss.length==2){
					cell.setCellValue(ss[0]);
					Comment comment = this.sheet.createDrawingPatriarch().createCellComment(
							new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
					comment.setString(new XSSFRichTextString(ss[1]));
					cell.setCellComment(comment);
				}else{
					cell.setCellValue(headerList.get(i));
				}
				sheet.autoSizeColumn(i);
			}
			for (int i = 0; i < headerList.size(); i++) {  //设置单元格宽度
				int colWidth = sheet.getColumnWidth(i)*2;
		        sheet.setColumnWidth(i, colWidth < 3000 ? 3000 : colWidth);  
			}
			
			for (int i = 0; i < dataList.size(); i++) {
				Row row = addRow();
				for (int j = 0; j < dataList.get(i).size(); j++) {
					addCell(row, j, dataList.get(i).get(j));
				}
			}
			
		}
		log.debug("Initialize success.");
		
	}

	/**
	 * 初始化函数
	 * @param title
	 * @param map
	 */
	private void initialize(List<String> headerList, Map<String,List<List<String>>> map) {
		this.wb = new SXSSFWorkbook(2000);
		this.styles = createStyles(wb);
		if (headerList == null){
			throw new RuntimeException("headerList not null!");
		}
		for (Map.Entry<String, List<List<String>>> entry : map.entrySet()) {
			String sheetName = entry.getKey();
			List<List<String>> dataList = entry.getValue();// 表格学生列表
			sheet = wb.createSheet(sheetName);
			rownum = 0;
			Row headerRow = sheet.createRow(rownum++);
			headerRow.setHeightInPoints(16);
			for (int i = 0; i < headerList.size(); i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellStyle(styles.get("header"));
				String[] ss = StringUtils.split(headerList.get(i), "**", 2);
				if (ss.length==2){
					cell.setCellValue(ss[0]);
					Comment comment = this.sheet.createDrawingPatriarch().createCellComment(
							new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
					comment.setString(new XSSFRichTextString(ss[1]));
					cell.setCellComment(comment);
				}else{
					cell.setCellValue(headerList.get(i));
				}
				sheet.autoSizeColumn(i);
			}
			for (int i = 0; i < headerList.size(); i++) {  //设置单元格宽度
				int colWidth = sheet.getColumnWidth(i)*2;
		        sheet.setColumnWidth(i, colWidth < 3000 ? 3000 : colWidth);  
			}
			
			for (int i = 0; i < dataList.size(); i++) {
				Row row = addRow();
				for (int j = 0; j < dataList.get(i).size(); j++) {
					addCell(row, j, dataList.get(i).get(j));
				}
			}
			
		}
		
		log.debug("Initialize success.");
		
	}

	/**
	 * 初始化函数
	 * @param title 表格标题，传“空值”，表示无标题
	 * @param headerList 表头列表
	 */
	private void initialize(String title, List<String> headerList) {
		this.wb = new SXSSFWorkbook(2000);
		this.styles = createStyles(wb);
		// Create title
		if (StringUtils.isNotBlank(title)){
			Row titleRow = sheet.createRow(rownum++);
			titleRow.setHeightInPoints(30);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellStyle(styles.get("title"));
			titleCell.setCellValue(title);
			sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(),
					titleRow.getRowNum(), titleRow.getRowNum(), headerList.size()-1));
		}
		// Create header
		if (headerList == null){
			throw new RuntimeException("headerList not null!");
		}
		Row headerRow = sheet.createRow(rownum++);
		headerRow.setHeightInPoints(16);
		for (int i = 0; i < headerList.size(); i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellStyle(styles.get("header"));
			String[] ss = StringUtils.split(headerList.get(i), "**", 2);
			if (ss.length==2){
				cell.setCellValue(ss[0]);
				Comment comment = this.sheet.createDrawingPatriarch().createCellComment(
						new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
				comment.setString(new XSSFRichTextString(ss[1]));
				cell.setCellComment(comment);
			}else{
				cell.setCellValue(headerList.get(i));
			}
			sheet.autoSizeColumn(i);
		}
		for (int i = 0; i < headerList.size(); i++) {  
			int colWidth = sheet.getColumnWidth(i)*2;
	        sheet.setColumnWidth(i, colWidth < 3000 ? 3000 : colWidth);  
		}
		log.debug("Initialize success.");
	}
	
	
	@SuppressWarnings("unchecked")
	public void exportExcel(Map<String, List<T>> dataset,
			OutputStream out) {
		this.wb = new SXSSFWorkbook(2000);
		this.styles = createStyles(wb);
		try {
			for (Map.Entry<String, List<T>> entry : dataset.entrySet()) {
				// 导出标题
				List<String> exportfieldtile = new ArrayList<String>();
				// 导出的字段的get方法
				List<Method> methodObj = new ArrayList<Method>();
				int index = 0;
				String sheetName = entry.getKey();
				List<T> list = entry.getValue();// 表格中学生列表
				// 生成一个表格
				sheet = wb.createSheet(sheetName);
				sheet.setDefaultColumnWidth(20);
				// 产生表格标题行
				Row row = sheet.createRow(0);
				// 取得实际泛型类
				T tObject = list.get(index);
				Class<T> clazz = (Class<T>) tObject.getClass();
				Field[] fileds = tObject.getClass().getDeclaredFields();
				for (Field field : fileds) {
					ExcelField ef = field.getAnnotation(ExcelField.class);
					// 如果设置了annottion
					if (ef != null && (ef.type() == 0 || ef.type() == 1)) {
						String exprot = ef.title();
						// 添加到标题
						exportfieldtile.add(exprot);
						String fieldname = field.getName();
						String getMethodName = "get"
								+ fieldname.substring(0, 1).toUpperCase()
								+ fieldname.substring(1);
						Method getMethod = clazz.getMethod(getMethodName,
								new Class[] {});
						methodObj.add(getMethod);
					}
				}
				for (int i = 0; i < exportfieldtile.size(); i++) {
					Cell cell = row.createCell(i);
					cell.setCellStyle(styles.get("header"));
					HSSFRichTextString text = new HSSFRichTextString(
							exportfieldtile.get(i));
					cell.setCellValue(text);
				}
				for (T t : list) {
					index++;
					row = sheet.createRow(index);
					// 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
					for (int i = 0; i < methodObj.size(); i++) {
						Cell cell = row.createCell(i);
						Method getMethod = methodObj.get(i);
						Object value = getMethod.invoke(t, new Object[] {});
						// 判断值的类型后进行强制类型转换
						String textValue = getValue(value);
						if (textValue != null) {
							Pattern p = Pattern.compile("^//d+(//.//d+)?$");
							Matcher matcher = p.matcher(textValue);
							if (matcher.matches()) {
								// 是数字当作double处理
								cell.setCellValue(Double.parseDouble(textValue));
							} else {
								HSSFRichTextString richString = new HSSFRichTextString(
										textValue);
								cell.setCellStyle(styles.get("data2"));
								cell.setCellValue(richString);
							}
						}

					}
				}

			}
			wb.write(out);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 教师名单
	 * @param headerList
	 * @param postList
	 * @param genderList
	 * @param gradeMap
	 */
	private void initialize(List<String> headerList,
			List<String> postList, List<String> genderList,
			LinkedHashMap<String, List<Map<String, List<String>>>> gradeMap) {
		this.wb = new XSSFWorkbook();
		this.styles = createStyles(wb);
		sheet = wb.createSheet("教师名单");
		if (headerList == null){
			throw new RuntimeException("headerList not null!");
		}
		rownum = 0;
		Row headerRow = sheet.createRow(rownum++);
		headerRow.setHeightInPoints(16);
		for (int i = 0; i < headerList.size(); i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellStyle(styles.get("header"));
			String[] ss = StringUtils.split(headerList.get(i), "**", 2);
			if (ss.length==2){
				cell.setCellValue(ss[0]);
				Comment comment = this.sheet.createDrawingPatriarch().createCellComment(
						new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
				comment.setString(new XSSFRichTextString(ss[1]));
				cell.setCellComment(comment);
			}else{
				cell.setCellValue(headerList.get(i));
			}
			sheet.autoSizeColumn(i);
		}
		for (int i = 0; i < headerList.size(); i++) {  
			int colWidth = sheet.getColumnWidth(i)*2;
	        sheet.setColumnWidth(i, colWidth < 3000 ? 3000 : colWidth);  
		}
		
		creatExcelHidePage(postList,genderList,gradeMap);   
		setDataValidation(wb,2,new int[]{5,6,7});  //设置校验 
		
	}
	
	//设置隐藏数据源信息
	public void creatExcelHidePage(	List<String> postList, List<String> genderList,LinkedHashMap<String, List<Map<String, List<String>>>> gradeMap){
		Sheet hideSheet = wb.createSheet(EXCEL_HIDE_SHEET_NAME);// 隐藏信息
		Row sexRow = hideSheet.createRow(0);
		creatRow(sexRow, genderList);
		// 第二行设置角色信息
		Row postRow = hideSheet.createRow(1);
		creatRow(postRow, postList);
		List<String> gradeList = new ArrayList<String>();
		for (Map.Entry<String, List<Map<String, List<String>>>> entry : gradeMap
				.entrySet()) {
			String gradeName = entry.getKey();
			gradeList.add(gradeName);
		}
		int clzssRowNum = 4;
		// 第三行设置年级信息
		Row gradeRow = hideSheet.createRow(2);
		creatRow(gradeRow, gradeList);
		Set<String> subjectNameSet = new LinkedHashSet<String>();
		for (Map.Entry<String, List<Map<String, List<String>>>> entry : gradeMap
				.entrySet()) {
			String gradeName = entry.getKey();
			//学科信息
			List<Map<String, List<String>>> subjectList = entry.getValue();
			Set<String> clzssNameSet = new LinkedHashSet<String>();
			clzssNameSet.add(gradeName);//第一个单元格加上年级学科的标识
			for (Map<String, List<String>> subjectMap : subjectList) {
				for (Map.Entry<String, List<String>> subjectEntry : subjectMap
						.entrySet()) {
					String subjectName = subjectEntry.getKey();
					subjectNameSet.add(subjectName);//学科名
					clzssNameSet.addAll(subjectEntry.getValue());
				}
			}
			Row clzssRow = hideSheet.createRow(clzssRowNum++);//班级列信息
			creatRow(clzssRow, new ArrayList<String>(clzssNameSet));
		}
		Row subjectRow = hideSheet.createRow(3);//学科列信息
		creatRow(subjectRow, new ArrayList<String>(subjectNameSet));
		
		creatExcelNameList(wb, HIDE_SHEET_NAME_SEX, 1, 1,genderList.size());   
		creatExcelNameList(wb, HIDE_SHEET_NAME_ROLE, 2, 1,postList.size()); 
        creatExcelNameList(wb, HIDE_SHEET_NAME_GRADE, 3, 1,gradeList.size());
        creatExcelNameList(wb, HIDE_SHEET_NAME_SUBJECT, 4 ,1,subjectNameSet.size());
        clzssRowNum = 5;
		for(Map.Entry<String, List<Map<String, List<String>>>> entry : gradeMap
				.entrySet()){
			String gradeName = entry.getKey();
			//学科信息
			Set<String> clzssNameSet = new LinkedHashSet<String>();
			clzssNameSet.add(gradeName);//第一个单元格加上年级学科的标识
			List<Map<String, List<String>>> subjectList = entry.getValue();
			for(Map<String, List<String>> subjectMap : subjectList){  
				for (Map.Entry<String, List<String>> subjectEntry : subjectMap
						.entrySet()) {
					clzssNameSet.addAll(subjectEntry.getValue());
				}
			} 
			creatExcelNameList(wb, gradeName, clzssRowNum++ ,2,clzssNameSet.size());  
		}
		// 设置隐藏页标志
		wb.setSheetHidden(wb.getSheetIndex(EXCEL_HIDE_SHEET_NAME), true);
	}   
	
	/**
	 * 创建一列数据
	 * 
	 * @param currentRow
	 * @param textList
	 */
	public void creatRow(Row currentRow, List<String> textList) {
		if (textList != null && textList.size() > 0) {
			int i = 0;
			for (String cellValue : textList) {
				Cell userNameLableCell = currentRow.createCell(i++);
				userNameLableCell.setCellValue(cellValue);
			}
		}
	}
	
	 /**  
     * 创建一个名称  
     * @param workbook  
     */  
	public void creatExcelNameList(Workbook workbook, String nameCode, int row,
			int start, int end) {
		Name name;
		if (workbook.getName(nameCode) != null) {
			name = workbook.getName(nameCode);
		} else {
			name = workbook.createName();
			name.setNameName(nameCode);
		}
		name.setRefersToFormula(getNamestr(row, start, end));
	} 
    
    public String getNamestr(int row,int start,int end){
        return EXCEL_HIDE_SHEET_NAME+"!"+"$"+CellReference.convertNumToColString(start-1) +"$"+row+":"+"$"+CellReference.convertNumToColString(end-1)+"$"+row;  
   } 
    
    /**
     *  
     * @param wb
     * @param startRow 超始行 
     * @param cols   级联的列号数组 
     */
    public void setDataValidation(Workbook wb,int startRow,int[] cols){   
		int sheetIndex = wb.getNumberOfSheets();
		String[] colNames = getColName(cols);
		if (sheetIndex > 0) {
			Sheet sheet = wb.getSheetAt(0);
			DataValidation data_validation_list = null;
			for (int row = startRow; row < 501; row++) {//设置数据行数，太多会导致excel编辑过慢
				// 性别添加验证数据
				data_validation_list = getDataValidationByFormula(
						HIDE_SHEET_NAME_SEX, row, 3);
				sheet.addValidationData(data_validation_list);
				// 角色验证
				data_validation_list = getDataValidationByFormula(
						HIDE_SHEET_NAME_ROLE, row, 4);
				sheet.addValidationData(data_validation_list);
				// 年级验证
				data_validation_list = getDataValidationByFormula(
						HIDE_SHEET_NAME_GRADE, row, 5);
				sheet.addValidationData(data_validation_list);
				/*for (int i = 0; i < colNames.length - 1; i++) {
					data_validation_list = getDataValidationByFormula(
							"INDIRECT(" + colNames[i] + row + ")", row,
							cols[i] + 1);
					sheet.addValidationData(data_validation_list);
				}*/
				data_validation_list = getDataValidationByFormula(
						HIDE_SHEET_NAME_SUBJECT, row,
						6);
				sheet.addValidationData(data_validation_list);
				data_validation_list = getDataValidationByFormula(
						"INDIRECT(" + colNames[0] + row + ")", row,
						7);
				sheet.addValidationData(data_validation_list);
			}
		}   
    }  
    
	public String[] getColName(int[] colIndex) {
		String[] colName = new String[colIndex.length];
		for (int i = 0; i < colIndex.length; i++) {
			colName[i] = CellReference.convertNumToColString(colIndex[i] - 1);
		}
		return colName;
	}
    
    public DataValidation getDataValidationByFormula(String formulaString,int naturalRowIndex,int naturalColumnIndex){   
    	 //四个参数分别是：起始行、终止行、起始列、终止列     
    	int firstRow = naturalRowIndex-1;   
        int lastRow = naturalRowIndex-1;   
        int firstCol = naturalColumnIndex-1;   
        int lastCol = naturalColumnIndex-1;   
        //加载下拉列表内容     
    	XSSFDataValidationHelper helper = new XSSFDataValidationHelper((XSSFSheet)sheet);  
        DataValidationConstraint constraint = helper.createFormulaListConstraint(formulaString);  
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,lastRow,firstCol,lastCol);  
        DataValidation data_validation_list = helper.createValidation(constraint, regions);  
        
        data_validation_list.setEmptyCellAllowed(false);  
	    // 是否出错警告  
        data_validation_list.setShowErrorBox(true);  
        data_validation_list.setErrorStyle(DataValidation.ErrorStyle.STOP);  
	    sheet.addValidationData(data_validation_list);
        return data_validation_list;
    } 
	
	/**
	 * <p>
	 * getValue方法-cell值处理.
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public String getValue(Object value) {
		String textValue = "";
		if (value == null) {
			return textValue;
		}
		if (value instanceof Boolean) {
			boolean bValue = (Boolean) value;
			textValue = "是";
			if (!bValue) {
				textValue = "否";
			}
		} else if (value instanceof Date) {
			Date date = (Date) value;
			textValue = sdf.format(date);
		} else {
			textValue = value.toString();
		}
		return textValue;
	}

	/**
	 * 创建表格样式
	 * @param wb 工作薄对象
	 * @return 样式列表
	 */
	private Map<String, CellStyle> createStyles(Workbook wb) {
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
		
		CellStyle style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		Font titleFont = wb.createFont();
		titleFont.setFontName("Arial");
		titleFont.setFontHeightInPoints((short) 16);
		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(titleFont);
		styles.put("title", style);

		style = wb.createCellStyle();
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		Font dataFont = wb.createFont();
		dataFont.setFontName("Arial");
		dataFont.setFontHeightInPoints((short) 10);
		style.setFont(dataFont);
		styles.put("data", style);
		
		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(CellStyle.ALIGN_LEFT);
		styles.put("data1", style);

		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(CellStyle.ALIGN_CENTER);
		styles.put("data2", style);

		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(CellStyle.ALIGN_RIGHT);
		styles.put("data3", style);
		
		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
//		style.setWrapText(true);
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font headerFont = wb.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerFont.setColor(IndexedColors.BLUE.getIndex());
		style.setFont(headerFont);
		styles.put("header", style);
		
		return styles;
	}

	/**
	 * 添加一行
	 * @return 行对象
	 */
	public Row addRow(){
		return sheet.createRow(rownum++);
	}
	

	/**
	 * 添加一个单元格
	 * @param row 添加的行
	 * @param column 添加列号
	 * @param val 添加值
	 * @return 单元格对象
	 */
	public Cell addCell(Row row, int column, Object val){
		return this.addCell(row, column, val, 2, Class.class);
	}
	
	/**
	 * 添加一个单元格
	 * @param row 添加的行
	 * @param column 添加列号
	 * @param val 添加值
	 * @param align 对齐方式（1：靠左；2：居中；3：靠右）
	 * @return 单元格对象
	 */
	public Cell addCell(Row row, int column, Object val, int align, Class<?> fieldType){
		Cell cell = row.createCell(column);
		CellStyle style = styles.get("data"+(align>=1&&align<=3?align:""));
		try {
			if (val == null){
				cell.setCellValue("");
			} else if (val instanceof String) {
				cell.setCellValue((String) val);
			} else if (val instanceof Integer) {
				cell.setCellValue((Integer) val);
			} else if (val instanceof Long) {
				cell.setCellValue((Long) val);
			} else if (val instanceof Double) {
				cell.setCellValue((Double) val);
			} else if (val instanceof Float) {
				cell.setCellValue((Float) val);
			} else if (val instanceof Date) {
				DataFormat format = wb.createDataFormat();
	            style.setDataFormat(format.getFormat("yyyy-MM-dd"));
				cell.setCellValue((Date) val);
			} else {
				if (fieldType != Class.class){
					cell.setCellValue((String)fieldType.getMethod("setValue", Object.class).invoke(null, val));
				}else{
					cell.setCellValue((String)Class.forName(this.getClass().getName().replaceAll(this.getClass().getSimpleName(), 
						"fieldtype."+val.getClass().getSimpleName()+"Type")).getMethod("setValue", Object.class).invoke(null, val));
				}
			}
		} catch (Exception ex) {
			log.info("Set cell value ["+row.getRowNum()+","+column+"] error: " + ex.toString());
			cell.setCellValue(val.toString());
		}
		cell.setCellStyle(style);
		return cell;
	}

	/**
	 * 添加数据（通过annotation.ExportField添加数据）
	 * @return list 数据列表
	 */
	public <E> ExportExcel<?> setDataList(List<E> list){
		for (E e : list){
			int colunm = 0;
			Row row = this.addRow();
			StringBuilder sb = new StringBuilder();
			for (Object[] os : annotationList){
				ExcelField ef = (ExcelField)os[0];
				Object val = null;
				// Get entity value
				try{
					if (StringUtils.isNotBlank(ef.value())){
						val = ReflectionUtil.invokeGetter(e, ef.value());
					}else{
						if (os[1] instanceof Field){
							val = ReflectionUtil.invokeGetter(e, ((Field)os[1]).getName());
						}else if (os[1] instanceof Method){
							val = ReflectionUtil.invokeMethod(e, ((Method)os[1]).getName(), new Class[] {}, new Object[] {});
						}
					}
					// If is dict, get dict label
//					if (StringUtils.isNotBlank(ef.dictType())){
//						val = DictUtils.getDictLabel(val==null?"":val.toString(), ef.dictType(), "");
//					}
				}catch(Exception ex) {
					// Failure to ignore
					log.info(ex.toString());
					val = "";
				}
				this.addCell(row, colunm++, val, ef.align(), ef.fieldType());
				sb.append(val + ", ");
			}
			log.debug("Write success: ["+row.getRowNum()+"] "+sb.toString());
		}
		return this;
	}
	
	/**
	 * 输出数据流
	 * @param os 输出数据流
	 */
	public ExportExcel<?> write(OutputStream os) throws IOException{
		wb.write(os);
		return this;
	}
	
	/**
	 * 输出到客户端
	 * @param fileName 输出文件名
	 */
	public ExportExcel<?> write(HttpServletResponse response, String fileName) throws IOException{
		response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename="+Encodes.urlEncode(fileName));
		write(response.getOutputStream());
		return this;
	}
	
	/**
	 * 输出到文件
	 * @param fileName 输出文件名
	 */
	public ExportExcel<?> writeFile(String name) throws FileNotFoundException, IOException{
		FileOutputStream os = new FileOutputStream(name);
		this.write(os);
		return this;
	}
	
	/**
	 * 清理临时文件
	 */
	public ExportExcel<?> dispose(){
		//wb.dispose();
		return this;
	}
	
	/**
	 * 导出测试
	 */
	public static void main(String[] args) throws Throwable {
		
		 
		
	}

}
