package titan.json;

/**
 * 自定义运行时异常.
 *
 * @author tian wei jun
 */
public class JsonParseRuntimeException extends RuntimeException {

  public JsonParseRuntimeException(Throwable cause) {
    super(cause);
  }

  public JsonParseRuntimeException(String message) {
    super(message);
  }
}
