package cn.pings.commons.util.excel;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static java.util.stream.Collectors.*;

/**
 *********************************************************
 ** @desc  ： excel导入/导出工具类
 ** @author  Pings
 ** @date    2017/12/25
 ** @version v1.0
 * *******************************************************
 */
public class ExcelUtil {

    /**
     *********************************************************
     ** @desc ： 读取Sheet表格
     ** @author Pings
     ** @date   2017/12/26
     ** @param  sheet 单个表格
     ** @return 单个表格数据
     * *******************************************************
     */
    private static List<String[]> parseSheet(Sheet sheet) {
//        return StreamSupport.stream(sheet.spliterator(), false)
//                .map(row -> StreamSupport.stream(row.spliterator(), false).map(ExcelUtil::getCellValue).toArray(String[]::new))
//                .collect(toList());

        int column = sheet.getRow(0).getPhysicalNumberOfCells();

        return StreamSupport.stream(sheet.spliterator(), false)
                .map(row -> Stream.iterate(0, i -> i + 1).limit(column).map(i -> ExcelUtil.getCellValue(row.getCell(i))).toArray(String[]::new))
                .collect(toList());

    }

    /**
     *********************************************************
     ** @desc ： 获取Cell单元格
     ** @author Pings
     ** @date   2018/11/21
     ** @param  cell   单元格
     ** @return 单元格数据
     * *******************************************************
     */
    private static String getCellValue(Cell cell) {
        if(cell == null) return "";
        String rst;

        switch (cell.getCellTypeEnum()){
            case STRING: rst = cell.getStringCellValue(); break;
            case NUMERIC: {
                    if(HSSFDateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        return sdf.format(cell.getDateCellValue());
                    } else {
                        cell.setCellType(CellType.STRING);
                        rst = cell.getStringCellValue();
                    }
            } break;
            case BOOLEAN: rst = String.valueOf(cell.getBooleanCellValue()); break;
            case FORMULA: rst = String.valueOf(cell.getCellFormula()); break;
            case BLANK: rst = ""; break;
            case ERROR: rst = "error"; break;

            default: rst = ""; break;
        }

        return rst;
    }

    /**
     *********************************************************
     ** @desc ： 读取excel文件
     ** @author Pings
     ** @date   2017/12/26
     ** @param  is 输入流
     ** @return 多个excel表格数据
     * *******************************************************
     */
    public static List<List<String[]>> parseExcel(InputStream is) {
        return open(is, workbook -> StreamSupport.stream(workbook.spliterator(), true).map(ExcelUtil::parseSheet).collect(toList()));
    }

    /**
     *********************************************************
     ** @desc ： 读取只包含一个表的excel文件
     ** @author Pings
     ** @date   2017/12/26
     ** @param  is 输入流
     ** @return 单个excel表格数据
     * *******************************************************
     */
    public static List<String[]> parseSingleExcel(InputStream is) {
        return open(is, workbook -> parseSheet(workbook.getSheetAt(0)));
    }

    /**
     *********************************************************
     ** @desc ： 打开excel文件
     ** @author Pings
     ** @date   2017/12/26
     ** @param  is   输入流
     ** @param  func 处理函数
     ** @return excel数据
     * *******************************************************
     */
    private static <R> R open(InputStream is, Function<Workbook, R> func) {
        try(Workbook workbook = WorkbookFactory.create(is)){

            return func.apply(workbook);
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *********************************************************
     ** @desc ： 创建只包含一个表的excel（.xlsx文件）
     ** @author Pings
     ** @date   2017/12/26
     ** @param  dataList  写入excel表的数据
     ** @param  out       输出流
     * *******************************************************
     */
    public static void createSingleExcel(List<Object[]> dataList, OutputStream out) {
        create(out, workbook -> writeSheet(workbook.createSheet(), dataList));
    }

    /**
     *********************************************************
     ** @desc ： 创建excel（.xlsx文件）
     ** @author Pings
     ** @date   2017/12/26
     ** @param  dataList  写入excel表的数据
     ** @param  out       输出流
     * *******************************************************
     */
    public static void createExcel(List<List<Object[]>> dataList, OutputStream out) {
        create(out, workbook -> dataList.parallelStream().forEach(newDataList -> writeSheet(workbook.createSheet(), newDataList)));
    }

    /**
     *********************************************************
     ** @desc ： 把数据写入Sheet表
     ** @author Pings
     ** @date   2017/12/26
     ** @param  sheet    单个表格
     ** @param  dataList 写入的数据
     * *******************************************************
     */
    private static void writeSheet(Sheet sheet, List<Object[]> dataList) {
        //**计数器
        final List<Integer> index = Arrays.asList(-1, -1);

        dataList.stream().forEach(dataArray -> {
            index.set(0, index.get(0) + 1);   //**行计数器
            Row row = sheet.createRow(index.get(0));

            index.set(1, -1);
            Arrays.stream(dataArray).forEach(data -> {
                index.set(1, index.get(1) + 1);  //**列计数器
                setCellObject(row.createCell(index.get(1)), data);
            });
        });
    }

    /**
     *********************************************************
     ** @desc ： 创建excel（.xlsx文件）
     ** @author Pings
     ** @date   2017/12/26
     ** @return excel文件
     * *******************************************************
     */
    private static void create(OutputStream out, Consumer<Workbook> consumer) {
        try(Workbook workbook = new XSSFWorkbook()){

            consumer.accept(workbook);
            workbook.write(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *********************************************************
     ** @desc ： 设置单元格的值
     ** @author Pings
     ** @date   2017/12/26
     ** @param  cell  单元格
     ** @param  obj   写入的数据
     * *******************************************************
     */
    private static void setCellObject(Cell cell, Object obj) {
        obj = obj ==  null ? "" : obj;
        cell.setCellValue(obj.toString());
    }
}
