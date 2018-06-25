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

	public static Integer parseCount(String aux) {
		// 28 likes, 6.6K Likes, 59 Me gusta, 12 mil Me gusta, 1 vez compartido
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
				} else if (aux.contains("mil ")) {
					a = a.replaceAll(",", ".");
					Double d = Double.valueOf(a);
					return (int) (d * 1000);
				} else if (aux.contains("millones ")) {
					a = a.replaceAll(",", ".");
					Double d = Double.valueOf(a);
					return (int) (d * 1000000);
				} else {
					a = a.replaceAll(",", "");
					return Integer.valueOf(a);
				}
			}
		} catch (Exception ex) {
			System.out.println(aux);
			ex.printStackTrace();
		}
		return null;
	}

	public static void main(String args[]) {
		// 503k views
		// 12k likes
		// 2.7k comments
		// 4.3k shares
		System.out.println(parseCount("28 likes"));
		System.out.println(parseCount("6.6K Likes"));
		System.out.println(parseCount("59 Me gusta"));
		System.out.println(parseCount("2.7k comments"));
		System.out.println(parseCount("503k views"));
		System.out.println(parseCount("4.3k shares"));
		System.out.println(parseCount("1 vez compartido"));

	}

}
