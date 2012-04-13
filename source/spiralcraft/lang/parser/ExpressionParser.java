//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.lang.parser;

import spiralcraft.lang.Expression;
import spiralcraft.lang.ParseException;
import spiralcraft.log.ClassLog;
import spiralcraft.util.string.StringPool;

import spiralcraft.common.namespace.UnresolvedPrefixException;
import spiralcraft.io.LookaheadStreamTokenizer;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;


/**
 * Create a parse tree for expression grammar
 */

public class ExpressionParser
{
  private static boolean debug=false;
  private static final ClassLog log
    =ClassLog.getInstance(ExpressionParser.class);

  private StringBuffer _progressBuffer;
  private LookaheadStreamTokenizer _tokenizer;
  private int _pos;
  private String _text;
  private StringPool stringPool=StringPool.INSTANCE;

  public <X> Expression<X> parse(String text)
    throws ParseException
  { 
    createTokenizer(text);
    _text=text;
    consumeToken();
    Node ret=parseBindingExpression();
    if (ret==null)
    { throwUnexpected();
    }
    if (_tokenizer.ttype!=StreamTokenizer.TT_EOF)
    { throwUnexpected();
    }
    return Expression.create(ret,text);    
  }

  
  private void createTokenizer(String expression)
  {
    
    _tokenizer=new LookaheadStreamTokenizer(new StringReader(expression));
    StreamTokenizer syntax=_tokenizer.lookahead;
    
    syntax.resetSyntax();
    
    syntax.wordChars('a','z');
    syntax.wordChars('A','Z');
    syntax.wordChars('_','_');
    syntax.wordChars('@','@');

    syntax.whitespaceChars('\r','\r');
    syntax.whitespaceChars('\n','\n');
    syntax.whitespaceChars('\t','\t');
    syntax.whitespaceChars(' ',' ');
    
    syntax.ordinaryChar('.');
    syntax.ordinaryChar('!');
    syntax.ordinaryChar('=');
    syntax.ordinaryChar(')');
    syntax.ordinaryChar('(');
    syntax.ordinaryChar(',');
    syntax.ordinaryChar('>');
    syntax.ordinaryChar('<');
    syntax.ordinaryChar('&');
    syntax.ordinaryChar('|');
    syntax.ordinaryChar('?');
    syntax.ordinaryChar(':');
    syntax.ordinaryChar('+');
    syntax.ordinaryChar('-');
    syntax.ordinaryChar('*');
    syntax.ordinaryChar('/');
    syntax.ordinaryChar('[');
    syntax.ordinaryChar(']');
    syntax.ordinaryChar('`');
    
    syntax.wordChars('0','9');

    syntax.quoteChar('"');

    syntax.quoteChar('\'');
    
    syntax.eolIsSignificant(false);
    
    _progressBuffer=new StringBuffer();
    _pos=0;
  }
  
   
  private void throwUnexpected()
    throws ParseException
  { throwUnexpected(null);
  }

  private void throwUnexpected(String msg)
    throws ParseException
  { 
    throw new ParseException
      ("Not expecting '"+tokenString()+"'"
      +(msg!=null?": "+msg+":":"")
      +" @line "+_tokenizer.lineno()
      ,_pos
      ,_progressBuffer.toString()
      ,_text
      );
  }

  
  private ParseException newException(String message)
  { 
    return new ParseException
      (message+": In '"+tokenString()+"' @line "+_tokenizer.lineno()
      ,_pos
      ,_progressBuffer.toString()
      ,_text
      );
  }


  private ParseException newException(String message,Exception cause)
  {
    return new ParseException
      (message+": In '"+tokenString()+"' @line "+_tokenizer.lineno()
      ,cause
      ,_pos
      ,_progressBuffer.toString()
      ,_text
      );
  }

  private void alert(String message)
  { System.err.println(message);
  }
  
  /**
   * Verifies and consumeTokens specified input, if not expected, throws exception
   */
  private void expect(char chr)
    throws ParseException
  {
    if (_tokenizer.ttype!=chr)
    { 
      throw new ParseException
        ("Expected '"+chr+"', not '"+tokenString()+"'"
        ,_pos
        ,_progressBuffer.toString()
        ,_text
        );
    		
    }
    consumeToken();
  }
  
