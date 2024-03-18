package titan.json.visitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import titan.ast.runtime.Ast;
import titan.ast.runtime.AstGrammar;
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

  public ContextAst convert() {
    return doConvert(source);
  }

  private ContextAst doConvert(Ast ast) {
    ContextAst contextAst = createContextAstByAst(ast);
    ArrayList<ContextAst> contextAstChildren = new ArrayList<>(ast.children.size());
    for (Ast astChild : ast.children) {
      ContextAst child = doConvert(astChild);
      child.parent = contextAst;
      contextAstChildren.add(child);
    }
    contextAst.children = contextAstChildren;
    return contextAst;
  }

  private ContextAst createContextAstByAst(Ast ast) {
    ContextAst contextAst = null;
    AstGrammar grammar = ast.grammar;
    switch (grammar.type) {
      case TERMINAL_FRAGMENT:
        break;
      case TERMINAL:
        contextAst = new TerminalAst(ast.grammar);
        contextAst.token = ast.token;
        break;
      case NONTERMINAL:
        try {
          String className = getContextAstClassName(ast);
          Class<?> contextClass = Class.forName(className);
          Constructor<?> constructor = contextClass.getDeclaredConstructor(AstGrammar.class);
          contextAst = (ContextAst) constructor.newInstance(grammar);
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
    String className = ast.grammar.name;
    className = className.substring(0, 1).toUpperCase() + className.substring(1);
    className = "titan.json.visitor.ContextAst$" + className + "Ast";
    return className;
  }
}
