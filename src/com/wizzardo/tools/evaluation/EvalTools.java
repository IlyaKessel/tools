/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.CollectionTools;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Moxa
 */
public class EvalTools {
    static final String CONSTRUCTOR = "%constructor%";
    static EvaluatingStrategy defaultEvaluatingStrategy;
    private static AtomicInteger variableCounter = new AtomicInteger();

    protected static int countOpenBrackets(String s, int from, int to) {
        int n = 0;
        for (int i = from; i < to; i++) {
            if (s.charAt(i) == '(' || s.charAt(i) == '[' || s.charAt(i) == '{') {
                n++;
            } else if (s.charAt(i) == ')' || s.charAt(i) == ']' || s.charAt(i) == '}') {
                n--;
            }
        }
        return n;
    }

    protected static String getTempVariableName() {
        return "__tempVariable_" + variableCounter.incrementAndGet();
    }

    protected static boolean inString(String s, int from, int to) {
        boolean inString = false;
        char quote = 0;
        for (int i = from; i < to; i++) {
            if (!inString) {
                if ((s.charAt(i) == '\'' || s.charAt(i) == '\"') && (i == 0 || (i >= 1 && s.charAt(i - 1) != '\\'))) {
                    quote = s.charAt(i);
                    inString = true;
                }
            } else if ((s.charAt(i) == quote) && i >= 1 && s.charAt(i - 1) != '\\') {
                inString = false;
            }
        }
        return inString;
    }

