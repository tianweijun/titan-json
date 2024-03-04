package titan.json.reflector;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import titan.ast.runtime.Grammar;
import titan.json.JsonParseRuntimeException;
import titan.json.visitor.ContextAst;
import titan.json.visitor.ContextAst.ArrAst;
import titan.json.visitor.ContextAst.ValueAst;

/**
 * JsonClassReflector.
 *
 * @author tian wei jun
 */
public class JsonClassReflector {

  public Class<?> klass;

  public JsonClassReflector(Class<?> klass) {
    this.klass = Primitives.wrap(klass);
  }

  public boolean isArray() {
    return klass.isArray();
  }

  public boolean isPrimitiveWrapperType() {
    return Primitives.isWrapperType(klass);
  }

  public boolean isString() {
    return String.class == klass;
  }

  public Object newInstance(ValueAst valueAst) {
    if (isPrimitiveWrapperType() || isString()) {
      return null;
    }
    if (isArray()) { // array
      return newInstanceByArray(valueAst);
    }
    // normal class
    Object obj = null;
    try {
      obj = klass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new JsonParseRuntimeException(e);
    }
    return obj;
  }

  public Object newInstanceByArray(ValueAst valueAst) {
    int lengthOfArray = getLengthOfArray(valueAst);
    return Array.newInstance(klass.getComponentType(), lengthOfArray);
  }

  private int getLengthOfArray(ValueAst valueAst) {
    int lengthOfArray = 0;
    ContextAst productionRuleAst = (ContextAst) valueAst.children.get(0);
    String grammarName = productionRuleAst.grammar.getName();
    if (grammarName.equals("arr")) {
      ArrAst arrAst = (ArrAst) productionRuleAst;
      for (ContextAst eleArrAst : arrAst.children) {
        Grammar eleArrGrammar = eleArrAst.grammar;
        if (eleArrGrammar.getName().equals("value")) {
          ++lengthOfArray;
        }
      }
    }
    return lengthOfArray;
  }

  public Field getField(String fieldName) {
    Field field = null;
    try {
      field = klass.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      throw new JsonParseRuntimeException(e);
    }
    return field;
  }
}
