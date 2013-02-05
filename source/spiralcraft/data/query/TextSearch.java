//
// Copyright (c) 1998,2007 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.data.query;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
//import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.FieldNotFoundException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.spi.ListCursor;

/**
 * 
 * A query which find partial term matches in indexed text fields and returns
 *   a SearchResult containing the item and ranking data
 */
public class TextSearch
  extends Query
{
//  private static final ClassLog log
//    =ClassLog.getInstance(Selection.class);
  
  private String[] fields;
  private Expression<String> queryStringX;
  private boolean matchAll=false;
  private int limit;
  
  public TextSearch()
  { 
  }
  


  public TextSearch(Query source,Expression<String> queryStringX)
  { 
    this.queryStringX=queryStringX;
    addSource(source);
  }
  
  public void setFields(String[] names)
  { 
    this.fields=names;
    if (names==null)
    { throw new IllegalArgumentException("Names cannot be null: "+toString());
    }
  }
  
  public String[] getFields()
  { return fields;
  }
  
  public Expression<String> getQueryStringX()
  { return queryStringX;
  }

  public void setQueryStringX(Expression<String> queryStringX)
  { this.queryStringX=queryStringX;
  }
  
  public void setMatchAll(boolean matchAll)
  { this.matchAll=matchAll;
  }
  
  public boolean getMatchAll()
  { return matchAll;
  }
  
  public void setLimit(int limit)
  { this.limit=limit;
  }
  
  public int getLimit()
  { return limit;
  }
  
  @Override
  public void resolve()
    throws DataException
  { 
    super.resolve();
    List<Query> sources=getSources();
    if (sources==null || sources.size()!=1)
    { throw new DataException
        ("TextSearch must have a single source source, not "
        +(sources!=null?sources.size():"0")
        );
    }
    this.type=sources.get(0).getType();
    
  }
  
  @Override
  public FieldSet getFieldSet()
  { return type.getFieldSet();
  }
    

 
  public void setSource(Query source)
  { 
    addSource(source);
  }
  

  
  
  @Override
  /**
   * The default binding uses a case insensitive keyword match against a full
   *   data scan.
   */
  public <T extends Tuple> BoundQuery<?,T> getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException
  { 
    if (this.debugLevel.isDebug())
    { log.log(Level.DEBUG,"Using default binding for TextSearch on "+getType().getURI(),new Exception());
    }

    return new TextSearchBinding<TextSearch,T,Tuple>(this,focus,store);
    
  }
  
  @Override
  public String toString()
  { 
    return super.toString()
      +getSources().toString();
  }
    
}

