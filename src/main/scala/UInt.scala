/*
 Copyright (c) 2011, 2012, 2013 The Regents of the University of
 California (Regents). All Rights Reserved.  Redistribution and use in
 source and binary forms, with or without modification, are permitted
 provided that the following conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the following
      two paragraphs of disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      two paragraphs of disclaimer in the documentation and/or other materials
      provided with the distribution.
    * Neither the name of the Regents nor the names of its contributors
      may be used to endorse or promote products derived from this
      software without specific prior written permission.

 IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF
 ANY, PROVIDED HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION
 TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 MODIFICATIONS.
*/

package Chisel
import Node._
import ChiselError._

object UInt {
  /* Implementation Note: scalac does not allow multiple overloaded
   method with default parameters so we define the following four
   methods to create UInt from litterals (with implicit and explicit
   widths) and reserve the default parameters for the "direction" method.
   */
  def apply(x: Int): UInt = Lit(x){UInt()};
  def apply(x: Int, width: Int): UInt = Lit(x, width){UInt()};
  def apply(x: String): UInt = Lit(x, -1){UInt()};
  def apply(x: String, width: Int): UInt = Lit(x, width){UInt()};
  def apply(x: String, base: Char): UInt = Lit(x, base, -1){UInt()};
  def apply(x: String, base: Char, width: Int): UInt = Lit(x, base, width){UInt()};

  def apply(dir: IODirection = null, width: Int = -1): UInt = {
    val res = new UInt();
    res.create(dir, width)
    res
  }
}


class UInt extends Bits /* with Numeric[UInt] */ {
  type T = UInt;

  /** Factory method to create and assign a *UInt* type to a Node *n*.
    */
  override def fromNode(n: Node): this.type = {
    UInt(OUTPUT).asTypeFor(n).asInstanceOf[this.type]
  }

  override def fromInt(x: Int): this.type = {
    UInt(x).asInstanceOf[this.type]
  }

  // to support implicit convestions
  def ===(b: UInt): Bool = LogicalOp(this, b, "===")

  def :=(src: UInt) {
    if(comp != null) {
      comp procAssign src.toNode;
    } else {
      this procAssign src.toNode;
    }
  }

  // arithmetic operators
  def zext(): SInt = Cat(UInt(0,1), this).toSInt
  def unary_-(): UInt = newUnaryOp("-");
  def unary_!(): Bool = Bool(OUTPUT).fromNode(UnaryOp(this, "!"));
  def << (b: UInt): UInt = newBinaryOp(b, "<<");
  def >> (b: UInt): UInt = newBinaryOp(b, ">>");
  def +  (b: UInt): UInt = newBinaryOp(b, "+");
  def *  (b: UInt): UInt = newBinaryOp(b, "*");
  def /  (b: UInt): UInt = newBinaryOp(b, "/");
  def %  (b: UInt): UInt = newBinaryOp(b, "%");
  def ?  (b: UInt): UInt = newBinaryOp(b, "?");
  def -  (b: UInt): UInt = newBinaryOp(b, "-");

  // order operators
  def >  (b: UInt): Bool = newLogicalOp(b, ">");
  def <  (b: UInt): Bool = newLogicalOp(b, "<");
  def <= (b: UInt): Bool = newLogicalOp(b, "<=");
  def >= (b: UInt): Bool = newLogicalOp(b, ">=");

  //UInt op SInt arithmetic
  def +   (b: SInt): SInt = SInt(OUTPUT).fromNode(BinaryOp(this.zext, b, "+"));
  def -   (b: SInt): SInt = SInt(OUTPUT).fromNode(BinaryOp(this.zext, b, "-"));
  def *   (b: SInt): SInt = SInt(OUTPUT).fromNode(BinaryOp(this.zext, b, "u*s"));
  def %   (b: SInt): SInt = SInt(OUTPUT).fromNode(BinaryOp(this.zext, b, "u%s"));
  def /   (b: SInt): SInt = SInt(OUTPUT).fromNode(BinaryOp(this.zext, b, "u/s"));
}
