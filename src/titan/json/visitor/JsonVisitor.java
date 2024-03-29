package titan.json.visitor;

import java.io.UnsupportedEncodingException;
import titan.ast.runtime.AstGrammar;
import titan.ast.runtime.StringUtils;
import titan.json.JsonParseRuntimeException;
import titan.json.reflector.JsonClassReflector;
import titan.json.reflector.Primitives;
import titan.json.visitor.ContextAst.ArrAst;
import titan.json.visitor.ContextAst.JsonAst;
import titan.json.visitor.ContextAst.ObjAst;
import titan.json.visitor.ContextAst.PairAst;
import titan.json.visitor.ContextAst.ValueAst;
import titan.json.visitor.ContextAstValue.ArrayContextAstValue;
import titan.json.visitor.ContextAstValue.ArrayElementContextAstValue;
import titan.json.visitor.ContextAstValue.FieldOfObjectContextAstValue;
import titan.json.visitor.ContextAstValue.RefContextAstValue;

/**
 * JsonVisitor.
 *
 * @author tian wei jun
 */
public class JsonVisitor<T> implements AstVisitor {

  private ContextAst sourceAst;
  private Class<T> classOfT;
  private String encoding;

  public JsonVisitor(ContextAst contextAst, Class<T> classOfT, String encoding) {
    this.sourceAst = contextAst;
    this.classOfT = classOfT;
    this.encoding = encoding;
  }

  public T parseObject() {
    JsonAst jsonAst = (JsonAst) sourceAst;
    visitJson(jsonAst);
    return Primitives.wrap(classOfT).cast(jsonAst.contextAstValue.getValue());
  }

  @Override
  public void visitJson(JsonAst jsonAst) {
    JsonClassReflector jsonClassReflector = new JsonClassReflector(classOfT);

    ValueAst valueAst = (ValueAst) jsonAst.children.get(0);
    // create root value 引用
    Object value = jsonClassReflector.newInstance(valueAst);
    jsonAst.initContextAstValue(jsonClassReflector, value);
    // 传递引用（父子等价）
    valueAst.passValueByParent();
    visitValue(valueAst);
  }

  @Override
  public void visitValue(ValueAst valueAst) {
    ContextAst productionRuleAst = (ContextAst) valueAst.children.get(0);
    productionRuleAst.passValueByParent();

    String grammarName = productionRuleAst.grammar.name;
    switch (grammarName) {
      case "Null":
        productionRuleAst.setValue(null);
        break;
      case "False":
        productionRuleAst.setValue(false);
        break;
      case "True":
        productionRuleAst.setValue(true);
        break;
      case "NUMBER":
        Class klass = valueAst.contextAstValue.classOfValue.klass;
        String textOfNumber = productionRuleAst.token.text;
        productionRuleAst.setValue(getNumber(klass, textOfNumber));
        break;
      case "STRING":
        String textOfString = productionRuleAst.token.text;
        textOfString = textOfString.substring(1, textOfString.length() - 1); // "xxx"--->xxx
        textOfString = encodeByString(textOfString);
        productionRuleAst.setValue(textOfString);
        break;
      case "obj": // （引用）【json.value、pair.String ':' value、arr.'[' value ( ',' value)* ']'、】
        ObjAst objAst = (ObjAst) productionRuleAst;
        visitObj(objAst);
        break;
      case "arr": // （引用）【json.value、pair.String ':' value、arr.'[' value ( ',' value)* ']'、】
        ArrAst arrAst = (ArrAst) productionRuleAst;
        visitArr(arrAst);
        break;
      default:
    }
  }

  private String encodeByString(String sourceString) {
    if (StringUtils.isBlank(encoding)) {
      return sourceString;
    }
    char[] chars = sourceString.toCharArray();
    byte[] bytes = new byte[chars.length];
    for (int indexOfChars = 0; indexOfChars < chars.length; indexOfChars++) {
      char ch = chars[indexOfChars];
      bytes[indexOfChars] = (byte) (ch & 0xFF);
    }
    String encodeString;
    try {
      encodeString = new String(bytes, encoding);
    } catch (UnsupportedEncodingException e) {
      throw new JsonParseRuntimeException(e);
    }
    return encodeString;
  }

