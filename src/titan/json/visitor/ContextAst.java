package titan.json.visitor;

import java.util.LinkedList;
import titan.ast.runtime.AstToken;
import titan.ast.runtime.Grammar;
import titan.json.reflector.JsonClassReflector;
import titan.json.visitor.ContextAstValue.ArrayContextAstValue;
import titan.json.visitor.ContextAstValue.RefContextAstValue;

/**
 * ContextAst.
 *
 * @author tian wei jun
 */
public class ContextAst {
  public Grammar grammar = null;
  public AstToken token = null;
  public LinkedList<ContextAst> children = new LinkedList<ContextAst>();

  ContextAst parent = null;
  ContextAstValue contextAstValue = null;

  public ContextAst(Grammar grammar) {
    this.grammar = grammar;
  }

  public void passValueByParent() {
    this.contextAstValue = parent.contextAstValue;
  }

  /**
   * (ARRAY_ELEMENT, FIELD_OF_OBJ,不可能是最开始的值); array,REF.
   *
   * @param value
   */
  public void initContextAstValue(JsonClassReflector jsonClassReflector, Object value) {
    if (jsonClassReflector.klass.isArray()) {
      ArrayContextAstValue arrayContextAstValue = new ArrayContextAstValue();
      arrayContextAstValue.array = value;
      this.contextAstValue = arrayContextAstValue;
    } else {
      RefContextAstValue refContextAstValue = new RefContextAstValue();
      refContextAstValue.ref[0] = value;
      this.contextAstValue = refContextAstValue;
    }
    this.contextAstValue.classOfValue = jsonClassReflector;
  }

  public void setValue(Object v) {
    contextAstValue.setValue(v);
  }

  public static class TerminalAst extends ContextAst {

    public TerminalAst(Grammar grammar) {
      super(grammar);
    }
  }

  public static class ArrAst extends ContextAst {

    public ArrAst(Grammar grammar) {
      super(grammar);
    }

    public void setArrayElementValue(Object v, int indexOfArray) {
      ArrayContextAstValue arrayContextAstValue = (ArrayContextAstValue) this.contextAstValue;
      arrayContextAstValue.set(v, indexOfArray);
    }
  }

  public static class JsonAst extends ContextAst {

    public JsonAst(Grammar grammar) {
      super(grammar);
    }
  }

  public static class ObjAst extends ContextAst {

    public ObjAst(Grammar grammar) {
      super(grammar);
    }
  }

  public static class PairAst extends ContextAst {

    public PairAst(Grammar grammar) {
      super(grammar);
    }
  }

  public static class ValueAst extends ContextAst {
    public ValueAst(Grammar grammar) {
      super(grammar);
    }
  }
}
