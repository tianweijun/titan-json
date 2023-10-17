package titan.json.visitor;

/**
 * AstVisitor.
 *
 * @author tian wei jun
 */
public interface AstVisitor {

  void visitJson(ContextAst.JsonAst jsonAst);

  void visitObj(ContextAst.ObjAst objAst);

  void visitArr(ContextAst.ArrAst arrAst);

  void visitValue(ContextAst.ValueAst valueAst);

  void visitPair(ContextAst.PairAst pairAst);
}
