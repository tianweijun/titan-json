package titan.json.serialization;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Stack;
import titan.json.JsonParseRuntimeException;
import titan.json.reflector.Primitives;

/**
 * .
 *
 * @author tian wei jun
 */
public class PrettySerialization {
  Object src;
  Stack<Integer> blankCountStack = new Stack<>();
  static final String NEWLINE = "\n";
  static final int ADDED_BLANK_COUNT = 2;

  public PrettySerialization(Object src) {
    this.src = src;
  }

  public String toJson() {
    if (null == src) {
      return "";
    }
    blankCountStack.push(0);
    String strJson = toJson(src);
    blankCountStack.pop();
    return strJson;
  }

  public String toJson(Object src) {
    String strJson = null;
    Class<?> clazz = src.getClass();
    if (Primitives.isWrapperType(clazz)) { // primitive
      strJson = src.toString();
    } else if (src instanceof String) {
      strJson = "\"" + (String) src + "\"";
    } else if (clazz.isArray()) {
      strJson = toJsonByArray(src);
    } else { // object
      strJson = toJsonByObject(src);
    }
    return strJson;
  }

  private String toJsonByArray(Object array) {
    StringBuilder strJson = new StringBuilder("[").append(NEWLINE);
    Integer blankCount = blankCountStack.peek() + ADDED_BLANK_COUNT;
    blankCountStack.push(blankCount);
    int length = Array.getLength(array);
    for (int i = 0; i < length; i++) {
      Object element = Array.get(array, i);
      String elementStrJson = "";
      if (null == element) {
        elementStrJson = "null";
      } else {
        elementStrJson = toJson(element);
      }
      for (int j = 0; j < blankCount; j++) {
        strJson.append(" ");
      }
      strJson.append(elementStrJson).append(",").append(NEWLINE);
    }
    boolean hasRedundantComma = length > 0;
    if (hasRedundantComma) { // 删除多余的','
      strJson.deleteCharAt(strJson.length() - 1 - NEWLINE.length());
    }
    blankCountStack.pop();
    for (int j = 0; j < blankCountStack.peek(); j++) {
      strJson.append(" ");
    }
    strJson.append("]");
    return strJson.toString();
  }

  private String toJsonByObject(Object obj) {
    StringBuilder strJson = new StringBuilder("{").append(NEWLINE);
    Integer blankCount = blankCountStack.peek() + ADDED_BLANK_COUNT;
    blankCountStack.push(blankCount);
    Class<?> clazz = obj.getClass();
    Field[] declaredFields = clazz.getDeclaredFields();
    boolean hasRedundantComma = false;
    for (Field field : declaredFields) {
      field.setAccessible(true);
      String key = field.getName();
      Object value = null;
      try {
        value = field.get(obj);
      } catch (IllegalAccessException e) {
        throw new JsonParseRuntimeException(e);
      }
      if (null != value) {
        for (int i = 0; i < blankCount; i++) {
          strJson.append(" ");
        }
        strJson
            .append("\"")
            .append(key)
            .append("\"")
            .append(":")
            .append(" ")
            .append(toJson(value))
            .append(",")
            .append(NEWLINE);
        hasRedundantComma = true;
      }
    }
    if (hasRedundantComma) { // 删除多余的','
      strJson.deleteCharAt(strJson.length() - 1 - NEWLINE.length());
    }
    blankCountStack.pop();
    for (int j = 0; j < blankCountStack.peek(); j++) {
      strJson.append(" ");
    }
    strJson.append("}");
    return strJson.toString();
  }
}
