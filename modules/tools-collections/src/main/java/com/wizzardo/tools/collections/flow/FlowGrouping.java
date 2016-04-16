package com.wizzardo.tools.collections.flow;

import com.wizzardo.tools.collections.flow.flows.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wizzardo on 08.11.15.
 */
public class FlowGrouping<K, T, A, B extends FlowGroup<K, T>> extends Flow<A, B> {

    protected final Map<K, FlowGroup<K, T>> groups;

    public FlowGrouping(Map<K, FlowGroup<K, T>> groups) {
        this.groups = groups;
    }

    public <V> Flow<V, V> flatMap(Mapper<? super B, V> mapper) {
        FlowContinue<V> continueCommand = new FlowContinue<V>(this);
        then(new FlowFlatMap<K, V, B>(mapper, continueCommand));
        return continueCommand;
    }

    public Map<K, List<T>> toMap() {
        return toMap(Flow.<K, T>flowGroupListMapper());
    }

    public <V> Map<K, V> toMap(Mapper<? super B, V> mapper) {
        return then(new FlowToMap<K, V, B>((Map<K, V>) groups, mapper)).startAndGet();
    }

    @Override
    public FlowGrouping<K, T, B, B> filter(final Filter<? super B> filter) {
        return this.then(new FlowGrouping<K, T, B, B>(new LinkedHashMap<K, FlowGroup<K, T>>()) {
            @Override
            public void process(B b) {
                if (filter.allow(b)) {
                    groups.put(b.getKey(), b);
                    child.process(b);
                }
            }
        });
    }

    @Override
    public FlowGrouping<K, T, B, B> each(final Consumer<? super B> consumer) {
        return this.then(new FlowGrouping<K, T, B, B>(groups) {
            @Override
            public void process(B b) {
                consumer.consume(b);

                Flow<B, ?> child = this.child;
                if (child != null)
                    child.process(b);
            }
        });
    }

    public FlowGrouping<K, T, B, B> each(final ConsumerWithInt<? super B> consumer) {
        return then(new FlowGrouping<K, T, B, B>(groups) {
            int index = 0;

            @Override
            public void process(B b) {
                consumer.consume(index++, b);

                Flow<B, ?> child = this.child;
                if (child != null)
                    child.process(b);
            }
        });
    }

    public Flow<B, T> merge() {
        return then(new FlowMerge<B, T>());
    }

    public <V> Flow<B, V> merge(Mapper<? super B, ? extends Flow<V, V>> mapper) {
        return then(new FlowMapMerge<B, V>(mapper));
    }
}
