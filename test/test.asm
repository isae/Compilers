%macro clib_prolog 1
mov ebx, esp 
and esp, 0xFFFFFFF0
sub esp, 12    
push ebx         
sub esp, %1
%endmacro

%macro clib_epilog 1
add esp, %1
pop ebx        
mov esp, ebx
%endmacro
    

extern _printf
extern _scanf
extern _gets


SECTION .rodata
format_in: db "%d", 0
format_out: db "%d", 10, 0

SECTION .data
	int_read: dd 0
	x: dd 0

SECTION .text
GLOBAL _main
    
_main:
	clib_prolog 16
	mov dword [esp+4], int_read
	mov dword [esp], format_in
	call _scanf
	clib_epilog 16
	push dword [int_read]
	pop eax
	mov [x], eax
	push dword [x]
	pop eax
	clib_prolog 16
	mov dword [esp+4], eax
	mov dword [esp], format_out
	call _printf
	clib_epilog 16

    mov eax, 0
    ret