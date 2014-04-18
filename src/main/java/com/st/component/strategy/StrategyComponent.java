package com.st.component.strategy;

import java.util.Map;

import com.st.component.Component;
import com.st.processor.Processor;

public class StrategyComponent implements Component {
	@SuppressWarnings("unused")
	private Map<String, Processor> processorsMap;
	
	public void setProcessorsMap(Map<String, Processor> processorsMap) {
		this.processorsMap = processorsMap;
	}
	
	public void processData() {

	}

}
