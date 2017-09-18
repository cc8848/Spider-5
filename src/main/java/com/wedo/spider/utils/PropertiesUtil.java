package com.wedo.spider.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class PropertiesUtil {

	/**
	 * 使用指定的类加载Properties文件，并且随类加载。 且只加载一次，只能加载内部文件。
	 * 
	 * @param clazz
	 * @param propertiesPath
	 * @return 返回Properties对象
	 * @throws IOException
	 */
	public static Properties getByStatic(@SuppressWarnings("rawtypes") final Class clazz, final String propertiesPath)
			throws IOException {
		// 类加载资源文件，优点：随类的加载而加载 缺点：整个生命周期只加载一次,且只能加载应用内部的文件
		InputStream in = clazz.getClassLoader().getResourceAsStream(propertiesPath);
		Properties pro = new Properties();
		pro.load(in);
		return pro;
	}

	/**
	 * 动态加载指定的properties文件，且可以随时加载。 加载多次，可加载外部文件。
	 * 
	 * @param clazz
	 * @param propertiesPath
	 * @return
	 * @throws IOException
	 */
	public static Properties getByDynamic(final String propertiesPath) {
		// 类加载资源文件，优点：可以随时加载，也可以加载外部文件 缺点：需要手动加载
		InputStream in;
		Properties pro = null;
		try {
			in = new FileInputStream(propertiesPath);
			pro = new Properties();
			pro.load(in);
			return pro;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pro;
	}

	/**
	 * 使用类加载Properties文件，并返回Map集 只能加载内部文件
	 * 
	 * @param clazz
	 * @param propertiesPath
	 * @return
	 */
	public static Map<String, String> pro2MapByStatic(@SuppressWarnings("rawtypes") final Class clazz,
			final String propertiesPath) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		Properties pro = getByStatic(clazz, propertiesPath);
		if (pro.isEmpty())
			return null;
		Set set = pro.keySet();
		for (Object s : set)
			map.put((String) s, pro.getProperty((String) s));
		return map;
	}

	/**
	 * 使用FileInputStream加载properties，并返回Map集 可以加载外部文件
	 * 
	 * @param propertiesPath
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> pro2MapByDynamic(final String propertiesPath) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		Properties pro = getByDynamic(propertiesPath);
		if (pro.isEmpty())
			return null;
		Set set = pro.keySet();
		for (Object s : set)
			map.put((String) s, pro.getProperty((String) s));
		return map;
	}

}
