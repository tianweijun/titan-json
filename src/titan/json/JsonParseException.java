package titan.json;

/**
 * JsonParseException.
 *
 * @author tian wei jun
 */
public class JsonParseException extends RuntimeException {

  public JsonParseException(String msg) {
    super(msg);
  }

  public JsonParseException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public JsonParseException(Throwable cause) {
    super(cause);
  }
}
