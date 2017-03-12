grammar Lang;

program
    :    (statement)? (';'statement)*
    ;
    
statement
    :    WS* (expr | assignment | whileLoop | cond | functionCall | forLoop | repeatLoop | functionDef) WS*
    ;

assignment
    : Var ':=' expr
    ;
    
whileLoop
    : 'while' WS+ expr WS+ 'do' WS+ program WS+ 'od' 
    ;

forLoop
    : 'for' 
        WS+ assignment ',' 
        expr ','
        assignment WS+ 
        'do'
        WS+ program WS+ 'od' 
    ;    
 
repeatLoop
    : 'repeat' WS+ program WS+ 'until' WS+ expr 
    ;    
   
cond
    : 'if' expr 'then' program 'else' program 'fi'
    ;

Var      :    ('A'..'Z'|'a'..'z')+;
Number   :    ('0'..'9')+;
String   :    '"'.*?'"';
Val      :    Number;

argList
    : Var? (',' Var)*
    ;

functionCall
    : Var '(' argList ')'
    ;

functionDef
    : 'fun' WS+ Var '(' argList ')' WS+
      'begin' program 'end'
    ;
  
/* Logical operations have the lowest precedence. */
expr
    :    addition 
         ( '<' addition 
         | '<=' addition
         | '>' addition
         | '>=' addition
         | '==' addition
         | '!=' addition
         | '|' addition
         | '||' addition
         | '&' addition
         | '&&' addition
         )* 
    ;
        
/* Addition and subtraction have higher precedence. */
addition
    :    multiplication 
         ( '+' multiplication 
         | '-' multiplication
         )* 
    ;

/* Multiplication and division have a higher precedence. */
multiplication
    :    atom
         ( '*' atom 
         | '/' atom
         | '%' atom
         )* 
    ;

/* An expression atom is the smallest part of an expression: a number. Or 
   when we encounter parenthesis, we're making a recursive call back to the
   rule 'additionExp'. As you can see, an 'atomExp' has the highest precedence. */
atom
    :    Val
    |    '(' expr ')'
    |    functionCall
    ;

/* We're going to ignore all white space characters */
WS  
    :   (' ' | '\t' | '\r'| '\n')
    ;