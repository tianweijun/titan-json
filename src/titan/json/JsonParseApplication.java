package titan.json;

import java.io.IOException;
import java.io.InputStream;
import titan.ast.runtime.Ast;
import titan.ast.runtime.RuntimeAutomataAstApplication;
import titan.json.serialization.PrettySerialization;
import titan.json.serialization.Serialization;
import titan.json.visitor.Ast2ContextAstConvertor;
import titan.json.visitor.JsonVisitor;

/**
 * JsonParseApplication.
 *
 * @author tian wei jun
 */
public class JsonParseApplication {

  private static RuntimeAutomataAstApplication runtimeAstApplication = null;

  /**
   * 序列化数据转为json实体.
   *
   * @param jsonByteInputStream 输入流
   * @return entity
   */
  public static <T> T fromJson(
      InputStream jsonByteInputStream, Class<T> classOfT, String encoding) {
    construct();
    Ast ast = runtimeAstApplication.buildAst(jsonByteInputStream);
    if (null != jsonByteInputStream) {
      try {
        jsonByteInputStream.close();
      } catch (IOException e) {
        throw new JsonParseRuntimeException(e);
      }
    }
    Ast contextAst = new Ast2ContextAstConvertor(ast).convert();
    return new JsonVisitor<T>(contextAst, classOfT, encoding).parseObject();
  }

  private static void construct() {
    if (null == runtimeAstApplication) {
      InputStream jsonAutomataInputStream =
          JsonParseApplication.class
              .getClassLoader()
              .getResourceAsStream("resources/automata.data");
      runtimeAstApplication = new RuntimeAutomataAstApplication();
      runtimeAstApplication.setContext(jsonAutomataInputStream);
      if (null != jsonAutomataInputStream) {
        try {
          jsonAutomataInputStream.close();
        } catch (IOException e) {
          throw new JsonParseRuntimeException(e);
        }
      }
    }
  }

  public static void destruct() {
    runtimeAstApplication = null; // helper gc
  }

  public static String toJson(Object src) {
    return new Serialization(src).toJson();
  }

  public static String toPrettyJson(Object src) {
    return new PrettySerialization(src).toJson();
  }
}
