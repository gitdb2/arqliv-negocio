package com.st.source;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.st.model.Data;

public class XmlFileDataSource implements DataSource {

	private String fileName;

	public void setFileName(String fileName) {
		this.fileName = this.getClass().getResource(fileName).getFile();
	}

	public XmlFileDataSource() {
	}

	public List<Data> listData() {
		List<Data> dataList = new ArrayList<Data>();

		DocumentBuilderFactory DBFactory = DocumentBuilderFactory.newInstance();
		DBFactory.setIgnoringElementContentWhitespace(true);
		try {
			DocumentBuilder builder = DBFactory.newDocumentBuilder();
			FileInputStream is = new FileInputStream(this.fileName);
			InputStreamReader in = new InputStreamReader(is);
			InputSource iss = new InputSource(in);
			Document doc = builder.parse(iss);
			Element element = doc.getDocumentElement();
			NodeList datosXml = element.getChildNodes();
			for (int i = 0; i < datosXml.getLength(); i++) {
				Node datoXml = datosXml.item(i);
				if (datoXml.getNodeType() == Node.ELEMENT_NODE) {
					Data dato = new Data(datoXml.getTextContent());
					dataList.add(dato);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataList;
	}
}