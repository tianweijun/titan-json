package titan.json.serialization;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import titan.json.JsonParseRuntimeException;
import titan.json.reflector.Primitives;

/**
 * .
 *
 * @author tian wei jun
 */
public class Serialization {
  Object src;

  public Serialization(Object src) {
    this.src = src;
  }

  public String toJson() {
    if (null == src) {
      return "";
    }
    return toJson(src);
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
    StringBuilder strJson = new StringBuilder("[");
    int length = Array.getLength(array);
    for (int i = 0; i < length; i++) {
      Object element = Array.get(array, i);
      String elementStrJson = "";
      if (null == element) {
        elementStrJson = "null";
      } else {
        elementStrJson = toJson(element);
      }
      strJson.append(elementStrJson).append(",");
    }
    boolean hasRedundantComma = length > 0;
    if (hasRedundantComma) { // 删除多余的','
      strJson.deleteCharAt(strJson.length() - 1);
    }
    strJson.append("]");
    return strJson.toString();
  }

  private String toJsonByObject(Object obj) {
    StringBuilder strJson = new StringBuilder("{");
    Class<?> clazz = obj.getClass();
    Field[] declaredFields = clazz.getDeclaredFields();
    boolean hasRedundantComma = false;
    for (Field field : declaredFields) {
      int modifiers = field.getModifiers();
      if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
        continue;
      }
      field.setAccessible(true);
      String key = field.getName();
      Object value = null;
      try {
        value = field.get(obj);
      } catch (IllegalAccessException e) {
        throw new JsonParseRuntimeException(e);
      }
      if (null != value) {
        strJson.append("\"").append(key).append("\"").append(":").append(toJson(value)).append(",");
        hasRedundantComma = true;
      }
    }
    if (hasRedundantComma) { // 删除多余的','
      strJson.deleteCharAt(strJson.length() - 1);
    }
    strJson.append("}");
    return strJson.toString();
  }
}
