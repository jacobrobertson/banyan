/*
 * @version Sep 18, 2004
 */
package com.robestone.banyan.util;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Jacob Robertson
 */
public class EntityMapperHelperMethods {

    public static void main(String[] args) throws Exception {
    	System.out.println('×' + (int) '×');
//        dumpAllSymbols();
//    	outputEntityHtmlHelperPage(new OutputStreamWriter(new FileOutputStream("C:\\Development\\Projects\\Roots\\files\\HtmlWork\\entities.html")));
    }
	public static void outputEntityHtmlHelperPage(Writer out) throws Exception {
		out.write("<html><body><table border='1'>");
        List<Object> entities = EntityMapper.getEntities();
        Collections.sort(entities, new EntityComparator());
        int len = entities.size();
        for (int i = 0; i < len; i++) {
            Entity e = (Entity) entities.get(i);
    		out.write("<tr><td>");
    		out.write(e.getName());
    		out.write("</td><td>&");
    		out.write(e.getName());
    		out.write(";</td><td>");
    		out.write(e.getSearchText());
    		out.write("</td><td>");
    		out.write(e.getSymbol());
    		out.write("</td><td>");
    		out.write(String.valueOf(e.getNumber()));
    		out.write("</td><td>");
    		out.write(EntityMapper.convertToHTMLRenderedEntities(String.valueOf("["+e.getName()+"]")));
    		out.write("</td><td>&#");
    		out.write(String.valueOf(e.getNumber()));
    		out.write(";</td></tr>");
        }
		out.write("</table></body></html>");
		out.close();
	}
    public static void dumpAllSymbols() {
        List<Object> entities = new ArrayList<Object>(); // null;//EntityMapper.getEntities();
        Collections.sort(entities, new EntityComparator());
        int len = entities.size();
        for (int i = 0; i < len; i++) {
            Entity e = (Entity) entities.get(i);
//    		addEntity("AElig", 198, "Æ", "AE");
            String line = "\taddEntity(\"" + e.getName() + "\",";
            line = StringUtils.rightPad(line, 27, ' ');
            line = line + e.getNumber() + ",";
            line = StringUtils.rightPad(line, 33, ' ');
            line = line + "\"" + (char) e.getNumber() + "\",";
            line = StringUtils.rightPad(line, 38, ' ');
            line = line + "\"" + e.getSearchText() + "\"); ";
          line = StringUtils.rightPad(line, 45, ' ');
          String hex = Integer.toString(e.getNumber(), 16).toUpperCase();
          hex = StringUtils.leftPad(hex, 4, '0');
          line = line + "// " + hex;
          
          System.out.println(line);
        }
    }
    public static class EntityComparator implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            Entity e1 = (Entity) o1;
            Entity e2 = (Entity) o2;
            
            int comp = Integer.valueOf(e1.getNumber()).compareTo(Integer.valueOf(e2.getNumber()));
            if (comp == 0) {
                return e1.getName().compareTo(e2.getName());
            } else {
                return comp;
            }
        }
    }
}
