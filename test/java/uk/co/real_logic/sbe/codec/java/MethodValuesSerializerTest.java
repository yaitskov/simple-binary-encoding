package uk.co.real_logic.sbe.codec.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Daneel Yaitskov
 */
public class MethodValuesSerializerTest
{
    enum E1
    {
        A, B
    }

    static List<Class> order = new ArrayList();

    @GroupOrder({X.Y.class, X.Z.class})
    static class X
    {
        class Z
        {
            public boolean publicBoolean()
            {
                order.add(Z.class);
                return true;
            }
        }

        class Y
        {
            public boolean publicBoolean()
            {
                order.add(Y.class);
                return true;
            }
        }

        class S
        {
            public boolean publicBoolean()
            {
                return true;
            }
        }

        public Z publicZ()
        {
            return new Z();
        }

        public Y publicY()
        {
            return new Y();
        }

        public List<S> publicListS()
        {
            return Arrays.asList(new S());
        }

        public E1 publicEnum()
        {
            return E1.A;
        }

        public int[] publicIntArr()
        {
            return new int[] { 1 };
        }

        public String publicString()
        {
            return "hello";
        }

        public int publicInt()
        {
            return 1;
        }

        public X publicSelfIgnored()
        {
            return X.this;
        }

        public int blaGlaLength() {
            return 3;
        }

        public int getBlaGla(final byte[] dst, final int dstof) {
            System.arraycopy("abc".getBytes(), 0, dst, dstof, 3);
            return 3;
        }

        public static int staticPublicIntIgnored()
        {
            return 23;
        }
    }

    @Test
    public void test() throws InvocationTargetException, IllegalAccessException
    {
        MethodValuesSerializer serializer = new MethodValuesSerializer(
                new MethodSelector(MethodSelector.objectAndIteratorMethods()));
        JsonObject expected = new JsonObject();
        expected.add("publicInt", new JsonPrimitive(1));
        expected.add("publicString", new JsonPrimitive("hello"));
        expected.add("publicEnum", new JsonPrimitive("A"));
        expected.add("blaGla", new JsonPrimitive("abc"));
        expected.add("blaGlaLength", new JsonPrimitive(3));
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(1));
        expected.add("publicIntArr", arr);
        JsonArray ss = new JsonArray();
        JsonObject s = new JsonObject();
        s.add("publicBoolean", new JsonPrimitive(true));
        ss.add(s);
        expected.add("publicListS", ss);
        JsonObject z = new JsonObject();
        z.add("publicBoolean", new JsonPrimitive(true));
        expected.add("publicZ", z);
        JsonObject y = new JsonObject();
        y.add("publicBoolean", new JsonPrimitive(true));
        expected.add("publicY", y);
        JsonElement got = serializer.serialize(new X());
        Assert.assertEquals(expected, got);
        Assert.assertEquals(Arrays.asList(X.Y.class, X.Z.class), order);
    }
}
