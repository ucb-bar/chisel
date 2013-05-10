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
import Component._
import scala.math._

object MuxLookup {
  def apply[S <: Bits, T <: Data] (key: S, default: T, mapping: Seq[(S, T)]): T = {
    var res = default;
    for ((k, v) <- mapping.reverse)
      res = Mux(key === k, v, res);
    res
  }

}

object MuxCase {
  def apply[T <: Data] (default: T, mapping: Seq[(Bool, T)]): T = {
    var res = default;
    for ((t, v) <- mapping.reverse){
      res = Mux(t, v, res);
    }
    res
  }
}

object Multiplex{
  def apply (t: Node, c: Node, a: Node): Node = {
    if (isFolding) {
      if (t.litOf != null) {
        return if (t.litOf.value == 0) a else c
      }
      if (c.litOf != null && a.litOf != null) {
        if (c.litOf.value == a.litOf.value) {
          return c
        }
        if (c.litOf.value == 1 && a.litOf.value == 0) {
          if(c.litOf.width == 1 && a.litOf.width == 1) return t
          val fill = NodeFill(max(c.litOf.width-1, a.litOf.width-1), Literal(0,1))
          fill.infer
          val bit = NodeExtract(t, 0)
          bit.infer
          val cat = Concatenate(fill, bit)
          cat.infer
          return cat
        }
      }
      if (a.isInstanceOf[Mux] && c.clearlyEquals(a.inputs(1))) {
        Multiplex(t.asInstanceOf[Bits] || a.inputs(0).asInstanceOf[Bits], c, a.inputs(2))
      }
    }
    new Mux().init("", maxWidth _, t, c, a);
  }
}

object isLessThan {

  def distFromData(x: java.lang.Class[_]): Int = {
    var xClass = x
    var xCnt = 0
    while(xClass.toString != "class Chisel.Data") {
      xClass = xClass.getSuperclass
      xCnt += 1
    }
    xCnt
  }

  def checkCommonSuperclass(x: java.lang.Class[_], y: java.lang.Class[_]) {
  }

  def apply(x: java.lang.Class[_], y: java.lang.Class[_]): Boolean = {
    checkCommonSuperclass(x, y)
    distFromData(x) > distFromData(y)
  }
}

object Mux {
  def apply[T <: Data](t: Bits, c: T, a: T): T = {
    val res = Multiplex(t, c.toNode, a.toNode)
    if (c.isInstanceOf[Bits]) {
      assert(a.isInstanceOf[Bits])
      if (c.getClass == a.getClass) {
        res.setTypeNodeNoAssign(c.fromNode(res).asInstanceOf[T])
      } else {
        res.setTypeNode(Bits(OUTPUT)).asInstanceOf[T]
      }
    } else {
      res.setTypeNodeNoAssign(c.fromNode(res).asInstanceOf[T])
    }
  }
}
class Mux extends Op {
  muxes += this;
  stack = Thread.currentThread.getStackTrace;
  op = "Mux";
  override def toString: String =
    inputs(0) + " ? " + inputs(1) + " : " + inputs(2)
  def ::(a: Node): Mux = { inputs(2) = a; this }

  override def forceMatchingWidths {
    if (inputs(1).width != width) inputs(1) = inputs(1).matchWidth(width)
    if (inputs(2).width != width) inputs(2) = inputs(2).matchWidth(width)
  }
}
