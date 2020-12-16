package com.test.commons.util;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.zip.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.exception.FileUtilException;

public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    private static final int BYTE_BUFFER_SIZE = 4096; //bytes
    private static final int CHAR_BUFFER_SIZE = 1024; //chars
    private static boolean _useNIO = false;
    private static File _tempDir;

    static {
    	//JDK 1.7+ 對檔案/目錄的 copy/move/delete 有 Files 等 NIO API 可使用
    	try {
    		Class.forName("java.nio.file.Files", false, FileUtil.class.getClassLoader()); //JDK 1.7+
    		_useNIO = true;
    	} catch(ClassNotFoundException e) {}
    }
    
    protected FileUtil() {}
    
    /**
     * 在檔案系統產生空的暫存檔, 檔名由系統自動訂定; 如果系統已經由 setTempDir()　指定暫存目錄則使用之,　否則在 OS 暫存目錄下產生.
     * @param prefix 暂存檔檔名起始字串, not null, 至少 3 個字元.
     * @param suffix 暂存檔檔名結尾字串, 若為 null 則預設為 ".tmp"
     * @return 空暫存檔物件(在同一 JVM 下檔名不會重複)
     * @see java.io.File#createTempFile(String, String)
     */
    public static File createTempFile(final String prefix, final String suffix) {
    	try {
	    	if(_tempDir == null)
	    		return File.createTempFile(prefix, suffix);
	    	
    		_tempDir.mkdirs();
    		return File.createTempFile(prefix, suffix, _tempDir);
    	} catch(IOException ie) {
    		throw new FileUtilException(ie.getMessage(), ie);
    	}
    }
    
    /**
     * 在檔案系統在 directory 下產生空的暫存檔, 檔名由系統自動訂定; 
     * 如果 directory 為 null, 且系統已經由 setTempDir()　指定暫存目錄則使用之,　否則在 OS 暫存目錄下產生.
     * @param prefix 暂存檔檔名起始字串, not null, 至少 3 個字元.
     * @param suffix 暂存檔檔名結尾字串, 若為 null 則預設為 ".tmp"
     * @param directory 自訂暫存目錄
     * @return 空暫存檔物件(在同一 JVM 下檔名不會重複)
     * @see java.io.File#createTempFile(String, String, File)
     */
    public static File createTempFile(final String prefix, final String suffix, final File directory) {
    	try {
	    	if(directory == null)
	    		return createTempFile(prefix, suffix);
	    	 
	    	directory.mkdirs();
	    	return File.createTempFile(prefix, suffix, directory);
    	} catch(FileUtilException fe) {
    		throw fe;
    	} catch(IOException ie) {
    		throw new FileUtilException(ie.getMessage(), ie);
    	}
    }
    
    /**
     * 將目錄(不含)之下所有次目錄及檔案刪除(不擲出 exception).
     * @return 當有任一檔案或目錄沒有正常刪除, 或其他任何問題發生時傳回 false
     */
    public static boolean deleteUnder(final File dir) {
        return deleteUnder(dir, null, true);
    }

    /**
     * 將目錄(不含)之下所有次目錄及檔案刪除(不擲出 exception).
     * @return 當有任一檔案或目錄沒有正常刪除, 或其他任何問題發生時傳回 false
     */
    public static boolean deleteUnder(final String dir) {
        return deleteUnder(new File(dir));
    }
    
    /**
     * 刪除 dir 目錄(不含)以下合乎 filter 條件的檔案(不擲出 exception).
     * @param dir
     * @param filter 
     * @param recursive 是否刪除子目錄及其內的檔案. 注意子目錄名亦受 filter 條件的限制, 不符條件者, 該子目錄及其下的檔案都將被略過.
     * @return 當有任一檔案或目錄沒有正常刪除, 或其他任何問題發生時傳回 false
     */
    public static boolean deleteUnder(final File dir, final FileFilter filter, final boolean recursive) {
    	if(dir == null)
    		return false;
        if(!dir.isDirectory())
            return false;

        boolean status = true;
        File[] files = (filter == null) ? dir.listFiles() : dir.listFiles(filter);
        
        for(int i = 0, j = files.length; i < j; i++) {
            File file = files[i];
            try {
                if(file.isDirectory() && recursive) {
                    status = delete(file, filter, recursive) && status;
                } else {
                    boolean bool;
                    status = (bool = file.delete()) && status; //檔案, 或非遞迴時的目錄
                    if(!bool && log.isDebugEnabled())
                        log.warn((file.isDirectory() ? "dir " : "file ") + file.getPath() + (file.exists() ? " not deleted." : " not exists."));
                }
            } catch(Exception e) {
                status = false;
                log.error(ExceptionUtil.getRootException(e).toString());
            }
        }
        return status;
    }
    
    /**
     * 刪除目錄(或檔案)(不擲出 exception).
     * @param dir 目錄或檔案
     * @param filter dir 下每個檔案及目錄的過濾器, 過濾通過者才予以刪除.
     * @param recursive 是否刪除 dir 以下的所有子目錄及其下的檔案, 否則只針對 dir 該一層的檔案及空目錄做刪除的動作. 
     *                  注意每個子目錄也帶受到 filter 的過濾, 不合 filter 的過濾條件者, 該子目錄及其下的檔案就直接被略過.
     * @return 當有任一檔案或目錄沒有正常刪除, 或其他任何問題發生時傳回 false
     */
    public static boolean delete(final File dir, final FileFilter filter, final boolean recursive) {
    	if(dir == null)
    		return false;
    	
        boolean status = true;

        if(dir.isDirectory()) {
            File[] files = (filter == null) ? dir.listFiles() : dir.listFiles(filter);
            for(int i = 0, j = files.length; i < j; i++) {
                File file = files[i];
                try {
                    if(file.isDirectory() && recursive) { //遞迴刪除目錄
                        status = delete(file, filter, recursive) && status;
                    } else {
                        boolean bool;
                        status = (bool = file.delete()) && status; //檔案, 或非遞迴時的目錄
                        if(!bool && log.isDebugEnabled())
                            log.warn((file.isDirectory() ? "dir " : "file ") + file.getPath() + (file.exists() ? " not deleted." : " not exists."));
                    }
                } catch(Exception e) {
                    status = false;
                    log.error(e.toString());
                }
            }
        }
        
        try {
            boolean bool;
            status = (bool = dir.delete()) && status;
            if(!bool && log.isDebugEnabled())
                log.warn("file " + dir.getPath() + (dir.isFile() ? " not deleted." : " not exists."));
        } catch(Exception e) {
            status = false;
            log.error(e.toString());
        }
        
        return status;
    }


    /**
     * 將目錄(或檔案)及其下所有次目錄及檔案刪除(不擲出 exception).
     * @param dir
     * @return 當有任一檔案或目錄沒有正常刪除, 或其他任何問題發生時傳回 false
     */
    public static boolean delete(final File dir) {
        return delete(dir, null, true);
    }

    /**
     * 將目錄(或檔案)及其下所有次目錄及檔案刪除(不擲出 exception).
     * @return 當有任一檔案或目錄沒有正常刪除, 或其他任何問題發生時傳回 false
     */
    public static boolean delete(final String dir) {
        return delete(new File(dir));
    }
    
    /**
     * 刪除一組檔案 (目錄略過).
     * @param files
     */
    public static void deleteFiles(final File ... files) {
    	if(files == null)
    		return;
        for(int i = 0; i < files.length; i++) {
            if(!files[i].isFile())
                continue;
            files[i].delete();
        }
    }

    /**
     * 檔案移為另一檔案, 或目錄移為另一目錄(含原內含的子目錄及檔案).
     * 參數 from 及 dest 值須同為檔案或目錄. 兩者之間的檔案所有者及權限可能不一致(在 JDk 1.7 以前的環境下執行的話).
     * @param from 來源檔案或目錄路徑, 全部檔案複製完畢後才刪除來源檔案/目錄
     * @param dest 目的檔案或目錄
     * @return 位於新位置的檔案(或目錄), 即 dest, 但為絕對路徑. 來源為 null 或非檔案/目錄者, 傳回 null. dest 為 null 者亦傳回 null
     * @throws FileUtilException 當來源檔案或目錄不存在, 或複製對象為目錄(檔案)但目的地已存在同名的檔案(目錄)時, 或其他任何 IO 問題發生時擲出
     */
    public static String moveAs(final String from, final String dest) {
    	String ret = copyAs(from, dest);
    	delete(from);
    	return ret;
    }
    
    /**
     * 檔案移為另一檔案, 或目錄移為另一目錄(含原內含的子目錄及檔案).
     * 參數 from 及 dest 值須同為檔案或目錄. 兩者之間的檔案所有者及權限可能不一致(在 JDk 1.7 以前的環境下執行的話).
     * @param from 來源檔案或目錄路徑, 全部檔案複製完畢後才刪除來源檔案/目錄
     * @param dest 目的檔案或目錄
     * @return 位於新位置的檔案(或目錄), 即 dest. 來源為 null 或非檔案/目錄者, 傳回 null. dest 為 null 者亦傳回 null
     * @throws FileUtilException 當來源檔案或目錄不存在, 或複製對象為目錄(檔案)但目的地已存在同名的檔案(目錄)時, 或其他任何 IO 問題發生時擲出
     */
    public static File moveAs(final File from, final File dest) {
    	File ret = copyAs(from, dest);
    	delete(from);
    	return ret;
    }

    /**
     * 將檔案或目錄(含內含的子目錄及檔案)移至目的目錄下.
     * @param from 檔案或目錄路徑, 全部檔案複製完畢後才刪除來源檔案/目錄
     * @param destDirPath 目的目錄. 目錄不存在者, 嘗試建立之. 來源與目的兩者之間的檔案所有者及權限可能不一致(在 JDk 1.7 以前的環境下執行的話).
     * @return 位於新位置的檔案(或目錄). 引數　from 或 destDirPath 為 null 者, 傳回 null
     * @throws FileUtilException 當來源檔案或目錄不存在, 或複製對象為目錄(檔案)但目的地已存在同名的檔案(目錄)時, 或其他任何 IO 問題發生時擲出
     */
    public static String moveUnder(final String from, final String destDirPath) {
    	String ret = copyUnder(from, destDirPath);
    	delete(from);
    	return ret;
    }
    
    /**
     * 將檔案或目錄(含內含的子目錄及檔案)移至目的目錄下.
     * @param from 檔案或目錄, 全部檔案複製完畢後才刪除來源檔案/目錄
     * @param destDirPath 目的目錄. 目錄不存在者, 嘗試建立之. 來源與目的兩者之間的檔案所有者及權限可能不一致(在 JDk 1.7 以前的環境下執行的話).
     * @return 位於新位置的檔案(或目錄). 引數　from 或 destDirPath 為 null 者, 傳回 null
     * @throws FileUtilException 當來源檔案或目錄不存在, 或複製對象為目錄(檔案)但目的地已存在同名的檔案(目錄)時, 或其他任何 IO 問題發生時擲出
     */
    public static File moveUnder(final File from, final File destDirPath) {
    	File ret = copyUnder(from, destDirPath);
    	delete(from);
    	return ret;
    }

    /**
     * 檔案複製為另一檔案, 或目錄複製為另一目錄(含內含的子目錄及檔案).
     * 參數 from 及 dest 值須同為檔案或目錄, 兩者之間的檔案所有者及權限可能不一致(在 JDk 1.7 以前的環境下執行的話).
     * @param from 來源檔案或目錄路徑
     * @param dest 目的檔案或目錄
     * @return 即 dest, 但為絕對路徑. 來源為 null 或非檔案/目錄者, 傳回 null. dest 為 null 者亦傳回 null
     * @throws FileUtilException 當來源檔案或目錄不存在, 或複製對象為目錄(檔案)但目的地已存在同名的檔案(目錄)時, 或其他任何 IO 問題發生時擲出
     */
    public static String copyAs(final String from, final String dest) {
    	if(from == null) {
			log.warn("1st argument (source file path) not specified");
			return null;
		}
		if(dest == null) {
			log.warn("2nd argument (destination file path) not specified");
			return null;
		}
    	File ret = copyAs(new File(from), new File(dest));
    	return (ret != null) ? ret.getAbsolutePath() : null;
    }
    
    /**
     * 檔案複製為另一檔案, 或目錄複製為另一目錄(含內含的子目錄及檔案).
     * 參數 from 及 dest 值須同為檔案或目錄, 兩者之間的檔案所有者及權限可能不一致(在 JDk 1.7 以前的環境下執行的話).
     * @param from 來源檔案或目錄
     * @param dest 目的檔案或目錄, 若目錄已存在者, 將被覆寫.
     * @return 複製後的新檔案(或目錄), 即引數 dest. 來源為 null 或非檔案/目錄者, 傳回 null. dest 為 null 者亦傳回 null
     * @throws FileUtilException 當來源檔案或目錄不存在, 或複製對象為目錄(檔案)但目的地已存在同名的檔案(目錄)時, 或其他任何 IO 問題發生時擲出
     */
    public static File copyAs(final File from, final File dest) {
    	try {
    		if(from == null) {
    			log.warn("1st argument (source file) not specified");
    			return null;
    		}
    		if(dest == null) {
    			log.warn("2nd argument (destination file) not specified");
    			return null;
    		}
	    	if(!from.exists())
				throw new FileNotFoundException(from.getAbsolutePath() + " does not exist");
			
			if(from.isFile()) { //dest 也得是 file
				if(dest.isDirectory())
					throw new IOException(dest.getAbsolutePath() + " already existed as a directory, the file of the same name can't be established");
				return copyFileAs(from, dest);
			}
			
			if(from.isDirectory()) { //dest 也得是 dir
				if(dest.isFile())
					throw new IOException(dest.getAbsolutePath() + " already existed as a file, the directory of the same name can't be created");
				final File from2 = from.isAbsolute() ? from : from.getAbsoluteFile();
				final File dest2 = dest.isAbsolute() ? dest : dest.getAbsoluteFile();
				if(from2.equals(dest2)) //來源, 目的路徑一樣
		    		return dest2;
				
				if(_useNIO) {
					ensureDirExist(dest2.getParentFile());
					final Path source = from2.toPath();
					final EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS); //將 symbolic link 所指的內容複製過去
					final TreeCopier tc = new TreeCopier(source, dest2.toPath());
					Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
					return dest2;
				}
				
				//JDK 1.6-
				from2.listFiles(new FileFilter() {
					@Override
					public boolean accept(final File f) {
						try {
							if(f.isFile())
								copyFileUnder(f, dest2);
							else if(f.isDirectory())
								copyAs(f, new File(dest2, f.getName()));
							else
								log.warn(f.getAbsolutePath() + " is neither a regular file nor a directory, ignore it");
							return true;
						} catch(Throwable t) {
							throw new FileUtilException(t.getMessage(), t);
						}
					}
				});
				return dest2;
			}
			
			log.warn(from.getAbsolutePath() + " is neither a regular file nor a directory, ignore it");
			return null;
    	} catch(IOException ie) {
    		throw new FileUtilException(ie.getMessage(), ie);
    	}
    }
    
    /**
     * 將檔案或目錄(含內含的子目錄及檔案)複製至目的目錄下.
     * @param from 檔案或目錄路徑
     * @param destDirPath 目的目錄. 目錄不存在者, 嘗試建立之. 來源與目的兩者之間的檔案所有者及權限可能不一致(在 JDk 1.7 以前的環境下執行的話).
     * @return 在新位置下的檔案(或目錄)路徑. 引數　from 或 destDirPath 為 null 者, 傳回 null
     * @throws FileUtilException 當來源檔案或目錄不存在, 或複製對象為目錄(檔案)但目的地已存在同名的檔案(目錄)時, 或其他任何 IO 問題發生時擲出
     */
    public static String copyUnder(final String from, final String destDirPath) {
    	if(from == null) {
			log.warn("1st argument (source file path) not specified");
			return null;
		}
		if(destDirPath == null) {
			log.warn("2nd argument (destination file path) not specified");
			return null;
		}
    	File ret = copyUnder(new File(from), new File(destDirPath));
    	return (ret != null) ? ret.getAbsolutePath() : null;
    }
    
    /**
     * 將檔案或目錄(含內含的子目錄及檔案)複製至目的目錄下.
     * @param from 來源檔案或目錄
     * @param destDir 目的目錄. 目錄不存在者, 嘗試建立之. 來源與目的兩者之間的檔案所有者及權限可能不一致(在 JDk 1.7 以前的環境下執行的話).
     * @return 在新位置下的檔案(或目錄). 引數　from 或 destDirPath 為 null 者, 傳回 null
     * @throws FileUtilException 當來源檔案或目錄不存在, 或複製對象為目錄(檔案)但目的地已存在同名的檔案(目錄)時, 或其他任何 IO 問題發生時擲出
     */
    public static File copyUnder(final File from, final File destDir) {
    	try {
    		if(from == null) {
    			log.warn("1st argument (source file) not specified");
    			return null;
    		}
    		if(destDir == null) {
    			log.warn("2nd argument (destination file) not specified");
    			return null;
    		}
    		if(!from.exists())
    			throw new FileNotFoundException(from.getAbsolutePath() + " does not exist");
    		final File from2 = from.isAbsolute() ? from : from.getAbsoluteFile();
    		final File destDir2 = destDir.isAbsolute() ? destDir : destDir.getAbsoluteFile();
    		
    		final File fromParent = from2.getParentFile();
    		if(fromParent != null && fromParent.equals(destDir2)) //指定的目的與來源路徑一樣, 不需複製
    			return from2;
    		
    		if(from2.isFile())
    			return copyFileUnder(from2, destDir2);
    		if(from2.isDirectory())
    			return copyAs(from2, new File(destDir2, from2.getName()));
			log.warn(from2.getAbsolutePath() + " is neither a regular file nor a directory, ignore it");
    		return null;
    	} catch(IOException ie) {
    		throw new FileUtilException(ie.getMessage(), ie);
    	}
    }

    /**
     * 切割檔案. 輸出檔的檔名將如下型式: newFileNamePrefix + newFileNameBody + newFileNamePostfix + n, n 由 1 起算.
     * 如. xxx.1, xxx.2, xxx.3...<br>
     * 即使輸入檔大小小於 maxChunkSize, 仍會輸出一個以 ".1" (或 ".part.1") 結尾的相同檔案.
     * @param input 輸入檔. 未指定(null)或檔案內容為空者, 傳回空陣列
     * @param outputDir 輸出檔案的放置目錄, 如果為 null, 則與輸入檔案的位置相同.
     * @param maxChunkSize 切割後的檔案的最大體積 (bytes).
     * @param newFileNamePrefix 輸出檔案檔名的前置字串, 可為 null.
     * @param newFileNameBody 輸出檔檔名. 如果為 null, 則與原輸入檔檔名(含副檔名)同.
     * @param newFileNamePostfix 輸出檔的後置字串. 如果為 null, 則為 ".part".
     * @return 分割後的檔案
     */
    public static File[] split(final File input, final File outputDir, final int maxChunkSize,
    		final String newFileNamePrefix, final String newFileNameBody, final String newFileNamePostfix) {
        InputStream in = null;
        OutputStream out = null;
        
        try {
        	if(input == null || input.length() == 0)
        		return new File[0];
        	
        	List<File> ret = new ArrayList<File>();
            InputStream fin = new FileInputStream(input);
            int fileSize = fin.available();
            int bufferSize = (BYTE_BUFFER_SIZE <= maxChunkSize) ? BYTE_BUFFER_SIZE : maxChunkSize;
            in = new BufferedInputStream(fin);
            byte[] buff = new byte[bufferSize];

            ensureDirExist(outputDir);
            
            for(int i = 0, j = 0; i < fileSize; i += maxChunkSize) {
                File outputFile = new File((outputDir == null) ? input.getParentFile() : outputDir,
                        ((newFileNamePrefix == null) ? "" : newFileNamePrefix) +
                        ((newFileNameBody == null) ? input.getName() : newFileNameBody) +
                        ((newFileNamePostfix == null) ? ".part" : newFileNamePostfix) + "." + (++j));
                ret.add(outputFile);
                out = new FileOutputStream(outputFile);

                for(int m = in.read(buff), n = m; m != -1; n += m) {
                    out.write(buff, 0, m);
                    if(n >= maxChunkSize)
                        break;
                    else if((maxChunkSize - n) < bufferSize)
                        m = in.read(buff, 0, (maxChunkSize - n));
                    else
                        m = in.read(buff);
                }
                out.close();
                out = null;
            }
            in.close();
            in = null;

            return ret.toArray(new File[ret.size()]);
        } catch(Throwable t) {
        	throw new FileUtilException(t.getMessage(), t);
        } finally {
            if(in != null) try { in.close(); } catch(Throwable t) {}
            if(out != null) try { out.close(); } catch(Throwable t) {}
        }
    }

    /**
     * 將檔案內容壓縮為 gzip 格式並化為 byte array.<br>
     * <b>注意: 資料量是否過大</b>
     * @param input 輸入檔案. 若未指定(null)或內容為空者, 則傳回空陣列
     * @return 已壓縮為 gzip 格式的 byte 陣列
     */
    public static byte[] getGZIPByteArray(final File input) {
        InputStream in = null;

        try {
        	if(input == null || input.length() == 0)
        		return new byte[0];
        	
            in = new FileInputStream(input);
            byte[] content = getGZIPByteArray(in);
            in.close();
            in = null;
            return content;
        } catch(Throwable t) {
        	throw new FileUtilException(t.getMessage(), t);
        } finally {
            if(in != null) try { in.close(); } catch(IOException ie) {}
        }
    }

    /**
     * 將資料來源內容壓縮為 gzip 格式並化為 byte array.<br>
     * <b>注意: 資料量是否過大</b>
     * @param input 資料來源. 若未指定(null)者, 則傳回空陣列
     * @return 已壓縮為 gzip 格式的 byte 陣列
     */
    public static byte[] getGZIPByteArray(final InputStream input) {
        GZIPOutputStream out = null;

        try {
        	if(input == null)
        		return new byte[0];
        	
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            out = new GZIPOutputStream(bout);
            dump(input, out);
            out.finish(); //少了這行就不能解壓縮
            out.close();
            out = null;
            return bout.toByteArray();
        } catch(Throwable t) {
        	throw new FileUtilException(t.getMessage(), t);
        } finally {
            if(out != null) try { out.close(); } catch(IOException ie) {}
        }
    }
    
    /**
     * 比較兩檔案內容是否一樣.
     * @param file1
     * @param file2
     * @return true:兩檔案內容一樣, false 則否
     */
    public static boolean isEqual(final File file1, final File file2) {
        if(file1 == null || file2 == null || !file1.isFile() || !file2.isFile())
            return false;
        if(file1.getAbsoluteFile().equals(file2.getAbsoluteFile()))
            return true;
        if(file1.length() != file2.length())
        	return false;

        InputStream in1 = null, in2 = null;
        
        try {
            in1 = new BufferedInputStream(new FileInputStream(file1));
            in2 = new BufferedInputStream(new FileInputStream(file2));
            byte[] buff1 = new byte[BYTE_BUFFER_SIZE];
            byte[] buff2 = new byte[BYTE_BUFFER_SIZE];
            
            while(true) {
                final int m = in1.read(buff1);
                final int n = in2.read(buff2);
                if(m != n)
                    return false;
                if(m <= 0) //兩邊都剛好讀完了
                    return true;
                
                if(m == BYTE_BUFFER_SIZE) {
                	if(!Arrays.equals(buff1, buff2))
                		return false;
                } else {
                	for(int i = 0, j = m - 1; i <= j; i++, j--) { //同一迴圈中 同時自陣列頭尾比較起
                		if(buff1[i] != buff2[i])
                			return false;
                		if(j > i) {
                			if(buff1[j] != buff2[j])
                				return false;
                		}
                	}
                }
            }
        } catch(Throwable t) {
        	throw new FileUtilException(t.getMessage(), t);
        } finally {
            if(in1 != null) try { in1.close(); } catch(IOException ie) {}
            if(in2 != null) try { in2.close(); } catch(IOException ie) {}
        }
    }
    
    /**
     * 解壓縮 gzip 檔.
     * @param gzipFile gzip 壓縮格式的輸入檔. 若未指定(null)者, 則傳回 null
     * @param dest 輸出檔  
     * @return 輸出檔
     */
    public static File gunzip(final File gzipFile, final File dest) {
    	InputStream in = null;
    	
    	try {
    		if(gzipFile == null)
    			return null;
    		if(dest == null)
    			throw new IllegalArgumentException("2nd argument (File) not specified");
    		
    		in = new GZIPInputStream(new FileInputStream(gzipFile));
    		dump(in, dest);
    		in.close();
    		in = null;
    		return dest;
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	} finally {
    		if(in != null) try { in.close(); } catch(Throwable t) {}
    	}
    }
    
    /**
     * 壓縮一檔案為 gzip 檔,
     * @param input 輸入檔. 未指定者(null)傳回 null
     * @param gzipFile gzip 格式的壓縮檔(輸出檔), 未指定(null)者, 以原檔名加上 ".gz" 為新檔名
     * @return gzip 格式的壓縮檔(同 gzipFile 物件)
     */
    public static File gzip(final File input, final File gzipFile) {
    	GZIPOutputStream out = null;
    	
    	try {
    		if(input == null)
    			return null;
    		
    		out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(gzipFile)));
    		dump(input, out);
    		out.finish();
    		out.close();
    		out = null;
    		return gzipFile;
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	} finally {
    		if(out != null) try { out.close(); } catch(Throwable t) {}
    	}
    }
    
    /**
     * 壓縮一檔案為 gzip 檔,
     * @param input 輸入檔
     * @return gzip 格式的壓縮檔(置於與輸入檔同一位置下, 檔名結尾加上 ".gz")
     */
    public static File gzip(final File input) {
    	return gzip(input, new File(input.getAbsolutePath() + ".gz"));
    }
    
    /**
     * 解壓縮 zip 檔.
     * @param zipFile 壓縮檔. 未指定(null)者, 傳回 null
     * @param destDir 解開後放置的位置, 未指定(null)者, 則在 zipFile 所在位置下解開
     * @return 解開後的所有檔案
     */
    public static File[] unzip(final File zipFile, final File destDir) {
        ZipFile zf = null;
        InputStream in = null;
        OutputStream out = null;
        
        try {
        	if(zipFile == null)
        		return null;
            if(!zipFile.isFile())
                throw new IOException("file " + zipFile.getPath() + " doesn't exist.");
            
            final File destDir2 = (destDir == null) ? zipFile.getParentFile() : destDir;
            if(destDir != null)
            	ensureDirExist(destDir2);
            
            zf = new ZipFile(zipFile);
            final Enumeration<? extends ZipEntry> enumZipFiles = zf.entries();
            final List<File> outFiles = new ArrayList<File>();
            
            while(enumZipFiles.hasMoreElements()) {
                final ZipEntry ze = enumZipFiles.nextElement();
                final File outFile = new File(destDir2, ze.getName());
                
                if(ze.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                in = zf.getInputStream(ze);
                dump(in, outFile);
                in.close();
                in = null;
                outFiles.add(outFile);

                outFile.setLastModified(ze.getTime()); //原檔案最後修改時間
            }
            zf.close();
            zf = null;
            
            File[] files = new File[outFiles.size()];
            outFiles.toArray(files);
            return files;
        } catch(Throwable t) {
        	throw new FileUtilException(t.getMessage(), t);
        } finally {
            if(zf != null) try { zf.close(); } catch(IOException ie) {}
            if(in != null) try { in.close(); } catch(IOException ie) {}
            if(out != null) try { out.close(); } catch(IOException ie) {}
        }
    }
    
    /**
     * 壓縮 zip 檔. TODO: 尚有中文檔名的問題.
     * @param zipFile 輸出的壓縮檔. 若未指定者(null)傳回 null
     * @param files 欲壓縮的來源檔案(可包含目錄). 若未指定(null)或數量為 0 者, 傳回 null
     * @param recursive 是否包括目錄及其下之所有檔案(false: 只壓入檔案)
     * @return 壓縮檔
     */
    public static File zip(final File zipFile, final File[] files, final boolean recursive) {
        InputStream in = null;
        ZipOutputStream out = null;
        
        try {
        	if(zipFile == null || files == null || files.length == 0)
        		return null;
        	ensureDirExist(zipFile.getParentFile());
        	
        	//TODO: JDK 1.7+ 提供 ZipOutputStream(OutputStream, Charset)
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            byte[] buff = new byte[BYTE_BUFFER_SIZE];
            
            int[] nameStartIndices = new int[files.length];
            for(int i = 0; i < files.length; i++) {
            	String basePath = files[i].getParent();
            	nameStartIndices[i] = (basePath == null) ? 0 : (basePath.endsWith("/")) ? basePath.length() : basePath.length() + 1; //baseDir 路徑後加上 "/" 的長度
            }
            
            zip(zipFile, out, buff, files, nameStartIndices, -1, recursive);
            out.finish();
            out.close();
            out = null;
            return zipFile;
        } catch(Throwable t) {
        	throw new FileUtilException(t.getMessage(), t);
        } finally {
            if(in != null) try { in.close(); } catch(IOException ie) {}
            if(out != null) try { out.close(); } catch(IOException ie) {}
        }
    }
    
    /**
     * 將多個檔案 files, 且限位於 baseDir 目錄下的檔案壓成 zipFile, 壓縮檔內不含 baseDir 資訊(壓縮檔解開後將不含 baseDir 目錄), 不在 baseDir 下的檔案不被納入.
     * TODO: 尚有中文檔名的問題.
     * @param zipFile 輸出檔. 若未指定者(null)傳回 null
     * @param files 欲壓縮的來源檔案. 若未指定(null)或數量為 0 者, 傳回 null
     * @param baseDir (optional)
     * @param recursive 當 files 成員為目錄時, 是否納入其下的子目錄及其內之檔案
     */
    public static File zip(final File zipFile, final File[] files, final File baseDir, final boolean recursive) {
        InputStream in = null;
        ZipOutputStream out = null;
        
        try {
        	if(baseDir == null)
        		return zip(zipFile, files, recursive);
        	if(zipFile == null || files == null || files.length == 0)
        		return null;
        	
            //過濾掉不在 baseDir 下的檔案
            final String basePath = baseDir.getAbsolutePath();
            final List<File> files1 = new ArrayList<File>(files.length);
            for(int i = 0; i < files.length; i++) {
                if(files[i].getAbsolutePath().startsWith(basePath))
                    files1.add(files[i]);
            }
            final File[] files2 = new File[files1.size()]; //蓋掉原 files
            files1.toArray(files2);

            ensureDirExist(zipFile.getParentFile());
        	
            //TODO: JDK 1.7+ 提供 ZipOutputStream(OutputStream, Charset)
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile))); //JDK 1.7+ 預設使用 UTF-8 作為檔名編碼, 不使用 Unicode extra field
            byte[] buff = new byte[BYTE_BUFFER_SIZE];
            final int baseDirLength = basePath.length();
            zip(zipFile, out, buff, files2, baseDirLength + 1, null, recursive);
            out.finish();
            out.close();
            out = null;
            return zipFile;
        } catch(Throwable t) {
        	throw new FileUtilException(t.getMessage(), t);
        } finally {
            if(in != null) try { in.close(); } catch(IOException ie) {}
            if(out != null) try { out.close(); } catch(IOException ie) {}
        }
    }
    
    /**
     * 將 baseDir 目錄下的特定檔名型式的檔案壓成 zipFile, 壓縮檔內不含 baseDir 資訊.
     * @param zipFile 輸出檔. 若未指定者(null)傳回 null
     * @param baseDir (若未指定者(null)傳回 null)
     * @param filter baseDir 目錄下將被壓縮的檔案之檔名的過濾器, null 者表示不對檔名設限
     * @param recursive 是否納入 baseDir 以下的子目錄及其內之檔案
     * @return 即 zipFile
     */
    public static File zipUnder(final File zipFile, final File baseDir, final FilenameFilter filter, final boolean recursive) {
        InputStream in = null;
        ZipOutputStream out = null;
        
        try {
        	if(zipFile == null || baseDir == null)
        		return null;
        	ensureDirExist(zipFile.getParentFile());
        	
        	//TODO: JDK 1.7+ 提供 ZipOutputStream(OutputStream, Charset)
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile))); //JDK 1.7+ 預設使用 UTF-8 作為檔名編碼, 不使用 Unicode extra field
            byte[] buff = new byte[BYTE_BUFFER_SIZE];
            File[] files = baseDir.listFiles(filter);
            String basePath = baseDir.getAbsolutePath();
            int nameStartIndex = (basePath.endsWith("/")) ? basePath.length() : basePath.length() + 1; //baseDir 路徑後加上 "/" 的長度
            zip(zipFile, out, buff, files, nameStartIndex, new FileFilter4Zip(filter, recursive), recursive);
            out.finish();
            out.close();
            out = null;
            return zipFile;
        } catch(Throwable t) {
        	throw new FileUtilException(t.getMessage(), t);
        } finally {
            if(in != null) try { in.close(); } catch(IOException ie) {}
            if(out != null) try { out.close(); } catch(IOException ie) {}
        }
    }
    
    /**
     * 把資料來源內容倒入目的地.
     * @param data 資料來源內容, 若為 null 則傳回 0
     * @param dest 資料目的地
     * @return 讀出的資料量(單位: byte)
     */
    public static long dump(final InputStream data, final OutputStream ... dest) {
    	try {
    		if(data == null)
    			return 0L;
    		if(dest == null || dest.length == 0)
    			throw new IllegalArgumentException("destination argument (OutputStream) not specified");
    		
    		if(dest.length == 1)
    			return dump1(data, dest[0]);
    		
    		final byte[] buff = new byte[BYTE_BUFFER_SIZE];
    		long ret = 0;
    		for(int n; (n = data.read(buff)) != -1; ) {
    			for(int i = 0; i < dest.length; i++) {
    				if(dest[i] == null)
    					throw new IllegalArgumentException((i + 1) + "th OutputStream argument is null");
    				
    				dest[i].write(buff, 0, n);
    			}
    			ret += n;
    		}
    		return ret;
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	}
    }
    
    /**
     * 把資料來源內容倒入目的地.
     * @param data 資料來源內容, 若為 null 則傳回 0
     * @param dest 資料目的地
     * @return 讀出的資料量(單位: char)
     */
    public static long dump(final Reader data, final Writer ... dest) {
    	try {
    		if(data == null)
    			return 0L;
    		if(dest == null || dest.length == 0)
    			throw new IllegalArgumentException("destination argument (Writer) not specified");
    		
    		if(dest.length == 1)
    			return dump1(data, dest[0]);
    		
    		final CharBuffer buff = CharBuffer.allocate(CHAR_BUFFER_SIZE);
    		long ret = 0;
    		while(data.read(buff) != -1) {
    			buff.flip();
    			for(int i = 0; i < dest.length; i++) {
    				if(dest[i] == null)
    					throw new IllegalArgumentException((i + 1) + "th Writer argument is null");
    				
    				dest[i].append(buff);
    			}
    			ret += buff.remaining();
    			buff.clear();
    		}
    		return ret;
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	}
    }
    
    /**
     * 把資料來源內容倒入目的地檔案.
     * @param data 資料來源內容, 若為 null 則傳回 0
     * @param dest 資料寫入的檔案, 如果已存在者, 內容將被覆蓋
     * @return 讀出的資料量(單位: byte)
     */
    public static long dump(final InputStream data, final File dest) {
    	try {
    		return dump1(data, dest, false);
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	}
    }
    
    /**
     * 把資料來源內容倒入目的地檔案.
     * @param data 資料來源內容, 若為 null 則傳回 0
     * @param dest 資料寫入的檔案, 如果已存在者, 內容將被覆蓋
     * @param encoding data 內容的編碼, 未指定(null)者, 以程式執行時期的 locale 環境為依據
     * @return 讀出的資料量(單位: char)
     */
    public static long dump(final Reader data, final File dest, final String encoding) {
    	try {
    		return dump1(data, dest, encoding, false);
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	}
    }
    
    /**
     * 把檔案內容倒入指定目的地.
     * @param data 資料來源(檔案), 若為 null 則傳回 0
     * @param dest 資料目的地
     * @return 移動的資料量(單位: byte)
     */
    public static long dump(final File data, final OutputStream dest) {
    	InputStream in = null;
    	
    	try {
    		if(data == null || data.length() == 0)
	    		return 0L;
	    	if(dest == null)
        		throw new IllegalArgumentException("destination argument (OutputStream) not specified");
    		in = new BufferedInputStream(new FileInputStream(data));
    		final long ret = dump1(in, dest);
    		in.close();
    		in = null;
    		
    		return ret;
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	} finally {
    		if(in != null) try { in.close(); } catch(Throwable t) {}
    	}
    }
    
    /**
     * 把檔案內容倒入記憶空間中.
     * @param data 資料來源(檔案)
     * @return 資料內容. 若參數 data 為 null 則傳回 null
     */
    public static byte[] dump(final File data) {
    	try {
    		if(data == null)
    			return null;
    		final int len = (int)data.length();
    		if(len == 0)
    			return new byte[0];
    		ByteArrayOutputStream out = new ByteArrayOutputStream(len); //不需 close
    		dump(data, out);
    		return out.toByteArray();
    	} catch(FileUtilException fe) {
    		throw fe;
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	}
    }
    
    /**
     * 把檔案內容倒入指定目的地.
     * @param data 資料來源(檔案), 若為 null 則傳回 0
     * @param dest 資料目的地
     * @param encoding data 內容的編碼, 未指定(null)者, 以程式執行時期的 locale 環境為依據
     * @return 移動的資料量(單位: char)
     */
    public static long dump(final File data, final Writer dest, final String encoding) {
    	Reader in = null;
    	
    	try {
    		if(data == null || data.length() == 0)
	    		return 0L;
	    	if(dest == null)
        		throw new IllegalArgumentException("destination argument (Writer) not specified");
	    	
	    	if(encoding != null)
	    		in = new BufferedReader(new InputStreamReader(new FileInputStream(data), encoding));
	    	else
	    		in = new BufferedReader(new FileReader(data));
    		final long ret = dump1(in, dest);
    		in.close();
    		in = null;
    		
    		return ret;
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	} finally {
    		if(in != null) try { in.close(); } catch(Throwable t) {}
    	}
    }

    /**
     * 對指定檔案寫入小量字串資料.
     * @param data 資料內容. 如果為 null 者, 不作任何動作
     * @param dest 欲存放資料的檔案, 如果已存在者, 內容將被覆蓋
     * @param encoding data 內容的編碼, 未指定(null)者, 以程式執行時期的 locale 環境為依據
     * @return 寫入的資料量(單位: char)
     */
    public static long dump(final String data, final File dest, final String encoding) {
    	try {
	    	return dump1(data, dest, encoding, false);
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	}
    }

    /**
     * 把資料來源內容附加至目的地檔案.
     * @param data 資料來源內容, 若為 null 則傳回 0
     * @param dest 資料寫入的檔案, 自檔案最後面開始附加資料
     * @return 讀出的資料量(單位: byte)
     */
    public static long append(final InputStream data, final File dest) {
    	try {
    		return dump1(data, dest, true);
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	}
    }
    
    /**
     * 把資料來源內容附加於目的地檔案.
     * @param data 資料來源內容, 若為 null 則傳回 0
     * @param dest 資料寫入的檔案, 自檔案最後面開始附加資料
     * @param encoding data 內容的編碼, 未指定(null)者, 以程式執行時期的 locale 環境為依據
     * @return 讀出的資料量(單位: char)
     */
    public static long append(final Reader data, final File dest, final String encoding) {
    	try {
    		return dump1(data, dest, encoding, true);
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	}
    }
    
    /**
     * 對指定檔案附加小量字串資料.
     * @param data 資料內容. 如果為 null 者, 不作任何動作
     * @param dest 欲存放資料的檔案, 自檔案最後附加資料
     * @param encoding data 內容的編碼, 未指定(null)者, 以程式執行時期的 locale 環境為依據
     * @return 寫入的資料量(單位: char)
     */
    public static long append(final String data, final File dest, final String encoding) {
    	try {
	    	return dump1(data, dest, encoding, true);
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	}
    }
    
    /**
     * 自指定的檔案讀出全部內容. 注意: 檔案過大恐怕對系統記憶空間的耗用造成衝擊.
     * @param file
     * @return 檔案的全部內容; 如果傳入 null 者, 傳回 null
     */
    public static byte[] toBytes(final File file) {
    	try {
    		if(file == null)
        		return null;
        	if(file.length() == 0)
        		return new byte[0];
    		ByteArrayOutputStream out = new ByteArrayOutputStream((int)file.length()); //不需人工 close()
    		dump(file, out);
    		return out.toByteArray();
    	} catch(Throwable t) {
    		throw new FileUtilException(t.getMessage(), t);
    	}
    }

    static void ensureDirExist(final File dir) {
    	if(dir != null) {
	    	if(dir.isFile())
	    		throw new IllegalStateException("path '" + dir.getAbsolutePath() + "' already existed as a file, the directory with the same name can't be created");
			if(!dir.isDirectory())
				dir.mkdirs();
    	}
	}
    
    //files 成員要和 namesStartIndices 成員相配
    static void zip(final File zipFile, final ZipOutputStream out, final byte[] buff, final File[] files, 
    		final int[] namesStartIndices, final int namesStartIndex, final boolean recursive) 
			throws IOException {
        InputStream in = null;
        
        try {
            for(int i = 0; i < files.length; i++) {
                final File file = files[i];
                final int pathStartIndex = (namesStartIndex == -1) ? namesStartIndices[i] : namesStartIndex;
                String path = file.getAbsolutePath().substring(pathStartIndex);
                if(file.isDirectory()) {
                    if(!recursive)
                        continue;
                    if(!path.endsWith("/"))
                        path += "/";
                    
                    final ZipEntry zentry = new ZipEntry(path);
                    zentry.setTime(file.lastModified());
                    
                    out.putNextEntry(zentry);
                    final File[] files2 = file.listFiles();
                    zip(zipFile, out, buff, files2, null, pathStartIndex, true); //遞迴
                    continue;
                }
                
                if(file.equals(zipFile)) //如果輸出檔剛好也在 來源目錄下, 應予排除
                    continue;
                
                final ZipEntry zentry = new ZipEntry(path);
                zentry.setTime(file.lastModified());
                
                out.putNextEntry(zentry);
                in = new BufferedInputStream(new FileInputStream(file));
                for(int j; (j = in.read(buff)) != -1; )
                    out.write(buff, 0, j);
                in.close();
                in = null;
            }
        } finally {
            if(in != null) try { in.close(); } catch(IOException ie) {}
        }
    }
    
    //nameStartIndex: 每個檔案完整路, 扣除 baseDir 的起始 index
    static void zip(final File zipFile, final ZipOutputStream out, final byte[] buff, final File[] files, 
    		final int nameStartIndex, final FileFilter4Zip filter, final boolean recursive) 
			throws IOException {
        InputStream in = null;
        
        try {
            for(int i = 0; i < files.length; i++) {
                final File file = files[i];
                String name = file.getAbsolutePath().substring(nameStartIndex); //取每個檔案的絕對路徑, 扣除前面的 baseDir 路徑字串
                if(file.isDirectory()) {
                    if(!recursive)
                        continue;
                    if(!name.endsWith("/"))
                        name += "/";
                    
                    final ZipEntry zentry = new ZipEntry(name);
                    zentry.setTime(file.lastModified());
                    
                    out.putNextEntry(zentry); //空目錄也納入 zip 檔
                    final File[] files2 = (filter == null) ? file.listFiles() : file.listFiles(filter); //列出目錄下之檔案
                    zip(zipFile, out, buff, files2, nameStartIndex, filter, recursive); //遞迴
                    continue;
                }
                
                if(file.equals(zipFile)) //如果輸出檔剛好也在 來源目錄下, 應予排除
                    continue;
                
                final ZipEntry zentry = new ZipEntry(name);
                zentry.setTime(file.lastModified());
                
                out.putNextEntry(zentry); //加入壓縮檔案
                in = new BufferedInputStream(new FileInputStream(file));
                for(int j; (j = in.read(buff)) != -1; )
                    out.write(buff, 0, j);
                in.close();
                in = null;
            }
        } finally {
            if(in != null) try { in.close(); } catch(IOException ie) {}
        }
    }
    
    //複製檔案至目標目錄下
    //@return 新檔案
    static File copyFileUnder(final File file, final File dir) throws IOException {
    	File ret = new File(dir, file.getName());
    	return copyFileAs(file, ret);
    }
    
    //複製檔案成另一新檔
    //@return 新檔案(即 dest (絕對路徑))
    static File copyFileAs(final File from, final File dest) throws IOException {
    	if(from == null || dest == null)
    		return null;
    	
    	final File from2 = from.isAbsolute() ? from : from.getAbsoluteFile();
    	final File dest2 = dest.isAbsolute() ? dest : dest.getAbsoluteFile();
    	if(from2.equals(dest2))
    		return dest2;
    	
    	if(_useNIO) {
    		ensureDirExist(dest2.getParentFile());
    		return copyFileAs(from2.toPath(), dest2.toPath()).toFile();
    	}
    	
    	InputStream in = null;
    	
    	try {
    		in = new FileInputStream(from2);
    		dump(in, dest2);
    		in.close();
    		in = null;

    		dest2.setLastModified(from2.lastModified());
    		if(from2.canExecute())
    			dest2.setExecutable(true, false);
    		
    		return dest2;
    	} finally {
    		if(in != null) try { in.close(); } catch(Throwable t) {}
    	}
    }
    
	//複製檔案成另一新檔(JDK 1.7+)
    static Path copyFileAs(final Path f1, final Path f2) throws IOException {
    	return Files.copy(f1, f2, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
    }

    //return: bytes
    static long dump1(final InputStream data, final OutputStream dest) throws IOException {
    	if(dest == null)
    		throw new IllegalArgumentException("OutputStream argument not specified");
    	
    	final byte[] buff = new byte[BYTE_BUFFER_SIZE];
		long ret = 0;
		for(int n; (n = data.read(buff)) != -1; ) {
			dest.write(buff, 0, n);
			ret += n;
			
			//if(Thread.interrupted()) //防止 thread 中斷時陷入無限等待 (NOTE: Guava common.io.ByteStream.copy(InputStream, OutputStream) 也沒有這樣的措施...
			//	throw new InterruptedException("stream copying stopped because of thread interruption");
		}
		return ret;
    }
    
	//return: bytes
    static long dump1(final InputStream data, final File dest, final boolean append) throws IOException {
    	OutputStream out = null;
    	
    	try {
    		if(data == null)
        		return 0L;
        	if(dest == null)
        		throw new IllegalArgumentException("destination argument (File) not specified");
        	ensureDirExist(dest.getParentFile());
        	
    		out = new BufferedOutputStream(new FileOutputStream(dest, append));
    		final long ret = dump1(data, out);
    		out.flush();
    		out.close();
    		out = null;
    		
    		return ret;
    	} finally {
    		if(out != null) try { out.close(); } catch(IOException ie) {}
    	}
    }

    //return: number of chars
    static long dump1(final Reader data, final Writer dest) throws IOException {
    	if(dest == null)
    		throw new IllegalArgumentException("Writer argument not specified");
    	
    	final CharBuffer buff = CharBuffer.allocate(CHAR_BUFFER_SIZE);
		long ret = 0;
		while(data.read(buff) != -1) {
			buff.flip();
			dest.append(buff);
			ret += buff.remaining(); //chars for CharBuffer
			buff.clear();
		}
		return ret;
    }

    //return: number of chars
    static long dump1(final Reader data, final File dest, final String encoding, final boolean append) throws IOException {
    	Writer out = null;
    	
    	try {
    		if(data == null)
        		return 0L;
        	if(dest == null)
        		throw new IllegalArgumentException("destination argument (File) not specified");
        	ensureDirExist(dest.getParentFile());
        	
        	if(encoding != null)
        		out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest, append), encoding));
        	else
        		out = new BufferedWriter(new FileWriter(dest, append));
    		final long ret = dump1(data, out);
    		out.flush();
    		out.close();
    		out = null;
    		
    		return ret;
    	} finally {
    		if(out != null) try { out.close(); } catch(IOException ie) {}
    	}
    }
    
    //return: number of chars
    static long dump1(final String data, final File dest, final String encoding, final boolean append) throws IOException {
    	Writer out = null;
    	
    	try {
	    	if(data == null)
	    		return 0L;
	    	if(dest == null)
        		throw new IllegalArgumentException("destination argument (File) not specified");
	    	
	    	if(encoding != null)
	    		out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest, append), encoding));
	    	else
	    		out = new BufferedWriter(new FileWriter(dest, append));
	    	out.write(data);
	    	out.flush();
	    	out.close();
	    	out = null;
	    	
	    	return data.length();
    	} finally {
    		if(out != null) try { out.close(); } catch(Throwable t) {}
    	}
    }
    
    static class FileFilter4Zip implements FileFilter {
        private FilenameFilter filter;
        private boolean recursive;
        
        public FileFilter4Zip(final FilenameFilter filter, final boolean recursive) {
            this.filter = filter;
            this.recursive = recursive;
        }
        
        public boolean accept(final File path) {
            if(path.isDirectory()) //目錄名稱不限制
                return this.recursive ? true : false;
            return (this.filter == null) ? true : this.filter.accept(path.getParentFile(), path.getName());
        }
    }
    
    //JDK 1.7+, 參考: http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/essential/io/examples/Copy.java
    static class TreeCopier implements FileVisitor<Path> {
    	private Path source;
    	private Path target;
    	
    	TreeCopier(final Path source, final Path target) {
    		this.source = source;
    		this.target = target;
    	}
    	
		@Override
		public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
			Path newdir = this.target.resolve(this.source.relativize(dir));
			try {
				Files.copy(dir, newdir, StandardCopyOption.COPY_ATTRIBUTES);
			} catch (FileAlreadyExistsException fe) {} //目錄已存在 -> 沒事
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			copyFileAs(file, this.target.resolve(this.source.relativize(file)));
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
			Path newdir = target.resolve(source.relativize(dir));
			try {
				FileTime time = Files.getLastModifiedTime(dir);
				Files.setLastModifiedTime(newdir, time);
			} catch(IOException ie) {
				log.warn("unable set modified time for dir '" + newdir + "': " + ie.toString());
			}
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}
    }
    
    protected static void setTempDir(final File tempDir) {
    	if(_tempDir != null)
    		throw new IllegalStateException("tempDir has been set as '" + _tempDir.getAbsolutePath() + "'");
    	_tempDir = tempDir;
    }
}
