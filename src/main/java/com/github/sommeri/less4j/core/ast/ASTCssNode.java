package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.PubliclyCloneable;

public abstract class ASTCssNode implements PubliclyCloneable {

  private ASTCssNode parent;
  private Visibility visibility = Visibility.DEFAULT;
  private int visibilityBlocks = 0;

  // I'm using underlying structure as identified in cycle detector. If it stops
  // to be identifying,
  // cycle detector must be modified. !
  private HiddenTokenAwareTree underlyingStructure;
  private List<Comment> openingComments = new ArrayList<Comment>();
  private List<Comment> orphanComments = new ArrayList<Comment>();
  private List<Comment> trailingComments = new ArrayList<Comment>();

  public ASTCssNode(HiddenTokenAwareTree underlyingStructure) {
    this.underlyingStructure = underlyingStructure;
    if (underlyingStructure == null)
      throw new IllegalArgumentException("Underlying can not be null. It is used for error reporting, so place there the closest token possible.");
  }


  @NotAstProperty
  public Visibility getVisibility() {
    return visibility;
  }

  @NotAstProperty
  public void setVisibility(Visibility isVisible) {
    this.visibility = isVisible;
  }

  @NotAstProperty
  public int getVisibilityBlocks() {
    return visibilityBlocks;
  }

  public boolean hasVisibilityBlock() {
    return visibilityBlocks != 0;
  }

  @NotAstProperty
  public void setVisibilityBlocks(int visibilityBlocks) {
    this.visibilityBlocks = visibilityBlocks;
  }

  public void addVisibilityBlocks(int blocks) {
    this.visibilityBlocks += blocks;
  }

  /**
   * WARNING: it is up to the programmer to keep parent and childs getters and
   * setters consistent. Members of this hierarchy are not responsible for that.
   */
  @NotAstProperty
  public abstract List<? extends ASTCssNode> getChilds();

  /**
   * WARNING: it is up to the programmer to keep parent and childs getters and
   * setters consistent. Members of this hierarchy are not responsible for that.
   */
  public ASTCssNode getParent() {
    return parent;
  }

  public boolean hasParent() {
    return getParent() != null;
  }

  /**
   * WARNING: it is up to the programmer to keep parent and childs getters and
   * setters consistent. Members of this hierarchy are not responsible for that.
   */
  public void setParent(ASTCssNode parent) {
    this.parent = parent;
  }

  public HiddenTokenAwareTree getUnderlyingStructure() {
    return underlyingStructure;
  }

  public void setUnderlyingStructure(HiddenTokenAwareTree underlyingStructure) {
    this.underlyingStructure = underlyingStructure;
  }

  @NotAstProperty
  public List<Comment> getTrailingComments() {
    if (trailingComments == null) {
        return Collections.emptyList();
    }
    return trailingComments;
  }

  public void setTrailingComments(List<Comment> trailingComments) {
    this.trailingComments = trailingComments;
  }

  public void addTrailingComments(List<Comment> comments) {
      if (trailingComments == null) {
        this.trailingComments = new ArrayList<Comment>(comments);
    } else {
        this.trailingComments.addAll(comments);
    }
  }

  public void addTrailingComment(Comment comment) {
    if (trailingComments == null) {
        this.trailingComments = new ArrayList<Comment>();
    }
    this.trailingComments.add(comment);
  }

  @NotAstProperty
  public List<Comment> getOpeningComments() {
    if (this.openingComments == null) {
       return Collections.emptyList();
    }

    return openingComments;
  }

  public void setOpeningComments(List<Comment> openingComments) {
    this.openingComments = openingComments;
  }

  public void addOpeningComments(List<Comment> openingComments) {
    if (this.openingComments == null) {
        this.openingComments = new ArrayList<Comment>(openingComments);
    } else {
        this.openingComments.addAll(openingComments);
    }
  }

  @NotAstProperty
  public List<Comment> getOrphanComments() {
    if (orphanComments == null) {
        return Collections.emptyList();
    }
    return orphanComments;
  }

  public void setOrphanComments(List<Comment> orphanComments) {
    this.orphanComments = orphanComments;
  }

  public abstract ASTCssNodeType getType();

  public boolean isFaulty() {
    return false;
  }

  public LessSource getSource() {
    return getUnderlyingStructure() == null ? null : getUnderlyingStructure().getSource();
  }

  public int getSourceLine() {
    return getUnderlyingStructure() == null ? -1 : getUnderlyingStructure().getLine();
  }

  public int getSourceColumn() {
    return getUnderlyingStructure() == null ? -1 : getUnderlyingStructure().getCharPositionInLine() + 1;
  }


@Override
  public ASTCssNode clone() {
    try {
      ASTCssNode clone = (ASTCssNode) super.clone();
      if (this.openingComments != null && !this.openingComments.isEmpty()) {
          clone.setOpeningComments(new ArrayList<Comment>(getOpeningComments()));
      } else {
          clone.setOpeningComments(null);
      }
      if (this.orphanComments != null && !this.orphanComments.isEmpty()) {
          clone.setOrphanComments(new ArrayList<Comment>(getOrphanComments()));
      } else {
          clone.setOrphanComments(null);
      }
      if (this.trailingComments != null && !this.trailingComments.isEmpty()) {
          clone.setTrailingComments(new ArrayList<Comment>(getTrailingComments()));
      } else {
          clone.setTrailingComments(null);
      }

      clone.setParent(null);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("This is a bug - please submit issue with this stack trace and an input.");
    }
  }

  public void configureParentToAllChilds() {
    List<? extends ASTCssNode> childs = getChilds();
    for (ASTCssNode kid : childs) {
      kid.setParent(this);
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getSimpleName()).append(" (");
    builder.append(getSourceLine()).append(":").append(getSourceColumn());
    builder.append(")");
    
    return builder.toString();
  }
  
  public enum Visibility {
    DEFAULT, 
    VISIBLE
  }

}
