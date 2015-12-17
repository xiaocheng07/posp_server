package cn.com.bestpay.posp.netty.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


public class PropertiesUtil {
	
	public static Properties getProperties(String propFile) throws Exception{
		Properties p = new Properties();
		File f = new File(propFile);
		InputStream is;
		if(f.exists()){//从指定路径文件中加载
			is = new FileInputStream(f);
			p.load(is);
			is.close();
		}else{//从资源文件中加载
			return getProperties(propFile,PropertiesUtil.class);
		}
		return p;
	}
	
	/**
	 * 获取配置
	 * @param propFile
	 * @param clz
	 * @return
	 * @throws Exception
	 */
	public static Properties getProperties(String propFile,Class<?> clz) throws Exception{
		Properties p = new Properties();
		File f = new File(propFile);
		InputStream is;
		if(f.exists()){//从指定路径文件中加载
			is = new FileInputStream(f);
		}else{//从资源文件中加载
			is = PropertiesUtil.class.getResourceAsStream(propFile);
		}
		if(is==null){
			is = PropertiesUtil.class.getClassLoader()
					.getResourceAsStream(propFile);
		}
		
		if(is==null){
			f = new File(clz.getClassLoader().getResource("").getPath()+"/"
					+clz.getPackage().getName().replaceAll("\\.", "/") +"/"+ propFile);
			is = new FileInputStream(f);
		}
		if(is!=null){
			p.load(is);
		}
		return p;
	}
	
}
