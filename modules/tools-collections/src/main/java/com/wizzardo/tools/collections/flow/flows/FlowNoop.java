package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.FlowProcessor;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowNoop<T> extends FlowProcessor<T, Object> {
    @Override
    protected void onEnd() {
    }

    @Override
    public void process(T t) {
    }
}
