package com.st.processor;

import com.st.model.Data;

public class PrintProcessor implements Processor {

	public void process(Data data) {
		System.out.println(data);
	}
}
