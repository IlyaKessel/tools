/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.evaluation;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Moxa
 */
public class EvalUtilsTest {

    public EvalUtilsTest() {
    }

    /**
     * Test of evaluate method, of class EvalUtils.
     */
    @Test
    public void testEvaluate() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();
        assertEquals(2, EvalUtils.evaluate("1+1", new HashMap<String, Object>()));
        assertEquals(5, EvalUtils.evaluate("1+1+3", new HashMap<String, Object>()));
        assertEquals("olo123", EvalUtils.evaluate("\"olo\"+1+(1+1)+3", new HashMap<String, Object>()));
        assertEquals("OLO123", EvalUtils.evaluate("(\"olo\"+1+(1+1)+3).toUpperCase()", new HashMap<String, Object>()));

        model.put("ololo", "qwerty");
        model.put("qwe", "  ololo  ");
        model.put("length", "111");
        assertEquals("QWERTYOLOLO123", EvalUtils.evaluate("ololo.concat(qwe.trim().substring(2)).concat(qwe.trim().substring(length.length()) + 123).toUpperCase()", model));

        assertEquals(5f, EvalUtils.evaluate("10f/2", new HashMap<String, Object>()));
        assertEquals(7, EvalUtils.evaluate("1+2*3", new HashMap<String, Object>()));
        assertEquals(9, EvalUtils.evaluate("1+2*(1+3)", new HashMap<String, Object>()));


        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(1, EvalUtils.evaluate("i++", model));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(2, EvalUtils.evaluate("i++ + 1", model));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(3, EvalUtils.evaluate("++i + 1", model));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(4, EvalUtils.evaluate("++i + i++", model));
        assertEquals(3, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(5, EvalUtils.evaluate("++i + ++i", model));
        assertEquals(3, model.get("i"));


        assertEquals(true, EvalUtils.evaluate("true", new HashMap<String, Object>()));
        assertEquals(false, EvalUtils.evaluate("false", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("!false", new HashMap<String, Object>()));


        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(1, EvalUtils.evaluate("i--", model));
        assertEquals(0, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(0, EvalUtils.evaluate("i-- - 1", model));
        assertEquals(0, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(-1, EvalUtils.evaluate("--i - 1", model));
        assertEquals(0, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(0, EvalUtils.evaluate("--i - i--", model));
        assertEquals(-1, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(1, EvalUtils.evaluate("--i - --i", model));
        assertEquals(-1, model.get("i"));


        assertEquals(true, EvalUtils.evaluate("1>0", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("1>=1", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("5>\"123\".length()", new HashMap<String, Object>()));
        assertEquals(false, EvalUtils.evaluate("1>\"123\".length()", new HashMap<String, Object>()));
        assertEquals(false, EvalUtils.evaluate("1<0", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("2<=1*2", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("2>=1*2", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("2== 1 +(3-2)*2-1", new HashMap<String, Object>()));


        model = new HashMap<String, Object>();
        model.put("a", "ololo");
        assertEquals(6, EvalUtils.evaluate("a!=null?a.length()+1:1+\"ololo\"", model));
        assertEquals("1ololo", EvalUtils.evaluate("a==null?a.length()+1:1+\"ololo\"", model));
        assertEquals(15, EvalUtils.evaluate("4+(a!=null?a.length()+1:1+\"ololo\")+5", model));


        model = new HashMap<String, Object>();
        model.put("i", 0);
        assertEquals(5, EvalUtils.evaluate("i+=5", model));
        assertEquals(10, EvalUtils.evaluate("i*=2", model));
        assertEquals(2, EvalUtils.evaluate("i/=5", model));
        assertEquals(0, EvalUtils.evaluate("i-=2", model));
        assertEquals(3, EvalUtils.evaluate("i+=1+2", model));


        model = new HashMap<String, Object>();
        model.put("i", 0);
        assertEquals(3, EvalUtils.evaluate("i=1+2", model));
        assertEquals(3, model.get("i"));


        System.out.println("test logic!");
        model = new HashMap<String, Object>();
        model.put("i", 0);
        int i = 0;
        boolean b = i++ > 1 || i++ > 1 || i++ > 1 || i++ > 1;
        assertEquals(b, EvalUtils.evaluate("i++>1||i++>1||i++>1||i++>1", model));
        assertEquals(i, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 0);
        i = 0;
        b = i++ > 1 || i++ > 1 || i++ > 1 | i++ > 1;
        assertEquals(b, EvalUtils.evaluate("i++>1||i++>1||i++>1|i++>1", model));
        assertEquals(i, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        assertEquals(true, EvalUtils.evaluate("x<++x", model));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        model.put("i", 1);
        model.put("n", 1);
        assertEquals(true, EvalUtils.evaluate("i<n&&x++<x?false:true", model));
        assertEquals(0, model.get("x"));

        model = new HashMap<String, Object>();
        model.put("i", 0);
        i = 0;
        b = i++ < 1 && i++ < 1 && i++ < 1 && i++ < 1;
        assertEquals(b, EvalUtils.evaluate("i++ < 1 && i++ < 1 && i++ < 1 && i++ < 1", model));
        assertEquals(i, model.get("i"));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 0);
        i = 0;
        b = i++ < 1 & i++ < 1 & i++ < 1 & i++ < 1;
        assertEquals(b, EvalUtils.evaluate("i++ < 1 & i++ < 1 & i++ < 1 & i++ < 1", model));
        assertEquals(i, model.get("i"));
        assertEquals(4, model.get("i"));


        assertEquals(true, EvalUtils.evaluate("true&&(false|!true) | 3>2 ", model));

        System.out.println("test static methods");
        assertEquals(1, EvalUtils.evaluate("java.lang.Math.abs(-1)", null));
        assertEquals(1, EvalUtils.evaluate("Math.abs(-1)", null));
        assertEquals(2, EvalUtils.evaluate("Math.abs(-1)+Math.abs(-1)", null));
        assertEquals(2d, EvalUtils.evaluate("Math.sqrt(2+2)", null));

        System.out.println("test constructors");
        assertEquals("ololo", EvalUtils.evaluate("new String(\"ololo\")", null));

        System.out.println("test fields");
        assertEquals(1, EvalUtils.evaluate("new java.awt.Point(1,2).x", null));
        assertEquals(3, EvalUtils.evaluate("new java.awt.Point(1,2).x + new java.awt.Point(1,2).y", null));


        System.out.println("test user functions");
        UserFunction y = new UserFunction("y", "x*2", "x");
        functions = new HashMap<String, UserFunction>();
        functions.put(y.getName(), y);
        assertEquals(10, EvalUtils.evaluate("y(5)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        functions.put("z", new UserFunction("z", "y(x)+x", "x"));
        assertEquals(15, EvalUtils.evaluate("z(5)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        functions.put("z", new UserFunction("z", "y(x)+x", "x"));
        assertEquals(11, EvalUtils.evaluate("z(5) - y(2)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        functions.put("z", new UserFunction("z", "y(x)+x", "x"));
        assertEquals(9, EvalUtils.evaluate("z(5) - z(2)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "2*2", null));
        assertEquals(4, EvalUtils.evaluate("y()", null, functions));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        model.put("g", 0);
        functions = new HashMap<String, UserFunction>();
        functions.put("it", new UserFunction("it", "i<end&&(g=++x)==g?it(i+1,end):g", "i", "end"));
        assertEquals(10, EvalUtils.evaluate("it(0,10)", model, functions));
        assertEquals(10, model.get("x"));
    }

    @Test
    public void testClone() throws Exception {
        String exp = "1+\"ololo\".substring(2)";
        ExpressionHolder eh = EvalUtils.prepare(exp, null);
        Object ob1 = eh.get(null);
        Object ob2 = eh.get(null);
        assertTrue(ob1 == ob2);
        Object ob3 = eh.clone().get(null);
        assertTrue(ob1 != ob3);
        assertTrue(ob1.equals(ob3));

    }
}