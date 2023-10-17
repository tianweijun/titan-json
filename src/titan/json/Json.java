package titan.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Json api.
 *
 * @author tian wei jun
 */
public class Json {

  public static <T> T fromJson(String strOfJson, Class<T> classOfT) {
    T object = null;
    try (InputStream jsonInputStream = new ByteArrayInputStream(strOfJson.getBytes())) {
      object = fromJson(jsonInputStream, classOfT);
    } catch (IOException e) {
      throw new JsonParseRuntimeException(e);
    }
    return object;
  }

  public static <T> T fromJson(InputStream jsonInputStream, Class<T> classOfT) {
    return JsonParseApplication.fromJson(jsonInputStream, classOfT);
  }

  public static String toJson(Object src) {
    return "";
  }

  public static void destruct() {
    JsonParseApplication.destruct();
  }
}
