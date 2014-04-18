package com.st.component.list;

import java.util.List;
import java.util.Set;

import com.st.component.Component;
import com.st.model.Data;
import com.st.processor.Processor;
import com.st.source.DataSource;

public class ListComponent implements Component {

	private Set<Processor> processors;

	public void setProcessors(Set<Processor> processors) {
		this.processors = processors;
	}

	public void processData() {
		for (Processor processor : processors) {
			// processor.process(data);

		}

	}

}
