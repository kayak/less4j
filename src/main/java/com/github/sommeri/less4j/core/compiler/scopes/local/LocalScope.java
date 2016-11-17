package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.List;
import java.util.Stack;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionFilter;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.ILocalScope;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner;
import com.github.sommeri.less4j.core.compiler.scopes.ScopeFactory;
import com.github.sommeri.less4j.core.compiler.scopes.local.MixinsDefinitionsStorage.MixinsPlaceholder;
import com.github.sommeri.less4j.core.compiler.scopes.local.VariablesDeclarationsStorage.VariablesPlaceholder;

public class LocalScope implements ILocalScope {

  // following is used only during debugging - to generate human readable
  // toString
  private String type;
  private final ASTCssNode owner;
  private boolean presentInAst = true;

  private LocalScopeData localData;
  private Stack<LocalScopeData> localDataSnapshots;
  private boolean cloneOnWrite = false;
  private boolean cloned;

  private List<String> names;

  public LocalScope(ASTCssNode owner, List<String> names, String type) {
    this.owner = owner;
    this.names = names;
    this.type = type;
    localData = new LocalScopeData();
  }

  public LocalScope(ASTCssNode owner, LocalScopeData initialLocalData, List<String> names, String type) {
    this.owner = owner;
    this.names = names;
    this.type = type;
    this.localData = initialLocalData;
  }

  public LocalScope(ASTCssNode owner, LocalScopeData initialLocalData, List<String> names, String type, boolean cloneOnWrite, boolean cloned) {
    this.owner = owner;
    this.names = names;
    this.type = type;
    localData = initialLocalData;
    this.cloneOnWrite = cloneOnWrite;
    this.cloned = cloned;
  }

  @Override
  public ASTCssNode getOwner() {
    return owner;
  }

  public String getType() {
    return type;
  }

  public boolean isBodyOwnerScope() {
    return ScopeFactory.BODY_OWNER.equals(getType());
  }

  public void removedFromAst() {
    presentInAst = false;
  }

  public boolean isPresentInAst() {
    return presentInAst;
  }

  public void addNames(List<String> names) {
    this.names.addAll(names);
  }

  public List<String> getNames() {
    return names;
  }

  public void registerVariable(AbstractVariableDeclaration declaration) {
    getLocalVariables().store(declaration);
  }

  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue) {
    getLocalVariables().store(node, replacementValue);
  }

  public void registerVariableIfNotPresent(String name, Expression replacementValue) {
    getLocalVariables().storeIfNotPresent(name, replacementValue);
  }

  public void registerVariable(String name, Expression replacementValue) {
    getLocalVariables().store(name, replacementValue);
  }

  public void addFilteredVariables(ExpressionFilter filter, IScope source) {
    getLocalVariables().addFilteredVariables(filter, source.getLocalVariables());
  }

  public Expression getValue(Variable variable) {
    return getLocalVariables().getValue(variable.getName());
  }

  public Expression getValue(String name) {
    return getLocalVariables().getValue(name);
  }

  @Override
  public void addAllMixins(List<FullMixinDefinition> mixins) {
    getLocalMixins().storeAll(mixins);
  }

  public void registerMixin(ReusableStructure mixin, IScope mixinsBodyScope) {
    getLocalMixins().store(new FullMixinDefinition(mixin, mixinsBodyScope));
  }

  public DataPlaceholder createDataPlaceholder() {
    VariablesPlaceholder variablesPlaceholder = getLocalVariables().createPlaceholder();
    MixinsPlaceholder mixinsPlaceholder = getLocalMixins().createPlaceholder();
    return new DataPlaceholder(variablesPlaceholder, mixinsPlaceholder);
  }

  public void addToDataPlaceholder(IScope otherScope) {
    getLocalVariables().addToFirstPlaceholderIfNotPresent(otherScope.getLocalVariables());
    getLocalMixins().addToPlaceholder(otherScope.getLocalMixins());
  }

  public void replacePlaceholder(DataPlaceholder placeholder, IScope otherScope) {
    getLocalVariables().replacePlaceholder(placeholder.getVariablesPlaceholder(), otherScope.getLocalVariables());
    getLocalMixins().replacePlaceholder(placeholder.getMixinsPlaceholder(), otherScope.getLocalMixins());
  }

  public void closeDataPlaceholder() {
    getLocalVariables().closePlaceholder();
    getLocalMixins().closePlaceholder();
  }

  public void add(IScope otherSope) {
    getLocalMixins().storeAll(otherSope.getLocalMixins());
    getLocalVariables().storeAll(otherSope.getLocalVariables());
  }

  public LocalScopeData getLocalData() {
    return localData;
  }

  public ILocalScope cloneCurrentDataSnapshot() {
    return new LocalScope(owner, localData, names, type, true, false);
  }

  public boolean hasTheSameLocalData(ILocalScope otherScope) {
    return otherScope.getLocalData() == localData;
  }

  /**
   * Do not call this method directly. Use {@link InScopeSnapshotRunner}
   * instead.
   */
  public void createCurrentDataSnapshot() {
    getLocalDataSnapshots().push(localData);
    cloneOnWrite = true;
    cloned = false;
  }

  /**
   * Do not call this method directly. Use {@link InScopeSnapshotRunner}
   * instead.
   */
  public void createOriginalDataSnapshot() {
    getLocalDataSnapshots().push(localData);
    localData = localDataSnapshots.firstElement();
    cloneOnWrite = true;
    cloned = false;
  }

  /**
   * Do not call this method directly. Use {@link InScopeSnapshotRunner}
   * instead.
   */
  public void discardLastDataSnapshot() {
    localData = getLocalDataSnapshots().pop();
    cloneOnWrite = false;
    cloned = false;
  }

  public MixinsDefinitionsStorage getLocalMixins() {
    cloneIfNeeded();
    return localData.getMixins();
  }

  public VariablesDeclarationsStorage getLocalVariables() {
    cloneIfNeeded();
    return localData.getVariables();
  }

  public List<FullMixinDefinition> getAllMixins() {
    return getLocalMixins().getAllMixins();
  }

  public List<FullMixinDefinition> getMixinsByName(List<String> nameChain, ReusableStructureName name) {
    return getLocalMixins().getMixins(nameChain, name);
  }

  public List<FullMixinDefinition> getMixinsByName(ReusableStructureName name) {
    return getLocalMixins().getMixins(name);
  }

  public List<FullMixinDefinition> getMixinsByName(String name) {
    return getLocalMixins().getMixins(name);
  }

  private void cloneIfNeeded() {
    if (cloneOnWrite && !cloned) {
      localData = localData.clone();
      cloned = true;
    }
  }

  @Override
  public String toString() {
    return localData.toString();
  }
  
  private Stack<LocalScopeData> getLocalDataSnapshots() {
      if (localDataSnapshots == null) {
          this.localDataSnapshots = new Stack<LocalScopeData>();
      }
      return localDataSnapshots;
  }

}
