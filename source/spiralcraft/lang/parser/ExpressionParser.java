package spiralcraft.lang.parser;

import spiralcraft.lang.Expression;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;

import java.math.BigDecimal;

/**
 * Create a parse tree for expression grammar
 */
public class ExpressionParser
{
  private StringBuffer _progressBuffer;
  private StreamTokenizer _tokenizer;
  private int _pos;

  public Expression parse(String text)
    throws ParseException
  { 
    createTokenizer(text);
    nextToken();
    return new Expression(parseExpression(),text);    
  }

  private void createTokenizer(String expression)
  {
    _tokenizer=new StreamTokenizer(new StringReader(expression));
    _tokenizer.parseNumbers();
    _tokenizer.ordinaryChar((int) '.');
    _tokenizer.ordinaryChar((int) '!');
    _tokenizer.ordinaryChar((int) '=');
    _tokenizer.ordinaryChar((int) ')');
    _tokenizer.ordinaryChar((int) '(');
    _tokenizer.ordinaryChar((int) ',');
    _tokenizer.ordinaryChar((int) '>');
    _tokenizer.ordinaryChar((int) '<');
    _tokenizer.ordinaryChar((int) '&');
    _tokenizer.ordinaryChar((int) '|');
    _tokenizer.ordinaryChar((int) '?');
    _tokenizer.ordinaryChar((int) ':');
    _tokenizer.ordinaryChar((int) '+');
    _tokenizer.ordinaryChar((int) '-');
    _tokenizer.ordinaryChar((int) '*');
    _tokenizer.ordinaryChar((int) '/');
    _tokenizer.ordinaryChar((int) '[');
    _tokenizer.ordinaryChar((int) ']');
    _tokenizer.quoteChar((int) '"');
    _progressBuffer=new StringBuffer();
    _pos=0;
  }

  private void throwUnexpected()
    throws ParseException
  { throw new ParseException("Not expecting "+_tokenizer.toString(),_pos,_progressBuffer.toString());
  }

  /**
   * Verifies and consumes specified input, if not expected, throws exception
   */
  private void expect(char chr)
    throws ParseException
  {
    if (_tokenizer.ttype!=chr)
    { throw new ParseException("Expected '"+chr+"', not "+_tokenizer.toString(),_pos,_progressBuffer.toString());
    }
    nextToken();
  }

  private boolean nextToken()
  { 

    try
    { _tokenizer.nextToken();
    }
    catch (IOException x)
    { 
      // Should never happen reading String
      x.printStackTrace();
    }
    if (_tokenizer.sval!=null)
    { 
      if (_tokenizer.ttype=='"')
      { _progressBuffer.append('"').append(_tokenizer.sval).append('"');
      }
      else
      { _progressBuffer.append(_tokenizer.sval);
      }
      _pos+=_tokenizer.sval.length();
    }
    else
    { 
      if (_tokenizer.ttype!=_tokenizer.TT_EOF)
      { 
        _progressBuffer.append((char) _tokenizer.ttype);
        _pos++;
      }
    }
    return _tokenizer.ttype!=_tokenizer.TT_EOF;
  }


  /**
   * Expression -> ConditionalExpression
   */
  private Node parseExpression()
    throws ParseException
  { return parseConditionalExpression();
  }


  /**
   * ConditionalExpression -> LogicalOrExpression 
   *                          ( "?" conditionalExpression ":" conditionalExpression )
   */
  private Node parseConditionalExpression()
    throws ParseException
  {
    Node node=parseLogicalOrExpression();
    if (_tokenizer.ttype=='?')
    { 
      nextToken();
      Node trueResult=parseConditionalExpression();
      expect(':');
      Node falseResult=parseConditionalExpression();
      node=new ConditionalNode(node,trueResult,falseResult);
    }
    return node;
  }

  /**
   * LogicalOrExpression -> logicalAndExpression 
   *                        ( "||" logicalAndExpression )*
   */
  private Node parseLogicalOrExpression()
    throws ParseException
  {
    Node node=parseLogicalAndExpression();
    return parseLogicalOrExpressionRest(node);
  }
  