  @Override
  public void visitArr(ArrAst arrAst) {
    JsonClassReflector classOfArr = arrAst.contextAstValue.classOfValue;
    Class<?> nakedArrClass = classOfArr.klass;
    if (!nakedArrClass.isArray()) {
      throw new JsonParseRuntimeException("text is a array,but entity isn't.");
    }
    Class<?> componentType = nakedArrClass.getComponentType();
    JsonClassReflector eleArrClassReflector = new JsonClassReflector(componentType);
    int indexOfArray = 0;
    ArrayContextAstValue arrayContextAstValue = (ArrayContextAstValue) arrAst.contextAstValue;
    for (ContextAst eleArrAst : arrAst.children) {
      AstGrammar eleArrGrammar = eleArrAst.grammar;
      if (eleArrGrammar.name.equals("value")) {
        ValueAst valueEleArrAst = (ValueAst) eleArrAst;
        // 新建引用
        Object vOfEleArr = eleArrClassReflector.newInstance(valueEleArrAst);
        // 设置引用arr[indexOfArray]=v
        if (!eleArrClassReflector.isPrimitiveWrapperType() && !eleArrClassReflector.isString()) {
          arrayContextAstValue.set(vOfEleArr, indexOfArray); // 设置当前元素ele被包含的数组arr的值
        }
        // 传引用
        if (eleArrClassReflector.isArray()) { // 转引用为arr
          ArrayContextAstValue eleArrayContextAstValue = new ArrayContextAstValue();
          eleArrayContextAstValue.set(vOfEleArr);
          valueEleArrAst.contextAstValue = eleArrayContextAstValue;
        } else if (eleArrClassReflector.isPrimitiveWrapperType()
            || eleArrClassReflector.isString()) {
          // 传引用arr[index](int,String,Double,...,String)
          ArrayElementContextAstValue arrayElementContextAstValue =
              new ArrayElementContextAstValue();
          arrayElementContextAstValue.set(arrayContextAstValue.array, indexOfArray);
          valueEleArrAst.contextAstValue = arrayElementContextAstValue;
        } else { // 转引用为ref(object...)
          RefContextAstValue refContextAstValue = new RefContextAstValue();
          refContextAstValue.setValue(vOfEleArr);
          valueEleArrAst.contextAstValue = refContextAstValue;
        }
        valueEleArrAst.contextAstValue.classOfValue = eleArrClassReflector;
        // 递归遍历数组元素
        visitValue(valueEleArrAst);
        indexOfArray++;
      }
    }
  }

  @Override
  public void visitObj(ObjAst objAst) {
    JsonClassReflector objClassReflector = objAst.contextAstValue.classOfValue;
    Object obj = objAst.contextAstValue.getValue();
    for (ContextAst objEleAst : objAst.children) {
      AstGrammar eleGrammar = objEleAst.grammar;
      if (eleGrammar.name.equals("pair")) {
        PairAst pair = (PairAst) objEleAst;
        // 传引用(field)
        String fieldName = pair.children.get(0).token.text;
        fieldName = formatFieldName(fieldName);
        FieldOfObjectContextAstValue fieldOfObjectContextAstValue =
            new FieldOfObjectContextAstValue(obj, fieldName);
        fieldOfObjectContextAstValue.setClassByObjClassReflector(objClassReflector);
        if (fieldOfObjectContextAstValue.isJsonField()) {
          pair.contextAstValue = fieldOfObjectContextAstValue;
          // 递归遍历语法树
          visitPair(pair);
        }
      }
    }
  }

  @Override
  public void visitPair(PairAst pairAst) {
    FieldOfObjectContextAstValue fieldOfObjectContextAstValue =
        (FieldOfObjectContextAstValue) pairAst.contextAstValue;
    JsonClassReflector fieldJsonClassReflector = fieldOfObjectContextAstValue.classOfValue;
    // 创建引用
    ValueAst pairValueAst = (ValueAst) pairAst.children.get(2);
    Object pairValue = fieldJsonClassReflector.newInstance(pairValueAst);
    // 设置值引用
    if (!fieldJsonClassReflector.isPrimitiveWrapperType() && !fieldJsonClassReflector.isString()) {
      fieldOfObjectContextAstValue.setValue(pairValue);
    }
    // 传引用
    if (fieldJsonClassReflector.isArray()) { // 转引用arr
      ArrayContextAstValue arrayContextAstValue = new ArrayContextAstValue();
      arrayContextAstValue.set(pairValue);
      pairValueAst.contextAstValue = arrayContextAstValue;
    } else if (fieldJsonClassReflector.isPrimitiveWrapperType()
        || fieldJsonClassReflector.isString()) {
      // 传引用fieldOfObject(int,String,Double,...,String)
      pairValueAst.contextAstValue = fieldOfObjectContextAstValue;
    } else { // 转引用为ref(object...)
      RefContextAstValue refContextAstValue = new RefContextAstValue();
      refContextAstValue.setValue(pairValue);
      pairValueAst.contextAstValue = refContextAstValue;
    }
    pairValueAst.contextAstValue.classOfValue = fieldJsonClassReflector;
    // 递归遍历语法树
    visitValue(pairValueAst);
  }

  private Object getNumber(Class klass, String text) {
    if (klass == Integer.class || klass == int.class) {
      return Integer.valueOf(text);
    }
    if (klass == Double.class || klass == double.class) {
      return Double.valueOf(text);
    }
    if (klass == Float.class || klass == float.class) {
      return Float.valueOf(text);
    }
    if (klass == Byte.class || klass == byte.class) {
      return Byte.valueOf(text);
    }
    if (klass == Short.class || klass == short.class) {
      return Short.valueOf(text);
    }
    if (klass == Long.class || klass == long.class) {
      return Long.valueOf(text);
    }
    throw new JsonParseRuntimeException(
        String.format("%s is not class of number.", klass.toString()));
  }

  private String formatFieldName(String fieldName) {
    int lengthOfFieldName = fieldName.length();
    if (lengthOfFieldName <= 2) {
      return fieldName;
    }
    if (fieldName.charAt(0) == '"' && fieldName.charAt(lengthOfFieldName - 1) == '"') {
      return fieldName.substring(1, lengthOfFieldName - 1);
    }
    return null;
  }
}
