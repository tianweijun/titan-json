package titan.json.encoding;

import java.io.UnsupportedEncodingException;
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
public class JsonObjectEncoder {
  String stringEncoding;

  public JsonObjectEncoder(String stringEncoding) {
    this.stringEncoding = stringEncoding;
  }

  public <T> T encode(T jsonObject, Class<T> classOfT) {
    if (Primitives.isWrapperType(classOfT) || null == jsonObject) {
      return jsonObject;
    }
    return (T) doEncode(jsonObject, classOfT);
  }

  private Object doEncode(Object jsonObject, Class<?> classOfT) {
    Object ret;
    if (String.class == classOfT) {
      ret = encodeByString((String) jsonObject);
    } else if (classOfT.isArray()) {
      ret = encodeByArray(jsonObject, classOfT);
    } else {
      ret = encodeByObject(jsonObject, classOfT);
    }
    return ret;
  }

  private String encodeByString(String sourceString) {
    char[] chars = sourceString.toCharArray();
    byte[] bytes = new byte[chars.length];
    for (int indexOfChars = 0; indexOfChars < chars.length; indexOfChars++) {
      char ch = chars[indexOfChars];
      bytes[indexOfChars] = (byte) (ch & 0xFF);
    }
    String encodeString;
    try {
      encodeString = new String(bytes, stringEncoding);
    } catch (UnsupportedEncodingException e) {
      throw new JsonParseRuntimeException(e);
    }
    return encodeString;
  }

  private Object encodeByObject(Object obj, Class<?> objClass) {
    Field[] declaredFields = objClass.getDeclaredFields();
    for (Field field : declaredFields) {
      int modifiers = field.getModifiers();
      if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
        continue;
      }
      field.setAccessible(true);
      Object value = null;
      try {
        value = field.get(obj);
      } catch (IllegalAccessException e) {
        throw new JsonParseRuntimeException(e);
      }
      if (value == null) {
        continue;
      }
      Class<?> valueClass = value.getClass();
      if (!Primitives.isWrapperType(valueClass)) {
        Object encodeValue = doEncode(value, valueClass);
        try {
          field.set(obj, encodeValue);
        } catch (IllegalAccessException e) {
          throw new JsonParseRuntimeException(e);
        }
      }
    }
    return obj;
  }

  private Object encodeByArray(Object array, Class<?> arrayClass) {
    Class<?> componentType = arrayClass.getComponentType();
    if (!Primitives.isWrapperType(componentType)) {
      int length = Array.getLength(array);
      for (int indexOfArray = 0; indexOfArray < length; indexOfArray++) {
        Object element = Array.get(array, indexOfArray);
        if (element != null) {
          Object encodeElement = doEncode(element, componentType);
          Array.set(array, indexOfArray, encodeElement);
        }
      }
    }
    return array;
  }
}
