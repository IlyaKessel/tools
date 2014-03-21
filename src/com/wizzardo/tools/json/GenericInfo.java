package com.wizzardo.tools.json;

import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/21/14
 */
class GenericInfo {
    final Class clazz;
    final GenericInfo[] typeParameters;
    final GenericInfo parent;
    private Map<String, GenericInfo> types;

    GenericInfo(Type c) {
        this(c, (Map) null);
    }

    GenericInfo(Class c, Class... generics) {
        clazz = c;
        parent = null;
        if (generics == null) {
            typeParameters = new GenericInfo[0];
            return;
        }
        typeParameters = new GenericInfo[generics.length];
        for (int i = 0; i < generics.length; i++) {
            typeParameters[i] = new GenericInfo(generics[i]);
        }
    }

    GenericInfo(Type c, Map<String, GenericInfo> types) {
        if (c instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) c;
            clazz = (Class) type.getRawType();
            Type[] args = type.getActualTypeArguments();
            TypeVariable<Class>[] variables = clazz.getTypeParameters();

            this.types = new HashMap<String, GenericInfo>();
            this.typeParameters = new GenericInfo[args.length];
            for (int i = 0; i < args.length; i++) {
                this.typeParameters[i] = new GenericInfo(args[i], types);
                this.types.put(variables[i].getName(), this.typeParameters[i]);
            }

            if (clazz.getGenericSuperclass() != null)
                parent = new GenericInfo(clazz.getGenericSuperclass(), this.types);
            else
                parent = null;
        } else if (c instanceof TypeVariable) {
            if (types != null) {
                GenericInfo g = types.get(((TypeVariable) c).getName());
                clazz = g.clazz;
                parent = g.parent;
                typeParameters = g.typeParameters;
            } else {
                clazz = Object.class;
                parent = null;
                typeParameters = new GenericInfo[0];
            }
        } else if (c instanceof GenericArrayTypeImpl) {
            parent = null;
            clazz = Array.class;
            typeParameters = new GenericInfo[]{new GenericInfo(((GenericArrayTypeImpl) c).getGenericComponentType())};
        } else {
            Class cl = (Class) c;
            if (cl.isArray()) {
                clazz = Array.class;
                typeParameters = new GenericInfo[]{new GenericInfo(cl.getComponentType())};
                parent = null;
                return;
            }

            clazz = cl;
            this.typeParameters = new GenericInfo[0];
            if (!clazz.isEnum() && clazz.getGenericSuperclass() != null)
                parent = new GenericInfo(clazz.getGenericSuperclass(), types);
            else
                parent = null;
        }
    }

    @Override
    public String toString() {
        return clazz.getSimpleName();
    }

    public GenericInfo getGenericType(Field f) {
        GenericInfo g = this;
        while (g != null && g.clazz != f.getDeclaringClass()) {
            g = g.parent;
        }
        if (g != null && f.getGenericType() instanceof TypeVariable) {
            return g.types.get(((TypeVariable) f.getGenericType()).getName());
        }
        return null;
    }
}