  private String tokenString()
  {
    if (_tokenizer.ttype==StreamTokenizer.TT_WORD)
    { return _tokenizer.sval;
    }
    else if (_tokenizer.ttype==StreamTokenizer.TT_EOF)
    { return "EOF";
    }
    else if (Character.isISOControl(_tokenizer.ttype))
    { return Integer.toHexString(_tokenizer.ttype);
    }
    else
    { return Character.toString((char) _tokenizer.ttype);
    }
  }
  
  private boolean consumeToken()
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
      else if (_tokenizer.ttype=='\'')
      { _progressBuffer.append('\'').append(_tokenizer.sval).append('\'');
      }
      else
      { _progressBuffer.append(_tokenizer.sval);
      }
      _pos+=_tokenizer.sval.length();
    }
    else
    { 
      if (_tokenizer.ttype!=StreamTokenizer.TT_EOF)
      { 
        _progressBuffer.append((char) _tokenizer.ttype);
        _pos++;
      }
    }
    return _tokenizer.ttype!=StreamTokenizer.TT_EOF;
  }

  /**
   * BindingExpression -> Expression ( ":=" Expression)
   * 
   * @return
   */
  private Node parseBindingExpression()
    throws ParseException
  {
    Node node=this.parseExpression();
    if (_tokenizer.ttype==':' && _tokenizer.lookahead.ttype=='=')
    { 
      if (node==null)
      { throw newException("Missing left hand side of binding expression");
      }
      consumeToken();
      consumeToken();
      node=node.bindFrom(parseExpression());
    }
    return node;
  }
  
  /**
   * Expression -> AssignmentExpression
   * 
   * @return
   * @throws ParseException
   */
  private Node parseExpression()
    throws ParseException
  { return parseAssignmentExpression();
  }
  


  
  /**
   * AssignmentExpression -> ConditionalExpression ( "=" Expression )
   */
  private Node parseAssignmentExpression()
    throws ParseException
  { 
    Node node=this.parseConditionalExpression();
    if (_tokenizer.ttype=='=')
    {
      if (node==null)
      { throw newException("Missing left hand side of assignment");
      }
      consumeToken();
      node=node.assign(this.parseExpression());
    }
    else if (_tokenizer.ttype=='+' && _tokenizer.lookahead.ttype=='=')
    {
      if (node==null)
      { throw newException("Missing left hand side of assignment");
      }
      consumeToken();
      consumeToken();
      node=node.assignAdditive(this.parseExpression());
      
    }
    else if (_tokenizer.ttype=='-' && _tokenizer.lookahead.ttype=='=')
    {
      if (node==null)
      { throw newException("Missing left hand side of assignment");
      }
      consumeToken();
      consumeToken();
      node=node.assignSubtractive(this.parseExpression());
      
    }
    return node;
  }


  /**
   * ConditionalExpression -> LogicalOrExpression 
   *                          ( "?" conditionalExpression ":" conditionalExpression )
   */
  private Node parseConditionalExpression()
    throws ParseException
  {
    Node node=this.parseLogicalOrExpression();
    if (_tokenizer.ttype=='?' && _tokenizer.lookahead.ttype!='=')
    { 
      consumeToken();
      Node trueResult=this.parseConditionalExpression();
      if (trueResult==null)
      { throw newException("Missing conditional result expression");
      }
      expect(':');
      Node falseResult=this.parseConditionalExpression();
      if (falseResult==null)
      { throw newException("Missing conditional result expression");
      }
      node=node.onCondition(trueResult, falseResult);
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
      consumeToken();
      expect('|');
      Node secondOperand=parseLogicalAndExpression();
      Node logicalOrNode = firstOperand.or(secondOperand);
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
      consumeToken();
      expect('&');
      Node secondOperand=parseExclusiveOrExpression();
      Node logicalAndNode = firstOperand.and(secondOperand);
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
      consumeToken();
      Node exclusiveOrNode = firstOperand.xor(parseEqualityExpression());
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
      consumeToken();
      expect('=');
      Node secondOperand=parseRelationalExpression();
      Node equalityNode = firstOperand.isNotEqual(secondOperand);
      return parseEqualityExpressionRest(equalityNode);
    }
    else if (_tokenizer.ttype=='=' && _tokenizer.lookahead.ttype=='=')
    {
      consumeToken();
      consumeToken();
      Node secondOperand=parseRelationalExpression();
      Node equalityNode = firstOperand.isEqual(secondOperand);
      return parseEqualityExpressionRest(equalityNode);
    }
    else if (_tokenizer.ttype=='?' && _tokenizer.lookahead.ttype=='=')
    {
      consumeToken();
      consumeToken();
      Node secondOperand=parseRelationalExpression();
      Node equalityNode = firstOperand.contains(secondOperand);
      return parseEqualityExpressionRest(equalityNode);
      
    }
    else
    { return firstOperand;
    }
  }


  /**
   * RelationalExpression -> RangeExpression
   *                         ( "<" | ">" | "<=" | ">=" ) RangeExpression)*
   */
  private Node parseRelationalExpression()
    throws ParseException
  { 
    Node node=parseRangeExpression();
    return parseRelationalExpressionRest(node);
  }
  
  private Node parseRelationalExpressionRest(Node firstOperand)
    throws ParseException
  {
    switch (_tokenizer.ttype)
    {
      case '>':
        consumeToken();
        if (_tokenizer.ttype=='=')
        {
          consumeToken();
          return parseRelationalExpressionRest
            (firstOperand.greaterThanOrEquals
              (parseRangeExpression()
              )
            );
        }
        else
        {
          return parseRelationalExpressionRest
            (firstOperand.greaterThan
              (parseRangeExpression()
              )
            );
        }
      case '<':
        consumeToken();
        if (_tokenizer.ttype=='=')
        {
          consumeToken();
          return parseRelationalExpressionRest
            (firstOperand.lessThanOrEquals
              (parseRangeExpression()
              )
            );
        }
        else
        {
          return parseRelationalExpressionRest
            (firstOperand.lessThan
              (parseRangeExpression()
              )
            );
        }
      default:
        return firstOperand;        
    }
  }

  /**
   * RangeExpression 
   *   -> AdditiveExpression 
   *          ( [ '..' | '.!'] AdditiveExpression 
   *          ) 
   */
  private Node parseRangeExpression()
    throws ParseException
  {
    Node node=parseAdditiveExpression();
    return parseRangeExpressionRest(node);
  }

  @SuppressWarnings("rawtypes")
  private Node parseRangeExpressionRest(Node firstOperand)
    throws ParseException
  {
    Node rangeStart=firstOperand;
    if (_tokenizer.ttype=='.')
    {
      consumeToken();
      boolean inclusive=false;
      Node rangeEnd;
      
      switch(_tokenizer.ttype)
      { 
        case '.':
          consumeToken();
          inclusive=true;
          break;
        case '!':
          consumeToken();
          inclusive=false;
          break;
          
        default:
          throw newException("Expected '..' or '.!' and rest of Range expression");
      }
      
      
      rangeEnd=parseAdditiveExpression();
      
      
      return new RangeNode(rangeStart,rangeEnd,inclusive);
    }
    else
    { return firstOperand;
    }
  }
  
  /**
   * AdditiveExpression -> MultiplicativeExpression
   *                      ( ['+' | '-'] MultiplicativeExpression )
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
        if (_tokenizer.lookahead.ttype!='='
            && _tokenizer.lookahead.ttype!='-'
            )
        {
          consumeToken();
          operation=firstOperand.minus(expectNode(parseMultiplicativeExpression()));
          node=parseAdditiveExpressionRest(operation);
        }
        else
        { node=firstOperand;
        }
        break;
      case '+':
        if (_tokenizer.lookahead.ttype!='='
            && _tokenizer.lookahead.ttype!='+'
            )
        {
          consumeToken();
          operation=firstOperand.plus(expectNode(parseMultiplicativeExpression()));
          node=parseAdditiveExpressionRest(operation);
        }
        else
        { node=firstOperand;
        }
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
    if (node!=null)
    { return parseMultiplicativeExpressionRest(node);
    }
    return null;
  }
  
  private Node parseMultiplicativeExpressionRest(Node firstOperand)
    throws ParseException
  {
    Node operation;
    Node node;
    switch (_tokenizer.ttype)
    {
      case '/':
        consumeToken();
        operation = firstOperand.divide(expectNode(parseUnaryExpression())); 
        node = parseMultiplicativeExpressionRest(operation);
        break;
      case '*':
        consumeToken();
        operation = firstOperand.times(expectNode(parseUnaryExpression())); 
        node = parseMultiplicativeExpressionRest(operation);
        break;
      case '%':
        consumeToken();
        operation = firstOperand.modulus(expectNode(parseUnaryExpression())); 
        node = parseMultiplicativeExpressionRest(operation);
        break;
      default:
        node = firstOperand;
        break;
    }
    return node;  
  }
  
  private Node expectNode(Node node)
    throws ParseException
  {
    if (node==null)
    { throwUnexpected("Unexpected end of Expression");
    }
    return node;
  }

  /**
   * UnaryExpression -> "-" UnaryExpression
   *                  | "!" UnaryExpression
   *                  | "{" ListExpression "}" ( PostfixExpression )
   *                  | PostfixExpression
   */
  private Node parseUnaryExpression()
    throws ParseException
  {
    switch (_tokenizer.ttype)
    {
      case '-':
        consumeToken();
        return parseUnaryExpression().negative(); 
      case '!':
        consumeToken();
        return parseUnaryExpression().not();
      default:
        return parsePostfixExpression();
    }
  }