  private Node parseLogicalOrExpressionRest(Node firstOperand)
    throws ParseException
  {
    if (_tokenizer.ttype=='|')
    { 
      nextToken();
      expect('|');
      Node secondOperand=parseLogicalAndExpression();
      Node logicalOrNode = new LogicalOrNode(firstOperand,secondOperand);
      return parseLogicalOrExpressionRest(logicalOrNode);
    }
    else
    { return firstOperand;
    }
  }

  /**
   * LogicalAndExpression -> exclusiveOrExpression 
   *                        ( "&&" exclusiveOrExpression )*
   */
  private Node parseLogicalAndExpression()
    throws ParseException
  {
    Node node=parseExclusiveOrExpression();
    return parseLogicalAndExpressionRest(node);
  }
  
  private Node parseLogicalAndExpressionRest(Node firstOperand)
    throws ParseException
  {
    if (_tokenizer.ttype=='&')
    { 
      nextToken();
      expect('&');
      Node secondOperand=parseExclusiveOrExpression();
      Node logicalAndNode = new LogicalAndNode(firstOperand,secondOperand);
      return parseLogicalAndExpressionRest(logicalAndNode);
    }
    else
    { return firstOperand;
    }
  }

  /**
   * ExclusiveOrExpression -> EqualityExpression 
   *                        ( "^" EqualityExpression )*
   */
  private Node parseExclusiveOrExpression()
    throws ParseException
  {
    Node node=parseEqualityExpression();
    return parseExclusiveOrExpressionRest(node);
  }
  
  private Node parseExclusiveOrExpressionRest(Node firstOperand)
    throws ParseException
  {
    if (_tokenizer.ttype=='^')
    { 
      nextToken();
      Node exclusiveOrNode = new ExclusiveOrNode(firstOperand,parseEqualityExpression());
      return parseExclusiveOrExpressionRest(exclusiveOrNode);
    }
    else
    { return firstOperand;
    }
  }

  /**
   * EqualityExpression -> RelationalExpression
   *                       ( ("!=" | "==") RelationalExpression )
   */
  private Node parseEqualityExpression()
    throws ParseException
  {
    Node node=parseRelationalExpression();
    return parseEqualityExpressionRest(node);
  }

  private Node parseEqualityExpressionRest(Node firstOperand)
    throws ParseException
  { 
    if (_tokenizer.ttype=='!')
    {
      nextToken();
      expect('=');
      Node secondOperand=parseRelationalExpression();
      Node equalityNode = new EqualityNode(true,firstOperand,secondOperand);
      return parseEqualityExpressionRest(equalityNode);
    }
    else if (_tokenizer.ttype=='=')
    {
      nextToken();
      expect('=');
      Node secondOperand=parseRelationalExpression();
      Node equalityNode = new EqualityNode(false,firstOperand,secondOperand);
      return parseEqualityExpressionRest(equalityNode);
    }
    else
    { return firstOperand;
    }
  }


  /**
   * RelationalExpression -> AdditiveExpression
   *                         ( "<" | ">" | "<=" | ">=" ) AdditiveExpression)*
   */
  private Node parseRelationalExpression()
    throws ParseException
  { 
    Node node=parseAdditiveExpression();
    return parseRelationalExpressionRest(node);
  }

  private Node parseRelationalExpressionRest(Node firstOperand)
    throws ParseException
  {
    boolean greaterThan=false;
    boolean equals=false;
    switch (_tokenizer.ttype)
    {
      case '>':
        greaterThan=true;
      case '<':
        nextToken();
        if (_tokenizer.ttype=='=')
        {
          nextToken();
          equals=true;
        }

        Node operation 
          = new RelationalNode
            (greaterThan
            ,equals
            ,firstOperand
            ,parseAdditiveExpression()
            );

        return parseRelationalExpressionRest(operation);
                
      default:
        return firstOperand;        
    }
  }

