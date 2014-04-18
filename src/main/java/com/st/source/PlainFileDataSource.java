package com.st.source;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.st.model.Data;

public class PlainFileDataSource implements DataSource {

	private String fileName;

	private BufferedReader br;

	public PlainFileDataSource() {
	}

	public void setFileName(String fileName) {
		this.fileName = this.getClass().getResource(fileName).getFile();
	}

	public List<Data> listData() {
		List<Data> dataList = new ArrayList<Data>();
		try {
			String linea;
			while ((linea = this.br.readLine()) != null) {
				Data dato = new Data(linea);
				dataList.add(dato);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataList;
	}

	public void initSource() throws Exception {
		System.out.println("init");
		try {
			FileReader fr = new FileReader(this.fileName);
			this.br = new BufferedReader(fr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void destroySource() {
		System.out.println("destroy");
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