class TextSearchBinding<Tq extends TextSearch,T extends Tuple,Ts extends Tuple>
  extends UnaryBoundQuery<Tq,T,Ts>
{

  private Focus<Ts> focus;
  private boolean resolved;
  private Channel<String>[] fieldBindings;
  private Channel<String> queryString;
  private boolean matchAll;
  private int limit;
  
  public TextSearchBinding
    (Tq query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    super(query,query.getSources(),paramFocus,store);
    try
    { this.queryString=paramFocus.bind(query.getQueryStringX());
    }
    catch (BindException x)
    { throw new DataException("Error binding query string",x);
    }
    this.debugLevel=query.debugLevel;
    this.matchAll=query.getMatchAll();
    this.limit=query.getLimit();
  }

  @Override
  @SuppressWarnings({"unchecked","rawtypes"})
  public void resolve() throws DataException
  { 
    if (!resolved)
    {
      super.resolve();
    

      focus=paramFocus.chain(sourceChannel);
      if (debugLevel.canLog(Level.DEBUG))
      { log.fine("Binding text search "+getQuery().getType().getURI());
      }
      
      ArrayList<Field<String>> searchFields=new ArrayList<Field<String>>();
      
      if (getQuery().getFields()==null)
      {
        for (Field<?> field:getSourceType().getFieldSet().fieldIterable())
        { 
          if (field.getType() 
              instanceof spiralcraft.data.types.standard.StringType
              )
          { searchFields.add( (Field<String>) field);
          }
        }
      }
      else
      {
        for (String fieldName:getQuery().getFields())
        { 
          Field<?> field=getSourceType().getField(fieldName);
          if (field==null)
          { throw new FieldNotFoundException(getSourceType(),fieldName);
          }
          else if (field.getType() 
              instanceof spiralcraft.data.types.standard.StringType
              )
          { searchFields.add( (Field<String>) field);
          }
        }
      }
      
      fieldBindings=new Channel[searchFields.size()];
      int i=0;
      for (Field<String> field:searchFields)
      { 
        try
        { fieldBindings[i++]=sourceChannel.resolve(focus,field.getName(),null);
        }
        catch (BindException x)
        { throw new DataException("Error binding "+field,x);
        }
      }
      
      resolved=true;
    }
  }
  

  @Override
  protected SerialCursor<T> newSerialCursor(SerialCursor<Ts> source)
    throws DataException
  { return new TextSearchScrollableCursor(source);
  }
  
  @Override
  protected ScrollableCursor<T> newScrollableCursor(ScrollableCursor<Ts> source)
  { return null;
  }

  
  
  protected class TextSearchScrollableCursor
    extends ListCursor<T>
  {

    LinkedList<TextSearchResult> result=new LinkedList<TextSearchResult>();

    private String[] keywords;


    @SuppressWarnings("unchecked")
    public TextSearchScrollableCursor(SerialCursor<Ts> source)
      throws DataException
    { 
      super(source.getFieldSet());

      String queryString=TextSearchBinding.this.queryString.get();
      if (queryString!=null)
      { keywords=queryString.toLowerCase().split(" ");
      }

      BitSet flags=null;
      if (matchAll)
      { flags=new BitSet(keywords.length);
      }
      
      while (source.next())
      { 
        
        
        Ts tuple=source.getTuple();
        if (tuple.isVolatile())
        { tuple=(Ts) tuple.snapshot();
        }
        sourceChannel.push(tuple);
        try
        { score(tuple,flags);
        }
        finally
        { sourceChannel.pop();
        }
        
      }
      Collections.sort(result);
      
      data=new LinkedList<T>();
      int count=0;
      for (TextSearchResult entry:result)
      { 
        if (limit==0 || count<limit)
        { data.add((T) entry.getData());
        }
        count++;
      }
    }

    private void score(Tuple t,BitSet flags)
      throws DataException
    { 
      if (flags!=null)
      { flags.clear();
      }
      int matches=0;
      
      if (keywords!=null)
      {
        
        for (Channel<String> binding : fieldBindings)
        { 
          String text=binding.get();
          if (text!=null)
          {
            text=text.toLowerCase();
          
            // bare bones matcher
            int i=0;
            for (String keyword:keywords)
            { 
              if (text.contains(keyword))
              { 
                matches++;
                if (matchAll)
                { flags.set(i);
                }
              }
              i++;
            }
          }
        }
      }
      
      if (matches>0 && (!matchAll || flags.cardinality()==flags.length()))
      { 
        TextSearchResult entry=new TextSearchResult();
        entry.data=t;
        entry.score=Integer.valueOf(matches).floatValue();;
        result.add(entry);
        
      }
      
    }
    
    @Override
    // TODO: Determine if we really need this override
    //   this is most likely redundant
    public Type<?> getResultType()
    { return TextSearchBinding.this.getType();
    }

  }
  

}

class TextSearchResult
  implements Comparable<TextSearchResult>
{
  float score;
  Tuple data;
  
  public float getScore()
  { return score;
  }
  
  public Tuple getData()
  { return data;
  }

  @Override
  public int compareTo(TextSearchResult o)
  { return -Float.compare(score,o.score);
  }
}