  /**
   * AdditiveExpression -> MultiplicativeExpression
   *                      ( ("+" | "-") MultiplicativeExpression )
   */
  private Node parseAdditiveExpression()
    throws ParseException
  {
    Node node=parseMultiplicativeExpression();
    return parseAdditiveExpressionRest(node);
  }

  private Node parseAdditiveExpressionRest(Node firstOperand)
    throws ParseException
  {
    Node operation;
    Node node;
    switch (_tokenizer.ttype)
    {
      case '-':
      case '+':
        char ttype=(char) _tokenizer.ttype;
        nextToken();
        operation 
          = new OpNode
            (firstOperand
            ,parseMultiplicativeExpression()
            ,ttype
            );
        node=parseAdditiveExpressionRest(operation);
        break;
      default:
        node=firstOperand;
        break;
    }
    return node;
    
  }

  /**
   * MultiplicativeExpressionn -> UnaryExpression
   *                      ( ("*" | "/" | "%") UnaryExpression )
   */
  private Node parseMultiplicativeExpression()
    throws ParseException
  {
    Node node=parseUnaryExpression();
    return parseMultiplicativeExpressionRest(node);
  }

  private Node parseMultiplicativeExpressionRest(Node firstOperand)
    throws ParseException
  {
    Node operation;
    Node node;
    switch (_tokenizer.ttype)
    {
      case '/':
      case '*':
      case '%':
        char ttype=(char) _tokenizer.ttype;
        nextToken();
        operation 
          = new OpNode
            (firstOperand
            ,parseUnaryExpression()
            ,ttype
            );
        node = parseMultiplicativeExpressionRest(operation);
        break;
      default:
        node = firstOperand;
        break;
    }
    return node;  
  }

  /**
   * UnaryExpression -> "-" UnaryExpression
   *                  | "!" UnaryExpression
   *                  | PostfixExpression
   */
  private Node parseUnaryExpression()
    throws ParseException
  {
    switch (_tokenizer.ttype)
    {
      case '-':
        nextToken();
        return new NumericNegateNode(parseUnaryExpression());
      case '!':
        nextToken();
        return new LogicalNegateNode(parseUnaryExpression());
      default:
        return parsePostfixExpression();
    }
  }

  /**
   * PostfixExpression -> FocusExpression
   *                     ( "(" ExpressionList ")" 
   *                     |  "[" Expression "]"
   *                     | "." IdentifierExpression
   *                     ) *
   */
  private Node parsePostfixExpression()
    throws ParseException
  {
    Node node = parseFocusExpression();
    return parsePostfixExpressionRest(node);
  }
  
  private Node parsePostfixExpressionRest(Node primary)
    throws ParseException
  {
    switch (_tokenizer.ttype)
    {
      case '(':
        if (!(primary instanceof ResolveNode))
        { throwUnexpected();
        }
        nextToken();
        Node methodCallNode
          =new MethodCallNode
            ((ResolveNode) primary
            ,parseExpressionList()
            );
        expect(')');
        return parsePostfixExpressionRest(methodCallNode);
      case '[':
        nextToken();
        Node subscriptNode=new SubscriptNode(primary,parseExpression());
        expect(']');
        return parsePostfixExpressionRest(subscriptNode);
      case '.':
        nextToken();
        IdentifierNode idNode=parseIdentifier();
        Node resolveNode=new ResolveNode(primary,idNode);
        return parsePostfixExpressionRest(resolveNode);
      default:
        return primary;
    }
    
    
  }

  /**
   * ExpressionList -> Expression ("," Expression)*
   */
  private List parseExpressionList()
    throws ParseException
  {
    List list=new LinkedList();
    list.add(parseExpression());
    parseExpressionListRest(list);
    return list;
  }

  private void parseExpressionListRest(List list)
    throws ParseException
  {
    if (_tokenizer.ttype==',')
    {
      nextToken();
      list.add(parseExpression());
      parseExpressionListRest(list);
    }
    return;
  }

