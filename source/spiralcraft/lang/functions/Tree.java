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
import spiralcraft.lang.util.FilterIterable;

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
  private Expression<Boolean> filterX;
  
  public Tree()
  {
  }
  
  public Tree(Expression<Tcollection> expansion)
  { this.expansion=expansion;
  }
  
  public Tree(Expression<Tcollection> expansion,Expression<Boolean> filterX)
  { 
    this.expansion=expansion;
    this.filterX=filterX;
  }
  
  public Tree(Expression<Tcollection> expansion
              ,Expression<Boolean> filterX
              ,Expression<Boolean> stopX
              )
  { 
    this.expansion=expansion;
    this.filterX=filterX;
    this.stopX=stopX;
  }

  /**
   * <p>An Expression which expands the children of the current node- evaluates
   *   to a collection of children.
   * </p>
   * 
   * @param x
   */
  public void setExpansionX(Expression<Tcollection> x)
  { this.expansion=x;
  }
  
  /**
   * <p>An Expression which determines whether the node will be included
   *   in the results
   * </p>
   * 
   * @param x
   */
  public void setFilterX(Expression<Boolean> x)
  { this.filterX=x;
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

    Channel<Boolean> filterChannel=filterX!=null?focus.bind(filterX):null;    
    return new TreeChannel
     (expansionChannel
     ,source
     ,itemChannel
     ,stopChannel
     ,filterChannel
     );
    
  }

  
  public class TreeChannel
    extends SourcedChannel<Tsource,Tcollection>
  {

    private final ThreadLocalChannel<Tsource> itemChannel;
    
    private final CollectionDecorator<Tcollection,Tsource> expansionDecorator;
    
    private Iterable<Tsource> expansionIterable;
    
//    private Channel<Tchannel> expansionChannel;
    private final Channel<Boolean> stopChannel;

    
    private FilterIterable<Tsource> filterIterable;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TreeChannel
      (Channel<Tcollection> expansionChannel
      ,Channel<Tsource> source
      ,ThreadLocalChannel<Tsource> itemChannel
      ,Channel<Boolean> stopChannel
      ,Channel<Boolean> filterChannel
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


      filterIterable=new FilterIterable<Tsource>
        (new Iterable<Tsource>()
          {
            @Override
            public Iterator<Tsource> iterator()
            {
              Tsource sourceVal=TreeChannel.this.source.get();
              TreeChannel.this.itemChannel.push(sourceVal);
              try
              {    
                return new TreeIterator
                  ( (Iterator<Tsource>) ArrayUtil.iterator(new Object[] {sourceVal})
                  );
              }
              finally
              { TreeChannel.this.itemChannel.pop();
              }
        
            }
          }
        
        ,itemChannel
        ,filterChannel
        ,false
        );    
        
      if (this.stopChannel==null)
      { expansionIterable=expansionDecorator;
      }
      else
      {
        expansionIterable
          =new FilterIterable<Tsource>
          (expansionDecorator
          ,itemChannel
          ,stopChannel
          ,true
          );
      }
    }
    
    
    @Override
    protected Tcollection retrieve()
    {
      Iterator<Tsource> it
        =filterIterable.iterator();
        
      Tcollection ret=expansionDecorator.newCollection();
      ret=expansionDecorator.addAll
        (ret
        ,it
        );
      return ret;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <D extends Decorator<Tcollection>> D decorate(Class<D> decorator)
      throws BindException
    { 
      if ( ((Class) decorator)==IterationDecorator.class)
      { 
        
        return (D) new IterationDecorator<Tcollection,Tsource>
          (this,source.getReflector())
        {
            
          @Override
          protected Iterator createIterator()
          { return filterIterable.iterator();
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
            Iterator<Tsource> childIter=expansionIterable.iterator();
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