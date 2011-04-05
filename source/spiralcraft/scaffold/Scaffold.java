package spiralcraft.scaffold;

import java.util.List;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.text.ParsePosition;

/**
 * <p>A tree node derived from a source code resource that defines part of the
 *   containership structure of an application model and creates components
 *   of the appropriate type. 
 * </p>
 * 
 * <p>A given Scaffold system knows how to create components of a given model
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public interface Scaffold<T extends Scaffold<T,C,X>,C,X extends ContextualException>
{

  /**
   * @return The children of this ScaffoldUnit
   */
  List<T> getChildren();
  
  /**
   * 
   * @return The parent of this ScaffoldUnit
   */
  T getParent();
  
  /**
   * The position in the source code document or data file that defined
   *   this Scaffold unit.
   * 
   * @return
   */
  public ParsePosition getPosition();
  
  /**
   * 
   * @param focus
   * @param parent
   * @return A bound Contextual
   */
  C bind(Focus<?> focus,C parent)
    throws X;
}