  /**
   * PrimaryExpression -> Number
   *                    | String 
   *                    | "true" 
   *                    | "false" 
   *                    | "null"
   *                    | "(" Expression ")"
   */
  private Node parsePrimaryExpression()
    throws ParseException
  {
    Node node=null;
    switch (_tokenizer.ttype)
    {
      case StreamTokenizer.TT_WORD:
        if (_tokenizer.sval.equals("true"))
        { node=new LiteralNode(Boolean.TRUE,Boolean.class);
        }
        else if (_tokenizer.sval.equals("false"))
        { node=new LiteralNode(Boolean.FALSE,Boolean.class);
        }
        else if (_tokenizer.sval.equals("null"))
        { node=new LiteralNode(null,Object.class);
        }
        else
        { throwUnexpected();
        }
//        else
//        { node=new IdentifierNode(_tokenizer.sval);
//        }
        nextToken();
        break;
      case '"':
        node=new LiteralNode(_tokenizer.sval,String.class);
        nextToken();
        break;
      case StreamTokenizer.TT_NUMBER:
        node=parseNumber();
        break;
      case '(':
        nextToken();
        node=parseExpression();
        expect(')');
        break;
      default:
        throwUnexpected();
    }
    return node;
  }

  private IdentifierNode parseIdentifier()
    throws ParseException
  { 
    IdentifierNode ret=null;
    if (_tokenizer.ttype!=StreamTokenizer.TT_WORD)
    { throwUnexpected();
    }
    else
    { 
      ret=
        new IdentifierNode(_tokenizer.sval);
      nextToken();
    }
    return ret;
  }

  private Node parseNumber()
    throws ParseException
  {
    double number=_tokenizer.nval;
    nextToken();
    if (_tokenizer.ttype==StreamTokenizer.TT_WORD)
    {
      if (_tokenizer.sval.equals("L"))
      { 
        nextToken();
        return new LiteralNode(new Long((long) number),Long.class);
      }
      else if (_tokenizer.sval.equals("D"))
      {
        nextToken();
        return new LiteralNode(new Double(number),Double.class);
      }
      else if (_tokenizer.sval.equals("F"))
      {
        nextToken();
        return new LiteralNode(new Float((float) number),Float.class);
      }
    }
    return new LiteralNode(new BigDecimal(Double.toString(number)),BigDecimal.class);
    
  }

  /**
   * FocusExpression -> ( "[" FocusSpecifier "]" ) 
   *                    ( "." Identifier 
   *                      | Identifier
   *                      | PrimaryExpression
   */
  private Node parseFocusExpression()
    throws ParseException
  { 
    FocusNode focusNode=null;
    if (_tokenizer.ttype=='[')
    {
      nextToken();
      focusNode=parseFocusSpecifier();
      expect(']');
    }

    switch (_tokenizer.ttype)
    {
      case StreamTokenizer.TT_WORD:
        IdentifierNode id=new IdentifierNode(_tokenizer.sval);
        nextToken();
        return new FocusResolveNode(focusNode,id);
      case '.':
        nextToken();
        IdentifierNode idNode=parseIdentifier();
        return new ResolveNode(focusNode,idNode);
      default:
        return parsePrimaryExpression();
    }
  }

  /**
   * FocusExpression -> FocusName ( ":" Expression )
   */
  private FocusNode parseFocusSpecifier()
    throws ParseException
  {
    StringBuffer focusName=new StringBuffer();
    
    while (true)
    {
      switch (_tokenizer.ttype)
      {
        case ']':
          return new FocusNode(focusName.toString(),null);
        case StreamTokenizer.TT_EOF:
          throwUnexpected();
        case ':':
          nextToken();
          return new FocusNode(focusName.toString(),parseExpression());
        case StreamTokenizer.TT_WORD:
        case StreamTokenizer.TT_NUMBER:
          focusName.append(_tokenizer.sval);
          nextToken();
          break;
        default:
          focusName.append((char) _tokenizer.ttype);
          nextToken();
          break;
      }
    }
  }

}
