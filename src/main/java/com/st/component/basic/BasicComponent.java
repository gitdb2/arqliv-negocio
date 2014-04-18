package com.st.component.basic;

import java.util.List;

import com.st.component.Component;
import com.st.model.Data;
import com.st.source.DataSource;

public class BasicComponent implements Component {

	private DataSource source;

	public void setSource(DataSource source) {
		this.source = source;
	}

	public void processData() {
		List<Data> dataList = this.source.listData();

		for (Data data : dataList) {
			System.out.println(data);
		}
	}

}
