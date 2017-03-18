grammar Lang;

@lexer::members {
    boolean ignore=true;
}

program
    :    (statement)? (';'statement)*
    ;
    
statement
    :    (whileLoop | cond | forLoop | repeatLoop | functionDef | functionCall | assignment |  expr)
    ;

assignment
    : variable ':=' expr
    ;
    
whileLoop
    : WHILE expr DO program OD
    ;

forLoop
    : FOR assignment ',' expr ','assignment WS
        'do'
        WS program WS 'od' 
    ;    
 
repeatLoop
    : 'repeat' WS program WS 'until' WS expr 
    ;    
   
cond
    : IF expr THEN program ELSE program FI
    ;

argList
    : expr? ( ',' expr)*
    ;

functionCall
    : variable '(' argList ')'
    ;

functionDef
    : 'fun' WS variable '(' argList ')' WS
      'begin' program 'end'
    ;
  
/* Logical operations have the lowest precedence. */
expr
    :    addition 
         (
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
             )
         )* 
    ;
        
addition
    :    multiplication 
         (
             ( '+' multiplication 
             | '-' multiplication
             )
         )* 
    ;

multiplication
    :    atom
         (
             ( '*' atom 
             | '/' atom
             | '%' atom
             )
         )* 
    ;

atom
    :    variable
    |    functionCall
    |    Number
    |    '(' expr ')'
    ;
    
variable: 
    Var;

WHILE : 'while' { ignore = false; } WS { ignore = true; };    
FOR : 'for' { ignore = false; } WS { ignore = true; };    
IF : 'if' { ignore = false; } WS { ignore = true; };    
DO : { ignore = false; } WS+ 'do'  WS+ { ignore = true; };    
OD : { ignore = false; } WS+ 'od' { ignore = true; };   
THEN : { ignore = false; } WS+ 'then'  WS+ { ignore = true; };   
ELSE : { ignore = false; } WS+ 'else'  WS+ { ignore = true; };   
FI : { ignore = false; } WS+ 'fi' { ignore = true; };    

Var      :    ('A'..'Z'|'a'..'z')+;
Number   :    ('+'|'-')?('0'..'9')+;
String   :    '"'.*?'"';
Val      :    Number;

WS  
    :   (' ' | '\t' | '\r'| '\n')  { if(ignore) skip(); };
    
    