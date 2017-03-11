grammar Lang;

program
    :    WS+ (statement';')*statement WS+
    ;
    
statement
    :    assignment | loop | cond | write
    ;

assignment
    : Var ':=' expr';'
    ;
    
loop
    : 'while' expr 'do' program 'od' 
    ;
   
cond
    : 'if' expr 'then' program 'else' program 'fi;'
    ;

Var      :    ('A'..'Z'|'a'..'z')+;
Number   :    ('0'..'9')+;
String   :    '"'.*?'"';
Val      :    Number|String;

read
    : 'read' WS+ '(' WS+ ')' WS+ 
    ;
    
write
    : 'write' WS+ '(' expr ');'
    ;

expr
    : WS+ additionExp WS+ | read | Val
    ;

/* Addition and subtraction have the lowest precedence. */
additionExp
    :    multiplyExp 
         ( '+' multiplyExp 
         | '-' multiplyExp
         )* 
    ;

/* Multiplication and division have a higher precedence. */
multiplyExp
    :    atomExp
         ( '*' atomExp 
         | '/' atomExp
         )* 
    ;

/* An expression atom is the smallest part of an expression: a number. Or 
   when we encounter parenthesis, we're making a recursive call back to the
   rule 'additionExp'. As you can see, an 'atomExp' has the highest precedence. */
atomExp
    :    Number
    |    '(' additionExp ')'
    ;

/* We're going to ignore all white space characters */
WS  
    :   (' ' | '\t' | '\r'| '\n')
    ;