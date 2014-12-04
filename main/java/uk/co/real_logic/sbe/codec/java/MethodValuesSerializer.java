package uk.co.real_logic.sbe.codec.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;

/**
 * Daneel Yaitskov
 */
public class MethodValuesSerializer
{
    private final MethodSelector methodSelector;

    public MethodValuesSerializer(MethodSelector methodSelector)
    {
        this.methodSelector = methodSelector;
    }

    public JsonElement serialize(Object object)
            throws InvocationTargetException, IllegalAccessException
    {
        return serialize(object, true);
    }

    JsonElement serialize(Object object, boolean visitIterable)
            throws InvocationTargetException, IllegalAccessException
    {
        if (object == null)
        {
            return JsonNull.INSTANCE;
        }
        Class clazz = object.getClass();
        if (Number.class.isAssignableFrom(clazz))
        {
            return new JsonPrimitive((Number) object);
        }
        else if (clazz == String.class)
        {
            return new JsonPrimitive((String) object);
        }
        else if (clazz == Boolean.class)
        {
            return new JsonPrimitive((Boolean) object);
        }
        else if (object instanceof Enum)
        {
            return new JsonPrimitive(((Enum) object).name());
        }
        else if (clazz.isArray())
        {
            JsonArray result = new JsonArray();
            int len = Array.getLength(object);
            for (int i = 0; i < len; ++i)
            {
                result.add(serialize(Array.get(object, i)));
            }
            return result;
        }
        else if (visitIterable && Iterable.class.isAssignableFrom(clazz))
        {
            Iterator iter = ((Iterable) object).iterator();
            JsonArray result = new JsonArray();
            while (iter.hasNext())
            {
                result.add(serialize(iter.next(), false));
            }
            return result;
        }
        else if (Map.class.isAssignableFrom(clazz))
        {
            JsonObject result = new JsonObject();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) object).entrySet())
            {
                result.add(entry.getKey().toString(), serialize(entry.getValue()));
            }
            return result;
        }
        else
        {
            return serializeObject(object, clazz);
        }
    }

    private JsonElement serializeObject(Object object, Class clazz)
            throws InvocationTargetException, IllegalAccessException
    {
        JsonObject result = new JsonObject();
        for (Method method : methodSelector.select(clazz))
        {
            Object re = method.invoke(object);
            if (re == object)
            {
                continue;
            }
            result.add(method.getName(), serialize(re));
        }
        for (Method method : clazz.getDeclaredMethods())
        {
            if (method.getReturnType() != Integer.TYPE)
            {
                continue;
            }
            final int mods = method.getModifiers();
            if (!Modifier.isPublic(mods))
            {
                continue;
            }
            if (Modifier.isStatic(mods))
            {
                continue;
            }
            Class[] params = method.getParameterTypes();
            if (params.length != 2
                    || !byte[].class.equals(params[0])
                    || !int.class.equals(params[1]))
            {
                continue;
            }
            String fieldName = Character.toLowerCase(method.getName().charAt(3))
                    + method.getName().substring(4);
            String lengthMethodName = fieldName + "Length";
            try
            {
                Method lengthMethod = clazz.getDeclaredMethod(lengthMethodName);
                int len = (Integer) lengthMethod.invoke(object);
                byte[] buf = new byte[len];
                method.invoke(object, buf, 0);
                int i = 0;
                while (i < buf.length && buf[i] != 0) { ++i; }
                result.add(fieldName, serialize(new String(buf, 0, i)));
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
