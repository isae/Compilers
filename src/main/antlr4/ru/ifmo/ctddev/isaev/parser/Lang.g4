grammar Lang;

program
    :    (statement)? (';'statement)*
    ;
    
statement
    :    WS* (assignment | whileLoop | cond | functionCall | forLoop | repeatLoop | functionDef | expr) WS*
    ;

assignment
    : variable ':=' WS* expr
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
    : 'if' WS+ expr WS+ 'then' WS+ program WS+ 'else' WS+ program WS+ 'fi'
    ;

Var      :    ('A'..'Z'|'a'..'z')+;
Number   :    ('+'|'-')?('0'..'9')+;
String   :    '"'.*?'"';
Val      :    Number;

argList
    : expr? (',' expr)*
    ;

functionCall
    : variable '(' argList ')' WS*
    ;

functionDef
    : 'fun' WS+ variable '(' argList ')' WS+
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
        
addition
    :    multiplication 
         ( '+' multiplication 
         | '-' multiplication
         )* 
    ;

multiplication
    :    atom
         ( '*' atom 
         | '/' atom
         | '%' atom
         )* 
    ;

atom
    :    Number
    |    WS* '(' expr ')' WS*
    |    variable
    |    functionCall
    ;
    
variable: 
    WS* Var WS*;

WS  
    :   (' ' | '\t' | '\r'| '\n') -> skip
    ;