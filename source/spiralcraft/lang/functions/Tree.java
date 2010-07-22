package spiralcraft.lang.functions;


import java.util.Iterator;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Decorator;

import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.util.IteratorStack;
import spiralcraft.util.ArrayUtil;

/**
 * <p>Provides a collections of nodes in a tree defined by an expansion
 *   function which maps a set of child nodes to each node.
 * </p> 
 * 
 * <p>The default and only traversal is depth-first, preorder.
 * </p>
 * 
 * @author mike
 *
 * @param <Tcollection>
 * @param <Tsource>
 */
public class Tree<Tcollection, Tsource>
  implements ChannelFactory<Tcollection, Tsource>
{

  private Expression<Tcollection> expansion;
  private Expression<Boolean> stopX;
  
  public Tree()
  {
  }
  
  public Tree(Expression<Tcollection> expansion)
  { this.expansion=expansion;
  }
  
  public Tree(Expression<Tcollection> expansion,Expression<Boolean> stopX)
  { 
    this.expansion=expansion;
    this.stopX=stopX;
  }
  
  /**
   * <p>An Expression which expands the children of the current node- evaluates
   *   to a collection of children.
   * </p>
   * 
   * @param x
   */
  public void setChildrenX(Expression<Tcollection> x)
  { this.expansion=x;
  }
  
  
  /**
   * <p>A boolean Expression which returns true when the current node should
   *   not be expanded further.
   * </p>
   * 
   * @param stopX
   */
  public void setStopX(Expression<Boolean> stopX)
  { this.stopX=stopX;
  }
  
  @Override
  public Channel<Tcollection> bindChannel(
    Channel<Tsource> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    ThreadLocalChannel<Tsource> itemChannel
      =new ThreadLocalChannel<Tsource>(source.getReflector());
    
    focus=focus.telescope(itemChannel);
    Channel<Tcollection> expansionChannel=focus.bind(expansion);
    Channel<Boolean> stopChannel=stopX!=null?focus.bind(stopX):null;
    
    return new TreeChannel
     (expansionChannel
     ,source
     ,itemChannel
     ,stopChannel
     );
    
  }

  
  public class TreeChannel
    extends SourcedChannel<Tsource,Tcollection>
  {

    private final ThreadLocalChannel<Tsource> itemChannel;
    
    private final CollectionDecorator<Tcollection,Tsource> expansionDecorator;
//    private Channel<Tchannel> expansionChannel;
    private final Channel<Boolean> stopChannel;

    @SuppressWarnings("unchecked")
    public TreeChannel
      (Channel<Tcollection> expansionChannel
      ,Channel<Tsource> source
      ,ThreadLocalChannel<Tsource> itemChannel
      ,Channel<Boolean> stopChannel
      )
      throws BindException
    { 
      super(expansionChannel.getReflector(),source);
      this.itemChannel=itemChannel;
//      this.expansionChannel=expansionChannel;
      this.expansionDecorator
        =expansionChannel.<CollectionDecorator>decorate(CollectionDecorator.class);
      if (expansionDecorator==null)
      { 
        throw new BindException
          ("Not a collection: "+expansionChannel.getReflector().getTypeURI());
      }
      this.stopChannel=stopChannel;
    }
    
    
    @SuppressWarnings("unchecked")
    @Override
    protected Tcollection retrieve()
    {
      Tsource sourceVal=source.get();
      itemChannel.push(sourceVal);
      
      try
      {
        Tcollection ret=expansionDecorator.newCollection();
        ret=expansionDecorator.add(ret,sourceVal);

        Iterator initial=expansionDecorator.iterator();
        if (initial!=null)
        {
        
          Iterator<Tsource> it
            =new TreeIterator(initial);
        
          ret=expansionDecorator.addAll
            (ret
            ,it
            );
        
        }
        return ret;
      }
      finally
      { itemChannel.pop();
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D extends Decorator<Tcollection>> D decorate(Class<D> decorator)
    { 
      if ( ((Class) decorator)==IterationDecorator.class)
      { 
        return (D) new IterationDecorator<Tcollection,Tsource>(this,source.getReflector())
        {

          @Override
          protected Iterator createIterator()
          {
            Tsource sourceVal=TreeChannel.this.source.get();
            itemChannel.push(sourceVal);
            try
            { 
              return new TreeIterator
                ( (Iterator<Tsource>) ArrayUtil.iterator(new Object[] {sourceVal})
                );
            }
            finally
            { itemChannel.pop();
            }
            
          }
        };
        
      }
      return super.decorate(decorator);
    }
    
    @Override
    protected boolean store(
      Tcollection val)
      throws AccessException
    {
      // TODO Auto-generated method stub
      return false;
    }
    
    class TreeIterator
      extends IteratorStack<Tsource>
    {
      
      public TreeIterator(Iterator<Tsource>... initial)
      { super(initial);
      }
      
      @Override
      public Tsource next()
      {
        Tsource next=super.next();
        
        itemChannel.push(next);
        try
        { 
          if (stopChannel==null || !Boolean.TRUE.equals(stopChannel.get()))
          {
            Iterator<Tsource> childIter=expansionDecorator.iterator();
            push(childIter);
          }
        }
        finally
        { itemChannel.pop();
        }
        
        return next;
        
      }
    }
  }
  
}