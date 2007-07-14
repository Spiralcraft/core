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
  private StreamTokenizer _tokenizer;
  private int _pos;

  @SuppressWarnings("unchecked") // Parse methods are polymorphic
  public <X> Expression<X> parse(String text)
    throws ParseException
  { 
    createTokenizer(text);
    consumeToken();
    Node ret=parseExpression();
    if (ret==null)
    { throwUnexpected();
    }
    return new Expression<X>(ret,text);    
  }

  
  private void createTokenizer(String expression)
  {
    _tokenizer=new StreamTokenizer(new StringReader(expression));
    _tokenizer.resetSyntax();
    
    _tokenizer.wordChars((int) 'a',(int) 'z');
    _tokenizer.wordChars((int) 'A',(int) 'Z');
    _tokenizer.wordChars((int) '_',(int) '_');

    _tokenizer.whitespaceChars((int) '\r',(int) '\r');
    _tokenizer.whitespaceChars((int) '\n',(int) '\n');
    _tokenizer.whitespaceChars((int) '\t',(int) '\t');
    _tokenizer.whitespaceChars((int) ' ',(int) ' ');
    
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
    
    _tokenizer.wordChars((int) '0',(int) '9');

    _tokenizer.quoteChar((int) '"');

    
    _progressBuffer=new StringBuffer();
    _pos=0;
  }

  private void throwUnexpected()
    throws ParseException
  { throw new ParseException("Not expecting "+_tokenizer.toString(),_pos,_progressBuffer.toString());
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
    { throw new ParseException("Expected '"+chr+"', not "+_tokenizer.toString(),_pos,_progressBuffer.toString());
    }
    consumeToken();
  }
  
  /**
   * Verifies and consumeTokens specified input, if not expected, throws exception
   */
//  private void throwGeneral(String message)
//    throws ParseException
//  { throw new ParseException(message,_pos,_progressBuffer.toString());
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
   * Expression -> ConditionalExpression
   */
  private Node parseExpression()
    throws ParseException
  { 
    return this.parseConditionalExpression();
  }


  /**
   * ConditionalExpression -> LogicalOrExpression 
   *                          ( "?" conditionalExpression ":" conditionalExpression )
   */
  private Node parseConditionalExpression()
    throws ParseException
  {
    Node node=this.parseLogicalOrExpression();
    if (_tokenizer.ttype=='?')
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
    else if (_tokenizer.ttype=='=')
    {
      consumeToken();
      expect('=');
      Node secondOperand=parseRelationalExpression();
      Node equalityNode = firstOperand.isEqual(secondOperand);
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

  // 
  // Beyond this point there is no left-hand-side recursion
  //

  /**
   * PostfixExpression -> FocusExpression
   *                     ( "[" Expression "]"
   *                     | "." DereferenceExpression
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
//      case '(':
        // XXX: An expression list after a resolved item is not necessarily part
        //   of the grammar. This should not be a case
        
//        if (!(primary instanceof ResolveNode))
//        { throwUnexpected();
//        }
//        consumeToken();
//        if (((ResolveNode) primary).getSource()==null)
//        { throwGeneral("Internal error: MethodCall source is null");
//        }
//        ResolveNode resolveNode=(ResolveNode) primary;
//        Node methodCallNode
//          =resolveNode.getSource().call
//            (resolveNode.getIdentifierName()
//            ,parseExpressionList()
//            );
//        expect(')');
//        return parsePostfixExpressionRest(methodCallNode);
      case '[':
        consumeToken();
        Node subscriptNode=new SubscriptNode(primary,parseExpression());
        expect(']');
        return parsePostfixExpressionRest(subscriptNode);
      case '.':
        consumeToken();
        return parseDereferenceExpression(primary);
      case '!': 
        consumeToken();
        return parsePostfixExpressionRest(new MetaNode(primary));
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
        if (focusNode==null)
        { focusNode=new CurrentFocusNode();
        }
        Node ret=parseFocusRelativeExpression(focusNode);
        if (debug)
        { alert("parseFocusExpression():'.'");
        }
        return ret;
      case StreamTokenizer.TT_WORD:
        // Either a literal, or 
        if (debug)
        { alert("parseFocusExpression.TT_WORD");
        }
        
        // If this is an identifier, parseIdentifierExpression() will
        //   catch this as a case not to use a resolve()
        return parsePrimaryExpression(focusNode);
      default:
        if (focusNode==null)
        { focusNode=new CurrentFocusNode();
        }
        return parsePrimaryExpression(focusNode);
        
    }
  }

  /**
   * FocusRelativeExpression -> "." FocusRelativeExpression 
   *                            | PostfixExpressionRest
   */
  @SuppressWarnings("unchecked") // Parser methods are heterogeneous  
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
      case StreamTokenizer.TT_EOF:
        // implicit 'this', refers to subject of focus
        return focusNode;
      case StreamTokenizer.TT_WORD:
        // a name resolve or method call against the subject of the focus
        return parseDereferenceExpression(focusNode);
      case '[':
        // an array subscript against the subject of the focus
      case '!': 
        // a meta-reference against the subject of the focus
        return parsePostfixExpressionRest(focusNode);
      case '.':
        // The parent focus
        consumeToken();
        FocusNode parentFocusNode=new ParentFocusNode(focusNode);
        return parseFocusRelativeExpression(parentFocusNode);
      default:
        throwUnexpected();
        return null;
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
      Node primary=new PrimaryIdentifierNode(focus,name);
      
      consumeToken();
      if (_tokenizer.ttype=='(')
      { 
        // XXX Support global functions
        throwUnexpected();
        return null;
      }
      else
      {
        // The PrimaryIdentifierNode is the node that will resolve
        //   the identifier. Pass it through.
        return primary;
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
  private FocusNode parseFocusSpecifier()
    throws ParseException
  {
    StringBuffer focusName=new StringBuffer();
    
    while (true)
    {
      switch (_tokenizer.ttype)
      {
        case ']':
          return new AbsoluteFocusNode(focusName.toString());
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
