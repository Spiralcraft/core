package spiralcraft.lang.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.common.namespace.StandardPrefixResolver;
import spiralcraft.lang.Expression;
import spiralcraft.lang.ParseException;
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceUtil;
import spiralcraft.vfs.UnresolvableURIException;

public class ExpressionReader
{

  public static ExpressionReader read(URI source)
    throws UnresolvableURIException,ParseException,IOException
  {
    ExpressionReader ret = new ExpressionReader(source);
    ret.read();
    return ret;
  }


  private final URI source;
  private final StandardPrefixResolver ns = new StandardPrefixResolver();
  private Resource resource;
  private int lineNum;
  private Expression<?> expression;
  private StructNode contextStruct;
  
  public ExpressionReader(URI source)
  { 
    this.source=source;
  }

  public Expression<?> getExpression()
  { return expression;
  }
  
  public StructNode getContextStruct()
  { return contextStruct;
  }
  
  private void read()
    throws UnresolvableURIException,ParseException,IOException
  {
    
    URI full=URIUtil.addPathSuffix(source,".expr");
    
    resource
      =Resolver.getInstance().resolve(full);
    
    String str=null;
    
    try
    { str=ResourceUtil.readAsciiString(resource);
    }
    catch (IOException x)
    { throw new IOException("Error reading "+resource.getURI()+" "+full,x);
    }
    
    NamespaceContext.push(ns);
    try
    { 
      try (BufferedReader reader = new BufferedReader(new StringReader(str)))
      {
        StringBuilder expr=new StringBuilder();
        StringBuilder headerBody=null;
        String headerName=null;
        String line;
        boolean headersDone=false;
        lineNum=0;
        while ((line=reader.readLine())!=null )
        {
          lineNum++;
          if (!headersDone)
          {
            if (line.trim().length()==0)
            { 
              if (headerBody!=null)
              { 
                processHeader(headerName,headerBody.toString());
                headerBody=null;
              }
              headersDone=true;
            }
            else if (line.startsWith(" ") || line.startsWith("\t"))
            { 
              if (headerBody==null)
              { throw new IOException("Header line cannot start with whitespace: "+locatorString());
              }
              else
              { 
                headerBody.append(" ");
                headerBody.append(line);
              }
            }
            else
            {
              if (headerBody!=null)
              { 
                processHeader(headerName,headerBody.toString());
                headerBody=null;
              }
              int colonPos=line.indexOf(":");
              if (colonPos<=0)
              { throw new IOException("Expecting a header name followed by a ':'. "+locatorString());
              }
              headerName=line.substring(0,colonPos);
              headerBody=new StringBuilder();
              headerBody.append(line.substring(colonPos+1));
            }
          }
          else
          {
            expr.append(line);
            expr.append("\r\n");
          }
        }
        
        expression=Expression.parse(expr.toString());
      }
    }
    finally
    { NamespaceContext.pop();
    }
    
  }
  
  private String locatorString()
  { return resource.getURI()+" (line "+lineNum+")";
  }
  
  private void processHeader(String headerName,String headerBody)
    throws IOException,ParseException
  {
    switch (headerName.intern())
    {
      case "ns":
        processNs(headerBody.toString());
        break;
      case "context":
        processContext(headerBody.toString());
        break;
      default:
        throw new IOException("Unrecognized header '"+headerName+"'. "+locatorString());
    }
  }
  
  private void processNs(String decl)
    throws IOException
  {
    int eqPos=decl.indexOf("=");
    if (eqPos<=0)
    { 
      throw new IOException("Namespace decl must be in form <prefix> '=' <URI>. "+locatorString());
    }
    String prefix=decl.substring(0,eqPos).trim();
    String url=decl.substring(eqPos+1).trim();
    ns.mapPrefix(prefix,URIPool.create(url));
  }
  
  private void processContext(String expr)
    throws ParseException,IOException
  { 
    Expression<?> contextExpr=Expression.parse(expr);
    Node root = contextExpr.getRootNode();
    if (!(root instanceof StructNode))
    { throw new IOException("Context must be a struct. "+locatorString());
    }
    contextStruct= (StructNode) root;
    
  }
}
