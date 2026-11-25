package spiralcraft.util.test;

import java.lang.reflect.Type;
import java.util.List;

import spiralcraft.util.lang.ClassUtil;
import spiralcraft.util.tree.AbstractNode;

/**
 * Classes for UtilTest's checks of ClassUtil.resolveTypeArgument and
 *   AbstractNode.getChildren(), and methods that name what they give
 */
public class TypeFixtures
{
  // Superclasses
  public static class Base<A,B> { }
  public static class Middle<X> extends Base<String,X> { }
  public static class Leaf extends Middle<Integer> { }
  public static class Listed<T> extends Middle<List<T>> { }
  public static class ListedLeaf extends Listed<Long> { }
  public static class RawLeaf extends Base<Object,Object> { }
  @SuppressWarnings("rawtypes")
  public static class Raw extends Base { }

  // Interfaces
  public interface Source<T> { }
  public interface Named<N> extends Source<N> { }
  public static class Implementation implements Named<String> { }
  public static class SubImplementation extends Implementation { }
  public static class Open<Q> implements Source<Q> { }
  public static class Closed extends Open<Double> { }

  // Nodes
  public static class Plain extends AbstractNode<Plain,Object> { }
  public static class PlainSub extends Plain { }
  public abstract static class Generic<N extends Generic<N>>
    extends AbstractNode<N,Object>
  { }
  public static class Bound extends Generic<Bound> { }
  public static class Unbound<N extends Generic<N>> extends Generic<N> { }

  /**
   * What resolveTypeArgument gives, by class names (nested in this class,
   *   or in full): the type's name, or "null"
   */
  public static String resolved(String type,String generic,int index)
    throws ClassNotFoundException
  {
    Type argument
      =ClassUtil.resolveTypeArgument(find(type),find(generic),index);
    return argument!=null?argument.getTypeName():"null";
  }

  /**
   * The simple name of the component class of getChildren()'s array for a
   *   node of that class with one child, or the exception's message
   */
  @SuppressWarnings({"rawtypes","unchecked"})
  public static String childArray(String nodeClass)
    throws Exception
  {
    Class<?> type=find(nodeClass);
    AbstractNode node=(AbstractNode) type.getConstructor().newInstance();
    node.addChild
      (type==Unbound.class
      ?new Bound()
      :(AbstractNode) type.getConstructor().newInstance()
      );
    try
    {
      return node.getChildren().getClass().getComponentType()
        .getSimpleName();
    }
    catch (IllegalStateException x)
    { return x.getMessage();
    }
  }

  /** getChildren() of a node with no children */
  public static Object noChildren()
  { return new Plain().getChildren();
  }

  private static Class<?> find(String name)
    throws ClassNotFoundException
  {
    return name.indexOf('.')>=0
      ?Class.forName(name)
      :Class.forName(TypeFixtures.class.getName()+"$"+name);
  }
}