    protected static LinkedList<String> getParts(String s) {
        LinkedList<String> l = new LinkedList<String>();
        boolean inString = false;
        char quote = 0;
        char[] chars = s.toCharArray();
        int from = 0;
        int brackets = 0;
        int squareBrackets = 0;
        int curlyBraces = 0;
        for (int i = 0; i < chars.length; i++) {
            if (!inString) {
                switch (chars[i]) {
                    case '"':
                    case '\'': {
                        quote = chars[i];
                        inString = true;
                        break;
                    }
                    case '(': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            l.add(new String(chars, from, i - from));
                            from = i;
                        }
                        brackets++;
                        break;
                    }
                    case '{': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            l.add(new String(chars, from, i - from));
                            from = i;
                        }
                        curlyBraces++;
                        break;
                    }
                    case '[': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            l.add(new String(chars, from, i - from));
                            from = i;
                        }
                        squareBrackets++;
                        break;
                    }
                    case ')': {
                        brackets--;
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            l.add(new String(chars, from, i + 1 - from));
                            from = i + 1;
                        }
                        break;
                    }
                    case '}': {
                        curlyBraces--;
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            l.add(new String(chars, from, i + 1 - from));
                            from = i + 1;
                        }
                        break;
                    }
                    case ']': {
                        squareBrackets--;
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            l.add(new String(chars, from, i + 1 - from));
                            from = i + 1;
                        }
                        break;
                    }
                    case '.': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from && i > 0 && chars[i - 1] != '*') {
                            l.add(new String(chars, from, i - from));
                            from = i;
                        }
                        break;
                    }
                }
            } else if ((chars[i] == quote) && i > 1 && chars[i - 1] != '\\') {
                inString = false;
            }
        }
        if (from != chars.length) {
            l.add(new String(chars, from, chars.length - from));
        }
        return l;
    }

    public static enum EvaluatingStrategy {
        DEFAULT_JAVA, FLOAT, DOUBLE
    }

    public static void setDefaultEvaluatingStrategy(EvaluatingStrategy defaultEvaluatingStrategy) {
        EvalTools.defaultEvaluatingStrategy = defaultEvaluatingStrategy;
    }

    public static Expression prepare(String exp) {
        return prepare(exp, null, new HashMap<String, UserFunction>());
    }

    public static Expression prepare(String exp, Map<String, Object> model) {
        return prepare(exp, model, new HashMap<String, UserFunction>());
    }

    private static Pattern brackets = Pattern.compile("[\\(\\)]");

    public static String trimBrackets(String s) {
        int db = s.indexOf("((");
        if (db != -1) {
            String t = s.substring(db + 2);
            Matcher m = brackets.matcher(t);
            int brackets = 2;
            boolean innerBrackets = false;
            while (m.find()) {
                if (m.group().equals("(")) {
                    if (brackets < 2)
                        innerBrackets = true;
                    brackets++;
                } else if (m.group().equals(")")) {
                    brackets--;
                }
                if (!innerBrackets && brackets == 0 && m.start() > 0 && t.charAt(m.start() - 1) == ')') {
                    return trimBrackets(s.substring(0, db + 1) + t.substring(0, m.start() - 1) + t.substring(m.start()));
                }
            }
        }

        if (s.startsWith("(") && s.endsWith(")")) {
            Matcher m = brackets.matcher(s);
            int brackets = 0;
            while (m.find()) {
                if (m.group().equals("(")) {
                    brackets++;
                } else if (m.group().equals(")")) {
                    brackets--;
                }
                if (brackets == 0 && m.end() != s.length()) {
                    return s;
                }
            }
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static boolean isMap(String s) {
        if (!s.startsWith("[") || !s.endsWith("]")) {
            return false;
        }
        boolean quotesSingle = false;
        boolean quotesDouble = false;
        int brackets = 0;
        for (int i = 1; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '\'':
                    if (!quotesDouble)
                        quotesSingle = !quotesSingle;
                    break;
                case '"':
                    if (!quotesSingle)
                        quotesDouble = !quotesDouble;
                    break;
                case '[': {
                    if (!quotesSingle && !quotesDouble)
                        brackets++;
                    break;
                }
                case ']': {
                    if (!quotesSingle && !quotesDouble)
                        brackets--;
                    break;
                }
                case ':':
                    if (!quotesSingle && !quotesDouble && brackets == 0) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private static boolean isList(String s) {
        return s.startsWith("[") && s.endsWith("]");
    }

    private static boolean isClosure(String exp) {
        return exp.startsWith("{") && exp.endsWith("}");
    }

    static List<String> getLines(String exp) {
        List<String> list = new ArrayList<String>();

        StringBuilder sb = new StringBuilder();
        char last = 0, stringChar = 0;
        boolean inString = false;
        for (char c : exp.toCharArray()) {
            if (inString) {
                if (c == stringChar && last != '\\') {
                    inString = false;
                }
            } else {
                if (c == ';' || c == '\n') {
                    String value = sb.toString().trim();
                    if (value.length() > 0)
                        list.add(value);
                    sb.setLength(0);
                    continue;
                }
                if (c == '"' || c == '\'') {
                    inString = true;
                }
            }
            last = c;
            sb.append(c);
        }
        String value = sb.toString().trim();
        if (value.length() > 0)
            list.add(sb.toString().trim());

        return list;
    }


    public static Expression prepare(String exp, Map<String, Object> model, Map<String, UserFunction> functions) {
//        System.out.println("try to prepare: " + exp);
        if (exp == null) {
            return null;
        }
        exp = exp.trim();
        String trimmed = trimBrackets(exp);
        while (trimmed != exp) {
            exp = trimmed;
            trimmed = trimBrackets(exp);
        }
        if (exp.length() == 0) {
            return null;
        }

        if (model == null) {
            model = new HashMap<String, Object>();
        }

        if (isClosure(exp)) {
            exp = exp.substring(1, exp.length() - 1).trim();
            ClosureExpression closure = new ClosureExpression();
            List<String> lines = getLines(exp);
            String firstLine = lines.get(0);
            firstLine = closure.parseArguments(firstLine);
            if (firstLine.length() == 0)
                lines.remove(0);
            else
                lines.set(0, firstLine);

            for (String s : lines) {
                closure.add(prepare(s, model, functions));
            }
            return closure;
        }

        {
            List<String> lines = getLines(exp);
            if (lines.size() > 1) {
                ClosureExpression closure = new ClosureExpression();
                for (String s : lines) {
                    closure.add(prepare(s, model, functions));
                }
                return closure;
            }

        }

        {
            if (exp.equals("null")) {
                return Expression.Holder.NULL;
            }
            Object obj = Expression.parse(exp);
            if (obj != null) {
                return new Expression.Holder(exp);
            }
            if (model.containsKey(exp)) {
                return new Expression.Holder(exp);
            }
            {
                Pattern p = Pattern.compile("def +([a-z]+[a-zA-Z_\\d]*)$");
                Matcher m = p.matcher(exp);
                if (m.find()) {
                    model.put(m.group(1), null);
                    return new Expression.Holder(m.group(1));
                }
            }
        }

        {
            Matcher m = actions.matcher(exp);
            List<String> exps = new ArrayList<String>();
            List<Operation> operations = new ArrayList<Operation>();
            int last = 0;
            Operation operation = null;
            Expression lastExpressionHolder = null;
            boolean ternary = false;
            int ternaryInner = 0;
            while (m.find()) {
                if (ternary) {
                    if (m.group().equals("?")) {
                        ternaryInner++;
                        continue;
                    }
                    if (!m.group().equals(":")) {
                        continue;
                    }
                    if (ternaryInner > 0) {
                        ternaryInner--;
                        continue;
                    }
                }
//                System.out.println(m.group());
                if (countOpenBrackets(exp, last, m.start()) == 0 && !inString(exp, last, m.start())) {
                    exps.add(exp.substring(last, m.start()).trim());
//                    lastExpressionHolder = new ExpressionHolder(exp.substring(last, m.start()));
                    lastExpressionHolder = prepare(exp.substring(last, m.start()), model, functions);
                    if (operation != null) {
                        //complete last operation
                        operation.end(m.start());
//                        operation.rightPart(new ExpressionHolder(exp.substring(last, operation.end())));
                        operation.rightPart(lastExpressionHolder);
                    }
                    operation = new Operation(lastExpressionHolder, Operator.get(m.group()), last, m.end());
                    operations.add(operation);
                    //add operation to list
                    last = m.end();
                    if (ternary) {
//                        lastExpressionHolder = new ExpressionHolder(exp.substring(last, exp.length()));
                        lastExpressionHolder = prepare(exp.substring(last, exp.length()), model, functions);
                        operation.rightPart(lastExpressionHolder);
                        break;
                    }
                    if (m.group().equals("?")) {
                        ternary = true;
                    }
                }
            }
            if (operation != null) {
                if (last != exp.length()) {
                    exps.add(exp.substring(last).trim());
                    operation.end(exp.length());
//                    operation.rightPart(new ExpressionHolder(exp.substring(last)));
                    operation.rightPart(prepare(exp.substring(last), model, functions));
                }

                Expression eh = null;
                while (operations.size() > 0) {
                    operation = null;
                    int n = 0;
                    for (int i = 0; i < operations.size(); i++) {
                        if (operation == null || operations.get(i).operator().priority > operation.operator().priority) {
                            operation = operations.get(i);
                            n = i;
                        }
                    }
                    if (operation.operator() == Operator.TERNARY) {
                        int ternaryIndex = n;
                        operation = null;
                        n = 0;
                        for (int i = 0; i < ternaryIndex; i++) {
                            if (operation == null || operations.get(i).operator().priority > operation.operator().priority) {
                                operation = operations.get(i);
                                n = i;
                            }
                        }
                        if (operation == null) {
                            operation = operations.get(0);
                        }
                    }

                    if (operation.operator() == Operator.TERNARY) {
                        operation.rightPart(operations.remove(n + 1));
                    }

                    //System.out.println("operation: " + operation);
                    if (n > 0) {
                        operations.get(n - 1).rightPart(operation);
                    }
                    if (n < operations.size() - 1) {
                        operations.get(n + 1).leftPart(operation);
                    }
                    eh = operations.remove(n);
                }
                return eh;
            }
        }

        {
            if (exp.equals("[]")) {
                return new Expression.CollectionExpression(new ArrayList());
            }
            if (exp.equals("[:]")) {
                return new Expression.MapExpression(new LinkedHashMap());
            }
            if (isMap(exp)) {
                Map<String, Expression> map = new LinkedHashMap<String, Expression>();
                for (Map.Entry<String, String> entry : parseMap(exp).entrySet()) {
                    map.put(entry.getKey(), prepare(entry.getValue(), model, functions));
                }
                return new Expression.MapExpression(map);
            }
            if (isList(exp)) {
                ArrayList l = new ArrayList();
                exp = exp.substring(1, exp.length() - 1);
                List<String> arr = parseArgs(exp);
                for (int i = 0; i < arr.size(); i++) {
                    l.add(prepare(arr.get(i), model, functions));
                }
                return new Expression.CollectionExpression(l);
            }
        }

        Expression thatObject = null;
        String methodName = null;
        {
            Pattern p = Pattern.compile("new ([a-z]+\\.)*(\\b[A-Z][a-zA-Z\\d]+)");
            Matcher m = p.matcher(exp);
            if (m.find()) {
                Class clazz = findClass(m.group().substring(4));
                if (clazz != null) {
                    thatObject = new Expression.Holder(clazz);
                    exp = exp.substring(m.end());
                    methodName = CONSTRUCTOR;
                }
            }
        }

        if (thatObject == null) {
            Pattern p = Pattern.compile("([a-z]+[a-zA-Z\\d]*)\\[");
            Matcher m = p.matcher(exp);
            if (m.find()) {
                thatObject = new Expression.Holder(m.group(1));
                exp = exp.substring(m.group(1).length());
            }
        }

        if (thatObject == null) {
            Pattern p = Pattern.compile("([a-z]+\\.)*(\\b[A-Z][a-zA-Z\\d]+)");
            Matcher m = p.matcher(exp);
            if (m.find()) {
                Class clazz = findClass(m.group());
                if (clazz != null) {
                    thatObject = new Expression.Holder(clazz);
                    exp = exp.substring(m.end());
                }
            }
        }

        if (thatObject == null) {
            Pattern p = Pattern.compile("^([a-z_]+\\w*)\\(.+");
            Matcher m = p.matcher(exp);
            if (m.find()) {
//                System.out.println("find user function: " + m.group(1) + "\t from " + exp);
//                System.out.println("available functions: " + functions);
                UserFunction function = functions.get(m.group(1)).clone();
                thatObject = function;
                exp = exp.substring(function.getName().length());
            }
        }


        List<String> parts = getParts(exp);
        String last = null;
//        System.out.println(exp);
        while (!parts.isEmpty()) {
            String temp = parts.toString();
            if (temp.equals(last))
                throw new IllegalStateException("loop at " + exp + "\t\t" + parts);
            last = temp;


            if (thatObject == null && parts.size() == 1 && parts.get(0).equals(exp)) {
                thatObject = new Expression.Holder(parts.remove(0));
                continue;
            }
            if (thatObject == null) {
                thatObject = prepare(parts.remove(0), model, functions);
                continue;
            }

            //.concat("ololo")
            if (parts.size() >= 2 && parts.get(0).startsWith(".")
                    && ((parts.get(1).startsWith("(") && parts.get(1).endsWith(")")) || (parts.get(1).startsWith("{") && parts.get(1).endsWith("}")))) {
                methodName = parts.remove(0).substring(1);
                Expression[] args = null;
                String argsRaw = trimBrackets(parts.remove(0));
                if (argsRaw.length() > 0) {
                    List<String> arr = parseArgs(argsRaw);
                    args = new Expression[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        args[i] = prepare(arr.get(i), model, functions);
                    }
                }
                thatObject = new Function(thatObject, methodName, args);

                //*.concat("ololo")
            } else if (parts.size() >= 1 && parts.get(0).startsWith("*.")) {
                String var = getTempVariableName();
                Expression[] args = new Expression[1];
                methodName = parts.remove(0).substring(1);
                String argsRaw;
                if (parts.size() >= 1 && parts.get(0).startsWith("(") && parts.get(0).endsWith(")"))
                    argsRaw = parts.remove(0);
                else
                    argsRaw = "";

                args[0] = prepare("{" + var + " -> " + var + methodName + argsRaw + "}", model, functions);
                thatObject = new Function(thatObject, "collect", args);

                //("ololo")
            } else if (parts.get(0).startsWith("(") && parts.get(0).endsWith(")")) {
                Expression[] args = null;
                String argsRaw = trimBrackets(parts.remove(0));
                if (argsRaw.length() > 0) {
                    List<String> arr = parseArgs(argsRaw);
                    args = new Expression[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        args[i] = prepare(arr.get(i), model, functions);
                    }
                }
                if (thatObject instanceof UserFunction) {
                    UserFunction function = (UserFunction) thatObject;
                    function.setArgs(args);
                    function.setUserFunctions(functions);
                } else {
                    thatObject = new Function(thatObject, methodName, args);
                }

                //.x
            } else if (parts.get(0).startsWith(".")) {
                String field = parts.remove(0).substring(1);
                thatObject = new Function(thatObject, field);

                //[0]
            } else if (parts.get(0).startsWith("[") && parts.get(0).endsWith("]")) {
                String argsRaw = parts.remove(0);
                argsRaw = argsRaw.substring(1, argsRaw.length() - 1);
                thatObject = new Operation(thatObject, prepare(argsRaw, model, functions), Operator.GET);
            }
        }

        return thatObject;
    }


    private static Class findClass(String s) {
        return findClass(s, null);
    }

    private static Class findClass(String s, String... imports) {
        try {
            return ClassLoader.getSystemClassLoader().loadClass(s);
        } catch (ClassNotFoundException e) {
            //ignore
        }
        try {
            return ClassLoader.getSystemClassLoader().loadClass("java.lang." + s);
        } catch (ClassNotFoundException e) {
            //ignore
        }
        if (imports != null) {
            for (String imp : imports) {
                if (imp.endsWith("." + s))
                    try {
                        return ClassLoader.getSystemClassLoader().loadClass(imp);
                    } catch (ClassNotFoundException e) {
                        //ignore
                    }
            }
        }
        return null;
    }

    private static List<String> parseArgs(String argsRaw) {
        ArrayList<String> l = new ArrayList<String>();
        Pattern p = Pattern.compile(",");
        Matcher m = p.matcher(argsRaw);
        int last = 0;
        while (m.find()) {
            if (countOpenBrackets(argsRaw, last, m.start()) == 0) {
                l.add(argsRaw.substring(last, m.start()));
                last = m.end();
            }
        }
        if (last > 0) {
            l.add(argsRaw.substring(last));
        } else if (last == 0 && argsRaw.length() > 0) {
            l.add(argsRaw);
        }
        return l;
    }

    private static Map<String, String> parseMap(String s) {
        Map<String, String> m = new LinkedHashMap<String, String>();
        s = s.substring(1, s.length() - 1);
        boolean quotesSingle = false;
        boolean quotesDouble = false;
        StringBuilder sb = new StringBuilder();
        String key = null;
        boolean escape = false;
        int brackets = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\'':
                    sb.append(ch);
                    if (!quotesDouble && !escape)
                        quotesSingle = !quotesSingle;
                    escape = false;
                    break;
                case '"':
                    sb.append(ch);
                    if (!quotesSingle && !escape)
                        quotesDouble = !quotesDouble;
                    escape = false;
                    break;
                case '(':
                    if (!quotesSingle && !quotesDouble) {
                        brackets++;
                    }
                    sb.append(ch);
                    break;
                case '[':
                    if (!quotesSingle && !quotesDouble) {
                        brackets++;
                    }
                    sb.append(ch);
                    break;
                case ')':
                    if (!quotesSingle && !quotesDouble) {
                        brackets--;
                    }
                    sb.append(ch);
                    break;
                case ']':
                    if (!quotesSingle && !quotesDouble) {
                        brackets--;
                    }
                    sb.append(ch);
                    break;
                case ':':
                    if (brackets > 0) {
                        sb.append(ch);
                        break;
                    }
                    if (!quotesSingle && !quotesDouble) {
                        key = sb.toString().trim();
                        sb.setLength(0);
                    } else {
                        sb.append(ch);
                    }
                    break;
                case ',':
                    if (brackets > 0) {
                        sb.append(ch);
                        break;
                    }
                    if (!quotesSingle && !quotesDouble) {
                        String value = sb.toString().trim();
                        m.put(key, value);
                        key = null;
                        sb.setLength(0);
                    } else {
                        sb.append(ch);
                    }
                    break;
                case '\\':
                    escape = !escape;
                    if (!escape) {
                        sb.append(ch);
                    }
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        if (sb.length() > 0) {
            String value = sb.toString().trim();
            m.put(key, value);
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String exp) {
//        System.out.println("evaluate: " + exp + "\t" + model);
        Expression ex = prepare(exp, null);
        return (T) ex.get(null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String exp, Map<String, Object> model) throws Exception {
//        System.out.println("evaluate: " + exp + "\t" + model);
        Expression ex = prepare(exp, model);
        return (T) ex.get(model);
    }

    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String exp, Map<String, Object> model, Map<String, UserFunction> functions) throws Exception {
//        System.out.println("evaluate: " + exp + "\t" + model);
        Expression ex = prepare(exp, model, functions);
        return (T) ex.get(model);
    }


    private static final Pattern actions = Pattern.compile("\\+\\+|--|\\.\\.|\\*=|\\*(?!\\.)|/=?|\\+=?|-=?|:|<<|<=?|>=?|==?|%|!=?|\\?|&&?|\\|\\|?");

    static {
        Function.setMethod(Collection.class, "collect", new CollectionTools.Closure3<Object, Object, Map, Object[]>() {
            @Override
            public Object execute(Object it, Map model, Object[] args) {
                List l = new ArrayList();
                ClosureExpression closure = (ClosureExpression) args[0];
                Collection c = (Collection) it;
                for (Object ob : c) {
                    l.add(closure.get(model, ob));
                }
                return l;
            }
        });
        Function.setMethod(Collection.class, "find", new CollectionTools.Closure3<Object, Object, Map, Object[]>() {
            @Override
            public Object execute(Object it, Map model, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                for (Object ob : c) {
                    if ((Boolean) closure.get(model, ob)) {
                        return ob;
                    }
                }
                return null;
            }
        });
        Function.setMethod(Collection.class, "findAll", new CollectionTools.Closure3<Object, Object, Map, Object[]>() {
            @Override
            public Object execute(Object it, Map model, Object[] args) {
                List l = new ArrayList();
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                for (Object ob : c) {
                    if ((Boolean) closure.get(model, ob)) {
                        l.add(ob);
                    }
                }
                return l;
            }
        });
        Function.setMethod(Collection.class, "findIndexOf", new CollectionTools.Closure3<Object, Object, Map, Object[]>() {
            @Override
            public Object execute(Object it, Map model, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                int i = 0;
                for (Object ob : c) {
                    if ((Boolean) closure.get(model, ob)) {
                        return i;
                    }
                    i++;
                }
                return -1;
            }
        });
        Function.setMethod(Collection.class, "each", new CollectionTools.Closure3<Object, Object, Map, Object[]>() {
            @Override
            public Object execute(Object it, Map model, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                for (Object ob : c) {
                    closure.get(model, ob);
                }
                return null;
            }
        });
        Function.setMethod(Collection.class, "eachWithIndex", new CollectionTools.Closure3<Object, Object, Map, Object[]>() {
            @Override
            public Object execute(Object it, Map model, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                int i = 0;
                for (Object ob : c) {
                    closure.get(model, ob, i++);
                }
                return null;
            }
        });
        Function.setMethod(Collection.class, "every", new CollectionTools.Closure3<Object, Object, Map, Object[]>() {
            @Override
            public Object execute(Object it, Map model, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                for (Object ob : c) {
                    if (!(Boolean) closure.get(model, ob))
                        return false;
                }
                return true;
            }
        });
        Function.setMethod(Collection.class, "any", new CollectionTools.Closure3<Object, Object, Map, Object[]>() {
            @Override
            public Object execute(Object it, Map model, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                for (Object ob : c) {
                    if ((Boolean) closure.get(model, ob))
                        return true;
                }
                return false;
            }
        });
        Function.setMethod(Collection.class, "join", new CollectionTools.Closure3<Object, Object, Map, Object[]>() {
            @Override
            public Object execute(Object it, Map model, Object[] args) {
                StringBuilder sb = new StringBuilder();
                Collection c = (Collection) it;
                for (Object ob : c) {
                    if (sb.length() != 0) {
                        sb.append(args[0]);
                    }
                    sb.append(ob);
                }
                return sb.toString();
            }
        });

        Function.setMethod(Number.class, "multiply", new CollectionTools.Closure3<Object, Object, Map, Object[]>() {

            @Override
            public Object execute(Object it, Map model, Object[] args) {
                if (args.length != 1)
                    throw new MissingMethodException(it.getClass(), "multiply", args);

                return Operation.multiply(it, args[0]);
            }
        });
    }

    public static void main(String[] args) {
        System.out.println(trimBrackets("sin((1+2)/(3))"));
    }
}