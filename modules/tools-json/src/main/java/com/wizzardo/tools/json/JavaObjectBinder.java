package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.CharTree;
import com.wizzardo.tools.misc.Pair;
import com.wizzardo.tools.misc.Unchecked;

import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JavaObjectBinder implements JsonBinder {
    protected Object object;
    protected Class clazz;
    protected JsonGeneric generic;
    protected JsonFields fields;
    protected JsonFieldInfo tempKey;
    protected String tempKeyName;
    protected CharTree<Pair<String, JsonFieldInfo>> fieldsTree;

    public JavaObjectBinder(JsonGeneric<?> generic) {
        this.clazz = generic.clazz;
        this.generic = generic;
        fields = generic.getFields();
        fieldsTree = generic.getFieldsTree();
        reset();
    }

    protected Object createInstance(Class clazz) {
        return Binder.createObject(clazz);
    }

    @Override
    public void add(Object value) {
        JsonFieldInfo fieldInfo = tempKey;
        if (fieldInfo == null)
            return;
        fieldInfo.reflection.setObject(object, value);
    }

    @Override
    public void add(JsonItem value) {
        throw new UnsupportedOperationException("Adding JsonItem is not supported while parsing into " + clazz);
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public JsonBinder getObjectBinder() {
        JsonFieldInfo info = tempKey;
        if (info == null)
            return null;

        if (Map.class.isAssignableFrom(info.field.getType()))
            return new JavaMapBinder(info.generic);

        if (info.serializer.type != Binder.SerializerType.OBJECT && info.serializer.type != Binder.SerializerType.NULL)
            throw new IllegalStateException("Cannot put object data into field " + info.field);

        try {
            return new JavaObjectBinder(info.generic);
        } catch (Exception e) {
            if (e.getClass().equals(NoSuchMethodException.class)) {
                throw new IllegalStateException("Cannot put object data into field " + info.field);
            } else
                throw Unchecked.rethrow(e);
        }
    }

    @Override
    public JsonBinder getArrayBinder() {
        JsonFieldInfo info = tempKey;
        if (info == null)
            return null;

        JsonGeneric type = generic.getGenericType(info.field);
        if (type != null)
            return new JavaArrayBinder(type);

        return new JavaArrayBinder(info.generic);
    }

    @Override
    public void setTemporaryKey(String key) {
        tempKeyName = key;
        tempKey = null;
    }

    @Override
    public void setTemporaryKey(Pair<String, JsonFieldInfo> pair) {
        tempKeyName = pair.key;
        tempKey = pair.value;
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        JsonFieldInfo f = tempKey;
        return f != null ? f.reflection : null;
    }

    @Override
    public CharTree.CharTreeNode<Pair<String, JsonFieldInfo>> getFieldsTree() {
        return fieldsTree.getRoot();
    }

    @Override
    public void reset() {
        object = createInstance(clazz);
    }
}
