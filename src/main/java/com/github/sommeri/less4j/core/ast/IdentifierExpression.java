package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class IdentifierExpression extends Expression {
  
  private String value;

  public IdentifierExpression(HiddenTokenAwareTree underlyingStructure) {
    this(underlyingStructure, null);
  }

  public IdentifierExpression(HiddenTokenAwareTree underlyingStructure, String value) {
    super(underlyingStructure);
    this.value = value != null ? value.intern() : value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value != null ? value.intern() : value;
  }

  @Override
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.IDENTIFIER_EXPRESSION;
  }

  @Override
  public String toString() {
    return "" + value;
  }

  @Override
  public IdentifierExpression clone() {
    return (IdentifierExpression) super.clone();
  }

}
