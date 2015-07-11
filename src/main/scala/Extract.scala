/*
 Copyright (c) 2011, 2012, 2013, 2014 The Regents of the University of
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

object NodeExtract {
  // extract one bit
  def apply(mod: Node, bit: Int): Node = apply(mod, bit, bit)
  def apply(mod: Node, bit: Node): Node = apply(mod, bit, bit, 1)

  // extract bit range
  def apply(mod: Node, hi: Int, lo: Int): Node = apply(mod, hi, lo, -1)
  def apply(mod: Node, hi: Int, lo: Int, width: Int): Node = {
    if (hi < lo) {
      if (!((hi == lo - 1) && Driver.isSupportW0W)) {
        ChiselError.error("Extract(hi = " + hi + ", lo = " + lo + ") requires hi >= lo")
      }
    }
    val w = if (width == -1) hi - lo + 1 else width
    val bits_lit = mod.litOf
    // Currently, we don't restrict literals to their width,
    // so we can't use the literal directly if it overflows its width.
    val wmod = mod.widthW
    if (lo == 0 && wmod.isKnown && w == wmod.needWidth()) {
      mod
    } else if (bits_lit != null) {
      Literal((bits_lit.value >> lo) & ((BigInt(1) << w) - BigInt(1)), w)
    } else {
      makeExtract(mod, Literal(hi), Literal(lo), Node.fixWidth(w))
    }
  }

  // extract bit range
  def apply(mod: Node, hi: Node, lo: Node, width: Int = -1): Node = {
    val hiLit = hi.litOf
    val loLit = lo.litOf
    val widthInfer = if (width == -1) Node.widthOf(0) else Node.fixWidth(width)
    if (hiLit != null && loLit != null) {
      apply(mod, hiLit.value.toInt, loLit.value.toInt, width)
    } else { // avoid extracting from constants and avoid variable part selects
      val rsh = Op(">>", widthInfer, mod, lo)
      val hiMinusLoPlus1 = Op("+", Node.maxWidth _, Op("-", Node.maxWidth _, hi, lo), UInt(1))
      val mask = Op("-", widthInfer, Op("<<", widthInfer, UInt(1), hiMinusLoPlus1), UInt(1))
      Op("&", widthInfer, rsh, mask)
    }
  }

  private def makeExtract(mod: Node, hi: Node, lo: Node, widthFunc: (=> Node) => Width) = {
    val res = new Extract
    res.init("", widthFunc, mod, hi, lo)
    res.hi = hi
    res.lo = lo
    res
  }
}

object Extract {
  //extract 1 bit
  def apply[T <: Bits](mod: T, bit: UInt)(gen: => Bool): Bool = {
    val x = NodeExtract(mod, bit)
    gen.fromNode(x)
  }

  def apply[T <: Bits](mod: T, bit: Int)(gen: => Bool): Bool = {
    val x = NodeExtract(mod, bit)
    gen.fromNode(x)
  }

  // extract bit range
  def apply[T <: Bits, R <: Bits](mod: T, hi: UInt, lo: UInt, w: Int = -1)(gen: => R): R = {
    val x = NodeExtract(mod, hi, lo, w)
    gen.fromNode(x)
  }

  def apply[T <: Bits, R <: Bits](mod: T, hi: Int, lo: Int)(gen: => R): R = {
    val x = NodeExtract(mod, hi, lo)
    gen.fromNode(x)
  }
}

class Extract extends Node {
  var lo: Node = null;
  var hi: Node = null;

  override def toString: String =
    ("/*" + (if (name != null && !name.isEmpty) name else "?") + "*/ Extract("
      + inputs(0) + (if (hi == lo) "" else (", " + hi)) + ", " + lo + ")")

  def validateIndex(x: Node) {
    val lit = x.litOf
    val w0 = inputs(0).widthW
    assert(lit == null || lit.value >= 0 && lit.value < w0.needWidth(),
           ChiselError.error("Extract(" + lit.value + ")" +
                    " out of range [0," + (w0.needWidth()-1) + "]" +
                    " of " + inputs(0), line))
  }

  override def canCSE: Boolean = true
  override def equalsForCSE(that: Node): Boolean = that match {
    case _: Extract => CSE.inputsEqual(this, that)
    case _ => false
  }

  // Eliminate any zero-width wires attached to this node.
  override def W0Wtransform() {
   /* We require that if the source is zero-width,
     *  the high and lo must be n-1 and n respectively.
     *  If this is true, we ensure our width is zero.
     */
    val hi_lit = inputs(1).litOf
    val lo_lit = inputs(2).litOf
    if (inputs(0).getWidth == 0 && hi_lit != null && lo_lit != null &&
        hi_lit.value == (lo_lit.value - 1)) {
      setWidth(0)
      modified = true
    } else {
      ChiselError.error("Extract(" + inputs(0) + ", " + inputs(1) + ", " + inputs(2) + ")" +
            " W0Wtransform", line)
    }
  }
}
