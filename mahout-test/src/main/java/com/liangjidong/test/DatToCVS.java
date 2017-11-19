package com.liangjidong.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class DatToCVS {
	public static void main(String[] args) throws IOException {
		String projectDir = System.getProperty("user.dir");
		File dat = new File(projectDir + "/src/main/ratings.dat");
		File out = new File(projectDir + "/src/main/ratings.base");
		if (!out.exists()) {
			out.createNewFile();
		}
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out)));
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dat)));
		String line = null;
		while ((line = reader.readLine()) != null) {
			line.replaceAll("::", "	");
			writer.write(line);
			writer.write('\n');
		}
		reader.close();
		writer.close();
	}
}
