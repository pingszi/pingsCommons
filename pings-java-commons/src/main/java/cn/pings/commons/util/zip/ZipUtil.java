package cn.pings.commons.util.zip;

import java.io.*;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *********************************************************
 ** @desc  ：zip压缩文件工具类                                             
 ** @author  Pings                                      
 ** @date    2016年3月29日 
 ** @version v1.0                                                                                  
 * *******************************************************
 */
public class ZipUtil {
	
	private static final int BUFFER = 8192; 
	
	/**
	 *********************************************************
	 ** @desc ：执行压缩操作                                            
	 ** @author Pings                                      
	 ** @date   2016年10月21日                                      
	 ** @param zipDir    压缩文件地址
	 ** @param srcFiles  被压缩的文件/文件夹 
	 ** @return                                              
	 * *******************************************************
	 */
	public static File compressExe(String zipDir, File[] srcFiles) {
		//**创建压缩文件
		File zipFile = new File(zipDir);
		if(zipFile.exists())
			zipFile.delete();
		try {
			zipFile.createNewFile();
		} catch (IOException e) {
			throw new RuntimeException(e);   
		}
		
		return compressExe(zipFile, srcFiles);
	}

	/**
	 *********************************************************
	 ** @desc ： 执行压缩操作                                            
	 ** @author Pings                                      
	 ** @date   2016年3月29日                                      
	 ** @param zipFile   压缩文件
	 ** @param srcFiles  被压缩的文件/文件夹 
	 ** @return                                              
	 * *******************************************************
	 */
	public static File compressExe(File zipFile, File[] srcFiles) {
		if(srcFiles.length == 0){
			return zipFile;
		}
		
        FileOutputStream fileOutputStream = null;
        CheckedOutputStream cos = null;
        ZipOutputStream out = null;
        try {    
            fileOutputStream = new FileOutputStream(zipFile);    
            cos = new CheckedOutputStream(fileOutputStream, new CRC32());    
            out = new ZipOutputStream(cos);    
            String basedir = "";  
            for(File file : srcFiles){
            	if(file.exists())        		
            		compressByType(file, out, basedir);   
            }
        } catch (Exception e) {   
            throw new RuntimeException(e);    
        }finally{
        	try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
        	
            try {
				cos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            try {
				fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        return zipFile;
    }    
    
	/**
	 *********************************************************
	 ** @desc ：判断是目录还是文件，根据类型（文件/文件夹）执行不同的压缩方法                                               
	 ** @author Pings                                      
	 ** @date   2016年3月29日                                      
	 ** @param file
	 ** @param out
	 ** @param basedir                                              
	 * *******************************************************
	 */
    private static void compressByType(File file, ZipOutputStream out, String basedir) {    
        //**判断是目录还是文件   
        if (file.isDirectory()) {     
            compressDirectory(file, out, basedir);    
        } else {     
            compressFile(file, out, basedir);    
        }    
    }    
    
    /**
     *********************************************************
     ** @desc ：压缩目录                                             
     ** @author Pings                                      
     ** @date   2016年3月29日                                  
     ** @param dir
     ** @param out
     ** @param basedir                                              
     * *******************************************************
     */
    private static void compressDirectory(File dir, ZipOutputStream out, String basedir) {    
        if (!dir.exists()){  
             return;    
        }  
             
        File[] files = dir.listFiles();    
        for (int i = 0; i < files.length; i++) {    
            //**递归    
            compressByType(files[i], out, basedir + dir.getName() + "/");    
        }    
    }    
    
    /**
     *********************************************************
     ** @desc ：压缩文件                                              
     ** @author Pings                                      
     ** @date   2016年3月29日                                  
     ** @param file
     ** @param out
     ** @param basedir                                              
     * *******************************************************
     */
    private static void compressFile(File file, ZipOutputStream out, String basedir) {    
        if (!file.exists()) {    
            return;    
        }  
        
        BufferedInputStream bis = null;
        try {    
            bis = new BufferedInputStream(new FileInputStream(file));    
            ZipEntry entry = new ZipEntry(basedir + file.getName());    
            out.putNextEntry(entry);    
            int count;    
            byte data[] = new byte[BUFFER];    
            while ((count = bis.read(data, 0, BUFFER)) != -1) {    
                out.write(data, 0, count);    
            }                   
        } catch (Exception e) {    
            throw new RuntimeException(e);    
        } finally{
        	try {
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
        }
    }    
}  
