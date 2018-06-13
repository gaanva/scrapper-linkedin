package com.rocasolida.scrap.util;

public class ScrapUtils {

	public static String getOSName() {
		try {
			String os = System.getenv("OS");
			if (os == null) {
				os = System.getProperty("os.name");
			}
			if (os != null && os.equalsIgnoreCase("Windows_NT")) {
				os = "Windows";
			}
			return os;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Integer parseLikeCount(String aux) {
		// 28 likes, 6.6K Likes
		try {
			if (aux != null) {
				aux = aux.toLowerCase();
				String a = aux.split(" ")[0];
				if (a.contains("k")) {
					a = a.replaceAll("k", "");
					Double d = Double.valueOf(a);
					return (int) (d * 1000);
				} else if (a.contains("m")) {
					a = a.replaceAll("m", "");
					Double d = Double.valueOf(a);
					return (int) (d * 1000000);
				} else {
					return Integer.valueOf(a);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void main(String args[]) {
		System.out.println(parseLikeCount("28 likes"));
		System.out.println(parseLikeCount("6.6K Likes"));
	}
}
