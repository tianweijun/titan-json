package titan.json.visitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import titan.ast.runtime.Ast;
import titan.ast.runtime.Grammar;
import titan.json.JsonParseRuntimeException;
import titan.json.visitor.ContextAst.TerminalAst;

/**
 * Ast2ContextAstConvertor.
 *
 * @author tian wei jun
 */
public class Ast2ContextAstConvertor {
  Ast source;

  public Ast2ContextAstConvertor(Ast source) {
    this.source = source;
  }

  public Ast convert() {
    return doConvert(source);
  }

  private ContextAst doConvert(Ast ast) {
    ContextAst contextAst = createContextAstByAst(ast);
    for (Ast astChild : ast.children) {
      ContextAst child = doConvert(astChild);
      child.parent = contextAst;
      contextAst.children.add(child);
    }
    return contextAst;
  }

  private ContextAst createContextAstByAst(Ast ast) {
    ContextAst contextAst = null;
    Grammar grammar = ast.grammar;
    switch (grammar.getType()) {
      case TERMINAL_FRAGMENT:
        break;
      case TERMINAL:
        contextAst = new TerminalAst(ast.grammar, ast.alias);
        contextAst.token = ast.token;
        break;
      case NONTERMINAL:
        try {
          String className = getContextAstClassName(ast);
          Class<?> contextClass = Class.forName(className);
          Constructor<?> constructor =
              contextClass.getDeclaredConstructor(Grammar.class, String.class);
          contextAst = (ContextAst) constructor.newInstance(grammar, ast.alias);
          contextAst.token = ast.token;
        } catch (ClassNotFoundException
            | NoSuchMethodException
            | InstantiationException
            | IllegalAccessException
            | InvocationTargetException e) {
          throw new JsonParseRuntimeException(e);
        }
        break;
      default:
    }
    return contextAst;
  }

  /**
   * json--->JsonAst.
   *
   * @param ast
   * @return
   */
  private String getContextAstClassName(Ast ast) {
    String className = ast.grammar.getName();
    char[] chars = className.toCharArray();
    char fchar = chars[0];
    if (97 <= fchar && fchar <= 122) { // 首字母小写转大写
      fchar ^= 32;
      chars[0] = fchar;
      className = new String(chars);
    }
    className = "titan.json.visitor.ContextAst$" + className + "Ast";
    return className;
  }
}
