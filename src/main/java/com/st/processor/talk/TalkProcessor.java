package com.st.processor.talk;

import com.st.model.Data;
import com.st.processor.Processor;

public class TalkProcessor implements Processor {

    public TalkProcessor() {
    }

    public void process(Data data) {
        System.out.println("speak "+data);
    }
}
