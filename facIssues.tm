* C-Minus Compilation to TM Code
* File: fac.tm
* * Standard prelude:
  0:  LD  6,0(0)	load gp with maxaddress
  1:  LDA  5,0(6)	copy gp to fp
  2:  ST  0,0(0)	clear location 0
* * input routine
  4:  ST  0,-1(5)	store return
  5:  IN  0,0,0	input
  6:  LD  7,-1(5)	return to caller
* * output routine
  7:  ST  0,-1(5)	store return
  8:  LD  0,-2(5)	load output value
  9:  OUT  0,0,0	output
 10:  LD  7,-1(5)	return to caller
  3:  LDA  7,7(7)	jump around I/O code
* * processing function: main
 11:  ST  0,-1(5)	store return  *FIX HIS SKIPS A LINE:  12:     ST  0,-1(5) 	store return
* -> CompoundExp
* * processing local var: x
* * processing local var: fac
* -> AssignExp
* -> VarExp
* -> SimpleVar: x
 12:  LDA  0,-2(5)	load id address
 13:  ST  0,-4(5)	op: push left (address)
* -> CallExp: input
 14:  ST  0,-6(5)	store arg val *FIX: SHOULDNT EXIST
 15:  ST  5,-5(5)	push ofp
 16:  LDA  5,-5(5)	push frame
 17:  LDA  0,1(7)	load return addr
 18:  LDA  7,-15(7)	jump to input routine
 19:  LD  5,0(5)	pop frame
 20:  LD  1,-4(5)	op: pop left (address)
 21:  ST  0,0(1)	assign: store value
* -> AssignExp
* -> VarExp
* -> SimpleVar: fac
 22:  LDA  0,-3(5)	load id address
 23:  ST  0,-4(5)	op: push left (address)
* -> IntExp: 1
 24:  LDC  0,1(0)	Load const
 25:  LD  1,-4(5)	op: pop left (address)
 26:  ST  0,0(1)	assign: store value
* -> WhileExp
* -> OpExp
* -> VarExp
* -> SimpleVar: x
 27:  LD  0,-2(5)	load id value
 28:  ST  0,-4(5)	op: push left
* -> IntExp: 1
 29:  LDC  0,1(0)	Load const
 30:  LD  1,-4(5)	op: pop left
 31:  SUB  0,1,0	op >
 33:  LDC  0,0(0)	false case
 35:  LDC  0,1(0)	true case
 32:  JGT  0,2(7)	jump if >  *FIX: THIS ENTIRE IF BLOCK IS WEORD
* -> CompoundExp
* -> AssignExp
* -> VarExp
* -> SimpleVar: fac
 37:  LDA  0,-3(5)	load id address
 38:  ST  0,-4(5)	op: push left (address)
* -> OpExp
* -> VarExp
* -> SimpleVar: fac
 39:  LD  0,-3(5)	load id value
 40:  ST  0,-5(5)	op: push left
* -> VarExp
* -> SimpleVar: x
 41:  LD  0,-2(5)	load id value
 42:  LD  1,-5(5)	op: pop left
 43:  MUL  0,1,0	op *
 44:  LD  1,-4(5)	op: pop left (address)
 45:  ST  0,0(1)	assign: store value
* -> AssignExp
* -> VarExp
* -> SimpleVar: x
 46:  LDA  0,-2(5)	load id address
 47:  ST  0,-4(5)	op: push left (address)
* -> OpExp
* -> VarExp
* -> SimpleVar: x
 48:  LD  0,-2(5)	load id value
 49:  ST  0,-5(5)	op: push left
* -> IntExp: 1
 50:  LDC  0,1(0)	Load const
 51:  LD  1,-5(5)	op: pop left
 52:  SUB  0,1,0	op -
 53:  LD  1,-4(5)	op: pop left (address)
 54:  ST  0,0(1)	assign: store value
 55:  LDA  7,-29(7)	jump to test
 36:  JEQ  0,19(7)	exit loop
* -> CallExp: output
* -> VarExp
* -> SimpleVar: fac
 56:  LD  0,-3(5)	load id value
 57:  ST  0,-6(5)	store arg val
 58:  ST  5,-5(5)	push ofp    *FIX HE HAS:  ST  5,-4(5) 	push ofp
 59:  LDA  5,-5(5)	push frame   *FIX HE HAS:  LDA  5,-4(5)
 60:  LDA  0,1(7)	load return addr
 61:  LDA  7,-55(7)	jump to output routine
 62:  LD  5,0(5)	pop frame
 63:  LD  7,-1(5)	return to caller
* -> 11:    LDA  7,52(7) 	jump around fn body  FIX: WERE MISSING THIS LINE
* * Finale: call main and halt
 64:  ST  5,0(5)	push ofp
 65:  LDA  5,0(5)	push frame
 66:  LDA  0,1(7)	load return addr
 67:  LDA  7,-57(7)	jump to main loc  *FIX  67:    LDA  7,-56(7) 	jump to main loc he has this our mainEntry is off by 1
 68:  LD  5,0(5)	pop frame
 69:  HALT  0,0,0	