//  /**
//   * ListExpression -> ExpressionList
//   */
//  @SuppressWarnings("unchecked") // Unknown type
//  private Node parseListExpression()
//    throws ParseException
//  { 
//    // Only used from ArraySelectorExpression
//    return new ListNode(parseExpressionList());
//  }
  
  // 
  // Beyond this point there is no left-hand-side recursion
  //

  /**
   * PostfixExpression -> 
   *   FocusExpression
   *   ( ArraySelectorExpression 
   *   | "." ( DereferenceExpression | ObjectLiteralExpression | ChannelMetaExpression)
   *   | "#" MapExpression
   *   | "$" ReduceExpression
   *   | "{" SubcontextExpression "}"
   *   ) *
   */
  private Node parsePostfixExpression()
    throws ParseException
  {
    // The focus expression returns a node which provides the 
    //   target of further dereference, or null to indicate no
    //   expression is present.
    Node node = parseFocusExpression();
    if (node!=null)
    { return parsePostfixExpressionRest(node);
    }
    else
    { return null;
    }
  }
  
  // Called from parsePostfixExpression() and internally
  private Node parsePostfixExpressionRest(Node primary)
    throws ParseException
  {
    // This handles the entire dot/subscript/member dereference chain
    switch (_tokenizer.ttype)
    {
      case '[':
        return parsePostfixExpressionRest
          (parseArraySelectorExpression(primary)
          );
      case '.':
        Node ret=parsePostfixDotExpression(primary);
        if (ret!=null)
        { return parsePostfixExpressionRest(ret);
        }
        else
        { 
          // Fall back up- end of postfix expression- not a postfix "."
          return primary;
        }
      case '#':
        return parsePostfixExpressionRest
          (parseMapExpression(primary)
          );
      case '$':
        return parsePostfixExpressionRest
          (parseReduceExpression(primary)
          );
      case '{':
        return parsePostfixExpressionRest
          (parseSubcontextExpression(primary)
          );
      default:
        return primary;
    }
    
    
  }
  
  private Node parsePostfixDotExpression(Node primary)
    throws ParseException
  {
    switch (_tokenizer.lookahead.ttype)
    {
      case StreamTokenizer.TT_WORD:
      case '(':
        consumeToken();
        return parseDereferenceExpression(primary);
      case '[':
        consumeToken();
        switch (_tokenizer.lookahead.ttype)
        {
          case '*':
            return parseObjectLiteralExpression(primary);
          default:
            return parseChannelMetaExpression(primary);
        }
      default:
        // .. and .! are RangeExpression tokens, so just ignore
        return null;
    }
  }
  
  /**
   * SubcontextExpression ->    "{" Expression "}"
   * 
   * @param source
   * @return
   */
  private Node parseSubcontextExpression(Node primary)
    throws ParseException
  {
    expect('{');
    List<Node> subexpressionList=parseExpressionList();
    if (subexpressionList==null)
    { throwUnexpected("Subcontext expression list cannot be empty");
    }
    Node ret=primary.subcontext(subexpressionList);
    expect('}');
    return ret;
  }
  
  
  /**
   * ArraySelectorExpression -> "["  Expression  "]"
   * 
   * @return
   */
  private Node parseArraySelectorExpression(Node primary)
    throws ParseException
  {
    expect('[');

    Node ret=parseExpression();

//    Node ret;    
//    switch (_tokenizer.ttype)
//    {
//      case '{':
//        // Dynamic list- used primarily to create fixed length typed Arrays
//        consumeToken();
//        ret=parseListExpression();
//        expect('}');
//        break;
//      default:
//        ret=parseExpression();
//    }
    
    if (ret==null)
    { throw newException("Expected selector expression");
    }
    Node subscriptNode=primary.subscript(ret);
    expect(']');
    return subscriptNode;
  }
  
  
  /**
   * FocusExpression -> 
   *   (  FocusSpecifier  ) 
   *     |
   *   ( "." FocusRelativeExpression 
   *     | ObjectLiteralExpression
   *     | ArrayLiteralExpression
   *     | IdentifierExpression
   *     | PrimaryExpression
   *   )
   */
  private Node parseFocusExpression()
    throws ParseException
  { 
    if (_tokenizer.ttype=='[')
    { 
      if (_tokenizer.lookahead.ttype=='*')
      { return parseObjectLiteralExpression(new CurrentFocusNode());
      }
      else if (_tokenizer.lookahead.ttype=='{')
      { return parseArrayLiteralExpression(new CurrentFocusNode());
      }
      else
      { 
        FocusNode focusSpec=parseFocusSpecifier();
        
        if (_tokenizer.ttype==StreamTokenizer.TT_WORD)
        { 
          log.warning
            ("Deprecated expression syntax '"
            +focusSpec.reconstruct()+" "+_tokenizer.sval
            +"'. Use '"
            +focusSpec.reconstruct()+"."+_tokenizer.sval
            +"' instead."
            );
          return parseDereferenceExpression(focusSpec);
        }
        else
        { return focusSpec;
        }
      }
    }

    switch (_tokenizer.ttype)
    {
      case StreamTokenizer.TT_EOF:
        // null/empty expression
        throwUnexpected();
        return null;
      case '.':
        
        FocusNode focusNode;
        switch (_tokenizer.lookahead.ttype)
        {
          case '.':
            focusNode=new CurrentFocusNode(".");
            // String of '.' characters means traverse up Focus chain
            consumeToken();      
            Node fre=parseFocusRelativeExpression(focusNode);
            if (debug)
            { alert("parseFocusExpression():'..' "+fre.reconstruct());
            }
            return fre;
          case '[':
            focusNode=new CurrentFocusNode(".");

            consumeToken();      
            if (_tokenizer.lookahead.ttype=='*')
            { return parseObjectLiteralExpression(focusNode);
            }
            else if (_tokenizer.lookahead.ttype=='@')
            { 
              throw newException("Aspect dereference not supported yet");
            }
            else
            { return parseChannelMetaExpression(focusNode);
            }
          default:
            focusNode=new CurrentFocusNode();
            Node dotExpr=parsePostfixDotExpression(focusNode);
            if (dotExpr!=null)
            { return dotExpr;
            }
            else
            { 
              // Reference is to subject of current focus with no postfix
              consumeToken();
              return focusNode;
            }
        }
      case StreamTokenizer.TT_WORD:
        // Either a literal, or 
        
        Node ret=parsePrimaryExpression(new CurrentFocusNode());
        if (debug)
        { alert("parseFocusExpression.TT_WORD "+ret.reconstruct());
        }
        return ret;
        // If this is an identifier, parseIdentifierExpression() will
        //   catch this as a case not to use a resolve()
      default:
        
        
        Node result = parsePrimaryExpression(new CurrentFocusNode());
        // No result and no focus node indicates end of expression
        return result;
        
    }
  }

  /**
   * FocusRelativeExpression -> "." ( FocusRelativeExpression )
   */
  private Node parseFocusRelativeExpression(FocusNode focusNode)
    throws ParseException
  { 
    if (focusNode==null)
    { 
      // Shouldn't happen
      throw new IllegalArgumentException
        ("ExpressionParser.parseFocusRelativeExpression: focusNode can't be null");
    }
    
    switch (_tokenizer.ttype)
    {
      case '.':
        // The parent focus
        FocusNode parentFocusNode=new ParentFocusNode(focusNode);
        switch (_tokenizer.lookahead.ttype)
        {
          case '.':
            // String of '.' characters means traverse up Focus chain
            consumeToken();      
            Node fre=parseFocusRelativeExpression(parentFocusNode);
            if (debug)
            { alert("parseFocusExpression():'..' "+fre.reconstruct());
            }
            return fre;
          case '[':
            
            
            // This would normally be a ObjectLiteral, but we need a way
            //   to subscript the subject if it is an array, and we can already 
            //  run an ObjectLiteral against the subject without the '.'.
            consumeToken();
            
            if (_tokenizer.lookahead.ttype=='*')
            { return parseObjectLiteralExpression(parentFocusNode);
            }
            else if (_tokenizer.lookahead.ttype=='@')
            { throw newException("Aspect dereference not supported yet");
            }
            else
            { return parseChannelMetaExpression(focusNode);
            }
          default:
            Node dotExpr=parsePostfixDotExpression(parentFocusNode);
            if (dotExpr!=null)
            { return dotExpr;
            }
            else
            { 
              // Reference is to subject of current focus with no postfix
              consumeToken();
              return parentFocusNode;
            }
        }
        
      default:
        return focusNode;
    }
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Node parseObjectLiteralExpression(Node source)
    throws ParseException
  {
    expect('[');
    expect('*');

    String typeString=parseURIName("]{");

    List<Node> params=null;
    if (_tokenizer.ttype=='{')
    { 
      consumeToken();
      params=parseExpressionList();
      expect('}');
    }
    expect(']');

    try
    { return new ObjectLiteralNode(source,typeString,params);
    }
    catch (UnresolvedPrefixException x)
    { throw newException("Unresolved prefix",x);
    }
  }
  
  @SuppressWarnings("rawtypes")
  private Node parseChannelMetaExpression(Node source)
    throws ParseException
  {
    expect('[');

    String typeString=parseURIName("]");
    expect(']');

    try
    { return new ChannelMetaNode(source,typeString);
    }
    catch (UnresolvedPrefixException x)
    { throw newException("Unresolved prefix",x);
    }
  }
  
  @SuppressWarnings("rawtypes")
  private Node parseArrayLiteralExpression(Node source)
    throws ParseException
  {
    expect('[');
    StructNode struct=parseStruct();
    expect(']');
    return new SubscriptNode(null,struct);
      
  }

  
  /**
   * ExpressionList -> Expression ("," Expression)*
   */
  private List<Node> parseExpressionList()
    throws ParseException
  {
    List<Node> list=new LinkedList<Node>();
    Node node=parseBindingExpression();
    if (node!=null)
    { 
      list.add(node);
      parseExpressionListRest(list);
    }
    return list;
  }

  
  private void parseExpressionListRest(List<Node> list)
    throws ParseException
  {
    if (_tokenizer.ttype==',')
    {
      consumeToken();
      Node node=parseBindingExpression();
      if (node==null)
      { throwUnexpected();
      }
      list.add(node);
      parseExpressionListRest(list);
    }
    return;
  }


  
  /**
   * PrimaryExpression -> Number
   *                    | StringLiteral
   *                    | "true" 
   *                    | "false" 
   *                    | "null"
   *                    | IdentifierExpression
   *                    | "(" Expression ")"
   *                    | Struct
   *                    | ExpressionLiteral
   */
  // Called only from parseFocusExpression
  @SuppressWarnings("rawtypes")
  private Node parsePrimaryExpression(FocusNode focus)
    throws ParseException
  {
    Node node=null;
    switch (_tokenizer.ttype)
    {
      case StreamTokenizer.TT_WORD:
        if (_tokenizer.sval.equals("true"))
        { 
          node=LiteralNode.TRUE;
          consumeToken();
        }
        else if (_tokenizer.sval.equals("false"))
        { 
          node=LiteralNode.FALSE;
          consumeToken();
        }
        else if (_tokenizer.sval.equals("null"))
        { 
          node=LiteralNode.NULL;
          consumeToken();
        }
        else if (Character.isDigit(_tokenizer.sval.charAt(0)))
        { node=parseNumber();
        }
        else
        { node=parseContextExpression(focus);
        }
        break;
      case '"':
        node=LiteralNode.get(stringPool.get(_tokenizer.sval));
        consumeToken();
        break;
      case '\'':
        String str=_tokenizer.sval;
        if (str.length()!=1)
        { throw newException("Single quotes must contain a Character literal");
        }
        node=LiteralNode.get(Character.valueOf(str.charAt(0)));
        consumeToken();
        break;
      case '(': //        "(" expression ")" - recursive reference
        consumeToken();
        node=new SyntaxNode("(",parseExpression(),")");
        expect(')');
        break;
      case '{':
        node=parseStruct();
        break;
      case '`':
        node=parseExpressionLiteral();
        break;
    }
    return node;
  }

  
  @SuppressWarnings({ "rawtypes" })
  private Node parseExpressionLiteral()
    throws ParseException
  {
    expect('`');
    Node node=LiteralNode.get(Expression.create(parseBindingExpression()));
    expect('`');
    return node;
  }
  
  
  /**
   * MapExpression ->  "{" Expression "}" 
   * 
   * <p>Follows postfixExpression + '#'
   * </p>
   */
  private Node parseMapExpression(Node subject)
    throws ParseException
  {
    expect('#');
    expect('{');
    Node inlineContextNode
      =subject.map(parseExpression());
    expect('}');
    return inlineContextNode;
  }

  /**
   * ReduceExpression ->  "[" Expression "]" 
   * 
   * <p>Follows postfixExpression + '$'
   * </p>
   */
  private Node parseReduceExpression(Node subject)
    throws ParseException
  {
    expect('$');
    expect('[');
    Node inlineContextNode
      =subject.reduce(parseExpression());
    expect(']');
    return inlineContextNode;
  }
  
  
  /**
   * StructDefinition -> "{" StructField ( "," StructField ...)* "}"
   */
  private StructNode parseStruct()
    throws ParseException
  {
    StructNode struct=new StructNode();

    
    expect('{');
    
    if (_tokenizer.ttype=='['
        && _tokenizer.lookahead.ttype=='#'
       )
    {
      // Publish as specified URI 
      expect('[');
      expect('#');
      try
      {  struct.setTypeQName(parseURIName("]"));
      }
      catch (UnresolvedPrefixException x)
      { throw newException("Unresolved prefix",x);
      }
      
      expect(']');
    }
    
    if (_tokenizer.ttype!='}')
    {
      parseStructField(struct);
      while (_tokenizer.ttype==',')
      { 
        consumeToken();
        parseStructField(struct);
      }
    }
    
    expect('}');
    return struct;
  }
  
  /**
   * FieldDefinition -> 
   *     [ FieldName ":" (TypeSpecifier) ( [ '=' | '~' ] FieldExpression ) ]
   *   | FieldExpression
   * 
   * @param struct
   * @return
   * @throws ParseException
   */
  private void parseStructField(StructNode struct)
    throws ParseException
  {
    
    
    StructField field=new StructField();
    
    if (_tokenizer.ttype==StreamTokenizer.TT_WORD
         && _tokenizer.lookahead.ttype==':'
       )
    {
      // Named definition
      field.name=stringPool.get(_tokenizer.sval);
      consumeToken();
      expect(':');
      
      
      if (_tokenizer.ttype=='[')
      {
        Node typeNode=parseFocusSpecifier();
        if (!(typeNode instanceof TypeFocusNode))
        { 
          throw newException
            ("Expected a type literal here (eg. '[@mylib:mytype]' ).");
        }
        field.type=(TypeFocusNode) typeNode;
      }
      
      switch (_tokenizer.ttype)
      { 
        case '~':
          consumeToken();
          field.source=parseExpression();
          field.passThrough=true;
          break;
        case '=':
          consumeToken();
          field.source=parseExpression();
          break;
      }
    }
    else
    {
      // Plain expression case
      field.source=parseExpression();
    }
    
    struct.addField(field);
  }
  
  
  /**
   * DereferenceExpressionPart ->  Name ( "(" expressionList ")" )
   *                           | "(" expressionList ")"
   */
  private Node parseDereferenceExpression(Node primary)
    throws ParseException
  { 
    if (_tokenizer.ttype!=StreamTokenizer.TT_WORD
        && _tokenizer.ttype!='('
       )
    { 
      throwUnexpected();
      return null;
    }
    else
    { 
      String name="";
      if (_tokenizer.ttype==StreamTokenizer.TT_WORD)
      { 
        name=stringPool.get(_tokenizer.sval);
        consumeToken();
      }
      if (_tokenizer.ttype=='(')
      { 
        // Method call
        consumeToken();
        Node node
          =primary.call
            (name
            ,parseExpressionList()
            );
        expect(')');
        return node;
      }
      else
      { return primary.resolve(name);
      }
    }
  }

  /**
   * ContextExpression -> Name ( "(" expressionList ")" )
   */
  // Called only from parsePrimaryExpression
  private Node parseContextExpression(FocusNode focus)
    throws ParseException
  { 
    if (_tokenizer.ttype!=StreamTokenizer.TT_WORD)
    { 
      throwUnexpected();
      return null;
    }
    else
    { 
      String name=stringPool.get(_tokenizer.sval);
      
      consumeToken();
      if (_tokenizer.ttype=='(')
      { 
        // Method call
        consumeToken();
        Node node=new ContextNode(focus)
          .call(name,parseExpressionList());
        expect(')');
        return node;
      }
      else
      {
        // The PrimaryIdentifierNode is the node that will resolve
        //   the identifier. Pass it through.
        return new ContextIdentifierNode(focus,name);
      }
    }
  }
  
  /**
   * Parse a general number
   */
  private Node parseNumber()
    throws ParseException
  {
    StringBuilder buff=new StringBuilder();
    parseDecimal(buff);
    String numberString=buff.toString();
//    System.out.println("Number:"+numberString);
    char typeIndicator=numberString.charAt(numberString.length()-1);
    if (!Character.isDigit(typeIndicator))
    {
      numberString=numberString.substring(0,numberString.length()-1);
      switch (typeIndicator)
      {
        case 'L':
          return LiteralNode.get(Long.parseLong(numberString));
        case 'D':
          return LiteralNode.get(Double.valueOf(numberString));
        case 'F':
          return LiteralNode.get(Float.valueOf(numberString));
        default:
          _tokenizer.pushBack();
          throwUnexpected();
          return null;
      }
    }
    else if (numberString.indexOf(".")>-1)
    { return LiteralNode.get(Double.valueOf(numberString));
    }
    else
    { return LiteralNode.get(Integer.parseInt(numberString));
    }
  }
  
  /**
   * Parse a Decimal number
   */
  private void parseDecimal(StringBuilder buff)
    throws ParseException
  {
    parseInteger(buff);
    if (_tokenizer.ttype=='.' 
        && _tokenizer.lookahead.ttype==StreamTokenizer.TT_WORD
        && isNumber(_tokenizer.lookahead.sval)
       )
    { 
      buff.append(".");
      consumeToken();
      parseInteger(buff);
    }
  }
  
  /**
   * Parse an Integer
   */
  private void parseInteger(StringBuilder buff)
    throws ParseException
  {
    if (_tokenizer.ttype==StreamTokenizer.TT_WORD
        && isNumber(_tokenizer.sval)
        )
    {
//      System.out.println(_tokenizer.sval);
      buff.append(_tokenizer.sval);
      consumeToken();
    }
    else
    { 
      // Can't accept something that doesn't start with a digit here 
      throwUnexpected();
    }
  }
  
  private boolean isNumber(String str)
  { 
    return str!=null 
      && str.length()>0 
      && Character.isDigit(str.charAt(0))
      ;
  }


  /**
   * FocusSpecifier -> '[' ('@') URI ']'
   */
  private FocusNode parseFocusSpecifier()
    throws ParseException
  {
    
    
    expect('[');

    String focusString=parseURIName("]");

    expect(']');

    
    FocusNode focusNode=null;
    
    char prefix=focusString.charAt(0);
    try
    {
    
      if (prefix=='@')
      { 
        TypeFocusNode typeFocusNode=new TypeFocusNode(focusString.substring(1),0);
        while (_tokenizer.ttype=='[' && _tokenizer.lookahead.ttype==']')
        {
          consumeToken();
          consumeToken();
          typeFocusNode=typeFocusNode.arrayType();
        }
        focusNode=typeFocusNode;
      }
      else 
      { focusNode=new AbsoluteFocusNode(focusString);
      }
    }
    catch (UnresolvedPrefixException x)
    { throw newException("Unresolved prefix",x);
    }

    return focusNode;
  }
  

  private String parseURIName(String endTokens)
  { 
    StringBuffer uriName=new StringBuffer();
    
    String uriPart;
    while ( (uriPart=parseURIPart(endTokens))!=null)
    { uriName.append(uriPart);
    }
      
    return uriName.toString();
  }

  private String parseURIPart(String endTokens)
  {
    String ret;
    
    if (endTokens.indexOf(_tokenizer.ttype)>=0)
    { return null;
    }
    
    switch (_tokenizer.ttype)
    {
      case StreamTokenizer.TT_WORD:
      case StreamTokenizer.TT_NUMBER:
        ret=stringPool.get(_tokenizer.sval);
        consumeToken();
        return ret;
        
      default:
        
        char chr=(char) _tokenizer.ttype;
        if (":/?#@!$%&'()*+,;=[]-._~".indexOf(chr)>-1
            || Character.isLetter(chr) 
            || Character.isDigit(chr)
           )
        {
          ret=Character.toString(chr);
          consumeToken();
          return ret;
        }
    }
    return null;
    
  }

}
