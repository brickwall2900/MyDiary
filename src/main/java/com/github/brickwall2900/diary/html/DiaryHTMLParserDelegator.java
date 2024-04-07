package com.github.brickwall2900.diary.html;

import com.github.brickwall2900.diary.html.elements.CustomElements;

import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.ParserDelegator;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class DiaryHTMLParserDelegator extends ParserDelegator {
    public DiaryHTMLParserDelegator() {
        try {
//            //for JDK version later than 1.6_26
//            Field f=javax.swing.text.html.parser.ParserDelegator.class.getDeclaredField("DTD_KEY");
//            AppContext appContext = AppContext.getAppContext();
//            f.setAccessible(true);
//            Object dtd_key=f.get(null);
//
//            DTD dtd = (DTD) appContext.get(dtd_key);
//
//            javax.swing.text.html.parser.Element div=dtd.getElement("div");
//            dtd.defineElement("button", div.getType(), true, true,div.getContent(),null, null,div.getAttributes());

            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandles.Lookup parserDelegatorLookup = MethodHandles.privateLookupIn(ParserDelegator.class, lookup);
            MethodHandle dtdKeyGetter = parserDelegatorLookup.findStaticGetter(ParserDelegator.class, "DTD_KEY", Object.class);
            Object dtdKey = dtdKeyGetter.invoke();
            Class<?> appContextClass = lookup.findClass("sun.awt.AppContext");
            MethodHandles.Lookup appContextLookup = MethodHandles.privateLookupIn(appContextClass, lookup);
            MethodHandle appContextGetter = appContextLookup.findStatic(appContextClass, "getAppContext", MethodType.methodType(appContextClass));
            Object appContext = appContextGetter.invoke();
            MethodHandle appContextGet = appContextLookup.findVirtual(appContextClass, "get", MethodType.methodType(Object.class, Object.class));
            DTD dtd = (DTD) appContextGet.invoke(appContext, dtdKey);

//            Field dtdKeyField = ParserDelegator.class.getDeclaredField("DTD_KEY");
//            dtdKeyField.setAccessible(true);
//            Object dtdKey = dtdKeyField.get(null);
//
//            Class<?> appContextClass = Class.forName("sun.awt.AppContext");
//            Method getAppContext = appContextClass.getDeclaredMethod("getAppContext");
//            getAppContext.setAccessible(true);
//            Object appContext = getAppContext.invoke(null);
//            Method get = appContextClass.getDeclaredMethod("get", Object.class);
//            get.setAccessible(true);
//            DTD dtd = (DTD) get.invoke(appContext, dtdKey);

            CustomElements.initElements();
            Element div = dtd.getElement("div");
            for (String name : CustomElements.MAP.keySet()) {
                dtd.defineElement(name, div.getType(), true, true, div.getContent(), null, null, div.getAttributes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            throw new RuntimeException("Something happened.", e);
        }
    }
}
