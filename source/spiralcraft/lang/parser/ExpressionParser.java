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
  private static boolean debug;

  private StringBuffer _progressBuffer;
  private LookaheadStreamTokenizer _tokenizer;
  private int _pos;

  public <X> Expression<X> parse(String text)
    throws ParseException
  { 
    createTokenizer(text);
    consumeToken();
    Node ret=parseExpression();
    if (ret==null)
    { throwUnexpected();
    }
    if (_tokenizer.ttype!=StreamTokenizer.TT_EOF)
    { throwUnexpected();
    }
    return new Expression<X>(ret,text);    
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
    
    syntax.wordChars('0','9');

    syntax.quoteChar('"');

    
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
      );
  }
  
  private void throwException(String message)
    throws ParseException
  {
    throw new ParseException
      (message+": In '"+tokenString()+"' @line "+_tokenizer.lineno()
      ,_pos
      ,_progressBuffer.toString()
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
  
  /**
   * Verifies and consumeTokens specified input, if not expected, throws exception
   */
//  private void throwGeneral(String message)
//    throws ParseException
//  { throw new ParseException(message,_pos,_progressBuffer.toString());
//  }
  

//  private void pushbackToken()
//  { 
//    
//    if (_tokenizer.sval!=null)
//    {
//      if (_tokenizer.ttype=='"')
//      { 
//        int len=_tokenizer.sval.length()+2;
//        _progressBuffer.setLength(_progressBuffer.length()-len);
//      }
//      else
//      { 
//        _progressBuffer.setLength
//          (_progressBuffer.length()-_tokenizer.sval.length());
//      }
//      _pos-=_tokenizer.sval.length();
//    }
//    
//    _tokenizer.pushBack();
//  
//  }
  
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
   * AssignmentExpression -> ConditionalExpression
   */
  private Node parseAssignmentExpression()
    throws ParseException
  { 
    Node node=this.parseConditionalExpression();
    if (_tokenizer.ttype=='=')
    {
      consumeToken();
      node=node.assign(this.parseExpression());
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
      expect(':');
      Node falseResult=this.parseConditionalExpression();
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
    switch (_tokenizer.ttype)
    {
      case '>':
        consumeToken();
        if (_tokenizer.ttype=='=')
        {
          consumeToken();
          return parseRelationalExpressionRest
            (firstOperand.greaterThanOrEquals
              (parseAdditiveExpression()
              )
            );
        }
        else
        {
          return parseRelationalExpressionRest
            (firstOperand.greaterThan
              (parseAdditiveExpression()
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
              (parseAdditiveExpression()
              )
            );
        }
        else
        {
          return parseRelationalExpressionRest
            (firstOperand.lessThan
              (parseAdditiveExpression()
              )
            );
        }
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
        consumeToken();
        operation=firstOperand.minus(parseMultiplicativeExpression());
        node=parseAdditiveExpressionRest(operation);
        break;
      case '+':
        consumeToken();
        operation=firstOperand.plus(parseMultiplicativeExpression());
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
        consumeToken();
        operation = firstOperand.divide(parseUnaryExpression()); 
        node = parseMultiplicativeExpressionRest(operation);
        break;
      case '*':
        consumeToken();
        operation = firstOperand.times(parseUnaryExpression()); 
        node = parseMultiplicativeExpressionRest(operation);
        break;
      case '%':
        consumeToken();
        operation = firstOperand.modulus(parseUnaryExpression()); 
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
      case '{':
        // Dynamic list- used primarily to create fixed length typed Arrays
        consumeToken();
        Node ret=parseListExpression();
        expect('}');
        return parsePostfixExpressionRest(ret);
      default:
        return parsePostfixExpression();
    }
  }

  /**
   * ListExpression -> ExpressionList
   */
  @SuppressWarnings("unchecked") // Unknown type
  private Node parseListExpression()
    throws ParseException
  { return new ListNode(parseExpressionList());
  }
  
  // 
  // Beyond this point there is no left-hand-side recursion
  //

  /**
   * PostfixExpression -> FocusExpression
   *                     ( "[" Expression "]"
   *                     | "." DereferenceExpression
   *                     | "#" AggregateProjectionExpression
   *                     } "{" TupleProjectionExpression
   *                     ) *
   */
  private Node parsePostfixExpression()
    throws ParseException
  {
    // The focus expression -always- returns a node which provides the 
    //   target of further dereference.
    Node node = parseFocusExpression();
    return parsePostfixExpressionRest(node);
  }
  
  @SuppressWarnings("unchecked") // Parser methods are polymorphic
  private Node parsePostfixExpressionRest(Node primary)
    throws ParseException
  {
    // This handles the entire dot/subscript/member dereference chain
    switch (_tokenizer.ttype)
    {
      case '[':
        consumeToken();
        Node subscriptNode=new SubscriptNode(primary,parseExpression());
        expect(']');
        return parsePostfixExpressionRest(subscriptNode);
      case '.':
        consumeToken();
        return parseDereferenceExpression(primary);
      case '#':
        consumeToken();
        return parseAggregateProjectionExpression(primary);
      case '{':
        consumeToken();
        return parseTupleProjectionExpression(primary);
      default:
        return primary;
    }
    
    
  }

  /**
   * FocusExpression -> ( "[" FocusSpecifier "]" ) 
   *                    ( "." FocusRelativeExpression 
   *                      | IdentifierExpression
   *                      | PrimaryExpression
   *                    )
   */
  private Node parseFocusExpression()
    throws ParseException
  { 
    FocusNode focusNode=null;
    if (_tokenizer.ttype=='[')
    {
      consumeToken();
      focusNode=parseFocusSpecifier();
      expect(']');
    }

    switch (_tokenizer.ttype)
    {
      case StreamTokenizer.TT_EOF:
        if (focusNode!=null)
        { return focusNode;
        }
        else
        { throwUnexpected();
        }
        return null;
      case '.':
        consumeToken();

        if (_tokenizer.ttype!=StreamTokenizer.TT_WORD)
        {
          if (focusNode==null)
          { focusNode=new CurrentFocusNode(".");
          }          
          Node ret=parseFocusRelativeExpression(focusNode);
          if (debug)
          { alert("parseFocusExpression():'..'");
          }
          return ret;
        }
        else
        {
          if (focusNode==null)
          { focusNode=new CurrentFocusNode();
          }
          Node ret=parseDereferenceExpression(focusNode);
          return ret;
        }
      case StreamTokenizer.TT_WORD:
        // Either a literal, or 
        if (debug)
        { alert("parseFocusExpression.TT_WORD");
        }
        
        // If this is an identifier, parseIdentifierExpression() will
        //   catch this as a case not to use a resolve()
        return parsePrimaryExpression(focusNode);
      default:
        // Figure out when this happens- probably only for a lone explicit
        //   focus node, or an empty expression
        Node result = parsePrimaryExpression(new CurrentFocusNode());
        // focusNode will only be present if it was explicit
        return result!=null?result:focusNode;
    }
  }

  /**
   * FocusRelativeExpression -> "." FocusRelativeExpression 
   *                            | PostfixExpressionRest
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
      //case StreamTokenizer.TT_EOF:
        // implicit 'this', refers to subject of focus
        //return focusNode;
      case StreamTokenizer.TT_WORD:
        // a name resolve or method call against the subject of the focus
        return parseDereferenceExpression(focusNode);
      case '[':
        // an array subscript against the subject of the focus
      case '!': 
        // a meta-reference against the subject of the focus
      case '#': 
        // an aggregate projection against the subject of the focus
      case '{': 
        // an tuple projection against the subject of the focus
        return parsePostfixExpressionRest(focusNode);
      case '.':
        // The parent focus
        consumeToken();
        FocusNode parentFocusNode=new ParentFocusNode(focusNode);
        return parseFocusRelativeExpression(parentFocusNode);
      default:
        // implicit 'this', refers to subject of focus
        return focusNode;
        // throwUnexpected();
        // return null;
    }
  }
  
  /**
   * ExpressionList -> Expression ("," Expression)*
   */
  private List<Node> parseExpressionList()
    throws ParseException
  {
    List<Node> list=new LinkedList<Node>();
    Node node=parseExpression();
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
      Node node=parseExpression();
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
   *                    | String Literal
   *                    | "true" 
   *                    | "false" 
   *                    | "null"
   *                    | IdentifierExpression
   *                    | "(" Expression ")"
   */
  private Node parsePrimaryExpression(FocusNode focus)
    throws ParseException
  {
    Node node=null;
    switch (_tokenizer.ttype)
    {
      case StreamTokenizer.TT_WORD:
        if (_tokenizer.sval.equals("true"))
        { 
          node=new LiteralNode<Boolean>(Boolean.TRUE,Boolean.class);
          consumeToken();
        }
        else if (_tokenizer.sval.equals("false"))
        { 
          node=new LiteralNode<Boolean>(Boolean.FALSE,Boolean.class);
          consumeToken();
        }
        else if (_tokenizer.sval.equals("null"))
        { 
          node=new LiteralNode<Void>(null,Void.class);
          consumeToken();
        }
        else if (Character.isDigit(_tokenizer.sval.charAt(0)))
        { node=parseNumber();
        }
        else
        { node=parseIdentifierExpression(focus);
        }
        break;
      case '"':
        node=new LiteralNode<String>(_tokenizer.sval,String.class);
        consumeToken();
        break;
        
//      case '0':
//      case '1':
//      case '2':
//      case '3':
//      case '4':
//      case '5':
//      case '6':
//      case '7':
//      case '8':
//      case '9':
//      case StreamTokenizer.TT_NUMBER:
//        node=parseNumber();
//        break;
      case '(': //        "(" expression ")" - recursive reference
        consumeToken();
        node=parseExpression();
        expect(')');
        break;
    }
    return node;
  }

  /**
   * AggregateProjectionExpression ->   "[" Expression "]"
   *                        | { TupleDefinition }
   *                        | ${ SummaryTupleDefinition }
   */
  private Node parseAggregateProjectionExpression(Node subject)
    throws ParseException
  {
    
    Node aggregate=null;
    switch (_tokenizer.ttype)
    {
      
      case '[':
        // a single element projection
        consumeToken();
        try
        { aggregate=subject.projectAggregate(parseExpression());
        }
        finally
        { expect(']');
        }
        return parsePostfixExpressionRest(aggregate);
      case '{':
        // shortcut for a tuple projection
        consumeToken();
        try
        { aggregate=subject.projectAggregate(parseTuple());
        }
        finally
        { expect('}');
        }
        return parsePostfixExpressionRest(aggregate);
        
        
      default:
        // implicit 'this', refers to subject of focus
        throwException("Expected #[expression] or #{tupleDefinition} here");
        return null;
    }    
  }
  
  /**
   * TupleProjectionExpression ->  { TupleDefinition }
   */
  private Node parseTupleProjectionExpression(Node subject)
    throws ParseException
  {
    
    Node aggregate=subject.projectTuple(parseTuple());
    expect('}');
    return parsePostfixExpressionRest(aggregate);
  }
  
  /**
   * Tuple -> (":" ( TypeSpecifier | [ "=" Expression ":" ] ) )
   * 
   *             ( TupleField ( "," TupleField ...)* )
   */
  private TupleNode parseTuple()
    throws ParseException
  {
    TupleNode tuple=new TupleNode();
    if (_tokenizer.ttype==':')
    {
      consumeToken();
      switch (_tokenizer.ttype)
      {
        case '[':
          // 'implements' declaration- can only implement data-only types.
          
          consumeToken();
          Node typeNode=parseFocusSpecifier();
          if (!(typeNode instanceof TypeFocusNode))
          { 
            throwException
              ("Expected a type literal here (eg. '[@mylib:mytype]' )");
          }
          expect(']');
          tuple.setType((TypeFocusNode) typeNode);
          break;
          
        case '=':
          // Delegates an existing object
          
          consumeToken();
          Node extentNode=parseExpression();
          expect(':');
          tuple.setBaseExtentNode(extentNode);
          break; 
          
        default:
          throwUnexpected
            ("Expected '[@typeURI]' or '=expression'");
          
      }      
      
    }
    
    if (_tokenizer.ttype!='}')
    {
      parseTupleField(tuple);
      while (_tokenizer.ttype==',')
      { 
        consumeToken();
        parseTupleField(tuple);
      }
    }
    return tuple;
  }
  
  /**
   * FieldDefinition -> 
   *   ( FieldName ":") ( TypeSpecifier | "=" Expression | "~" Expression ] )*1 
   * 
   * @param tuple
   * @return
   * @throws ParseException
   */
  private void parseTupleField(TupleNode tuple)
    throws ParseException
  {
    
    
    TupleField field=new TupleField();
    
    if (_tokenizer.ttype==StreamTokenizer.TT_WORD)
    {
      // Named definition
      field.name=_tokenizer.sval;
      consumeToken();
      expect(':');
    }
        
    
    switch (_tokenizer.ttype)
    {
      case '[':
        consumeToken();
        Node typeNode=parseFocusSpecifier();
        if (!(typeNode instanceof TypeFocusNode))
        { 
          throwException
            ("Expected a type literal here (eg. '[@mylib:mytype]' )");
        }
        expect(']');
        field.type=(TypeFocusNode) typeNode;
        break;
        
      case '=':
        consumeToken();
        field.source=parseExpression();
        break;
        
      case '~':
      
        consumeToken();
        field.source=parseExpression();
        field.passThrough=true;
        break;
        
      default:
        throwUnexpected
          ("Expected '[@typeURI]', '=expression' or '~expression'");
    }
    
    tuple.addField(field);
  }
  
  
  /**
   * DereferenceExpression -> Name ( "(" expressionList ")" )
   */
  private Node parseDereferenceExpression(Node primary)
    throws ParseException
  { 
    if (_tokenizer.ttype!=StreamTokenizer.TT_WORD)
    { 
      throwUnexpected();
      return null;
    }
    else
    { 
      String name=_tokenizer.sval;
      consumeToken();
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
        return parsePostfixExpressionRest(node);
      }
      else
      { return parsePostfixExpressionRest(primary.resolve(name));
      }
    }
  }

  /**
   * IdentifierExpression -> Name ( "(" expressionList ")" )
   */
  private Node parseIdentifierExpression(FocusNode focus)
    throws ParseException
  { 
    if (_tokenizer.ttype!=StreamTokenizer.TT_WORD)
    { 
      throwUnexpected();
      return null;
    }
    else
    { 
      String name=_tokenizer.sval;
      
      consumeToken();
      if (_tokenizer.ttype=='(')
      { 
        // Method call
        consumeToken();
        Node node=new ContextNode(focus)
          .call(name,parseExpressionList());
        expect(')');
        return parsePostfixExpressionRest(node);
      }
      else
      {
        // The PrimaryIdentifierNode is the node that will resolve
        //   the identifier. Pass it through.
        return new PrimaryIdentifierNode(focus,name);
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
          return new LiteralNode<Long>(new Long(numberString),Long.class);
        case 'D':
          return new LiteralNode<Double>(new Double(numberString),Double.class);
        case 'F':
          return new LiteralNode<Float>(new Float(numberString),Float.class);
        default:
          _tokenizer.pushBack();
          throwUnexpected();
          return null;
      }
    }
    else if (numberString.indexOf(".")>-1)
    { return new LiteralNode<Double>(new Double(numberString),Double.class);
    }
    else
    { return new LiteralNode<Integer>(new Integer(numberString),Integer.class);
    }
  }
  
  /**
   * Parse a Decimal number
   */
  private void parseDecimal(StringBuilder buff)
    throws ParseException
  {
    parseInteger(buff);
    if (_tokenizer.ttype=='.')
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
        && Character.isDigit(_tokenizer.sval.charAt(0))
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
  


  /**
   * FocusExpression -> FocusName 
   */
  @SuppressWarnings("fallthrough")
  private FocusNode parseFocusSpecifier()
    throws ParseException
  {
    StringBuffer focusName=new StringBuffer();
    
    while (true)
    {
      switch (_tokenizer.ttype)
      {
        case ']':
          String focusString=focusName.toString();
          if (focusString.startsWith("@"))
          { return new TypeFocusNode(focusString.substring(1));
          }
          else
          { return new AbsoluteFocusNode(focusString);
          }
        case StreamTokenizer.TT_EOF:
          throwUnexpected();
        case StreamTokenizer.TT_WORD:
        case StreamTokenizer.TT_NUMBER:
          focusName.append(_tokenizer.sval);
          consumeToken();
          break;
        default:
          focusName.append((char) _tokenizer.ttype);
          consumeToken();
          break;
      }
    }
  }
  



}
